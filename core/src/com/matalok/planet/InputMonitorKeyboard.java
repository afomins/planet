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
import java.util.LinkedList;

import com.badlogic.gdx.InputProcessor;

// -----------------------------------------------------------------------------
public class InputMonitorKeyboard
  extends CommonObject
  implements InputProcessor {
    // *************************************************************************
    // InputMonitorKeyboard
    // *************************************************************************
    private LinkedList<Interfaces.IKeyboardClient> m_clients;

    // -------------------------------------------------------------------------
    public InputMonitorKeyboard() {
        super("input-kbd");
        m_clients = new LinkedList<Interfaces.IKeyboardClient>();
    }

    // -------------------------------------------------------------------------
    public void RegisterClient(Interfaces.IKeyboardClient client) {
        m_clients.add(client);
    }

    // -------------------------------------------------------------------------
    private boolean HandleKeyPress(int key, boolean up) {
        for(Interfaces.IKeyboardClient c : m_clients) {
            if(up) c.OnKeyUp(key);
            else   c.OnKeyDown(key, true);
        }
        return true;
    }

    // *************************************************************************
    // InputProcessor
    // *************************************************************************
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean scrolled(int amount) { return false; }

    // -------------------------------------------------------------------------
    @Override 
    public boolean keyDown(int keycode) { 
        return HandleKeyPress(keycode, false); 
    }

    // -------------------------------------------------------------------------
    @Override 
    public boolean keyUp(int keycode) { 
        return HandleKeyPress(keycode, true);
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
        }
        return true;
    }
}
