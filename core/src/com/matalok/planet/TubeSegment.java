// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.matalok.planet.Interfaces.ICamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class TubeSegment 
  extends DynamicModel {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static int inst_num = 0;
    private static Material material = new Material(
        ColorAttribute.createDiffuse(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.5f), 
        new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));

    // *************************************************************************
    // TubeSegment
    // *************************************************************************
    public int id;

    // -------------------------------------------------------------------------
    public TubeSegment() {
        id = TubeSegment.inst_num++;
    }

    // -------------------------------------------------------------------------
    private static Vector3[] step_template = null;
    private Vector3[] GetStepTemplate(float[] angles, float radius) {
        if(step_template == null || 
           step_template.length != angles.length) {
            step_template = new Vector3[angles.length];
            for(int i = 0; i < angles.length; i++) {
                step_template[i] = new Vector3(
                    radius * MathUtils.cos(angles[i]),
                    radius * MathUtils.sin(angles[i]), 0.0f);
            }
        }
        return step_template;
    }

    // -------------------------------------------------------------------------
    public void BuildGeometry(PathSegment path_segment) {
        // Vertex array of original path
        float[] path_va = path_segment.GetVertexArray();

        // Number of steps in path
        int step_num = path_segment.steps.length;

        // Tube sectors angles
        float[] sector_angles = Main.p.cfg.tube_sector_angles;

        // Normalized position of each sector vertex
        Vector3[] sector_template = GetStepTemplate(sector_angles, Main.p.cfg.tube_radius);

        // Create tube vertex array
        int vertex_num = sector_angles.length * step_num;
        float tube_va[] = CreateVertexArray(vertex_num * 3, true);

        // Fill vertex array
        int tube_idx = 0, path_idx = 0;
        Vector3 step_pos = new Vector3();

        for(int i = 0; i < step_num; i++) {
            Quaternion step_rot = path_segment.steps[i].rot;
            for(int j = 0; j < sector_angles.length; j++) {
                step_pos.set(sector_template[j]).mul(step_rot);
                tube_va[tube_idx++] = step_pos.x + path_va[path_idx + 0];
                tube_va[tube_idx++] = step_pos.y + path_va[path_idx + 1];
                tube_va[tube_idx++] = step_pos.z + path_va[path_idx + 2];
            }
            path_idx += 3;
        }

        // Create tube index array
        tube_idx = 0;
        short tube_ia[] = CreateIndexArray(vertex_num * 2/* + step_num * 2*/, false);

        // Fill index array
        for(int i = 0; i < vertex_num; i += sector_angles.length) {
            for(int j = 0; j < sector_angles.length - 1; j++) {
                tube_ia[tube_idx++] = (short)(i + j);
                tube_ia[tube_idx++] = (short)(i + j + 1);
            }

            // Last vertex of sector connects to first vertex
            tube_ia[tube_idx++] = (short)(i + sector_angles.length - 1);
            tube_ia[tube_idx++] = (short)(i);

            // Connect sectors together
//            tube_ia[tube_idx++] = (short)(i);
//            tube_ia[tube_idx++] = (short)(i + sector_angles.length);
        }
    }

    // *************************************************************************
    // DynamicModel
    // *************************************************************************
    @Override protected void PrepareModel() {
    }

    // -------------------------------------------------------------------------
    @Override protected void CreateModel(ModelBuilder mb, Mesh mesh) {
        mb.part(new MeshPart(
                    "tube-segment-" + id,
                    mesh, 0, this.m_index_array.length,
                    GL20.GL_LINES), TubeSegment.material);
    }

    // -------------------------------------------------------------------------
    @Override protected void FinalizeModel(ModelInstance inst) {
    }

    // -------------------------------------------------------------------------
    @Override protected void CleanupModel() {
    }

    // -------------------------------------------------------------------------
    @Override protected void LogVertexArray() {
        Log.Debug("Vertex array :: segment=%d array_size=%d", id, m_vertex_array.length);
        int offset = 0;
        for(int idx = 0; idx < m_vertex_array.length / 3; idx++) {
            Log.Debug(
              "  > vertex_id=%d attrib_offset=%d pos=%.2f:%.2f:%.2f", idx, offset,
              m_vertex_array[offset + 0], m_vertex_array[offset + 1], m_vertex_array[offset + 2]);
            offset += 3;
        }
    }

    // -------------------------------------------------------------------------
    @Override protected void LogIndexArray() {
        Log.Debug("Index array :: segment=%d array_size=%d", id, m_index_array.length);
        for(int idx = 0; idx < m_index_array.length; idx += 2) {
            Log.Debug(
              "  > line=%d v0=%d v1=%d", idx / 2, 
              m_index_array[idx + 0], m_index_array[idx + 1]);
        }
    }

    // *************************************************************************
    // Interfaces.ISmartModel
    // *************************************************************************
    @Override public RenderableProvider TestModelVisibility(ICamera camera) {
        return m_model_inst;
    }
}
