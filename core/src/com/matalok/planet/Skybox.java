// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

// -----------------------------------------------------------------------------
public class Skybox 
  extends CommonObject {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static final int FACE_FRONT   = 0;
    public static final int FACE_RIGHT   = 1;
    public static final int FACE_BACK    = 2;
    public static final int FACE_LEFT    = 3;
    public static final int FACE_TOP     = 4;
    public static final int FACE_BOTTOM  = 5;
    public static final int FACE_NUM     = 6;
    public static final String[] FACE_NAMES = new String[] {
        "front", "right", "back", "left", "top", "bottom"
    };

    private static final float[] SKY_VERTICES = new float[] {
        -1.0f,      -1.0f,      1.0f,   // 0 - Front face (+z)
         1.0f,      -1.0f,      1.0f,   // 1
         1.0f,       1.0f,      1.0f,   // 2
        -1.0f,       1.0f,      1.0f,   // 3
        -1.0f,      -1.0f,     -1.0f,   // 4 - Back face (-z)
         1.0f,      -1.0f,     -1.0f,   // 5
         1.0f,       1.0f,     -1.0f,   // 6
        -1.0f,       1.0f,     -1.0f,   // 7
    };

    private static final short[] FACE_VERTEX_IDX = new short[] {
        0, 3, 2, 1,                     // Front
        1, 2, 6, 5,                     // Right
        5, 6, 7, 4,                     // Back
        4, 7, 3, 0,                     // Left
        4, 0, 1, 5,                     // Bottom
        3, 7, 6, 2,                     // Top
    };

    private static VertexAttribute[] VERTEX_ATTRIB = new VertexAttribute[] {
        VertexAttribute.Position(),
        VertexAttribute.TexCoords(0)
    };

    public static int VERTEX_SIZE       = 5;                                // Attributes per vertex (x, y, z, u, v)
    public static int FACE_TRIANGLE_NUM = 2;                                // Triangles per face
    public static int FACE_VERTEX_NUM   = 4;                                // Vertices per face
    public static int FACE_VERTEX_SIZE  = FACE_VERTEX_NUM * VERTEX_SIZE;    // Attributes per face
    public static int SKY_VERTEX_NUM    = FACE_VERTEX_NUM * FACE_NUM;       // Vertices per skybox
    public static int SKY_VERTEX_SIZE   = FACE_VERTEX_SIZE * FACE_NUM;      // Vertex attributes per skybox
    public static int SKY_TRIANGLE_NUM  = FACE_TRIANGLE_NUM * FACE_NUM;     // Triangles per skybox

    // *************************************************************************
    // Skybox
    // *************************************************************************
    private LinkedList<SkyboxModel> m_layers;

    // -------------------------------------------------------------------------
    public Skybox() {
        super("skybox");
        m_layers = new LinkedList<SkyboxModel>();
    }

    // -------------------------------------------------------------------------
    public void AddLayer(TextureAtlas ta, float scale) {
        // One texture only
        Utils.Assert(ta.getTextures().size == 1, 
            "Failed to create skybox layer :: ta-size=%d", ta.getTextures().size);

        // Skybox material
        Material material = new Material(
            TextureAttribute.createDiffuse(ta.getTextures().first()));

        // Build skybox geometry
        SkyboxModel sky = new SkyboxModel(material, scale);
        sky.FillArrays(Skybox.SKY_VERTICES, Skybox.FACE_VERTEX_IDX, ta);

        // Build model
        sky.BuildDynamicModel(Skybox.VERTEX_ATTRIB);

        // Add to stack
        m_layers.add(sky);
    }

    // -------------------------------------------------------------------------
    public void Log() {
        int idx = 0;
        for(SkyboxModel l : m_layers) {
            Log.Debug("Skybox :: idx=%d", idx);
            l.LogVertexArray();
        }
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnRender(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            Camera camera = Renderer.GetArgsCamera(args);
            List<RenderableProvider> renderables = Renderer.GetArgsRenderables(args);

            for(SkyboxModel l : m_layers) {
                // Render segments that are visible by camera
                RenderableProvider inst = l.TestModelVisibility(camera);
                if(inst != null) {
                    renderables.add(inst);
                }
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnDispose(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_POST) {
            for(SkyboxModel l : m_layers) {
                l.dispose();
            }
        }
        return true;
    }
}
