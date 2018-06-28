// -----------------------------------------------------------------------------
package com.matalok.planet;

//-----------------------------------------------------------------------------
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;

// -----------------------------------------------------------------------------
public class Tube 
  extends CommonObject {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static VertexAttribute[] VERTEX_ATTRIB = new VertexAttribute[] {
        VertexAttribute.Position()
    };

    // *************************************************************************
    // Tube
    // *************************************************************************
    private HashMap<PathSegment, TubeSegment> m_segments_active, m_segments_old;

    // -------------------------------------------------------------------------
    public Tube() {
        super("tube");
        m_segments_active = new HashMap<PathSegment, TubeSegment>();
        m_segments_old = new HashMap<PathSegment, TubeSegment>();
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Get ship position
            ShipCtrl ship_ctrl = Main.p.engine.ship_ctrl;
            float path_pos = ship_ctrl.GetLinearPos();

            // Get list of path segments that should build tube
            Path path = Main.p.engine.path;
            LinkedList<PathSegment> segments = new LinkedList<PathSegment>();
            if(!path.GetSegments(path_pos, Main.p.cfg.tube_length, segments)) {
                return true;
            }

            // Swap active and old segment maps 
            HashMap<PathSegment, TubeSegment> tmp = m_segments_active;
            m_segments_active = m_segments_old;
            m_segments_old = tmp;

            // Build map of active segments
            for(PathSegment s : segments) {
                TubeSegment tube_seg = null;

                // Extract segment from old map if it exists
                if(m_segments_old.containsKey(s)) {
                    tube_seg = m_segments_old.get(s);
                    m_segments_old.remove(s);

                // Create new segment if it did not exist previously
                } else {
                    tube_seg = new TubeSegment();
                    tube_seg.BuildGeometry(s);
                    tube_seg.BuildDynamicModel(Tube.VERTEX_ATTRIB);
                }

                // Fill active map
                m_segments_active.put(s, tube_seg);
            }

            // Dispose non-active segments and cleanup old segment map 
            for(TubeSegment seg : m_segments_old.values()) {
                seg.dispose();
            }
            m_segments_old.clear();
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnRender(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            List<RenderableProvider> renderables = Renderer.GetArgsRenderables(args);
            for(TubeSegment seg : m_segments_active.values()) {
                renderables.add(seg.m_model_inst);
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnDispose(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_POST) {
            for(TubeSegment seg : m_segments_active.values()) {
                seg.dispose();
            }
        }
        return true;
    }
}
