// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class Shooter 
  extends CommonObject {
    // *************************************************************************
    // Shooter
    // *************************************************************************
    private Model m_model;
    private long m_prev_shoot_time; 

    // -------------------------------------------------------------------------
    public Shooter() {
        super("shooter");
    }

    // -------------------------------------------------------------------------
    public void SetBullet(Model model) {
        m_model = model;
    }

    // -------------------------------------------------------------------------
    public void Shoot(Vector3 pos, Quaternion rot) {
        long cur_time = Main.p.time_man.GetCur();
        if(m_prev_shoot_time + Main.p.cfg.shooter_timeout < cur_time) {
            ShooterBullet b = (ShooterBullet)AddChild(new ShooterBullet());
            b.Init(m_model, pos, rot);

            m_prev_shoot_time = cur_time;
        }
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnRender(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            Main.p.gui.WriteMsg("shooter :: bullets=%d", GetChildNum());
        }
        return true;
    }
}
