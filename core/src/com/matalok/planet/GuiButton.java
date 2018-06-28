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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

// -----------------------------------------------------------------------------
public class GuiButton 
  extends CommonObject
  implements Interfaces.ISchedulerClient {
    // *************************************************************************
    // SUBCLASS
    // *************************************************************************
    private class ClickListenerWrapper 
      extends ClickListener { 
        // *********************************************************************
        // ClickListenerWrapper
        // *********************************************************************
        public Boolean is_toggle, do_scheduling;
        public int[] evt_ids;
        public String[] names;

        // ---------------------------------------------------------------------
        @Override 
        public void clicked(InputEvent event, float x, float y) {
            int idx = (!is_toggle || m_button.isChecked()) ? 0 : 1;

            // Schedule event
            if(do_scheduling) {
                Main.p.scheduler.ScheduleEvent(evt_ids[idx], null, 0, true);
            }

            // Change button text
            if(is_toggle) {
                m_button.setText(names[idx]);
            }
        }
    }

    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static Actor Create(Skin skin, String name, int evt_id) {
        GuiButton b = new GuiButton(skin, new String[]{ name }, new int[]{ evt_id });
        return b.m_button;
    }

    // -------------------------------------------------------------------------
    public static Actor Create(Skin skin, String unpress_name, String press_name, 
      int unpress_evt_id, int press_evt_id) {
        GuiButton b = new GuiButton(skin, 
            new String[] { unpress_name, press_name }, 
            new int[] { unpress_evt_id, press_evt_id });
        return b.m_button;
    }

    // *************************************************************************
    // GuiButton
    // *************************************************************************
    private TextButton m_button;
    private ClickListenerWrapper m_clc_listener;

    // -------------------------------------------------------------------------
    private GuiButton(Skin skin, String[] names, int[] evt_ids) {
        super("gui-button", CommonObject.PRIO_LAST);
        m_clc_listener = new ClickListenerWrapper();

        m_clc_listener.names = names;
        m_clc_listener.evt_ids = evt_ids;
        m_clc_listener.is_toggle = (evt_ids.length > 1);

        m_button = new TextButton(names[m_clc_listener.is_toggle ? 1 : 0], skin, 
            m_clc_listener.is_toggle ? "toggle" : "default");
        m_button.setName("b_" + names[m_clc_listener.is_toggle ? 1 : 0]);
        m_button.addListener(m_clc_listener);
        m_clc_listener.do_scheduling = true;
    }

    // -------------------------------------------------------------------------
    private int GetEventIdx(int event_id) {
        for(int i = 0; i < m_clc_listener.evt_ids.length; i++) {
            if(m_clc_listener.evt_ids[i] == event_id) {
                return i;
            }
        }
        return -1;
    }

    // *************************************************************************
    // Interfaces.ISchedulerClient
    // *************************************************************************
    @Override public boolean OnCreate(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            if(m_clc_listener.is_toggle) {
                Main.p.scheduler.RegisterClient(m_clc_listener.evt_ids[0], this);
                Main.p.scheduler.RegisterClient(m_clc_listener.evt_ids[1], this);
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public void OnEvent(Interfaces.IEvent event) {
        // Validate event
        int expected_event_idx = GetEventIdx(event.GetEventId());
        Utils.Assert(expected_event_idx != -1, 
            "GuiButton received invalid event :: evt_id=%d", event.GetEventId());

        // Simulate click
        int current_event_idx = (!m_clc_listener.is_toggle || m_button.isChecked()) ? 0 : 1;
        if(current_event_idx != expected_event_idx) {
            m_clc_listener.do_scheduling = false;
            m_button.toggle();
            m_clc_listener.clicked(null, 0, 0);
            m_clc_listener.do_scheduling = true;
        }
    }
}
