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
