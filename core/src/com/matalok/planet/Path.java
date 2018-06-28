// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class Path 
  extends CommonObject 
  implements UtilsFlags.IFlagger {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static final float STEP_SIZE            = 5.0f * MathUtils.degRad;
    private static final float NORMAL_SIZE          = 0.2f;
    private static final float ARRAY_GROWTH_FACTOR  = 1.6f;
    private static final int ARRAY_SIZE             = 64;

    // *************************************************************************
    // FLAGS
    // *************************************************************************
    public static final int F_INTERPOLATE_YPR       = 1 << 0;

    // *************************************************************************
    // Path
    // *************************************************************************
    private LinkedList<Vector3> m_vertices;
    private PathSegment[] m_segments;
    private int m_segment_num;
    private boolean m_is_loop;
    private int m_flags;

    // -------------------------------------------------------------------------
    public Path() {
        super("path");
        m_vertices = new LinkedList<Vector3>();
        m_segments = new PathSegment[Path.ARRAY_SIZE];
    }

    // -------------------------------------------------------------------------
    private static Vector3 grow_vertex = new Vector3();
    public void Grow(float bearing, float length, float height) {
        // Get last segment 
        Utils.Assert(m_vertices.size() > 1, "Failed to grow path, not enough initial vertices");

        // Get bearing of last 2 added vertices
        ListIterator<Vector3> it = m_vertices.listIterator(m_vertices.size());
        Vector3 end = it.previous(),
                pre_end = it.previous();
        float initial_bearing = UtilsGeo.GetBearing(pre_end, end);

        // New vertex continues previous bearing
        UtilsGeo.GetDest(grow_vertex, end, length, initial_bearing + bearing, height);
        AddVertex(UtilsGeo.GetLon(grow_vertex), UtilsGeo.GetLat(grow_vertex), height);
    }

    // -------------------------------------------------------------------------
    public void AddVertexDeg(float lon, float lat, float height) {
        AddVertex(MathUtils.degRad * lon, MathUtils.degRad * lat, height);
    }

    // -------------------------------------------------------------------------
    public void AddVertex(float lon, float lat, float height) {
        // Create new vertex
        m_vertices.add(new Vector3(lon, lat, height));

        // Segment can not be build if less than 3 vertices
        if(m_vertices.size() < 3) {
            return;
        }

        // Get spline key-points
        ListIterator<Vector3> it = m_vertices.listIterator(m_vertices.size());
        Vector3 
            post_end = it.previous(),
            end = it.previous(),
            start = it.previous(),
            pre_start = (m_vertices.size() > 3) ? it.previous() :
                new Vector3(end).sub(start).scl(-1.0f).add(start);

        // Create then damn segment
        CreateSegment(pre_start, start, end, post_end);
    }

    // -------------------------------------------------------------------------
    public void MakeLoop() {
        Utils.Assert(m_vertices.size() >= 3, 
            "Failed to create loop path, not enough vertices :: num=%d",
            m_vertices.size());

        // Get spline key-points
        ListIterator<Vector3> it_end = m_vertices.listIterator(m_vertices.size());
        ListIterator<Vector3> it_begin = m_vertices.listIterator();
        Vector3 
            last = it_end.previous(),
            pre_last = it_end.previous(),
            pre_pre_last = it_end.previous(),
            start = it_begin.next(),
            post_start = it_begin.next();

        // Create then damn segments connecting end to beginning
        CreateSegment(pre_pre_last, pre_last, last, start);
        CreateSegment(pre_last, last, start, post_start);
        m_is_loop = true;
    }

    // -------------------------------------------------------------------------
    public boolean GetSegments(float offset, float length, LinkedList<PathSegment> segments) {
        // Find first segment
        int idx = PathUtils.SearchSegmentArrayIdx(
            m_segments, 0, m_segment_num, offset);
        if(idx < 0) {
            return false;
        }

        // Make length relative to end of first segment 
        length += m_segments[idx].len;

        // Build list of segments until length
        while(length > 0.0f) {
            // Push segment
            PathSegment seg = m_segments[idx];
            segments.add(seg);

            // Decrease length
            length -= seg.len;

            // Index of next segment
            idx = (idx < m_segment_num - 1) ? idx + 1 : 0;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    private static Vector3 norm_ypr_from = new Vector3();
    private static Vector3 norm_ypr_to = new Vector3();
    public boolean GetDirectionEx(Utils.DirectionEx dest, float offset, int flags) {
        float orig_offset = offset;

        // Find matching segment
        int cur_seg_idx = PathUtils.SearchSegmentArrayIdx(
            m_segments, 0, m_segment_num, offset);
        if(cur_seg_idx < 0) {
            return false;
        }
        PathSegment cur_seg = m_segments[cur_seg_idx];

        // Make segment-local offset
        offset -= cur_seg.offset;

        // Find matching step inside segment
        int cur_step_idx = PathUtils.SearchSegmentArrayIdx(
            cur_seg.steps, 0, cur_seg.steps.length, offset);
        if(cur_step_idx < 0) {
            return false;
        }
        PathStep cur_step = cur_seg.steps[cur_step_idx];

        // Make step-local offset
        offset -= cur_step.offset;
        if(offset < 0.0f || offset >= cur_step.len) {
            Log.Err(
                "Failed to find path offset position, wrong offset :: offset=%.2f len=%.2f", 
                offset, cur_step.len);
            return false;
        }

        // Get next step from current segment
        PathStep next_step = null;
        if(cur_step_idx < cur_seg.steps.length - 1) {
            next_step = cur_seg.steps[cur_step_idx + 1];

        // Get first step from next segment
        } else {
            PathSegment next_seg = (cur_seg_idx < m_segment_num - 1) ? 
                m_segments[cur_seg_idx + 1] : m_segments[0];
            next_step = next_seg.steps[0];
        }

        // Normalize YPR angles
        Path.norm_ypr_from.set(cur_step.dir.ypr);
        Path.norm_ypr_to.set(next_step.dir.ypr);
        UtilsAngle.NormalizeDiff(Path.norm_ypr_from, Path.norm_ypr_to);

        // Set normalized YPR angles of current step
        dest.ypr.set(Path.norm_ypr_from);

        // Interpolate YPR angles between current step and next step
        if(FlagTest(Path.F_INTERPOLATE_YPR)) {
            float step_done = offset / cur_step.len;
            dest.ypr.add(
                Utils.GetDiff(Path.norm_ypr_from, Path.norm_ypr_to).
                    scl(step_done));
        }

        // Get position by offset inside current step
        if(UtilsFlags.TestFlag(flags, Utils.DirectionEx.F_POS)) {
            dest.pos = cur_step.GetPosByOffset(
               dest.pos, cur_seg.seg_model.m_vertex_array, offset);
        }

        // Direction vector is unchanged
        if(UtilsFlags.TestFlag(flags, Utils.DirectionEx.F_VECT)) {
            dest.dir.set(cur_step.dir.dir);
        }

        // Calculate quaternion from YPR
        if(UtilsFlags.TestFlag(flags, Utils.DirectionEx.F_ROT)) {
            UtilsAngle.SetRotQuaternion(dest.rot, dest.ypr);
        }

        // Debug
        if(UtilsFlags.TestFlag(flags, Utils.DirectionEx.F_ALL)) {
            Main.p.gui.WriteMsg("path :: progress=%.2f:%.2f(%d%%) seg=%d:%d step=%d:%d",
                orig_offset, GetLength(), (int)(100 * orig_offset / GetLength()),
                cur_seg_idx, m_segment_num,
                cur_step_idx, cur_seg.steps.length);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    public void Log() {
        Log.Debug("Path segments: %d", m_segment_num);
        for(int i = 0; i < m_segment_num; i++) {
            PathSegment seg = m_segments[i];
            Log.Debug("  > idx=%d offset=%.2f len=%.2f step_num=%d", 
                i, seg.offset, seg.len, seg.steps.length);
            for(PathStep step : seg.steps) {
                step.Log(seg.seg_model.GetVertexArray());
            }
        }
    }

    // -------------------------------------------------------------------------
    public float GetLength() {
        return (m_segment_num > 0) ? m_segments[m_segment_num - 1].GetEnd() : 0.0f;
    }

    // -------------------------------------------------------------------------
    private void CreateSegment(Vector3 pre_start, Vector3 start, 
      Vector3 end, Vector3 post_end) {
        Utils.Assert(m_is_loop == false, "Failed to create segment for looped path");

        // Create new segment
        PathSegment seg = 
            new PathSegment(pre_start, start, end, post_end, 
                Path.NORMAL_SIZE, Path.STEP_SIZE);
 
        // New segment continues last segment
        if(m_segment_num > 0) {
            seg.UpdateOffset(GetLength());
        }

        // Resize segment array if limit reached
        if(m_segment_num == m_segments.length) {
            PathSegment[] new_segments = 
                new PathSegment[(int)(m_segment_num + Path.ARRAY_GROWTH_FACTOR)];
            Log.Debug("Resizing path segment array :: old_size=%d new_size=%d", 
                m_segment_num, new_segments.length);

            // Copy old to new
            java.lang.System.arraycopy(m_segments, 0, new_segments, 0, m_segment_num);
            m_segments = new_segments;
        }

        // Append new segment to array
        m_segments[m_segment_num++] = seg;
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnReset(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Clear vertices
            m_vertices.clear();

            // Dispose models
            OnDispose(CommonObject.STAGE_POST, null);

            // Reset segments
            m_segments = new PathSegment[Path.ARRAY_SIZE];
            m_segment_num = 0;
            m_is_loop = false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnRender(int stage, Object[] args) {
/*
        if(stage == CommonObject.STAGE_PRE) {
            List<RenderableProvider> renderables = Renderer.GetArgsRenderables(args);
            for(int i = 0; i < m_segment_num; i++) {
                renderables.add(m_segments[i].seg_model.m_model_inst);
                renderables.add(m_segments[i].nor_model.m_model_inst);
            }
        }
*/
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnDispose(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_POST) {
            for(int i = 0; i < m_segment_num; i++) {
                m_segments[i].seg_model.dispose();
            }
        }
        return true;
    }

    // *************************************************************************
    // UtilsFlags.IFlagger
    // *************************************************************************
    public boolean FlagTest(int mask) {
        return UtilsFlags.TestFlag(m_flags, mask);
    }

    // -------------------------------------------------------------------------
    public void FlagSet(int mask) {
        m_flags = UtilsFlags.SetFlag(this, m_flags, mask);
    }

    // -------------------------------------------------------------------------
    public void FlagUnset(int mask) {
        m_flags = UtilsFlags.UnsetFlag(this, m_flags, mask);
    }

    // -------------------------------------------------------------------------
    public boolean OnFlagChange(int flag, boolean is_set) {
        return true;
    }
}
