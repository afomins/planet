// -----------------------------------------------------------------------------
package com.matalok.planet;

//-----------------------------------------------------------------------------
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class ShipCamera 
  extends Camera 
  implements Interfaces.IMouseClient, Interfaces.IKeyboardClient, 
    Interfaces.IShipCtrlClient {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static final Vector3 ORIGIN_OFFSET = new Vector3(UtilsAngle.VECT_FORW);
    private static final Vector3 ORIGIN_POS    = new Vector3(UtilsAngle.VECT_FORW).scl(-1.0f);

    // *************************************************************************
    // ShipCamera
    // *************************************************************************
    private Vector3 m_pos_polar, m_offset_polar;
    private Utils.FFloat m_ship_rotation;
    private TweenManager m_tween;

    // -------------------------------------------------------------------------
    public ShipCamera() {
        m_pos_polar = new Vector3();
        m_offset_polar = new Vector3();
        m_ship_rotation = new Utils.FFloat(0.0f);
        m_tween = new TweenManager();
    }

    // -------------------------------------------------------------------------
    public void SetPosOffset(Vector3 pos, Vector3 offset) {
        m_pos_polar.set(pos);
        m_offset_polar.set(offset);
        FinalizeAngles();
    }

    // -------------------------------------------------------------------------
    private void FinalizeAngles() {
        // Normalize horizontal angle
        UtilsAngle.SetPolarH(m_pos_polar, 
            UtilsAngle.Normalize(
                UtilsAngle.GetPolarH(m_pos_polar)));

        // Clamp vertical angle
        UtilsAngle.SetPolarV(m_pos_polar, 
            UtilsAngle.Clamp(
                UtilsAngle.GetPolarV(m_pos_polar), 
                -MathUtils.PI / 2, MathUtils.PI / 2, MathUtils.degRad));
    }

    // *************************************************************************
    // Interface.ICamera
    // *************************************************************************
    private static Vector3 target_pos = new Vector3();
    private static Quaternion local_rot = new Quaternion();
    @Override public void UpdateCamera() {
        // Tween
        m_tween.update(Main.p.time_man.GetDeltaSec());

        // Build rotation quaternion
        if(FlagTest(Camera.F_RELATIVE_ROTATION)) {
            local_rot.setFromAxisRad(UtilsAngle.VECT_FORW, m_ship_rotation.v);
            local_rot.mulLeft(m_target.GetRot());
        } else {
            local_rot.idt();
        }

        //
        // UP vector
        //
        m_gdx_camera.up.set(UtilsAngle.VECT_UP);
        if(FlagTest(Camera.F_TARGET_LOCKED)) {
            m_gdx_camera.up.mul(local_rot);
        }

        //
        // Target position
        //
        if(FlagTest(Camera.F_TARGET_LOCKED)) {
            float radius = UtilsAngle.GetPolarR(m_offset_polar);
            target_pos.set(ShipCamera.ORIGIN_OFFSET).scl(radius);
            UtilsAngle.Rotate(target_pos, m_offset_polar);
            target_pos.mul(local_rot);
        } else {
            target_pos.setZero();
        }
        target_pos.add(m_target.GetPos());

        //
        // Camera position
        //
        float radius = UtilsAngle.GetPolarR(m_pos_polar);
        m_gdx_camera.position.set(ShipCamera.ORIGIN_POS).scl(radius);
        UtilsAngle.Rotate(m_gdx_camera.position, m_pos_polar);

        if(FlagTest(Camera.F_TARGET_LOCKED)) {
            m_gdx_camera.position.mul(local_rot);
        }
        m_gdx_camera.position.add(target_pos);

        //
        // Finalize camera
        //
        m_gdx_camera.lookAt(target_pos);

        Vector3 pos = m_target.GetPos();
        Main.p.gui.WriteMsg("target pos :: x=%.2f y=%.2f z=%.2f", 
            pos.x, pos.y, pos.z);

        // Debug
        Main.p.gui.WriteMsg("camera pos :: type=%s h=%.2f v=%.2f r=%.2f rot=%.2f",
            FlagTest(Camera.F_TARGET_LOCKED) ? "locked" : "unlocked",
            MathUtils.radDeg * UtilsAngle.GetPolarH(m_pos_polar),
            MathUtils.radDeg * UtilsAngle.GetPolarV(m_pos_polar),
            UtilsAngle.GetPolarR(m_pos_polar),
            m_ship_rotation.v * MathUtils.radDeg);

        Main.p.gui.WriteMsg("camera off :: h=%.2f v=%.2f r=%.2f",
            MathUtils.radDeg * UtilsAngle.GetPolarH(m_offset_polar),
            MathUtils.radDeg * UtilsAngle.GetPolarV(m_offset_polar),
            UtilsAngle.GetPolarR(m_offset_polar));
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnReset(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            m_ship_rotation.v = 0.0f;
            m_tween.killAll();
        }
        return true;
    }

    // -------------------------------------------------------------------------
    private static Matrix4 shaper_mat = new Matrix4();
    @Override public boolean OnShape(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
/*
            ShapeRenderer shaper = Renderer.GetShaper(args);

            shaper_mat.idt().translate(m_target.GetPos()).rotate(m_target.GetRot());
            shaper.setTransformMatrix(shaper_mat);

            shaper.rotate(1.0f, 0.0f, 0.0f, 90.0f);
            shaper.begin(ShapeRenderer.ShapeType.Line);
            shaper.setColor(Color.ORANGE);

            float radius = UtilsAngle.GetPolarR(m_offset_polar);
            shaper.circle(0.0f, 0.0f, radius, 16);

            float angle_h = UtilsAngle.GetPolarH(m_offset_polar);
            shaper.line(0.0f, 0.0f, 
                MathUtils.sin(angle_h) * radius, 
                MathUtils.cos(angle_h) * radius);
            shaper.end();
*/
        }
        return true;
    }

    // *************************************************************************
    // InputMonitorMouse.IClient
    // *************************************************************************
    @Override public void OnMove(float x, float y, int button) {
        if(button == Input.Buttons.RIGHT) {
            UtilsAngle.AddPolarH(m_pos_polar, x * Main.p.cfg.ship_cam_rotate_h_factor);
            UtilsAngle.AddPolarV(m_pos_polar, y * Main.p.cfg.ship_cam_rotate_v_factor);

        } else if(button == Input.Buttons.LEFT) {
            UtilsAngle.AddPolarV(m_offset_polar, x * Main.p.cfg.ship_cam_rotate_v_factor);
            UtilsAngle.AddPolarR(m_offset_polar, 
                UtilsAngle.GetPolarR(m_pos_polar) * y * Main.p.cfg.ship_cam_move_factor);
        }
        FinalizeAngles();
    }

    // -------------------------------------------------------------------------
    @Override public void OnScroll(int size) {
        float r = UtilsAngle.AddPolarR(m_pos_polar, 
            UtilsAngle.GetPolarR(m_pos_polar) * size * Main.p.cfg.ship_cam_scroll_factor);
        if(r < 0.1f) UtilsAngle.SetPolarR(m_pos_polar, 0.1f);
    }

    // *************************************************************************
    // InputMonitorKeyboard.IClient
    // *************************************************************************
    @Override public void OnKeyUp(int key) {
    }

    // -------------------------------------------------------------------------
    @Override public void OnKeyDown(int key, boolean first_time) {
        float step = 0.01f;
        switch(key) {
            case Input.Keys.INSERT:     m_offset_polar.x += step; break;
            case Input.Keys.BACKSPACE:  m_offset_polar.x -= step; break;
            case Input.Keys.HOME:       m_offset_polar.y += step; break;
            case Input.Keys.END:        m_offset_polar.y -= step; break;
            case Input.Keys.PAGE_UP:    m_offset_polar.z += step; break;
            case Input.Keys.PAGE_DOWN:  m_offset_polar.z -= step; break;
        }
    }

    // *************************************************************************
    // UtilsFlags.IFlagger
    // *************************************************************************
    @Override public boolean OnFlagChange(int flag, boolean is_set) {
        if(flag == Camera.F_TARGET_LOCKED) {
            if(is_set) {
                Log.Debug("Locking camera");
            } else {
                Log.Debug("Unlocking camera");
            }
        }
        return true;
    }

    // *************************************************************************
    // Interfaces.IShipCtrlClient
    // *************************************************************************
    @Override public void OnRotate(float dest) {
        dest = UtilsAngle.Normalize(dest);

        // Limit ship tilting
        if(Main.p.cfg.ship_cam_tilt_limit) {
            boolean dest_good = (
                dest >= Main.p.cfg.ship_cam_tilt_min || 
                dest <= Main.p.cfg.ship_cam_tilt_max);

            // Adjust destination when in bad-zone
            if(!dest_good) {
                float diff = MathUtils.PI - dest;
                boolean is_right = (diff > 0);
                diff = (is_right) ? diff : -diff;

                float thresh = MathUtils.PI / 2 - 10.0f * MathUtils.degRad;
                if(diff >= thresh) {
                    dest = (is_right) ? 
                        Main.p.cfg.ship_cam_tilt_max : 
                        Main.p.cfg.ship_cam_tilt_min;
                } else {
                    dest = MathUtils.sin(diff) * Main.p.cfg.ship_cam_tilt_max * 
                        ((is_right) ? 1.0f : -1.0f);
                }
            }
        }

        // Normalize angles
        Vector2 norm = UtilsAngle.NormalizeDiff(m_ship_rotation.v, dest);
        m_ship_rotation.v = UtilsAngle.GetNormFrom(norm);
        dest = UtilsAngle.GetNormTo(norm);

        // Restart tweening
        m_tween.killAll();
        Tween.to(m_ship_rotation, 0, Main.p.cfg.ship_cam_rotate_duration)
             .ease(aurelienribon.tweenengine.equations.Sine.OUT)
             .target(dest)
             .start(m_tween);
    }

    // -------------------------------------------------------------------------
    @Override public void OnShoot(int x, int y) {
    }
}
