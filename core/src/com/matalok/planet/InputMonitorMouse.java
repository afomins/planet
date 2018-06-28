// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import java.util.LinkedList;

import com.badlogic.gdx.InputProcessor;

// -----------------------------------------------------------------------------
public class InputMonitorMouse
  extends CommonObject
  implements InputProcessor {
    // *************************************************************************
    // InputMonitorMouse
    // *************************************************************************
    private int m_prev_x, m_prev_y;
    private int m_active_button;
    private LinkedList<Interfaces.IMouseClient> m_clients;

    // -------------------------------------------------------------------------
    public InputMonitorMouse() {
        super("input-mouse");
        m_clients = new LinkedList<Interfaces.IMouseClient>();
        m_active_button = Main.p.cfg.input_gesture_button;
    }

    // -------------------------------------------------------------------------
    public void RegisterClient(Interfaces.IMouseClient client) {
        m_clients.add(client);
    }

    // -------------------------------------------------------------------------
    private void SavePrevPos(int x, int y) {
        m_prev_x = x;
        m_prev_y = y;
    }

    // -------------------------------------------------------------------------
    private boolean HandleScroll(int size) {
        for(Interfaces.IMouseClient c : m_clients) {
            c.OnScroll(size);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    private boolean HandleMove(int x, int y) {
        if(m_active_button == Main.p.cfg.input_gesture_button) {
            return false;
        }

        int dx = x - m_prev_x, 
            dy = y - m_prev_y;

        for(Interfaces.IMouseClient c : m_clients) {
            c.OnMove(dx, dy, m_active_button);
        }
        SavePrevPos(x, y);
        return true;
    }

    // -------------------------------------------------------------------------
    private boolean HandleTouchDown(int x, int y, int button) {
        if(button == Main.p.cfg.input_gesture_button) {
            return false;
        }
        SavePrevPos(x, y);
        m_active_button = button;
        return true;
    }

    // -------------------------------------------------------------------------
    private boolean HandleTouchUp(int x, int y, int button) {
        boolean rc = HandleMove(x, y);
        m_active_button = Main.p.cfg.input_gesture_button;
        return rc;
    }

    // *************************************************************************
    // InputProcessor
    // *************************************************************************
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }

    // -------------------------------------------------------------------------
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return HandleTouchDown(screenX, screenY, button);
    }

    // -------------------------------------------------------------------------
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return HandleTouchUp(screenX, screenY, button);
    }

    // -------------------------------------------------------------------------
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return HandleMove(screenX, screenY);
    }

    // -------------------------------------------------------------------------
    @Override
    public boolean scrolled(int amount) {
        return HandleScroll(amount);
    }
}
