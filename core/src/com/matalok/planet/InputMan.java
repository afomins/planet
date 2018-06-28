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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;

// -----------------------------------------------------------------------------
public class InputMan 
  extends CommonObject {
    // *************************************************************************
    // InputMan
    // *************************************************************************
    public InputMonitorMouse mouse;
    public InputMonitorGesture gest;
    public InputMonitorKeyboard kbd;

    private InputMultiplexer m_mplx;

    // -------------------------------------------------------------------------
    public InputMan() {
        super("input-man");
        m_mplx = new InputMultiplexer();
        Gdx.input.setInputProcessor(m_mplx);
    }

    // -------------------------------------------------------------------------
    public void RegisterClient(InputProcessor ip, int idx) {
        m_mplx.addProcessor(idx, ip);
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnCreate(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Mouse monitor
            mouse = (InputMonitorMouse)AddChild(new InputMonitorMouse());
            m_mplx.addProcessor(mouse);

            // Gesture monitor
            gest = (InputMonitorGesture)AddChild(new InputMonitorGesture());
            m_mplx.addProcessor(new GestureDetector(gest));

            // Keyboard monitor
            kbd = (InputMonitorKeyboard)AddChild(new InputMonitorKeyboard());
            m_mplx.addProcessor(kbd);
        }
        return true;
    }
}
