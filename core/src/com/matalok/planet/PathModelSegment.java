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
import com.matalok.planet.Interfaces.ICamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

// -----------------------------------------------------------------------------
public class PathModelSegment 
  extends DynamicModel {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static int inst_num = 0;
    private static Material material = new Material(
        ColorAttribute.createDiffuse(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.3f), 
        new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));

    // *************************************************************************
    // DynamicModel
    // *************************************************************************
    public int id;

    // -------------------------------------------------------------------------
    public PathModelSegment() {
        id = PathModelSegment.inst_num++;
    }

    // -------------------------------------------------------------------------
    @Override protected void PrepareModel() {
    }

    // -------------------------------------------------------------------------
    @Override protected void CreateModel(ModelBuilder mb, Mesh mesh) {
        mb.part(new MeshPart(
                    "path-segment-" + id,
                    mesh, 0, m_vertex_array.length / 3,
                    GL20.GL_LINE_STRIP), PathModelSegment.material);
    }

    // -------------------------------------------------------------------------
    @Override protected void FinalizeModel(ModelInstance inst) {
    }

    // -------------------------------------------------------------------------
    @Override protected void CleanupModel() {
    }

    // -------------------------------------------------------------------------
    @Override protected void LogVertexArray() {
    }

    // -------------------------------------------------------------------------
    @Override protected void LogIndexArray() {
    }

    // *************************************************************************
    // Interfaces.ISmartModel
    // *************************************************************************
    @Override public RenderableProvider TestModelVisibility(ICamera camera) {
        return m_model_inst;
    }
}
