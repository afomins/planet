// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class PathStep 
  extends PathUtils.GenericSegment {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public int idx;
    public Vector3 normal;
    public Utils.Direction dir;
    public Quaternion rot;

    // *************************************************************************
    // PathSegmentStep
    // *************************************************************************
    public PathStep(int i) {
        idx = i;
        dir = new Utils.Direction();
        normal = new Vector3();
        rot = new Quaternion();
    }

    // -------------------------------------------------------------------------
    public void FinalizeDir(float[] va) {
        // Direction = next_vertex - current_vertex
        len = dir.Rebuild(
            va[idx + 3 + 0] - va[idx + 0], 
            va[idx + 3 + 1] - va[idx + 1], 
            va[idx + 3 + 2] - va[idx + 2]).vect_len;
    }

    // -------------------------------------------------------------------------
    // http://stackoverflow.com/questions/3035590/bank-angle-from-up-vector-and-look-at-vector
    static Vector3 y_proj = new Vector3();
    static Vector3 cross = new Vector3();
    public void FinalizeRoll() {
        // Project Y on plane perpendicular to look (dir.vect)
        y_proj.set(
            -(dir.dir.y * dir.dir.x),
            1.0f - (dir.dir.y * dir.dir.y),
            -(dir.dir.y * dir.dir.z)).nor();

        // Get absolute angle between Y projected and Up (normal)
        float angle = (float)Math.acos(normal.dot(y_proj));
        cross.set(normal).crs(y_proj);

        // Magic formula
        UtilsAngle.SetRotRoll(dir.ypr, 
            (dir.dir.dot(cross) < 0.0f) ? angle : -angle);

        // Quaternion that describes step orientation
        UtilsAngle.SetRotQuaternion(rot, dir.ypr);
    }

    // -------------------------------------------------------------------------
    public Vector3 GetPosByOffset(Vector3 dest, float[] va, float offset) {
        return dest.set(
            va[idx + 0] + dir.dir.x * offset,
            va[idx + 1] + dir.dir.y * offset,
            va[idx + 2] + dir.dir.z * offset);
    }

    // -------------------------------------------------------------------------
    public void Log(float[] va) {
        Log.Debug("    > offset=%.2f len=%.2f pos=(%.2f, %.2f, %.2f) dir=(%.2f, %.2f, %.2f)",
            offset, len, va[idx + 0], va[idx + 1], va[idx + 2],
            dir.dir.x, dir.dir.y, dir.dir.z);
    }
}
