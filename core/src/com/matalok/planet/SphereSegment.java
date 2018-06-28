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

//-----------------------------------------------------------------------------
import java.util.ListIterator;

import com.matalok.planet.Interfaces.ICamera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class SphereSegment 
  extends DynamicModel {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static float BACKFACE_CULLING_THRESH = -0.3f;

    // -------------------------------------------------------------------------
    private static int inst_num = 0;

    // *************************************************************************
    // SphereSegment
    // *************************************************************************
    public int id;

    // -------------------------------------------------------------------------
    private Vector3 m_center;
    private Vector3 m_normal;
    private int m_triangle_num;
    private int m_vertex_num;
    private SphereUtils.Surface[] m_surfaces;

    // -------------------------------------------------------------------------
    public SphereSegment(Material[] materials) {
        id = SphereSegment.inst_num++;
        m_vertex_num = 0;

        m_surfaces = new SphereUtils.Surface[] {
            new SphereUtils.Surface(materials[Sphere.SURF_WATER]),
            new SphereUtils.Surface(materials[Sphere.SURF_SAND]),
            new SphereUtils.Surface(materials[Sphere.SURF_GRASS]),
            new SphereUtils.Surface(materials[Sphere.SURF_ROCK]),
            new SphereUtils.Surface(materials[Sphere.SURF_SNOW]),
        };
    }

    // -------------------------------------------------------------------------
    public int AllocVertex() {
        return m_vertex_num++;
    }

    // -------------------------------------------------------------------------
    public void Log(boolean detailed) {
        String str = String.format("  > segment=%d ", id);
        for(int i = 0; i < m_surfaces.length; i++) {
            SphereUtils.Surface s = m_surfaces[i];
            str += String.format("%s=%d ", Sphere.SURF_NAMES[i], s.triangles.size());
            if(detailed) {
                for(int j = 0; j < s.triangles.size(); j++) {
                    SphereTriangle t = s.triangles.get(j);
                    Log.Debug("      > triangle=%d v0=%d(%d) v1=%d(%d) v2=%d(%d)",
                        i, t.vertices[0].id, t.vertex_inst[0],
                           t.vertices[1].id, t.vertex_inst[1],
                           t.vertices[2].id, t.vertex_inst[2]);
                }
            }
        }
        Log.Debug(str);
    }

    // -------------------------------------------------------------------------
    public SphereTriangle AddTriangle(int surface, SphereTriangle t) {
        // First triangle defines surface center and normal
        if(m_triangle_num == 0) {
            Vector3 v0 = t.vertices[0].pos,
                    v1 = t.vertices[1].pos,
                    v2 = t.vertices[2].pos;
            m_center = new Vector3(Utils.GetMiddle(v0, v1, v2));
            m_normal = new Vector3(Utils.GetTriangleNormal(v0, v1, v2));
        }

        // Add triangle to list
        m_triangle_num++;
        m_surfaces[surface].triangles.add(t);
        return t;
    }

    // -------------------------------------------------------------------------
    public SphereTriangle AddTriangle(SphereVertex v0,
                                      SphereVertex v1,
                                      SphereVertex v2,
                                      int surface) {
        return AddTriangle(surface, new SphereTriangle(this, v0, v1, v2, surface));
    }

    // -------------------------------------------------------------------------
    public void RebuildSuface() {
        int idx = 0;
        for(SphereUtils.Surface s : m_surfaces) {
            // Set region in index array
//            s.mesh_part.indexOffset = idx;
//            s.mesh_part.numVertices = s.triangles.size() * 3;
            s.mesh_part.offset = idx;
            s.mesh_part.size = s.triangles.size() * 3;

            // Fill region in index array
            for(SphereTriangle t : s.triangles) {
                m_index_array[idx + 0] = (short)t.vertex_inst[0];
                m_index_array[idx + 1] = (short)t.vertex_inst[1];
                m_index_array[idx + 2] = (short)t.vertex_inst[2];
                idx += 3;
            }
        }
        m_is_dirty_index = true;
    }

    // -------------------------------------------------------------------------
    public void UpdateSurface(Pixmap map, Palette pal) {
        for(SphereUtils.Surface surf : m_surfaces) {
            ListIterator<SphereTriangle> it = surf.triangles.listIterator();
            while(it.hasNext()) {
                // Read triangle
                SphereTriangle t = it.next();

                // First vertex defines type of surface
                SphereVertex v = t.vertices[0];
                int new_type = pal.GetColorIdx(v.GetRgba(map));
                if(new_type == -1) {
                    Log.Err("Failed to update surface :: segment=%d vertex=%d rgba=0x%08X", 
                        id, v.id, v.GetRgba(map));
                    continue;
                }

                // Move triangle to correct surface
                if(new_type != t.type) {
                    // Remove triangle from current surface
                    it.remove();

                    // Add triangle to new surface
                    m_surfaces[new_type].triangles.add(t);
                    t.type = new_type;

                    // Segment's index array is dirty
                    m_is_dirty_index = true;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    public int Refine(SphereUtils.VertexCache cache) {
        // Refine all triangle of all surfaces
        for(SphereUtils.Surface s : m_surfaces) {
            int triangle_num = s.triangles.size();
            for(int i = 0; i < triangle_num; i++) {
                SphereTriangle t = s.triangles.get(i);
                Utils.Assert(t.segment.equals(this), 
                    "Failed to refine segment, wrong triangle :: segment=%d triangle=%d",
                    id, t.segment.id);
                t.Refine(cache);
            }
        }
        return m_triangle_num;
    }

    // -------------------------------------------------------------------------
    public void CreateVertexArray() {
        CreateVertexArray(m_vertex_num * SphereVertex.TOTAL_SIZE, false);
    }

    // -------------------------------------------------------------------------
    public void CreateIndexArray() {
        CreateIndexArray(m_triangle_num * 3, false);
    }

    // *************************************************************************
    // DynamicModel
    // *************************************************************************
    @Override public void PrepareModel() {
        // Flush triangle normal to vertex array
        for(SphereUtils.Surface s : m_surfaces) {
            for(SphereTriangle t : s.triangles) {
                t.Finalize();
            }
        }
    }

    // -------------------------------------------------------------------------
    @Override public void CreateModel(ModelBuilder mb, Mesh mesh) {
        // Create meshpart for each type of surface
        for(int i = 0; i < m_surfaces.length; i++) {
            mb.part(new MeshPart(
                        "sphere-segment-" + id + "-" + Sphere.SURF_NAMES[i],
                        mesh, 0, 
                        m_index_array.length,
                        GL20.GL_TRIANGLES),
                m_surfaces[i].material);
        }
    }

    // -------------------------------------------------------------------------
    @Override public void FinalizeModel(ModelInstance inst) {
        for(int i = 0; i < m_surfaces.length; i++) {
            m_surfaces[i].mesh_part = inst.nodes.get(0).parts.get(i).meshPart;
        }
    }

    // -------------------------------------------------------------------------
    @Override protected void CleanupModel() {
    }

    // -------------------------------------------------------------------------
    @Override public void LogVertexArray() {
        Log.Debug("Vertex array :: segment=%d array_size=%d", id, m_vertex_array.length);
        int offset = 0;
        for(int idx = 0; idx < m_vertex_num; idx++) {
            for(int inst = 0; inst < SphereVertex.INSTANCE_NUM; inst++) {
                Log.Debug(
                  "  > vertex_id=%d inst_id=%d attrib_offset=%d pos=%.2f:%.2f:%.2f norm=%.2f:%.2f:%.2f", idx, inst, offset, 
                  m_vertex_array[offset + 0], m_vertex_array[offset + 1], m_vertex_array[offset + 2],
                  m_vertex_array[offset + 3], m_vertex_array[offset + 4], m_vertex_array[offset + 5]);
                offset += SphereVertex.ATTRIB_NUM;
            }
        }
    }

    // -------------------------------------------------------------------------
    @Override public void LogIndexArray() {
        Log.Debug("Index array :: segment=%d array_size=%d", id, m_index_array.length);
        for(int idx = 0; idx < m_index_array.length; idx += 3) {
            Log.Debug(
              "  > trig=%d v0=%d v1=%d v2=%d", idx / 3, 
              m_index_array[idx + 0], m_index_array[idx + 1], m_index_array[idx + 2]);
        }
    }

    // *************************************************************************
    // Interfaces.ISmartModel
    // *************************************************************************
    @Override public RenderableProvider TestModelVisibility(ICamera camera) {
        // Segments is visible if it's normal is facing camera
        return camera.TestBackfaceCulling(m_normal, m_center, 
            SphereSegment.BACKFACE_CULLING_THRESH) ? m_model_inst : null;
    }
}
