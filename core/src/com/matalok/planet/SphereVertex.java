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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class SphereVertex {
    // -------------------------------------------------------------------------
    // Make it an array list because fast random access is needed
    public static class List extends ArrayList<SphereVertex> {
        // *********************************************************************
        // STATIC
        // *********************************************************************
        private static final long serialVersionUID = 42424202L;

        // *********************************************************************
        // SphereVertex.List
        // *********************************************************************
        public List() { super(); }
        public List(int size) { super(size); }
    }

    // *************************************************************************
    // STATIC
    // *************************************************************************

    // Vertex might be shared by up to 6 adjacent triangles each using unique instance
    public static final int INSTANCE_NUM  = 6;

    // Vertex is described by 6 float attributes
    public static final int ATTRIB_POS_X  = 0;
    public static final int ATTRIB_POS_Y  = 1;
    public static final int ATTRIB_POS_Z  = 2;
    public static final int ATTRIB_NORM_X = 3;
    public static final int ATTRIB_NORM_Y = 4;
    public static final int ATTRIB_NORM_Z = 5;
    public static final int ATTRIB_NUM    = 6;

     // Vertex total size (size of all instances) is 36 floats
    public static final int TOTAL_SIZE = INSTANCE_NUM * ATTRIB_NUM;

    // -------------------------------------------------------------------------
    private static int inst_num = 0;
    private static VertexAttribute[] vertex_attrib = new VertexAttribute[] {
        VertexAttribute.Position(),
        VertexAttribute.Normal()
    };

    // -------------------------------------------------------------------------
    public static VertexAttribute[] GetVertexAttributes() {
        return vertex_attrib;
    }

    // *************************************************************************
    // SphereVertex.SegmentEntry
    // *************************************************************************
    private static class SegmentEntry {
        // ---------------------------------------------------------------------
        public SphereSegment segment;
        public LinkedList<Integer> free_inst;
        public int instance_id;
        public int attribute_offset;

        // ---------------------------------------------------------------------
        public SegmentEntry(SphereSegment s) {
            // Initially all instances are free
            free_inst = new LinkedList<Integer>();
            for(int i = 0; i < SphereVertex.INSTANCE_NUM; i++) {
                free_inst.add(i);
            }

            // Linked segment
            segment = s;

            // Id of selected instance
            instance_id = s.AllocVertex();

            // Offset where first instance starts in segment's vertex array
            attribute_offset = instance_id * SphereVertex.TOTAL_SIZE;
        }

        // ---------------------------------------------------------------------
        public int Alloc() {
            Utils.Assert(free_inst.size() > 0, 
                "Failed to allocate vertex instance, vertex limit reached");
            return free_inst.pop();
        }

        // ---------------------------------------------------------------------
        public void Free(int inst) {
            Utils.Assert(inst >= 0 && inst < SphereVertex.INSTANCE_NUM, 
                "Failed to free vertex instance, wrong instance :: inst=%d", inst);
            for(Integer i : free_inst) {
                Utils.Assert(i != inst, 
                    "Failed to free vertex instance, instance already in free list :: inst=%d", inst);
            }
            free_inst.add(inst);
        }

        // ---------------------------------------------------------------------
        public void Finalize() {
            // List of free vertex instances will not be needed anymore
            free_inst.clear();
            free_inst = null;
        }
    }

    // *************************************************************************
    // SphereVertex
    // *************************************************************************
    public int id;
    public Vector3 pos, norm;
    public Vector3 geo_rad, geo_deg, geo_unit;
    public float height;
    public boolean is_normal;

   // -------------------------------------------------------------------------
    private LinkedList<SphereVertex.SegmentEntry> m_segments;

    // -------------------------------------------------------------------------
    public SphereVertex(Vector3 v) {
        this(v.x, v.y, v.z);
    }

    // -------------------------------------------------------------------------
    public SphereVertex(float x, float y, float z) {
        // Id
        id = SphereVertex.inst_num++;

        // Distance from center
        height = (float)Math.sqrt(x * x + y * y + z * z);
        is_normal = true;

        // Normal vertex and position are initially equal
        norm = new Vector3(x / height, y / height, z / height);
        pos = new Vector3(norm);

        // Geographical coordinates in radiants
        geo_rad = UtilsGeo.CartesianToGeo(new Vector3(), x, y, z);

        // Normalize longitude
        geo_rad.x = UtilsAngle.Normalize(UtilsGeo.GetLon(geo_rad));

        // Make unit-size geographical coordinates
        geo_unit = new Vector3(geo_rad);
        geo_unit.x /= MathUtils.PI2;                // Longitude
        geo_unit.y /= (MathUtils.PI / 2);           // Latitude

        // Geographical coordinates in degrees
        geo_deg = new Vector3(geo_rad).scl(MathUtils.radiansToDegrees);

        // Array of linked segments
        m_segments = new LinkedList<SphereVertex.SegmentEntry>();
    }

    // -------------------------------------------------------------------------
    public SphereVertex LinkSegment(SphereSegment s) {
        ListIterator<SegmentEntry> it = m_segments.listIterator();
        while(it.hasNext()) {
            if(it.next().segment.equals(s)) {
                return this; // Segment is already linked
            }
        }

        // Link
        m_segments.add(new SphereVertex.SegmentEntry(s));
        return this;
    }

    // -------------------------------------------------------------------------
    public void Finalize() {
        for(SphereVertex.SegmentEntry se : m_segments) {
            if(se != null) {
                se.Finalize();
            }
        }
    }

    // -------------------------------------------------------------------------
    private SphereVertex.SegmentEntry GetSegmentEntry(SphereSegment s) {
        for(SphereVertex.SegmentEntry se : m_segments) {
            if(se.segment.equals(s)) {
                return se;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    public int Alloc(SphereSegment s) {
        SphereVertex.SegmentEntry se = GetSegmentEntry(s);
        Utils.Assert(se != null, 
            "Failed to allocate vertex instance, unknown segment :: vertex=%d segment=%d", 
            id, s.id);
        return se.Alloc();
    }

    // -------------------------------------------------------------------------
    public void Free(SphereSegment s, int inst) {
        SphereVertex.SegmentEntry se = GetSegmentEntry(s);
        Utils.Assert(se != null, 
            "Failed to free vertex instance, unknown segment :: inst=%d vertex=%d segment=%d", 
            inst, id, s.id);
        se.Free(inst);
    }

    // -------------------------------------------------------------------------
    public SphereVertex Scale(float value) {
        height = value;
        is_normal = false;
        pos.set(norm).scl(value);
        return this;
    }

    // -------------------------------------------------------------------------
    public SphereVertex Reset() {
        Scale(1.0f);
        is_normal = true;
        return this;
    }

    // -------------------------------------------------------------------------
    public void WritePosition() {
        // All vertex instances in all segments have same position
        for(SphereVertex.SegmentEntry se : m_segments) {
            float[] va = se.segment.GetVertexArray();
            for(int offset = se.attribute_offset; 
                    offset < se.attribute_offset + SphereVertex.TOTAL_SIZE; 
                    offset += SphereVertex.ATTRIB_NUM) {
                va[offset + SphereVertex.ATTRIB_POS_X] = pos.x;
                va[offset + SphereVertex.ATTRIB_POS_Y] = pos.y;
                va[offset + SphereVertex.ATTRIB_POS_Z] = pos.z;
            }

            // Vertex array becomes dirty
            se.segment.m_is_dirty_vertex = true;
        }
    }

    // -------------------------------------------------------------------------
    public void WriteNormal(SphereSegment s, int inst, Vector3 normal) {
        // Get offset of vertex instance in segmetn's vertex array
        SphereVertex.SegmentEntry se = GetSegmentEntry(s);
        Utils.Assert(se != null, 
            "Failed to write vertex normal, unknown segment :: vertex=%d segment=%d inst=%d", 
            id, s.id, inst);
        int offset = se.attribute_offset + inst * SphereVertex.ATTRIB_NUM;

        // Write normal
        float[] va = s.GetVertexArray();
        va[offset + SphereVertex.ATTRIB_NORM_X] = normal.x;
        va[offset + SphereVertex.ATTRIB_NORM_Y] = normal.y;
        va[offset + SphereVertex.ATTRIB_NORM_Z] = normal.z;

        // Vertex array becomes dirty
        s.m_is_dirty_vertex = true;
    }

    // -------------------------------------------------------------------------
    public int GetInstanceId(SphereSegment s, int inst) {
        SphereVertex.SegmentEntry se = GetSegmentEntry(s);
        Utils.Assert(se != null, 
            "Failed to get instance id, unknown segment :: vertex=%d segment=%d inst=%d", 
            id, s.id, inst);
        return se.instance_id;
    }

    // -------------------------------------------------------------------------
    public int GetRgba(Pixmap map) {
        // Map geographical coordinates to texture coordinates
        float x = 1.0f - UtilsGeo.GetLon(geo_unit);
        float y = 0.5f - UtilsGeo.GetLat(geo_unit) / 2.0f;

        // Map pixel
        int pos_x = (int)(x * map.getWidth());
        int pos_y = (int)(y * map.getHeight());

        // Return pixel color
        if(pos_x >= map.getWidth()) pos_x = map.getWidth() - 1;
        if(pos_y >= map.getHeight()) pos_y = map.getHeight() - 1;
        return map.getPixel(pos_x, pos_y);
    }
}
