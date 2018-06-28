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
import java.util.List;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class Ship 
  extends CommonObject 
  implements Interfaces.IShipCtrlClient, Interfaces.IRigidBody {
    // *************************************************************************
    // Ship
    // *************************************************************************
    private ModelInstance m_model_inst;
    private Utils.DirectionEx m_direx;

    // -------------------------------------------------------------------------
    public Ship() {
        super("ship");
        m_direx = new Utils.DirectionEx();
    }

    // -------------------------------------------------------------------------
    public void SetModel(Model model) {
        m_model_inst = new ModelInstance(model);
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    private Utils.DirectionEx direx_next = new Utils.DirectionEx();
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Get ship controller
            ShipCtrl ctrl = Main.p.engine.ship_ctrl;

            // Inherit position and rotation from controller
            m_direx.pos.set(ctrl.GetPos());
            m_direx.rot.set(ctrl.GetRot());

            // Get path rotation few steps ahead
            ctrl.GetDirectionEx(
                direx_next, Main.p.cfg.ship_next_direx_offset, Utils.DirectionEx.F_ROT);
            float yaw_diff = direx_next.rot.getYawRad() - m_direx.rot.getYawRad();

            // Set global position and rotation inherited from path
            Matrix4 tr = m_model_inst.transform;
            tr.setToTranslation(m_direx.pos);
            tr.rotate(m_direx.rot);

            // Rotate around path center
            float angle = ctrl.GetRotateAngle();
            UtilsAngle.SetRotRoll(tr, angle);
            tr.translate(0.0f, Main.p.cfg.tube_radius, 0.0f);

            // Roll around ship center
            UtilsAngle.SetRotRoll(tr, yaw_diff);

            // Export current pos/rot via Interfaces.IRigidBody
            tr.getRotation(m_direx.rot);
            tr.getTranslation(m_direx.pos);

            // Scale
            tr.scale(Main.p.cfg.ship_scale, Main.p.cfg.ship_scale, Main.p.cfg.ship_scale);
            Main.p.gui.WriteMsg("ship :: rotate=%.2f", angle * MathUtils.radDeg);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnRender(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            List<RenderableProvider> renderables = Renderer.GetArgsRenderables(args);
            renderables.add(m_model_inst);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnShape(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            ShapeRenderer shaper = Renderer.GetShaper(args);
            Main.p.renderer.ShapeAxis(shaper, m_model_inst.transform, 
                0.04f / Main.p.cfg.ship_scale);
        }
        return true;
    }

    // *************************************************************************
    // Interfaces.IRigidBody
    // *************************************************************************
    @Override public Vector3    GetPos() { return m_direx.pos; }
    @Override public Quaternion GetRot() { return m_direx.rot; }

    // *************************************************************************
    // Interfaces.IShipCtrlClient
    // *************************************************************************
    @Override public void OnRotate(float dest) {
    }

    // -------------------------------------------------------------------------
    @Override public void OnShoot(int x, int y) {
        ShipCtrl ctrl = Main.p.engine.ship_ctrl;
        Main.p.engine.shooter.Shoot(
            m_direx.pos,                // Position of the ship rotated around controller
            ctrl.GetRot());             // Initial rotation of controller
    }
}
