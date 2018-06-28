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
import java.util.LinkedList;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class SphereTriangle {
    // -------------------------------------------------------------------------
    // Make it a linked-list because fast add/delete is needed
    public static class List extends LinkedList<SphereTriangle> {
        // ---------------------------------------------------------------------
        private static final long serialVersionUID = 42424201L;

        // *********************************************************************
        // SphereTriangle.List
        // *********************************************************************
        public List() { super(); }
    }

    // *************************************************************************
    // SphereTriangle
    // *************************************************************************
    public SphereVertex[] vertices;
    public SphereSegment segment;
    public int[] vertex_inst;
    public int type;

    // -------------------------------------------------------------------------
    public SphereTriangle(SphereSegment s,
                          SphereVertex v0,
                          SphereVertex v1,
                          SphereVertex v2,
                          int t) {
        // Linked segment
        segment = s;

        // Vertices
        vertices = new SphereVertex[] {v0, v1, v2};

        // Each vertex should allocate unique instance from linked segment
        vertex_inst = new int[] {v0.Alloc(s), v1.Alloc(s), v2.Alloc(s)};

        // Surface type
        type = t;
    }

    // -------------------------------------------------------------------------
    public void SetVertex(int idx, SphereVertex v) {
        // Free old vertex instance
        vertices[idx].Free(segment, vertex_inst[idx]);

        // Set new vertex
        vertices[idx] = v;

        // Allocate new instance
        vertex_inst[idx] = v.Alloc(segment);
    }

    // -------------------------------------------------------------------------
    public void Finalize() {
        // Get triangle normal
        Vector3 normal = Utils.GetTriangleNormal(
            vertices[0].pos, vertices[1].pos, vertices[2].pos);

        // Finalize all vertices
        for(int i = 0; i < 3; i++) {
            // Update vertex normal
            vertices[i].WriteNormal(segment, vertex_inst[i], normal);

            // Local vertex-id is not needed anymore, substitute it's value with 
            // vertex-instance offset in vertex array
            vertex_inst[i] += 
                vertices[i].GetInstanceId(segment, vertex_inst[i]) * SphereVertex.INSTANCE_NUM;
        }
    }

    // -------------------------------------------------------------------------
    private SphereVertex GetMiddleVertex(SphereVertex v0, 
                                         SphereVertex v1,
                                         SphereUtils.VertexCache vertex_cache) {
        // Get cache key
        long key = SphereUtils.VertexCache.GetKey(v0.id, v1.id);

        // Get middle vertex by key
        SphereVertex v = (vertex_cache.HasVertex(key)) ?
            vertex_cache.GetVertex(key) : // Get from cache ...
            vertex_cache.PutVertex(key,   // ... or create and cache new vertex 
                Utils.GetMiddle(v0.norm, v1.norm));
        return v;
    }

    // -------------------------------------------------------------------------
    public void Refine(SphereUtils.VertexCache cache) {
        // Get vertices
        SphereVertex 
            // Existing vertices
            v0 = vertices[0],
            v1 = vertices[1],
            v2 = vertices[2],

            // New middle vertices (should be linked with segment)
            v01 = GetMiddleVertex(v0, v1, cache).LinkSegment(segment),
            v12 = GetMiddleVertex(v1, v2, cache).LinkSegment(segment),
            v20 = GetMiddleVertex(v2, v0, cache).LinkSegment(segment);

        // Modify current triangle
        SetVertex(1, v01);
        SetVertex(2, v20);

        // Add 3 new triangles
        segment.AddTriangle(v1, v12, v01, type);
        segment.AddTriangle(v2, v20, v12, type);
        segment.AddTriangle(v01, v12, v20, type);
    }
}
