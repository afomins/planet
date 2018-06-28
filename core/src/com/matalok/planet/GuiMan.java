// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

// -----------------------------------------------------------------------------
public class GuiMan 
  extends CommonObject {
    // *************************************************************************
    // GuiMan
    // *************************************************************************
    public GuiCompass compass;
    public GuiCanvas canvas;

    private Stage m_stage;
    private Skin m_skin_custom, m_skin_main;
    private Label[] m_msg_stack;
    private int m_msg_stack_size;

    // -------------------------------------------------------------------------
    public GuiMan() {
        super("gui-man");
    }

    // -------------------------------------------------------------------------
    public void WriteMsg(String fmt, Object... args) {
        if(m_msg_stack_size >= m_msg_stack.length) {
            Log.Err("Failed to write gui message, no space in stack :: size=%d", m_msg_stack_size);
            return;
        }
        m_msg_stack[m_msg_stack_size++].setText(String.format(fmt, args));
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnCreate(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Custom skin
            m_skin_custom = new Skin();
            m_skin_custom.addRegions(new TextureAtlas("uiskin-custom.atlas"));

            // Main skin
            m_skin_main = new Skin(Gdx.files.internal("uiskin.json"));

            // 
            // Background
            //
            canvas = new GuiCanvas(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//            canvas.setDebug(true);
            canvas.left().top().setFillParent(true);

            //
            // Left table
            //
            Table left_table = new Table();
//            left_table.setDebug(true);
            left_table.left().top().setFillParent(true);
            m_msg_stack = new Label[12];
            for(int i = 0; i < m_msg_stack.length; i++) {
                m_msg_stack[i] = new Label("", m_skin_main, "default");
                left_table.add(m_msg_stack[i]).expandX().align(Align.left).row();
            }

            //
            // Right table
            //
            Table right_table = new Table();
            right_table.right().setFillParent(true);
//            right_table.setDebug(true);

            // Quit button
            float button_height = 40.0f;
            right_table.add(
                GuiButton.Create(m_skin_main, "quit", Scheduler.EVT_QUIT)).
                height(button_height).fill().row();

            // Reset button
            right_table.add(
                GuiButton.Create(m_skin_main, "reset", Scheduler.EVT_RESET_LEVEL)).
                height(button_height).fill().row();

            // Camera-lock button
            right_table.add(
                GuiButton.Create(m_skin_main, "cam-lock", "cam-unlock",
                    Scheduler.EVT_CAMERA_LOCK, Scheduler.EVT_CAMERA_UNLOCK)).
                    height(button_height).fill().row();

            // Filler
            right_table.add().expandY().row();

            // Compas
            compass = new GuiCompass(
                m_skin_custom.getDrawable("compas/background"), 
                m_skin_custom.getDrawable("compas/arrow"));
            right_table.add(compass).fill().row();

            // Stage
            m_stage = new Stage();
            m_stage.addActor(canvas);
            m_stage.addActor(left_table);
            m_stage.addActor(right_table);

            // Stage should receive input
            Main.p.input_man.RegisterClient(m_stage, 0);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnResize(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            Integer width = (Integer)args[0];
            Integer height = (Integer)args[1];
            m_stage.getViewport().update(width, height, true);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnRender(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            for(int i = m_msg_stack_size; i < m_msg_stack.length; i++) {
                m_msg_stack[i].setText("");
            }

            m_stage.act(Gdx.graphics.getDeltaTime());
            m_stage.draw();
            m_msg_stack_size = 0;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnDispose(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_POST) {
            canvas.dispose();
            m_stage.dispose();
            m_skin_main.dispose();
            m_skin_custom.dispose();
        }
        return true;
    }
}
