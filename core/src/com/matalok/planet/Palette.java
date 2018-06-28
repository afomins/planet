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
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;

// -----------------------------------------------------------------------------
public class Palette {
    // -------------------------------------------------------------------------
    public static final String DATA_SECTION = "#";
    public static final int BUFF_SIZE = 64;

    // -------------------------------------------------------------------------
    private List<Color> m_list;
    private HashMap<Integer, Integer> m_map;

    // -------------------------------------------------------------------------
    public Palette(FileHandle file) {
        Log.Debug("Reading GIMP palette :: file=%s", file.path());

        // Save index color in list
        m_list = new ArrayList<Color>();

        // Map RGBA value to palette index
        m_map = new HashMap<Integer, Integer>();

        // Go...
        try {
            boolean is_data_section = false;
            BufferedReader reader = file.reader(Palette.BUFF_SIZE);
            for(;;) {
                // Read file line by line
                String line = reader.readLine();
                if(line == null) {
                    break;
                }

                // Search for data section
                if(!is_data_section) {
                    if(line.equals(Palette.DATA_SECTION)) {
                        is_data_section = true;
                    }
                    continue;
                }

                // Split string to RGB tokens
                String[] rgb = line.trim().split("\\s+");
                Utils.Assert(rgb.length == 4, 
                    "Failed to read palette file, wrong format :: len=%d line=%s", rgb.length, line);

                // Read color components
                int r = Integer.parseInt(rgb[0]);
                int g = Integer.parseInt(rgb[1]);
                int b = Integer.parseInt(rgb[2]);

                // Get RGBA key
                int rgba_key = r << 24 | g << 16 | b << 8 | 255;
                Log.Debug("  > r=%03d g=%03d b=%03d rgba=0x%08X comment=%s", 
                    r, g, b, rgba_key, rgb[3]);

                // Save palette
                m_map.put(rgba_key, m_list.size());
                m_list.add(new Color(r / 255.0f, g / 255.0f, b / 255.0f, 1.0f));
            }

        // Damn...
        } catch(Exception ex) {
            Utils.Assert(false, "Failed to read palette file :: ex=%s", ex.toString());
        }
    }

    // -------------------------------------------------------------------------
    public int GetColorIdx(int rgba) {
        return (!m_map.containsKey(rgba)) ? -1 : m_map.get(rgba);
    }
}
