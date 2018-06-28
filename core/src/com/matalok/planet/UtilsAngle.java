// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class UtilsAngle {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static final Vector3 VECT_FORW = Vector3.Z;
    public static final Vector3 VECT_UP   = Vector3.Y;
    public static final Vector3 VECT_LEFT = Vector3.X;

    // *************************************************************************
    // FUNCTION
    // *************************************************************************
    public static Vector3 SetYpr(Vector3 v, float yaw, float pitch, float roll) {
        return v.set(pitch, yaw, roll);
    }

    // -------------------------------------------------------------------------
    public static Vector3 SetYp(Vector3 v, float yaw, float pitch) {
        UtilsAngle.SetRotYaw(v, yaw);
        UtilsAngle.SetRotPitch(v, pitch);
        return v;
    }

    // -------------------------------------------------------------------------
    public static Vector3 SetYpFromDir(Vector3 v, Vector3 dir) {
        UtilsAngle.SetRotYaw(v, MathUtils.PI / 2 - MathUtils.atan2(dir.z, dir.x));
        UtilsAngle.SetRotPitch(v, -(float)Math.asin(dir.y));
        return v;
    }

    // -------------------------------------------------------------------------
    public static Quaternion SetRotQuaternion(Quaternion q, Vector3 v) {
        return q.setEulerAnglesRad(
            UtilsAngle.GetRotYaw(v), 
            UtilsAngle.GetRotPitch(v), 
            UtilsAngle.GetRotRoll(v)); 
    }

    // -------------------------------------------------------------------------
    public static Quaternion SetRotQuaternion(Quaternion q, float y, float p, float r) {
        return q.setEulerAnglesRad(y, p, r);
    }

    // -------------------------------------------------------------------------
    public static float GetRotYaw(Vector3 v) { return v.y; }
    public static float GetRotPitch(Vector3 v) { return v.x; }
    public static float GetRotRoll(Vector3 v) { return v.z; }
    public static float SetRotYaw(Vector3 v, float val) { v.y = val; return v.y; };
    public static float SetRotPitch(Vector3 v, float val) { v.x = val; return v.x; };
    public static float SetRotRoll(Vector3 v, float val) { v.z = val; return v.z; };

    // -------------------------------------------------------------------------
    public static Matrix4 SetRotYaw(Matrix4 m, float angle) { return m.rotateRad(UtilsAngle.VECT_UP, angle); }     // Y
    public static Matrix4 SetRotPitch(Matrix4 m, float angle) { return m.rotateRad(UtilsAngle.VECT_LEFT, angle); } // X
    public static Matrix4 SetRotRoll(Matrix4 m, float angle) { return m.rotateRad(UtilsAngle.VECT_FORW, angle); }  // Z

    // -------------------------------------------------------------------------
    public static float GetPolarH(Vector3 v) { return v.x; }
    public static float GetPolarV(Vector3 v) { return v.y; }
    public static float GetPolarR(Vector3 v) { return v.z; }
    public static float SetPolarH(Vector3 v, float val) { v.x = val; return v.x; }
    public static float SetPolarV(Vector3 v, float val) { v.y = val; return v.y; }
    public static float SetPolarR(Vector3 v, float val) { v.z = val; return v.z; }
    public static float AddPolarH(Vector3 v, float val) { v.x += val; return v.x; }
    public static float AddPolarV(Vector3 v, float val) { v.y += val; return v.y; }
    public static float AddPolarR(Vector3 v, float val) { v.z += val; return v.z; }

    // -------------------------------------------------------------------------
    private static Vector3 rotate_vertical_axis = new Vector3(Vector3.X);
    private static Vector3 rotate_horizontal_axis = new Vector3(Vector3.Y);
    public static void Rotate(Vector3 dest, Vector3 angle) {
        dest.rotateRad(rotate_vertical_axis, angle.y);      // Vertical
        dest.rotateRad(rotate_horizontal_axis, angle.x);    // Horizontal
    }

    // -------------------------------------------------------------------------
    public static float Clamp(float angle, float min, float max, float threshold) {
        if(angle > max) {
            angle = max - threshold;
        } else if(angle < min) {
            angle = min + threshold;
        }
        return angle;
    }

    // -------------------------------------------------------------------------
    public static float Normalize(float angle) {
        if(angle > 0.0f) {
            angle %= MathUtils.PI2;
        } else if(angle < 0.0f) {
            angle %= -MathUtils.PI2;
            angle = MathUtils.PI2 + angle;
        }
        return angle;
    }

    // -------------------------------------------------------------------------
    private static Vector2 normalized_angles = new Vector2();
    public static float GetNormFrom(Vector2 v) { return v.x; }
    public static float GetNormTo(Vector2 v) { return v.y; }

    // -------------------------------------------------------------------------
    public static Vector2 NormalizeDiff(float from, float to) {
        // Normalize angles to make them in [0; 2*pi] range
        from = UtilsAngle.Normalize(from);
        to = UtilsAngle.Normalize(to);

        // Adjust angles to make shortest path
        float diff = to - from;
        if(diff > MathUtils.PI) {
            to -= MathUtils.PI2;
        } else if(diff < -MathUtils.PI) {
            to += MathUtils.PI2;
        }
        return normalized_angles.set(from, to);
    }

    // -------------------------------------------------------------------------
    public static void NormalizeDiff(Vector2 from, Vector2 to) {
        // Normalize X
        Vector2 v = UtilsAngle.NormalizeDiff(from.x, to.x);
        from.x = v.x; to.x = v.y;

        // Normalize Y
        v = UtilsAngle.NormalizeDiff(from.y, to.y);
        from.y = v.x; to.y = v.y;
    }

    // -------------------------------------------------------------------------
    public static void NormalizeDiff(Vector3 from, Vector3 to) {
        // Normalize X
        Vector2 v = UtilsAngle.NormalizeDiff(from.x, to.x);
        from.x = v.x; to.x = v.y;

        // Normalize Y
        v = UtilsAngle.NormalizeDiff(from.y, to.y);
        from.y = v.x; to.y = v.y;

        // Normalize Z
        v = UtilsAngle.NormalizeDiff(from.z, to.z);
        from.z = v.x; to.z = v.y;
    }
}
