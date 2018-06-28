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
import java.util.List;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class ShooterBullet 
  extends CommonObject
  implements Interfaces.ISmartModel, Interfaces.IRigidBody {
    // *************************************************************************
    // ShooterBullet
    // *************************************************************************
    private ModelInstance m_model_inst;
    private Utils.DirectionEx m_direx;
    private float m_total_distance;

    // -------------------------------------------------------------------------
    public ShooterBullet() {
        super("bullet");
        m_direx = new Utils.DirectionEx();
    }

    // -------------------------------------------------------------------------
    public void Init(Model model, Vector3 pos, Quaternion rot) {
        // Initial position
        m_model_inst = new ModelInstance(model);
        m_model_inst.transform.set(pos, rot);

        // Initial direction
        m_direx.pos.set(pos);
        m_direx.rot.set(rot);
        m_direx.dir.set(UtilsAngle.VECT_FORW);

        // Debug
        Log.Debug("Shooting bullet :: pos=(%.2f, %.2f, %.2f)", 
            pos.x, pos.y, pos.z);
    }

    // -------------------------------------------------------------------------
    private static Vector3 move_delta = new Vector3();
    private boolean MoveForward(long time_delta) {
        // Test total distance
        float linear_delta = Main.p.cfg.bullet_speed_linear * time_delta;
        m_total_distance += linear_delta;
        if(Main.p.cfg.bullet_distance_limit > 0.0f && 
           Main.p.cfg.bullet_distance_limit < m_total_distance) {
            // Distance limit reached
            Log.Debug("Bullet distance limit reached :: name=%s distance=%.2f", 
                GetNameId(), m_total_distance);
            return false;
        }

        // Adjust direction (pitch)
        float angular_delta = Main.p.cfg.bullet_speed_angular * time_delta;
        m_direx.dir.rotateRad(UtilsAngle.VECT_LEFT, angular_delta);

        // Get delta of linear movement
        move_delta
            .set(m_direx.dir)
            .mul(m_direx.rot)
            .scl(linear_delta);

        // Move forward
        m_direx.pos.add(move_delta);

        // Apply transformation
        Matrix4 tr = m_model_inst.transform;
        tr.setToTranslation(m_direx.pos);
        tr.rotate(m_direx.rot);
        tr.scale(Main.p.cfg.bullet_scale, Main.p.cfg.bullet_scale, Main.p.cfg.bullet_scale);

        // Test altitude limit
        float altitude = m_direx.pos.len();
        if(altitude < Main.p.cfg.bullet_altitude_limit_low ||
           altitude > Main.p.cfg.bullet_altitude_limit_high) {
            // Altitude limit reached
            Log.Debug("Bullet altitude limit reached :: name=%s alt=%.2f", GetNameId(), altitude);
            return false;
        } else {
            // Altitude is good
            return true;
        }
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            if(!MoveForward(Main.p.time_man.GetDelta())) {
                Expire();
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnRender(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            Camera camera = Renderer.GetArgsCamera(args);
            List<RenderableProvider> renderables = Renderer.GetArgsRenderables(args);
            RenderableProvider inst = TestModelVisibility(camera);
            if(inst != null) {
                renderables.add(inst);
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnShape(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
//            ShapeRenderer shaper = Renderer.GetShaper(args);
//            Main.p.renderer.ShapeAxis(shaper, m_model_inst.transform, 
//                0.02f / Main.p.cfg.bullet_scale);
        }
        return true;
    }

    // *************************************************************************
    // Interfaces.ISmartModel
    // *************************************************************************
    @Override public RenderableProvider TestModelVisibility(Interfaces.ICamera camera) {
        return m_model_inst;
    }

    // *************************************************************************
    // Interfaces.IRigidBody
    // *************************************************************************
    @Override public Vector3    GetPos() { return m_direx.pos; }
    @Override public Quaternion GetRot() { return m_direx.rot; }
}
