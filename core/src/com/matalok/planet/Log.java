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
public class Log {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static java.text.DecimalFormat m_formatter = 
        new java.text.DecimalFormat("000000.0000");

    // -------------------------------------------------------------------------
    public static void Debug(String fmt, Object... args) {
        Log.Raw("DBG", fmt, args);
    }

    // -------------------------------------------------------------------------
    public static void Info(String fmt, Object... args) {
        Log.Raw("INF", fmt, args);
    }

    // -------------------------------------------------------------------------
    public static void Err(String fmt, Object... args) {
        Log.Raw("ERR", fmt, args);
    }

    // -------------------------------------------------------------------------
    public static void Raw(String lvl, String fmt, Object... args) {
        if(Main.p != null) {
            float time = Utils.MsecToSec(Main.p.time_man.GetCur());
            System.out.printf("%s :: %s :: ", m_formatter.format(time), lvl);
        }
        System.out.println(String.format(fmt, args));
    }
}
