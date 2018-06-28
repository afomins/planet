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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class Utils {
    // *************************************************************************
    // CLASS
    // *************************************************************************
    public static class FFloat {
        // ---------------------------------------------------------------------
        public float v;

        // ---------------------------------------------------------------------
        public FFloat(float value) {
            this.v = value;
        }
    }

    // -------------------------------------------------------------------------
    public static class Direction {
        // ---------------------------------------------------------------------
        public float vect_len;  // Length of direction vector
        public Vector3 dir;     // Direction as a unit vector
        public Vector3 ypr;     // Direction as a yaw-pitch-roll tuple

        // ---------------------------------------------------------------------
        public Direction() {
            dir = new Vector3();
            ypr = new Vector3();
        }

        // ---------------------------------------------------------------------
        public Direction Rebuild(float x, float y, float z) {
            // Direction
            dir.x = x; dir.y = y; dir.z = z;

            // Length
            vect_len = dir.len();

            // Normalize direction
            dir.nor();

            // Yaw-pitch rotation
            UtilsAngle.SetYpFromDir(ypr, dir);
            return this;
        }
    }

    // -------------------------------------------------------------------------
    public static class DirectionEx extends Utils.Direction {
        // *********************************************************************
        // STATIC
        // *********************************************************************
        public static final int F_VECT = 1 << 0;
        public static final int F_YPR  = 1 << 1;
        public static final int F_POS  = 1 << 2;
        public static final int F_ROT  = 1 << 3;
        public static final int F_ALL  = F_VECT | F_YPR | F_POS | F_ROT;

        // *********************************************************************
        // DirectionEx
        // *********************************************************************
        public Vector3 pos;
        public Quaternion rot;

        // ---------------------------------------------------------------------
        public DirectionEx() {
            super();
            pos = new Vector3();
            rot = new Quaternion();
        }
    }

    // *************************************************************************
    // FUNCTION
    // *************************************************************************
    public static void Assert(Boolean statement, String fmt, Object... args) {
        if(statement) {
            return;
        }

        Log.Err(fmt, args);
        Integer a = 42, b = 0, c = a / b;
        Log.Err("Wow, I'm still alive!!! o_O c=%d", c);
    }

    // -------------------------------------------------------------------------
    public static float StrToFloat(String str) {
        return Float.parseFloat(str);
    }

    // -------------------------------------------------------------------------
    public static float MsecToSec(long msec) {
        return msec / 1000.0f;
    }

    // -------------------------------------------------------------------------
    private static Vector3 normal = new Vector3();
    private static Vector3 normal_tmp = new Vector3();
    public static Vector3 GetTriangleNormal(Vector3 v0, Vector3 v1, Vector3 v2) {
        normal.set(v1.x - v0.x, v1.y - v0.y, v1.z - v0.z);
        normal_tmp.set(v2.x - v0.x, v2.y - v0.y, v2.z - v0.z);
        return normal.crs(normal_tmp).nor();
    }

    // -------------------------------------------------------------------------
    private static Vector3 middle = new Vector3();
    public static Vector3 GetMiddle(Vector3 v0, Vector3 v1) {
        return middle.set(
            (v0.x + v1.x) / 2.0f, 
            (v0.y + v1.y) / 2.0f, 
            (v0.z + v1.z) / 2.0f);
    }

    // -------------------------------------------------------------------------
    public static Vector3 GetMiddle(Vector3 v0, Vector3 v1, Vector3 v2) {
        return middle.set(
            (v0.x + v1.x + v2.x) / 3.0f, 
            (v0.y + v1.y + v2.y) / 3.0f, 
            (v0.z + v1.z + v2.z) / 3.0f);
    }

    // -------------------------------------------------------------------------
    private static Vector3 diff3 = new Vector3();
    public static Vector3 GetDiff(Vector3 from, Vector3 to) {
        return diff3.set(to).sub(from);
    }

    // -------------------------------------------------------------------------
    private static Vector2 diff2 = new Vector2();
    public static Vector2 GetDiff(Vector2 from, Vector2 to) {
        return diff2.set(to).sub(from);
    }
}
