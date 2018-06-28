// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import java.util.LinkedList;

import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;

// -----------------------------------------------------------------------------
public class InputMonitorGesture
  extends CommonObject
  implements GestureListener {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static final int PAN_START    = 0;
    public static final int PAN_CONTINUE = 1;
    public static final int PAN_STOP     = 2;

    // *************************************************************************
    // InputMonitorGesture
    // *************************************************************************
    private LinkedList<Interfaces.IGestureClient> m_clients;
    private int m_pan_state; 

    // -------------------------------------------------------------------------
    public InputMonitorGesture() {
        super("input-gest");
        m_clients = new LinkedList<Interfaces.IGestureClient>();
        m_pan_state = InputMonitorGesture.PAN_STOP;
    }

    // -------------------------------------------------------------------------
    public void RegisterClient(Interfaces.IGestureClient client) {
        m_clients.add(client);
    }

    // -------------------------------------------------------------------------
    private boolean HandleFling(float velocityX, float velocityY) {
        for(Interfaces.IGestureClient c : m_clients) {
            c.OnSwipe(velocityX, velocityY);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    private boolean HandleTap(float x, float y, int count) {
        for(Interfaces.IGestureClient c : m_clients) {
            c.OnTap(x, y, count);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    private boolean HandlePan(float x, float y, float dx, float dy, boolean stop) {
        if(stop) {
            // Stopping panning
            m_pan_state = InputMonitorGesture.PAN_STOP;

        } else {
            // Continuing panning
            switch(m_pan_state) {
                case InputMonitorGesture.PAN_STOP:
                    m_pan_state = InputMonitorGesture.PAN_START; break;
                case InputMonitorGesture.PAN_START:
                    m_pan_state = InputMonitorGesture.PAN_CONTINUE; break;
            }
        }

        for(Interfaces.IGestureClient c : m_clients) {
            c.OnPan(x, y, dx, dy, m_pan_state);
        }
        return true;
    }

    // *************************************************************************
    // GestureListener
    // *************************************************************************
    @Override public boolean touchDown(float x, float y, int pointer, int button) { return false; }
    @Override public boolean longPress(float x, float y) { return false; }
    @Override public boolean zoom(float initialDistance, float distance) { return false; }
    @Override public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) { return false; }

    // -------------------------------------------------------------------------
    @Override public boolean pan(float x, float y, float deltaX, float deltaY) {
        return HandlePan(x, y, deltaX, deltaY, false);
    }

    // -------------------------------------------------------------------------
    @Override public boolean panStop(float x, float y, int pointer, int button) { 
        return HandlePan(x, y, 0.0f, 0.0f, true);
    }

    // -------------------------------------------------------------------------
    @Override public boolean tap(float x, float y, int count, int button) { 
        return (button == Main.p.cfg.input_gesture_button) ? 
            HandleTap(x, y, count) : false; 
    }

    // -------------------------------------------------------------------------
    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return (button == Main.p.cfg.input_gesture_button) ? 
            HandleFling(velocityX, velocityY) : false;
    }
}
