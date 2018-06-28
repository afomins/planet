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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public abstract class Camera 
  extends CommonObject 
  implements Interfaces.ICamera, UtilsFlags.IFlagger {
    // *************************************************************************
    // FLAGS
    // *************************************************************************
    public static int F_TARGET_MANDATORY    = 1 << 0;
    public static int F_TARGET_LOCKED       = 1 << 1;
    public static int F_RELATIVE_ROTATION   = 1 << 2;

    // *************************************************************************
    // STATIC
    // *************************************************************************
    protected static Vector3 tmp_v3 = new Vector3();

    // *************************************************************************
    // Camera
    // *************************************************************************
    protected PerspectiveCamera m_gdx_camera;
    protected Interfaces.IRigidBody m_target; // XXX: target should be acquired by ID
    protected int m_flags;

    // -------------------------------------------------------------------------
    public Camera() {
        super("camera");

        // Camera
        m_gdx_camera = new PerspectiveCamera(Main.p.cfg.camera_fov, 
          Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        m_gdx_camera.near = Main.p.cfg.camera_clip_near;
        m_gdx_camera.far = Main.p.cfg.camera_clip_far;

        // GL stuff
        Gdx.gl.glClearColor(
            Main.p.cfg.camera_clear_color.r, 
            Main.p.cfg.camera_clear_color.g, 
            Main.p.cfg.camera_clear_color.b, 
            Main.p.cfg.camera_clear_color.a);
    }

    // -------------------------------------------------------------------------
    public com.badlogic.gdx.graphics.Camera GetGdxCamera() {
        return m_gdx_camera;
    }

    // -------------------------------------------------------------------------
    public void SetTarget(Interfaces.IRigidBody target) {
        m_target = target;
    }

    // *************************************************************************
    // Interfaces.ICamera
    // *************************************************************************
    // http://en.wikipedia.org/wiki/Back-face_culling
    @Override public boolean TestBackfaceCulling(Vector3 norm, Vector3 pos, float threshold) {
        // Get camera-to-triangle vector
        Camera.tmp_v3.set(m_gdx_camera.position).sub(pos);

        // Backface-culling test passes if dot-product is greater than threshold
        return (norm.dot(Camera.tmp_v3) >= threshold);
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Check target
            if(m_target == null && 
              FlagTest(Camera.F_TARGET_MANDATORY)) {
                return true;
            }

            // GL stuff
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            // Update camera transform
            UpdateCamera();

            // Apply camera
            m_gdx_camera.update();
        }
        return true;
    }

    // *************************************************************************
    // UtilsFlags.IFlagger
    // *************************************************************************
    @Override public boolean FlagTest(int mask) {
        return UtilsFlags.TestFlag(m_flags, mask);
    }

    // -------------------------------------------------------------------------
    @Override public void FlagSet(int mask) {
        m_flags = UtilsFlags.SetFlag(this, m_flags, mask);
    }

    // -------------------------------------------------------------------------
    @Override public void FlagUnset(int mask) {
        m_flags = UtilsFlags.UnsetFlag(this, m_flags, mask);
    }
}
