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
public class UtilsFlags {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public interface IFlagger {
        // ---------------------------------------------------------------------
        boolean FlagTest(int mask);
        void FlagSet(int mask);
        void FlagUnset(int mask);
        boolean OnFlagChange(int flag, boolean is_set);
    }

    // -------------------------------------------------------------------------
    public static boolean TestFlag(int flags, int mask) {
        return ((flags & mask) == mask);
    }

    // -------------------------------------------------------------------------
    public static int SetFlag(int flags, int mask) {
        return flags | mask;
    }

    // -------------------------------------------------------------------------
    public static int UnsetFlag(int flags, int mask) {
        return flags & ~mask;
    }

    // -------------------------------------------------------------------------
    public static int SetFlag(UtilsFlags.IFlagger flagger, int flags, int mask) {
        int new_val = UtilsFlags.SetFlag(flags, mask);
        return UtilsFlags.ValidateFlagChange(flagger, flags, new_val) ? 
            new_val : flags;
    }

    // -------------------------------------------------------------------------
    public static int UnsetFlag(UtilsFlags.IFlagger flagger, int flags, int mask) {
        int new_val = UtilsFlags.UnsetFlag(flags, mask);
        return UtilsFlags.ValidateFlagChange(flagger, flags, new_val) ? 
            new_val : flags;
    }

    // -------------------------------------------------------------------------
    private static boolean ValidateFlagChange(UtilsFlags.IFlagger flagger, 
      int f_old, int f_new) {
        boolean error = false;
        int f_mask = 1;
        while(f_old != f_new) {
            int f_old_val = f_old & 1,
                f_new_val = f_new & 1;

            // Flag was unset
            if(f_old_val == 1 && f_new_val == 0) {
                error |= !flagger.OnFlagChange(f_mask, false);

            // Flag was set
            } else if(f_old_val == 0 && f_new_val == 1) {
                error |= !flagger.OnFlagChange(f_mask, true);
            }

            // Move forward
            f_mask <<= 1;
            f_old >>= 1;
            f_new >>= 1;
        }
        return !error;
    }
}
