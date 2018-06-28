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
import java.util.HashMap;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class SphereUtils {
    // -------------------------------------------------------------------------
    public static interface ISphereManager {
        public SphereVertex CreateVertex(Vector3 v);
    }

    // -------------------------------------------------------------------------
    public static class VertexCache {
        // *********************************************************************
        // STATIC
        // *********************************************************************
        public static long GetKey(int idx0, int idx1) {
            Utils.Assert(idx0 != idx1, 
                "Failed to get vertex key, wrong index:: idx=%d", idx0);

            // Indices should be in ascending order
            if(idx0 > idx1) {
                int tmp = idx0;
                idx0 = idx1;
                idx1 = tmp;
            }
            return (long)idx0 << 32 | (long)idx1;
        }

        // *********************************************************************
        // VertexCache
        // *********************************************************************
        private HashMap<Long, SphereVertex> m_map;
        private SphereUtils.ISphereManager m_manager;

        // ---------------------------------------------------------------------
        public VertexCache(int size, SphereUtils.ISphereManager manager) {
            m_map = new HashMap<Long, SphereVertex>(size);
            m_manager = manager;
        }

        // ---------------------------------------------------------------------
        public Boolean HasVertex(long key) {
            return m_map.containsKey(key);
        }

        // ---------------------------------------------------------------------
        public SphereVertex GetVertex(long key) {
            Utils.Assert(HasVertex(key), 
                "Failed to get cached vertex, key does not exist :: key=%d", key);
            return m_map.get(key);
        }

        // ---------------------------------------------------------------------
        public SphereVertex PutVertex(long key, Vector3 v) {
            // Cache vertex ...
            Utils.Assert(!HasVertex(key), 
                "Failed to cache vertex, key already exists :: key=%d", key);

            // Manager creates new vertex ...
            SphereVertex sv = m_manager.CreateVertex(v);

            // ... and we cache it
            m_map.put(key, sv);
            return sv;
        }
    }

    // -------------------------------------------------------------------------
    public static class Surface {
        // ---------------------------------------------------------------------
        public Material material;
        public MeshPart mesh_part;
        public SphereTriangle.List triangles;

        // ---------------------------------------------------------------------
        public Surface(Material m) {
            material = m;
            triangles = new SphereTriangle.List();
        }
    }
}
