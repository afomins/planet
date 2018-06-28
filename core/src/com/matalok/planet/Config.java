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
import java.util.HashMap;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class Config {
    // *************************************************************************
    // ASSET
    // *************************************************************************
    public HashMap<String, String[]> ass_man = new HashMap<String, String[]>() {{
        put("model", new String[] {
                "ship.g3db",                                    // 0
                "bullet.g3db"});                                // 1
        put("ta", new String[] {
                "skybox/purple-nebula-complex.atlas"});         // 0
    }};
    public String asset_model_ship   = ass_man.get("model")[0],
                  asset_model_bullet = ass_man.get("model")[1],
                  asset_ta_skybox    = ass_man.get("ta")[0];

    // *************************************************************************
    // CAMERA
    // *************************************************************************
    public float   camera_fov         = 70.0f;
    public float   camera_clip_near   = 0.1f;
    public float   camera_clip_far    = 300.0f;
    public Color   camera_clear_color = new Color(0.3f, 0.3f, 0.3f, 1.0f);
    public int     camera_target      = 0; // [0 - ship; 1 - sphere]
    public Vector3 camera_pos         = (camera_target == 0) ? 
        new Vector3(350.0f, 50.0f, 0.50f) :     // Lock on ship
        new Vector3(150.0f, 42.0f, 2.5f);       // Lock on sphere

    public Vector3 camera_offset      = (camera_target == 0) ? 
        new Vector3(-10.0f, 32.0f, 0.31f) : // Lock on ship
        new Vector3(0.0f, 0.0f, 0.0f);      // Lock on sphere

    // *************************************************************************
    // SHIP
    // *************************************************************************
    public float ship_scale             = 0.02f;
    public float ship_next_direx_offset = 10.0f * MathUtils.degRad;

    // *************************************************************************
    // SHIP-CAMERA
    // *************************************************************************
    public float   ship_cam_scroll_factor   = 0.05f;
    public float   ship_cam_rotate_h_factor = -0.01f;
    public float   ship_cam_rotate_v_factor = 0.01f;
    public float   ship_cam_rotate_duration = 1.0f;
    public float   ship_cam_move_factor     = 0.001f;
    public float   ship_cam_tilt_max        = 80.0f * MathUtils.degRad;
    public float   ship_cam_tilt_min        = MathUtils.PI2 - ship_cam_tilt_max;
    public boolean ship_cam_tilt_limit      = true;

    // *************************************************************************
    // SHIP-CTRL
    // *************************************************************************
    public float ship_ctrl_speed           = 0.0003f;
//    public float ship_ctrl_speed           = 0.00f;
    public float ship_ctrl_rotate_duration = 0.7f;

    // *************************************************************************
    // BULLET
    // *************************************************************************
    public float bullet_scale               = 0.0001f;
    public float bullet_speed_linear        = 0.002f;
    public float bullet_speed_angular       = 0.002f;
    public float bullet_altitude_limit_low  = 1.0f;
    public float bullet_altitude_limit_high = 2.0f;
    public float bullet_distance_limit      = 3.0f;

    // *************************************************************************
    // SPHERE
    // *************************************************************************
    public int sphere_detail = 5;

    // *************************************************************************
    // RENDERER
    // *************************************************************************
    public boolean renderer_shapes = true;

    // *************************************************************************
    // INPUT-MAN
    // *************************************************************************
    public int input_gesture_button = Input.Buttons.LEFT;

    // *************************************************************************
    // SHOOTER
    // *************************************************************************
    public int shooter_timeout = 100;

    // *************************************************************************
    // SKYBOX
    // *************************************************************************
    public float skybox_scale = 30.0f;

    // *************************************************************************
    // TUBE
    // *************************************************************************
    public float tube_length            = 3.0f;
    public float tube_radius            = 0.08f;
    public float[] tube_sector_angles    = new float[] {
        000.0f * MathUtils.degRad, 045.0f * MathUtils.degRad,  
        090.0f * MathUtils.degRad, 135.0f * MathUtils.degRad,
        180.0f * MathUtils.degRad, 225.0f * MathUtils.degRad, 
        270.0f * MathUtils.degRad, 315.0f * MathUtils.degRad
    };
}
