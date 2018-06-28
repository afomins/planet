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
import java.util.ListIterator;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class Interfaces {
    // -------------------------------------------------------------------------
    public interface ICommonObject {
        // Generic callback methods
        public boolean OnCreate(int stage, Object[] args);
        public boolean OnReset(int stage, Object[] args);
        public boolean OnResize(int stage, Object[] args);
        public boolean OnPrepare(int stage, Object[] args);
        public boolean OnRender(int stage, Object[] args);
        public boolean OnShape(int stage, Object[] args);
        public boolean OnPause(int stage, Object[] args);
        public boolean OnResume(int stage, Object[] args);
        public boolean OnDispose(int stage, Object[] args);

        // Utility methods
        public boolean IsSame(ICommonObject obj);
        public int GetPriority();
        public int GetId();
        public String GetName();
        public String GetNameId();
        public ListIterator<Interfaces.ICommonObject> GetChildrenIt(boolean from_beginning);
        public int GetChildNum();

        // Cleanup methods
        public void Expire();
        public boolean IsExpired();
    }

    // -------------------------------------------------------------------------
    public interface ICamera {
        public void UpdateCamera();
        public boolean TestBackfaceCulling(Vector3 norm, Vector3 pos, float threshold);
    }

    // -------------------------------------------------------------------------
    public interface IRigidBody {
        Vector3 GetPos();
        Quaternion GetRot();
    }

    // -------------------------------------------------------------------------
    public interface IEvent {
        public int GetEventId();
    }

    // -------------------------------------------------------------------------
    public interface ISchedulerClient extends Interfaces.ICommonObject {
        public void OnEvent(Interfaces.IEvent event);
    }

    // -------------------------------------------------------------------------
    public interface IShipCtrlClient {
        void OnRotate(float angle);
        void OnShoot(int x, int y);
    }

    // -------------------------------------------------------------------------
    public interface IGestureClient {
        void OnSwipe(float x, float y);
        void OnTap(float x, float y, int count);
        void OnPan(float x, float y, float dx, float dy, int state);
    }

    // -------------------------------------------------------------------------
    public interface IMouseClient {
        void OnScroll(int size);
        void OnMove(float x, float y, int button);
    }

    // -------------------------------------------------------------------------
    public interface IKeyboardClient {
        void OnKeyUp(int key);
        void OnKeyDown(int key, boolean first_time);
    }

    // -------------------------------------------------------------------------
    public interface ISmartModel {
        RenderableProvider TestModelVisibility(ICamera camera);
    }
}
