// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import java.util.LinkedList;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class ShipCtrl
  extends CommonObject
  implements Interfaces.IRigidBody, 
             Interfaces.IKeyboardClient, Interfaces.IGestureClient {
    // *************************************************************************
    // ShipCtrl
    // *************************************************************************
    private Utils.DirectionEx m_direx;
    private float m_pos;
    private LinkedList<Interfaces.IShipCtrlClient> m_clients;

    private float[] m_rotate_snap_angles;
    private int m_rotate_snap_idx;

    private Utils.FFloat m_rotate_angle;
    private TweenManager m_tween;

    // -------------------------------------------------------------------------
    public ShipCtrl() {
        super("ship-ctrl");
        m_direx = new Utils.DirectionEx();
        m_clients = new LinkedList<Interfaces.IShipCtrlClient>();
        m_tween = new TweenManager();

        // Rotation
        m_rotate_snap_angles = Main.p.cfg.tube_sector_angles;
        m_rotate_angle = new Utils.FFloat(0);
    }

    // -------------------------------------------------------------------------
    public boolean GetDirectionEx(Utils.DirectionEx dest, float offset, int flags) {
        Path path = Main.p.engine.path;
        if(!path.GetDirectionEx(dest, m_pos + offset, flags)) {
            Log.Err("Failed to update direx from ship controller");
            return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    public float GetRotateAngle() {
        return m_rotate_angle.v;
    }

    // -------------------------------------------------------------------------
    public void RegisterClient(Interfaces.IShipCtrlClient client) {
        m_clients.add(client);
    }

    // -------------------------------------------------------------------------
    public float GetLinearPos() {
        return m_pos;
    }

    // -------------------------------------------------------------------------
    private void Rotate(int dir) {
        // Get next snap idx
        int old_idx = m_rotate_snap_idx;
        m_rotate_snap_idx += dir;
        m_rotate_snap_idx %= m_rotate_snap_angles.length;
        if(m_rotate_snap_idx < 0) {
            m_rotate_snap_idx += m_rotate_snap_angles.length;
        }

        // Normalize angles
        float to = m_rotate_snap_angles[m_rotate_snap_idx];
        Vector2 norm = UtilsAngle.NormalizeDiff(m_rotate_angle.v, to);
        m_rotate_angle.v = UtilsAngle.GetNormFrom(norm);
        to = UtilsAngle.GetNormTo(norm);

        // Restart tweening
        m_tween.killAll();
        Tween.to(m_rotate_angle, 0, Main.p.cfg.ship_ctrl_rotate_duration)
             .ease(aurelienribon.tweenengine.equations.Back.INOUT)
             .target(to)
             .start(m_tween);

        // Dbg
        Log.Debug("Rotating ship :: dir=%d idx=%d->%d angle=%.2f->%.2f",
                dir, old_idx, m_rotate_snap_idx, 
                UtilsAngle.GetNormFrom(norm) * MathUtils.radDeg, 
                UtilsAngle.GetNormTo(norm) * MathUtils.radDeg);

        // Notify clients
        for(Interfaces.IShipCtrlClient c : m_clients) {
            c.OnRotate(to);
        }
    }

    // -------------------------------------------------------------------------
    private void Shoot(int x, int y) {
        // Notify clients
        for(Interfaces.IShipCtrlClient c : m_clients) {
            c.OnShoot(x, y);
        }
    }

    // -------------------------------------------------------------------------
    private void MoveForward() {
        Path path = Main.p.engine.path;
        m_pos = Main.p.time_man.GetCur() * Main.p.cfg.ship_ctrl_speed;
        float limit = path.GetLength();
        if(m_pos >= limit) {
            m_pos %= limit;
        }
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Tween
            m_tween.update(Main.p.time_man.GetDeltaSec());

            // Get direx for current offset
            if(!GetDirectionEx(m_direx, 0.0f, Utils.DirectionEx.F_ALL)) {
                return false;
            }

            // Update compass
            Main.p.gui.compass.Rotate(
                UtilsGeo.GetBearingByCartesian(m_direx.pos, m_direx.dir, 0.1f));

            // Move forward
            MoveForward();
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnReset(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            m_rotate_snap_idx = 0;
            m_rotate_angle.v = m_rotate_snap_angles[m_rotate_snap_idx];
            m_tween.killAll();
        }
        return true;
    }

    // *************************************************************************
    // Interface.IRigidBody
    // *************************************************************************
    @Override public Vector3    GetPos() { return m_direx.pos; }
    @Override public Quaternion GetRot() { return m_direx.rot; }

    // *************************************************************************
    // InputMonitorKeyboard.IClient
    // *************************************************************************
    @Override public void OnKeyUp(int key) {
    }

    // -------------------------------------------------------------------------
    @Override public void OnKeyDown(int key, boolean first_time) {
        switch(key) {
            case Input.Keys.LEFT:   Rotate(-1);     break;
            case Input.Keys.RIGHT:  Rotate(+1);     break;
            case Input.Keys.SPACE:  Shoot(-1, -1);  break;
        }
    }

    // *************************************************************************
    // InputMonitorGesture.IClient
    // *************************************************************************
    @Override public void OnSwipe(float x, float y) {
        Log.Debug("Swipe :: x=%s.2f y=%.2f", x, y);
//        if(x > 0.0f) Rotate(+1);
//        else         Rotate(-1);
    }

    // -------------------------------------------------------------------------
    @Override public void OnTap(float x, float y, int count) {
        Log.Debug("Tap :: x=%s.2f y=%.2f count=%d", x, y, count);
        if(x > Gdx.graphics.getWidth() / 2.0f) {
            Rotate(+1);
        } else {
            Rotate(-1);
        }
//        Shoot();
    }

    // -------------------------------------------------------------------------
    @Override public void OnPan(float x, float y, float dx, float dy, int state) {
        Log.Debug("pan :: xy=(%.2f, %.2f) dxy=(%.2f, %.2f) state=%d", x, y, dx, dy, state);

        GuiCanvas canvas = Main.p.gui.canvas;
        canvas.pm.setColor(Color.WHITE);

        if(state == InputMonitorGesture.PAN_START) {
            canvas.CursorSet(x, y);
        }

        Vector2 cursor = canvas.CursorGrow(dx, dy);
/*
        canvas.pm.drawLine(
            (int)(cursor.x - dx), (int)(cursor.y - dy), 
            (int)cursor.x, (int)cursor.y);
        canvas.Flush();
*/
        Shoot((int)cursor.x, (int)cursor.y);
    }
}
