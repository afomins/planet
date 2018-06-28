/*
 * Planet
 * Copyright (C) 2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.matalok.planet.Interfaces.ICamera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

// -----------------------------------------------------------------------------
public class SkyboxModel 
  extends DynamicModel {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static int inst_num = 0;

    // *************************************************************************
    // SkyboxModel
    // *************************************************************************
    public int id;
    private float m_scale;
    private Material m_material;

    // -------------------------------------------------------------------------
    public SkyboxModel(Material material, float scale) {
        id = SkyboxModel.inst_num++;
        m_scale = scale;
        m_material = material;
    }

    // -------------------------------------------------------------------------
    public void FillArrays(float[] sky_vertices, short[] face_vertex_idx, TextureAtlas ta) {
        // Create vertex array 
        float[] va = CreateVertexArray(Skybox.SKY_VERTEX_SIZE, true);

        // Create index array
        short[] ia = CreateIndexArray(Skybox.SKY_TRIANGLE_NUM * 3, true);

        // Walk all faces
        short[] v_idx = new short[Skybox.FACE_VERTEX_NUM];
        for(int i = 0; i < Skybox.FACE_NUM; i++) {
            // Get index in sky_vertices
            int face_idx = i * Skybox.FACE_VERTEX_NUM;
            v_idx[0] = (short) (face_vertex_idx[face_idx + 0] * 3);
            v_idx[1] = (short) (face_vertex_idx[face_idx + 1] * 3);
            v_idx[2] = (short) (face_vertex_idx[face_idx + 2] * 3);
            v_idx[3] = (short) (face_vertex_idx[face_idx + 3] * 3);

            // Get texture region
            String face_name = Skybox.FACE_NAMES[i];
            AtlasRegion region = ta.findRegion(face_name);
            Utils.Assert(region != null, 
                "Failed to find skybox region :: name=%s", face_name);

            //
            // Fill vertex array
            //
            int va_idx = i * Skybox.FACE_VERTEX_SIZE;
            for(int j = 0; j < Skybox.FACE_VERTEX_NUM; j++) {
                // Fill position
                va[va_idx++] = sky_vertices[v_idx[j] + 0];          // x
                va[va_idx++] = sky_vertices[v_idx[j] + 1];          // y
                va[va_idx++] = sky_vertices[v_idx[j] + 2];          // z

                // Fill texture coordinates
                float u = 0.0f, v = 0.0f;
                if(j == 0) { u = region.getU();    v = region.getV();   } else  // XXX: Ordering is wrong causing skybox to be bottom up
                if(j == 1) { u = region.getU();    v = region.getV2();  } else
                if(j == 2) { u = region.getU2();   v = region.getV2();  } else
                if(j == 3) { u = region.getU2();   v = region.getV();   }
                va[va_idx++] = u;                                   // u
                va[va_idx++] = v;                                   // v
            }

            //
            // Fill index array
            //
            int ia_idx = i * Skybox.FACE_TRIANGLE_NUM * 3;

            // 1st triangle (0, 2, 1)
            ia[ia_idx++] = (short) (face_idx + 0);
            ia[ia_idx++] = (short) (face_idx + 1);
            ia[ia_idx++] = (short) (face_idx + 2);

            // 2nd triangle (0, 3, 2)
            ia[ia_idx++] = (short) (face_idx + 0);
            ia[ia_idx++] = (short) (face_idx + 2);
            ia[ia_idx++] = (short) (face_idx + 3);
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
                "skybox-" + id,
                mesh, 0, m_index_array.length,
                GL20.GL_TRIANGLES), m_material);
    }

    // -------------------------------------------------------------------------
    @Override protected void FinalizeModel(ModelInstance inst) {
        inst.transform.scl(m_scale);
    }

    // -------------------------------------------------------------------------
    @Override protected void CleanupModel() {
    }

    // -------------------------------------------------------------------------
    @Override protected void LogVertexArray() {
        float[] va = GetVertexArray();
        int va_offset = 0, vertex_idx = 0;
        for(int i = 0; i < Skybox.FACE_NUM; i++) {
            Log.Debug("Face :: idx=%d name=%s va_offset=%d", 
                i, Skybox.FACE_NAMES[i], va_offset);
            for(int j = 0; j < Skybox.FACE_VERTEX_NUM; j++) {
                Log.Debug(" > vertex_idx=%d pos=%.2f:%.2f:%.2f tex=%.2f:%.2f", 
                    vertex_idx, 
                    va[va_offset++], va[va_offset++], va[va_offset++],
                    va[va_offset++], va[va_offset++]);
                vertex_idx++;
            }
        }
    }

    // -------------------------------------------------------------------------
    @Override protected void LogIndexArray() {
    }

    // *************************************************************************
    // Interfaces.ISmartModel
    // *************************************************************************
    @Override public RenderableProvider TestModelVisibility(ICamera camera) {
        return m_model_inst;
    }
}
