// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

// -----------------------------------------------------------------------------
public class Renderer extends CommonObject {
    // *************************************************************************
    // FUNCTION
    // *************************************************************************
    public static boolean InitArgs(Renderer rm, Object[] args) {
        if(rm.m_camera == null) {
            return false;
        }

        // Prepare arguments
        args[0] = rm.m_camera;
        args[1] = rm.m_renderables;
        args[2] = rm.m_shaper;
        return true;
    }

    // -------------------------------------------------------------------------
    public static Camera GetArgsCamera(Object[] args) {
        return (Camera)args[0];
    }

    // -------------------------------------------------------------------------
    public static List<RenderableProvider> GetArgsRenderables(Object[] args) {
        return (List<RenderableProvider>)args[1];
    }

    // -------------------------------------------------------------------------
    public static ShapeRenderer GetShaper(Object[] args) {
        return (ShapeRenderer)args[2];
    }

    // *************************************************************************
    // Renderer
    // *************************************************************************
    private Camera m_camera;
    private ArrayList<RenderableProvider> m_renderables;
    private ModelBatch m_model_batch;
    private Environment m_env;
    private ShapeRenderer m_shaper;

    // -------------------------------------------------------------------------
    public Renderer() {
        super("renderer");
        m_renderables = new ArrayList<RenderableProvider>(256);
    }

    // -------------------------------------------------------------------------
    public void SetCamera(Camera camera) {
        m_camera = camera;
    }

    // -------------------------------------------------------------------------
    public void ShapeAxis(ShapeRenderer shaper, Matrix4 transform, float size) {
        if(m_camera == null) {
            return;
        }

        shaper.setTransformMatrix(transform);
        shaper.begin(ShapeRenderer.ShapeType.Line);
        shaper.setColor(Color.RED);
        shaper.line(-size * 0.1f, 0.0f, 0.0f, size, 0.0f, 0.0f);
        shaper.setColor(Color.GREEN);
        shaper.line(0.0f, -size * 0.1f, 0.0f, 0.0f, size, 0.0f);
        shaper.setColor(Color.BLUE);
        shaper.line(0.0f, 0.0f, -size * 0.1f, 0.0f, 0.0f, size);
        shaper.end();
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnCreate(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Model batch for faster rendering
            m_model_batch = new ModelBatch();

            // Environment
            m_env = new Environment();
            m_env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.0f));
            m_env.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
            m_env.add(new DirectionalLight().set(1.0f, 0.7f, 0.2f, 1f, 0.8f, 0.2f));

            // Shaper
            m_shaper = new ShapeRenderer();
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            m_renderables.clear();
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnRender(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            m_model_batch.begin(m_camera.GetGdxCamera());
            m_model_batch.render(m_renderables, m_env);
            m_model_batch.end();

        } else if(stage == CommonObject.STAGE_POST) {
            m_shaper.setProjectionMatrix(m_camera.GetGdxCamera().combined);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnDispose(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_POST) {
            m_shaper.dispose();
            m_model_batch.dispose();
        }
        return true;
    }
}
