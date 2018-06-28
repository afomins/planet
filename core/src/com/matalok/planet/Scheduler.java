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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

// -----------------------------------------------------------------------------
public class Scheduler 
  extends CommonObject {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static final int EVT_QUIT                = 0;
    public static final int EVT_RESET_LEVEL         = 1;
    public static final int EVT_LOAD_LEVEL          = 2;
    public static final int EVT_CAMERA_UNLOCK       = 3;
    public static final int EVT_CAMERA_LOCK         = 4;
    public static final int EVT_NUM                 = 5;
    private static final String[] EVT_NAMES = new String[] {
        "quit", "reset_level", "load_level", "cam_unlock", "cam_lock"};

    // *************************************************************************
    // SUBCLASS
    // *************************************************************************
    public class Event
      implements Interfaces.IEvent {
        // *********************************************************************
        // Event
        // *********************************************************************
        public int id;
        public String arg;
        public long fire_time;

        // *********************************************************************
        // Interfaces.IEvent
        // *********************************************************************
        @Override public int GetEventId() {
            return id;
        }
    }

    // *************************************************************************
    // Scheduler
    // *************************************************************************
    private LinkedList<Event> m_event_queue, m_event_queue_copy;
    private Hashtable<Integer, LinkedList<Interfaces.ISchedulerClient>> m_clients;

    // -------------------------------------------------------------------------
    public Scheduler() {
        super("scheduler");
        m_event_queue = new LinkedList<Scheduler.Event>();
        m_event_queue_copy = new LinkedList<Scheduler.Event>();
        m_clients = new Hashtable<Integer, LinkedList<Interfaces.ISchedulerClient>>();
    }

    // -------------------------------------------------------------------------
    public void ScheduleEvent(int event_id, String arg, long timeout, Boolean relative_time) {
        // Create new event entry
        Event event = new Event();
        event.id = event_id;
        event.arg = arg;
        event.fire_time = timeout + ((relative_time) ? Main.p.time_man.GetCur() : 0);

        // Add new event so that list is sorted in time-acceding order
        ListIterator<Scheduler.Event> it = m_event_queue.listIterator();
        while(it.hasNext()) {
            Scheduler.Event e = it.next();
            if(event.fire_time < e.fire_time) {
                it.previous();
                break;
            }
        }

        Log.Debug("Scheduling event :: event=%s args=%s fire_time=%d queue_size=%d", 
            Scheduler.EVT_NAMES[event_id], arg, event.fire_time, m_event_queue.size());
        it.add(event);
    }

    // -------------------------------------------------------------------------
    public void RegisterClient(Interfaces.ISchedulerClient client) {
        for(int i = 0; i < Scheduler.EVT_NUM; i++) {
            RegisterClient(i, client);
        }
    }

    // -------------------------------------------------------------------------
    public void RegisterClient(int event_id, Interfaces.ISchedulerClient client) {
        if(!m_clients.containsKey(event_id)) {
            m_clients.put(event_id, new LinkedList<Interfaces.ISchedulerClient>());
        }

        m_clients.get(event_id).add(client);
        Log.Debug("Registering scheduler client :: name=%s event=%s", 
            client.GetNameId(), Scheduler.EVT_NAMES[event_id]);
    }

    // -------------------------------------------------------------------------
    private void FireEvent(Event event) {
        if(!m_clients.containsKey(event.id)) {
            return;
        }

        ListIterator<Interfaces.ISchedulerClient> it = m_clients.get(event.id).listIterator();
        while(it.hasNext()) {
            Interfaces.ISchedulerClient client = it.next();
            Log.Debug("Firing event :: client=%s event=%s args=%s trigger_time=%d(%d) queue_size=%d", 
                    client.GetNameId(), Scheduler.EVT_NAMES[event.id], 
                    event.arg, event.fire_time, Main.p.time_man.GetCur(), m_event_queue_copy.size());

            client.OnEvent(event);
        }
    }

    // -------------------------------------------------------------------------
    private void SwapEventQueues() {
        LinkedList<Event> tmp = m_event_queue;
        m_event_queue = m_event_queue_copy;
        m_event_queue_copy = tmp;
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Swap 'event_queue_copy' <--- 'm_event_queue'
            SwapEventQueues();

            // Handle events from 'event_queue_copy'
            long cur_time = Main.p.time_man.GetCur();
            while(m_event_queue_copy.size() > 0 && 
                  m_event_queue_copy.getFirst().fire_time < cur_time) {

                Event evt = m_event_queue_copy.remove();
                FireEvent(evt);
            }

            // Swap back 'event_queue_copy' ---> 'm_event_queue'
            SwapEventQueues();

            // Reschedule new events  
            if(m_event_queue_copy.size() > 0) {
                Log.Debug("Rescheduling new events :: num=%d", m_event_queue_copy.size());
                for(Event evt: m_event_queue_copy) {
                    ScheduleEvent(evt.id, evt.arg, evt.fire_time, false);
                }
            }

            // Cleanup temporary array
            m_event_queue_copy.clear();
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnReset(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            m_event_queue.clear();
            m_event_queue_copy.clear();
        }
        return true;
    }
}
