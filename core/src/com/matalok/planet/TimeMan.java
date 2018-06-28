// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import java.util.Calendar;

import com.badlogic.gdx.Gdx;

// -----------------------------------------------------------------------------
public class TimeMan extends CommonObject {
    // *************************************************************************
    // TimeMan
    // *************************************************************************
    private long m_startup, m_cur, m_delta;
    private float m_cur_sec, m_delta_sec;

    // -------------------------------------------------------------------------
    public TimeMan() {
        super("time-man");
        m_startup = Calendar.getInstance().getTimeInMillis();
        UpdateTime();
    }

    // -------------------------------------------------------------------------
    public long GetCur() { return m_cur; }
    public long GetDelta() { return m_delta; }
    public float GetCurSec() { return m_cur_sec; }
    public float GetDeltaSec() { return m_delta_sec; }

    // -------------------------------------------------------------------------
    private void UpdateTime() {
        long prev = m_cur; 
        m_cur = Calendar.getInstance().getTimeInMillis() - m_startup;
        m_cur_sec = Utils.MsecToSec(m_cur);
        m_delta = m_cur - prev;
        m_delta_sec = Utils.MsecToSec(m_delta);
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            UpdateTime();
            Main.p.gui.WriteMsg("timing :: fps=%d frame-len=%d", 
                    Gdx.graphics.getFramesPerSecond(), GetDelta());
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnReset(int stage, Object[] args) {
        return true; // XXX
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnPause(int stage, Object[] args) {
        return true; // XXX
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnResume(int stage, Object[] args) {
        return true; // XXX
    }
}
