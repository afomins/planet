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
import java.util.Arrays;
import java.util.Comparator;

// -----------------------------------------------------------------------------
public class PathUtils {
    // -------------------------------------------------------------------------
    public static class GenericSegment {
        // ---------------------------------------------------------------------
        protected float offset;
        protected float len;

        // ---------------------------------------------------------------------
        public float UpdateOffset(float offset) {
            this.offset = offset;
            return offset + len;
        }

        // ---------------------------------------------------------------------
        public float GetEnd() {
            return offset + len;
        }
    }

    // -------------------------------------------------------------------------
    public static class SegmentComparator implements Comparator<PathUtils.GenericSegment> {
        // ---------------------------------------------------------------------
        @Override
        public int compare(PathUtils.GenericSegment el, PathUtils.GenericSegment key) {
            return key.offset < el.offset          ? +42 :
                   key.offset > el.offset + el.len ? -42 : 0;
        }
    }

    // -------------------------------------------------------------------------
    private static SegmentComparator seg_comparator = new SegmentComparator();
    private static GenericSegment seg_search_key = new GenericSegment();
    public static int SearchSegmentArrayIdx(PathUtils.GenericSegment[] array, 
      int lo_idx, int hi_idx, float offset) {
        // Do binary search
        seg_search_key.offset = offset;
        int idx = Arrays.binarySearch(array, lo_idx, hi_idx, seg_search_key, seg_comparator);
        if(idx < 0) {
            Log.Err("Failed to search segment array, nothing found :: lo=%d hi=%d offset=%.2f", 
              lo_idx, hi_idx, offset);
            return -42;
        }

        // Sanity check
        Utils.Assert(idx <= hi_idx,
            "Failed to search segment array, wrong index :: lo=%d hi=%d offset=%.2f",
            lo_idx, hi_idx, offset);
        return idx;
    }

    // -------------------------------------------------------------------------
    public static GenericSegment SearchSegmentArray(PathUtils.GenericSegment[] array, 
      int lo_idx, int hi_idx, float offset) {
        int idx = PathUtils.SearchSegmentArrayIdx(array, lo_idx, hi_idx, offset);
        return idx < 0 ? null : array[idx];
    }
}
