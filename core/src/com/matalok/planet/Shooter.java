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
