// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class Sphere
  extends CommonObject 
  implements SphereUtils.ISphereManager, Interfaces.IRigidBody {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static final int SURF_WATER  = 0;
    public static final int SURF_SAND   = 1;
    public static final int SURF_GRASS  = 2;
    public static final int SURF_ROCK   = 3;
    public static final int SURF_SNOW   = 4;
    public static final String[] SURF_NAMES = new String[] {
        "water", "sand", "grass", "rock", "snow"
    };

    //
    // Default surface materials
    //
    public static final Material[] SURF_MATERIALS = new Material[] {
        new Material(ColorAttribute.createDiffuse(Color.BLUE)),     // SURF_WATER
        new Material(ColorAttribute.createDiffuse(Color.YELLOW)),   // SURF_SAND
        new Material(ColorAttribute.createDiffuse(Color.GREEN)),    // SURF_GRASS
        new Material(ColorAttribute.createDiffuse(Color.GRAY)),     // SURF_ROCK
        new Material(ColorAttribute.createDiffuse(Color.WHITE)),    // SURF_SNOW
    };

    //
    // 12 vertices of base icosphere
    // http://blog.andreaskahler.com/2009/06/creating-icosphere-mesh-in-code.html
    //
    public static final float GOLDEN_RATIO = (1.0f + (float)Math.sqrt(5.0f)) * 0.5f;
    private static final float[] ICOSPHERE_VERTICES = new float[] {
       -1.0f,           GOLDEN_RATIO,   0.0f,                           // XY plane
        1.0f,           GOLDEN_RATIO,   0.0f,
       -1.0f,          -GOLDEN_RATIO,   0.0f,
        1.0f,          -GOLDEN_RATIO,   0.0f,
        0.0f,          -1.0f,           GOLDEN_RATIO,                   // YZ plane
        0.0f,           1.0f,           GOLDEN_RATIO,
        0.0f,          -1.0f,          -GOLDEN_RATIO,
        0.0f,           1.0f,          -GOLDEN_RATIO,
        GOLDEN_RATIO,   0.0f,          -1.0f,                           // XZ plane
        GOLDEN_RATIO,   0.0f,           1.0f,
       -GOLDEN_RATIO,   0.0f,          -1.0f,
       -GOLDEN_RATIO,   0.0f,           1.0f
    }; 

    //
    // 20 triangles of base icosphere
    //
    private static final int[] ICOSPHERE_TRIANGLES = new int[] {
        0, 11, 5,    0, 5, 1,     0, 1, 7,     0, 7, 10,    0, 10, 11,  // Triangles around vertex 0
        1,  5,  9,   5,  11, 4,   11, 10, 2,   10, 7,  6,   7,  1,  8,  // Adjacent triangles
        3, 9, 4,     3, 4, 2,     3, 2, 6,     3, 6, 8,     3, 8, 9,    // Triangles around vertex 3
        4, 9, 5,     2, 4, 11,    6, 2, 10,    8, 6, 7,     9, 8, 1     // Adjacent triangles
    };

    // *************************************************************************
    // Sphere
    // *************************************************************************
    private SphereVertex.List m_vertices;
    private SphereSegment[] m_segments;

    // -------------------------------------------------------------------------
    public Sphere() {
        super("sphere");
    }

    // -------------------------------------------------------------------------
    private SphereVertex AddVertex(float x, float y, float z) {
        SphereVertex v = new SphereVertex(x, y, z);
        m_vertices.add(v);
        return v;
    }

    // -------------------------------------------------------------------------
    public void Create(int refine_lvl) {
        // Create base sphere ...
        CreateIcosphere(Sphere.ICOSPHERE_VERTICES, 
            Sphere.ICOSPHERE_TRIANGLES, Sphere.SURF_MATERIALS);

        // ... and refine it
        Refine(refine_lvl);
    }

    // -------------------------------------------------------------------------
    public void Finalize() {
        // Create mesh arrays
        for(SphereSegment s : m_segments) {
            s.CreateVertexArray();
            s.CreateIndexArray();
        }

        // Finalize vertices
        for(SphereVertex v : m_vertices) {
            v.Finalize();
        }

        // Write vertex position to vertex arrays
        for(SphereVertex v : m_vertices) {
            v.WritePosition();
        }

        // Create segment models
        for(SphereSegment s : m_segments) {
            s.BuildDynamicModel(SphereVertex.GetVertexAttributes());
            s.RebuildSuface();
        }
        Log(false);
    }

    // -------------------------------------------------------------------------
    public void Log(boolean detailed) {
        // Vertices
        Log.Debug("Sphere vertices : %d", m_vertices.size());
        if(detailed) {
            for(int i = 0; i < m_vertices.size(); i++) {
                SphereVertex v = m_vertices.get(i);
                Log.Debug("  > idx=%d x=%.2f y=%.2f z=%.2f lon=%.2f lat=%.2f", 
                    i, v.norm.x, v.norm.y, v.norm.z,
                    v.geo_deg.x, v.geo_deg.y);
            }
        }

        // Segments
        Log.Debug("Sphere segments : %d", m_segments.length);
        for(SphereSegment s : m_segments) {
            s.Log(detailed);
        }
    }

    // -------------------------------------------------------------------------
    public SphereVertex GetVertex(int id) {
        Utils.Assert(id < m_vertices.size(), 
            "Failed to get sphere vertex, index out of bounds :: idx=%d size=%d", 
            id, m_vertices.size());
        return m_vertices.get(id);
    }

    // -------------------------------------------------------------------------
    public void ReadHeightmap(Pixmap map, float max_height) {
        for(SphereVertex v : m_vertices) {
            int red = v.GetRgba(map) >> 24;
            v.Scale(1.0f + red * max_height).WritePosition();
        }
    }

    // -------------------------------------------------------------------------
    public void ReadSuface(Pixmap map, Palette pal) {
        for(SphereSegment s : m_segments) {
            s.UpdateSurface(map, pal);
            s.RebuildSuface();
        }
    }

    // -------------------------------------------------------------------------
    private void CreateIcosphere(float[] vert, int[] trig, Material[] mat) {
        Log.Info("Creating base sphere");

        // Set vertices
        m_vertices = new SphereVertex.List();
        for(int i = 0; i < vert.length; i += 3) {
            AddVertex(vert[i + 0], vert[i + 1], vert[i + 2]);
        }

        // Each triangle of base sphere represents a segment
        m_segments = new SphereSegment[trig.length / 3];
        for(int i = 0; i < m_segments.length; i++) {
            // Create new segment
            SphereSegment s = m_segments[i] = new SphereSegment(mat);

            // Link vertices with segment
            int offset = i * 3;
            SphereVertex v0 = m_vertices.get(trig[offset + 0]).LinkSegment(s);
            SphereVertex v1 = m_vertices.get(trig[offset + 1]).LinkSegment(s);
            SphereVertex v2 = m_vertices.get(trig[offset + 2]).LinkSegment(s);

            // Add initial triangle to segment
            s.AddTriangle(v0, v1, v2, Sphere.SURF_WATER);
        }
//        Log(false);
    }

    // -------------------------------------------------------------------------
    private void Refine(int level) {
        // Repeat multiple times
        Log.Info("Refining sphere :: it_num=%d", level);
        while(--level >= 0) {
            SphereUtils.VertexCache cache = 
                new SphereUtils.VertexCache(m_vertices.size(), this);

            int triangle_num = 0;
            for(SphereSegment s : m_segments) {
                triangle_num += s.Refine(cache);
            }
            Log.Info("  > it=%d vert=%d trig=%d", 
                level, m_vertices.size(), triangle_num);
        }
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Flush segment data to GPU
            for(SphereSegment s : m_segments) {
                if(s.FlushMeshArrays()) {
                    Log.Debug(
                        "Flushing sphere segment data to GPU :: segment=%d vertex_num=%d index_num=%d", 
                        s.id, DynamicModel.flushed_vertex_num, DynamicModel.flushed_index_num);
                }
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnRender(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            Camera camera = Renderer.GetArgsCamera(args);
            List<RenderableProvider> renderables = Renderer.GetArgsRenderables(args);

            int prev_size = renderables.size();
            for(SphereSegment s : m_segments) {
                // Render segments that are visible by camera
                RenderableProvider inst = s.TestModelVisibility(camera);
                if(inst != null) {
                    renderables.add(inst);
                }
            }
            Main.p.gui.WriteMsg("visible sphere segments: %d/%d", 
                renderables.size() - prev_size, m_segments.length);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnShape(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            ShapeRenderer shaper = Renderer.GetShaper(args);
            Main.p.renderer.ShapeAxis(shaper, new Matrix4(), 2.2f);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnDispose(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_POST) {
            for(SphereSegment s : m_segments) {
                s.dispose();
            }
        }
        return true;
    }

    // *************************************************************************
    // SphereUtils.ISphereManager
    // *************************************************************************
    @Override public SphereVertex CreateVertex(Vector3 v) {
        return AddVertex(v.x, v.y, v.z);
    }

    // *************************************************************************
    // Interfaces.IRigidBody
    // *************************************************************************
    private static Quaternion no_rotation = new Quaternion();
    @Override public Vector3    GetPos() { return Vector3.Zero; }
    @Override public Quaternion GetRot() { return no_rotation; }
}
