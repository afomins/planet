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
