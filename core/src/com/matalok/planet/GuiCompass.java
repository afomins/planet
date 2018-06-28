// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;

// -----------------------------------------------------------------------------
public class GuiCompass
  extends Container<Image> {
    // *************************************************************************
    // GuiCompas
    // *************************************************************************
    public GuiCompass(Drawable background, Drawable arrow) {
        Image arrow_img = new Image(arrow, Scaling.none);
        arrow_img.setOrigin(
            arrow_img.getWidth() / 2.0f, 
            arrow_img.getHeight() / 2.0f);

        setBackground(background);
        setActor(arrow_img);
        setTransform(true);
    }

    // -------------------------------------------------------------------------
    public void Rotate(float angle) {
        getActor().setRotation(angle * MathUtils.radDeg);
    }
}
