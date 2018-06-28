// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

// -----------------------------------------------------------------------------
public class Main 
  extends CommonObject
  implements ApplicationListener, Interfaces.ISchedulerClient {
    // *************************************************************************
    // Main
    // *************************************************************************
    public static Main p;

    // -------------------------------------------------------------------------
    public Config cfg;
    public TimeMan time_man;
    public InputMan input_man;
    public Scheduler scheduler;
    public GuiMan gui;
    public AssMan ass_man;
    public Engine engine;
    public Renderer renderer;

    // -------------------------------------------------------------------------
    public Main() {
        super("main");
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnCreate(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Initial config
            cfg = new Config();

            // Time manager
            time_man = (TimeMan)AddChild(new TimeMan());

            // Input manager
            input_man = (InputMan)AddChild(new InputMan());

            // Main singleton
            Main.p = this;
            Log.Info("Starting planet...");

            // Tween engine
            UtilsTween.Init();

            // Scheduler
            scheduler = (Scheduler)AddChild(new Scheduler());

            // Asset manager
            ass_man = (AssMan)AddChild(new AssMan());

            // Game engine
            engine = (Engine)AddChild(new Engine());

            // Render manager
            renderer = (Renderer)AddChild(new Renderer());

            // Gui manager
            gui = (GuiMan)AddChild(new GuiMan());

        } else if(stage == CommonObject.STAGE_POST) {
            // Load first level
            scheduler.RegisterClient(this);
            scheduler.ScheduleEvent(Scheduler.EVT_LOAD_LEVEL, null, 0, true);
        }
        return true;
    }

    // *************************************************************************
    // ApplicationListener
    // *************************************************************************
    @Override public void create() { 
        CommonObject.Walk(this, true, CommonObject.MTH_CREATE, null);
    }

    // -------------------------------------------------------------------------
    private static Object[] resize_args = new Object[2];
    @Override public void resize(int width, int height) { 
        resize_args[0] = new Integer(width);
        resize_args[1] = new Integer(height);
        CommonObject.Walk(this, true, CommonObject.MTH_RESIZE, resize_args); 
    }

    // -------------------------------------------------------------------------
    private static Object[] render_args = new Object[3];
    @Override public void render() {
        // Prepare frame
        CommonObject.Walk(this, true, CommonObject.MTH_PREPARE, null);

        // Render frame
        if(Renderer.InitArgs(renderer, render_args)) {
            // Render scene
            CommonObject.Walk(this, true, CommonObject.MTH_RENDER, render_args);

            // Render shapes
            if(cfg.renderer_shapes) {
                CommonObject.Walk(this, true, CommonObject.MTH_SHAPE, render_args);
            }
        }
    }

    // -------------------------------------------------------------------------
    @Override public void dispose() { 
        CommonObject.Walk(this, false, CommonObject.MTH_DISPOSE, null);
    }

    // -------------------------------------------------------------------------
    @Override public void pause() { 
        CommonObject.Walk(this, false, CommonObject.MTH_PAUSE, null);
    }

    // -------------------------------------------------------------------------
    @Override public void resume() { 
        CommonObject.Walk(this, true, CommonObject.MTH_RESUME, null);
    }

    // *************************************************************************
    // Interfaces.ISchedulerClient
    // *************************************************************************
    @Override public void OnEvent(Interfaces.IEvent event) {
        switch(event.GetEventId()) {
            // -----------------------------------------------------------------
            case Scheduler.EVT_QUIT:
                Gdx.app.exit();
            break;

            // -----------------------------------------------------------------
            case Scheduler.EVT_RESET_LEVEL:
            case Scheduler.EVT_LOAD_LEVEL:
                // Reset game state
                CommonObject.Walk(
                    this, true, CommonObject.MTH_RESET, null);

                // Load new level
                engine.Start(
                    "earth_height_map.png", 
                    "earth_surface_map.png", 
                    "earth_surface.gpl", 0.0005f);
            break;
        }
    }
}
