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
import com.badlogic.gdx.math.Vector3;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;

// -----------------------------------------------------------------------------
public class UtilsTween {
    // -------------------------------------------------------------------------
    public static final int TWEEN_X     = 1 << 0;
    public static final int TWEEN_Y     = 1 << 1;
    public static final int TWEEN_Z     = 1 << 2;
    public static final int TWEEN_XY    = TWEEN_X | TWEEN_Y;
    public static final int TWEEN_XZ    = TWEEN_X | TWEEN_Z;
    public static final int TWEEN_YZ    = TWEEN_Y | TWEEN_Z;
    public static final int TWEEN_XYZ   = TWEEN_X | TWEEN_XY | TWEEN_Z;

    // -------------------------------------------------------------------------
    public static class Vector3Accessor implements TweenAccessor<Vector3> {
        // *********************************************************************
        // TweenAccessor
        // *********************************************************************
        @Override
        public int getValues(Vector3 target, int tweenType, float[] returnValues) {
            int idx = 0;
            if(UtilsFlags.TestFlag(tweenType, TWEEN_X)) returnValues[idx++] = target.x;
            if(UtilsFlags.TestFlag(tweenType, TWEEN_Y)) returnValues[idx++] = target.y;
            if(UtilsFlags.TestFlag(tweenType, TWEEN_Z)) returnValues[idx++] = target.z;
            return idx;
        }

        // ---------------------------------------------------------------------
        @Override
        public void setValues(Vector3 target, int tweenType, float[] newValues) {
            int idx = 0;
            if(UtilsFlags.TestFlag(tweenType, TWEEN_X)) target.x = newValues[idx++];
            if(UtilsFlags.TestFlag(tweenType, TWEEN_Y)) target.y = newValues[idx++];
            if(UtilsFlags.TestFlag(tweenType, TWEEN_Z)) target.z = newValues[idx++];
        }
    }

    // -------------------------------------------------------------------------
    public static class FloatAccessor implements TweenAccessor<Utils.FFloat> {
        // *********************************************************************
        // TweenAccessor
        // *********************************************************************
        @Override
        public int getValues(Utils.FFloat target, int tweenType, float[] returnValues) {
            returnValues[0] = target.v;
            return 1;
        }

        // ---------------------------------------------------------------------
        @Override
        public void setValues(Utils.FFloat target, int tweenType, float[] newValues) {
            target.v = newValues[0];
        }
    }

    // -------------------------------------------------------------------------
    public static void Init() {
        Tween.registerAccessor(Vector3.class, new Vector3Accessor());
        Tween.registerAccessor(Utils.FFloat.class, new FloatAccessor());
    }
}
