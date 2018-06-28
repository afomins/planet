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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Disposable;

// -----------------------------------------------------------------------------
public class GuiCanvas
  extends Container<Image> 
  implements Disposable {
    // *************************************************************************
    // GuiCanvas
    // *************************************************************************
    public Pixmap pm;
    private Vector2 m_cursor;
    private Texture m_tex;

    // -------------------------------------------------------------------------
    public GuiCanvas(int width, int height) {
        // Pixmap
        pm = new Pixmap(width, height, Pixmap.Format.RGBA4444);

        // Texture
        m_tex = new Texture(pm);

        // Widget
        Image arrow_img = new Image(m_tex);
        setActor(arrow_img);

        // Reset texture
        pm.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        Flush();

        // Cursor
        m_cursor = new Vector2();
    }

    // -------------------------------------------------------------------------
    public void Clear() {
        pm.setColor(1.0f, 1.0f, 1.0f, 0.0f);
    }

    // -------------------------------------------------------------------------
    public void Flush() {
        m_tex.draw(pm, 0, 0);
    }

    // -------------------------------------------------------------------------
    public Vector2 CursorSet(float x, float y) {
        return m_cursor.set(x, y);
    }

    // -------------------------------------------------------------------------
    public Vector2 CursorGrow(float dx, float dy) {
        return m_cursor.add(dx, dy);
    }

    // *************************************************************************
    // GuiCanvas
    // *************************************************************************
    @Override public void dispose() {
        m_tex.dispose();
        pm.dispose();
    }
}
