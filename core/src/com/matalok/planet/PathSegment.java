// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class PathSegment 
  extends PathUtils.GenericSegment {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static VertexAttribute[] VERTEX_ATTRIB = new VertexAttribute[] {
        VertexAttribute.Position()
    };

    // *************************************************************************
    // PathSegment
    // *************************************************************************
    public PathModelSegment seg_model;
    public PathModelNormal nor_model;
    public PathStep[] steps;

    // -------------------------------------------------------------------------
    public float[] GetVertexArray() {
        return seg_model.GetVertexArray();
    }

    // -------------------------------------------------------------------------
    private static Vector2[] control_points = new Vector2[] 
        { new Vector2(), new Vector2(), new Vector2(), new Vector2(), };
    public PathSegment(Vector3 pre_start, Vector3 start, Vector3 end, Vector3 post_end, 
      float normal_size, float step_size) {
        // Estimate segment distance
        float distance = UtilsGeo.GetDistance(start, end);
        int vertex_num = (int)(distance / step_size) + 2;

        Log.Debug(
            "Creating path :: vnum=%d v0=%.2f:%.2f v1=%.2f:%.2f v2=%.2f:%.2f v3=%.2f:%.2f est-len=%.2f", 
            vertex_num, pre_start.x, pre_start.y, start.x, start.y, 
            end.x, end.y, post_end.x, post_end.y,
            distance);

        // Create dynamic models
        seg_model = new PathModelSegment();
        nor_model = new PathModelNormal();

        // Build vertex array for segment-model
        PathSegment.control_points[0].set(pre_start.x, pre_start.y);
        PathSegment.control_points[1].set(start.x, start.y);
        PathSegment.control_points[2].set(end.x, end.y);
        PathSegment.control_points[3].set(post_end.x, post_end.y);
        CreateSegmentVertexArray(vertex_num, PathSegment.control_points, 
          start.z, end.z);

        // Build steps
        CreateStep(vertex_num);

        // Build vertex array for normal-model
        CreateNormalVertexArray(normal_size);

        // Finalize dynamic models
        seg_model.BuildDynamicModel(PathSegment.VERTEX_ATTRIB);
        nor_model.BuildDynamicModel(PathSegment.VERTEX_ATTRIB);
    }

    // -------------------------------------------------------------------------
    private static Vector2 tmp_v2 = new Vector2();
    private static Vector3 tmp_v3 = new Vector3();
    private void CreateSegmentVertexArray(int size, Vector2[] cp, float height_start, 
      float height_end) {
        // Create vertex array with (x, y, z) per vertex
        float[] va = seg_model.CreateVertexArray(size * 3, false);

        //
        // Index array not used
        //

        // Height elevation from start to end
        float height_diff = height_end - height_start;

        // Get target hemisphere 
        Vector2 start = cp[1], end = cp[2];
        boolean target_north = (UtilsGeo.GetLat(start) >= 0.0f || 
                                UtilsGeo.GetLat(end) >= 0.0f);

        // Remap control points from geo-coordinates (lon, lat) to cartesian 
        // spheric-coordinates (x, y)
        for(Vector2 v : cp) {
            // Normalize longitude
            float lon = UtilsGeo.SetLon(v, 
                UtilsAngle.Normalize(UtilsGeo.GetLon(v)));

            // Clamp latitude to make it at least 1 degree from the pole 
            float lat = UtilsAngle.Clamp(
                UtilsGeo.GetLat(v), 
                -MathUtils.PI / 2, MathUtils.PI / 2, MathUtils.degRad);

            // Remap
            UtilsGeo.GeoToSpheric(v, lon, lat, target_north);
        }

        // Create spline from control points
        CatmullRomSpline<Vector2> path = new CatmullRomSpline<Vector2>(cp, false);

        // Walk spline and fill segment vertices
        float pos = 0.0f, step = 1.0f / (size - 1);
        for(int i = 0; 
                i < va.length;
                i += 3, pos += step) {
            // Get spheric coordinate from spline (x, y)
            path.valueAt(PathSegment.tmp_v2, pos);

            // Convert spheric (x, y) coordinate to geo coordinates (lon, lat)
            UtilsGeo.SphericToGeo(PathSegment.tmp_v2, target_north);

            // Get 3d space coordinates (x, y, z)
            UtilsGeo.GeoToCartesian(
                PathSegment.tmp_v3,
                PathSegment.tmp_v2.x, PathSegment.tmp_v2.y, 
                height_start + height_diff * pos);

            // Fill vertex coordinates
            va[i + 0] = PathSegment.tmp_v3.x;
            va[i + 1] = PathSegment.tmp_v3.y;
            va[i + 2] = PathSegment.tmp_v3.z;
        }
    }

    // -------------------------------------------------------------------------
    private void CreateNormalVertexArray(float size) {
        // Vertex array of segment 
        float[] va_seg = seg_model.GetVertexArray();

        // Allocate 6 vertices per normal:
        //  * 3 vertices for start
        //  * 3 vertices for end
        float[] va_nor = nor_model.CreateVertexArray(steps.length * 6, false);
        for(PathStep s : steps) {
            // Build normal vector of the step
            s.normal.set(
                va_seg[s.idx + 0], 
                va_seg[s.idx + 1], 
                va_seg[s.idx + 2]).nor();

            // Start of normal
            int idx = s.idx * 2;
            va_nor[idx + 0] = va_nor[idx + 3] = va_seg[s.idx + 0];
            va_nor[idx + 1] = va_nor[idx + 4] = va_seg[s.idx + 1];
            va_nor[idx + 2] = va_nor[idx + 5] = va_seg[s.idx + 2];

            // End of normal
            va_nor[idx + 3] += s.normal.x * size;
            va_nor[idx + 4] += s.normal.y * size;
            va_nor[idx + 5] += s.normal.z * size;

            // Calculate roll-angle of the step
            s.FinalizeRoll();
        }
    }

    // -------------------------------------------------------------------------
    private void CreateStep(int vertex_num) {
        len = 0.0f;
        steps = new PathStep[vertex_num - 1];
        for(int i = 0; i < steps.length; i++) {
            PathStep step = steps[i] = new PathStep(i * 3);
            step.FinalizeDir(seg_model.GetVertexArray());
            len = step.UpdateOffset(len);
        }
    }
}
