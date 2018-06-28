// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;

// -----------------------------------------------------------------------------
public abstract class DynamicModel 
  implements Disposable, Interfaces.ISmartModel {
    // *************************************************************************
    // ABSTRACT
    // *************************************************************************
    protected abstract void PrepareModel();
    protected abstract void CreateModel(ModelBuilder mb, Mesh mesh);
    protected abstract void FinalizeModel(ModelInstance inst);
    protected abstract void CleanupModel();
    protected abstract void LogVertexArray();
    protected abstract void LogIndexArray();

    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static float VA_DBG_FILLER = 77.77f;
    private static short IA_DBG_FILLER = 77;

    // Number of vertices/indices flushed during last call
    public static int flushed_vertex_num = 0;
    public static int flushed_index_num = 0;

    // *************************************************************************
    // DynamicModel
    // *************************************************************************
    protected float[] m_vertex_array;
    protected short[] m_index_array;
    protected boolean m_is_dirty_vertex;
    protected boolean m_is_dirty_index;
    protected ModelInstance m_model_inst;

    // -------------------------------------------------------------------------
    private Mesh m_mesh;
    private Model m_model;
    private int m_attrib_num;

    // -------------------------------------------------------------------------
    public DynamicModel() {
        // Initially mesh arrays are dirty
        m_is_dirty_vertex = m_is_dirty_index = true;
    }

    // -------------------------------------------------------------------------
    public int GetAttribNum() {
        return m_attrib_num;
    }

    // -------------------------------------------------------------------------
    public float[] CreateVertexArray(int size, boolean dbg_filler) {
        Utils.Assert(m_vertex_array == null, "Failed to create vertrex array");
        m_vertex_array = new float[size];
        if(dbg_filler) {
            for(int i = 0; i < m_vertex_array.length; i++) {
                m_vertex_array[i] = DynamicModel.VA_DBG_FILLER;
            }
        }
        return m_vertex_array;
    }

    // -------------------------------------------------------------------------
    public float[] GetVertexArray() {
        return m_vertex_array;
    }

    // -------------------------------------------------------------------------
    public short[] CreateIndexArray(int size, boolean dbg_filler) {
        Utils.Assert(m_index_array == null, "Failed to create index array");
        m_index_array = new short[size];
        if(dbg_filler) {
            for(int i = 0; i < m_index_array.length; i++) {
                m_index_array[i] = DynamicModel.IA_DBG_FILLER;
            }
        }
        return m_index_array;
    }

    // -------------------------------------------------------------------------
    public void BuildDynamicModel(VertexAttribute[] vertex_attrib) {
        // Calculate number of attributes per vertex 
        m_attrib_num = 0;
        for(VertexAttribute a : vertex_attrib) {
            m_attrib_num += a.numComponents;
        }

        // Hmmm...
        int vertex_num = m_vertex_array.length / m_attrib_num;
        int index_num = (m_index_array != null) ? m_index_array.length : 0;
        Log.Debug(
            "Creating dynamic model :: vertex_num=%d vertex_array_size=%d idx_array_size=%d",
            vertex_num, m_vertex_array.length, index_num);

        // 1) Prepare model before creating
        PrepareModel();

        // Create dynamic mesh
        m_mesh = new Mesh(false, vertex_num, index_num, vertex_attrib);

        // Bind vertex array
        m_mesh.setVertices(m_vertex_array);

        // Bind optional index array
        if(m_index_array != null) {
            m_mesh.setIndices(m_index_array);
        }

        // Begin model builder
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        // 2) Create mesh parts
        CreateModel(mb, m_mesh);

        // Finish model
        m_model = mb.end();

        // Create model instance
        m_model_inst = new ModelInstance(m_model);

        // 3) Finalize model
        FinalizeModel(m_model_inst);
    }

    // -------------------------------------------------------------------------
    private int FlushVertices() {
        m_mesh.updateVertices(0, m_vertex_array);
        return m_vertex_array.length / m_attrib_num;
    }

    // -------------------------------------------------------------------------
    private int FlushIndices() {
        if(m_index_array == null) {
            return 0;
        }

        m_mesh.setIndices(m_index_array);
        return m_index_array.length;
    }

    // -------------------------------------------------------------------------
    public boolean FlushMeshArrays() {
        if(!m_is_dirty_vertex && !m_is_dirty_index) {
            return false;
        }

        // Flush
        DynamicModel.flushed_vertex_num = (m_is_dirty_vertex) ? FlushVertices() : 0;
        DynamicModel.flushed_index_num = (m_is_dirty_index) ? FlushIndices() : 0;

        // Reset dirty flags 
        m_is_dirty_vertex = m_is_dirty_index = false;
        return true;
    }

    // *************************************************************************
    // Disposable
    // *************************************************************************
    @Override public void dispose() {
        CleanupModel();
        m_model.dispose();
    }
}
