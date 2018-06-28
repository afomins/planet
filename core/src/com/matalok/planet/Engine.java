// -----------------------------------------------------------------------------
package com.matalok.planet;

//-----------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class Engine 
  extends CommonObject 
  implements Interfaces.ISchedulerClient {
    // *************************************************************************
    // Engine
    // *************************************************************************
    public Path path;
    public Tube tube;
    public Ship ship;
    public ShipCtrl ship_ctrl;
    public ShipCamera ship_camera;
    public Sphere sphere;
    public Shooter shooter;
    public Skybox skybox;

    // -------------------------------------------------------------------------
    public Engine() {
        super("engine");
    }

    // -------------------------------------------------------------------------
    public void Start(String height_map, String surface_map, 
      String surface_palette, float max_height) {
        //
        // Sphere
        //

        // Sphere textures
        Pixmap hm = Main.p.ass_man.GetPixmap(height_map),
               sm = Main.p.ass_man.GetPixmap(surface_map);

        // Update height & surface
        sphere.ReadHeightmap(hm, max_height);
        sphere.ReadSuface(sm, 
            new Palette(Main.p.ass_man.GetFile(surface_palette)));

        // Dispose sphere textures
        hm.dispose();
        sm.dispose();

        //
        // Path
        //

        // Create path
        float h = 1.2f, 
              h_max = 0.1f,
              lat = 15.0f;
        path.AddVertexDeg(0.0f, lat, h);
        path.AddVertexDeg(45.0f, lat, h);
        path.AddVertexDeg(90.0f, lat, h);

        for(int i = 0; i < 20; i++) {
            path.Grow(
                MathUtils.random(25.0f, 45.0f) * MathUtils.degRad, 
                45 * MathUtils.degRad, 
                h + MathUtils.random(h_max));
        }

        path.MakeLoop();
        path.Log();
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnCreate(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            // Ship controller
            ship_ctrl = (ShipCtrl)AddChild(new ShipCtrl());

            // Skybox
            skybox = (Skybox)AddChild(new Skybox());

            // Ship
            ship = (Ship)AddChild(new Ship());

            // Camera
            ship_camera = (ShipCamera)AddChild(new ShipCamera());

            // Sphere
            sphere = (Sphere)AddChild(new Sphere());

            // Path
            path = (Path)AddChild(new Path());
            path.FlagSet(Path.F_INTERPOLATE_YPR);

            // Tube
            tube = (Tube)AddChild(new Tube());

            // Shooter
            shooter = (Shooter)AddChild(new Shooter());

        } else if(stage == CommonObject.STAGE_POST) {
            //
            // Ship controller
            //
            ship_ctrl.RegisterClient(ship_camera);
            ship_ctrl.RegisterClient(ship);
            Main.p.input_man.gest.RegisterClient(ship_ctrl);
            Main.p.input_man.kbd.RegisterClient(ship_ctrl);

            //
            // Skybox
            //
            skybox.AddLayer(Main.p.ass_man.GetTextureAtlas(
                Main.p.cfg.asset_ta_skybox), Main.p.cfg.skybox_scale);
//            skybox.Log();

            //
            // Ship
            //
            ship.SetModel(Main.p.ass_man.GetModel(Main.p.cfg.asset_model_ship));

            //
            // Sphere
            //
            sphere.Create(Main.p.cfg.sphere_detail);
            sphere.Finalize();

            //
            // Camera
            //
            ship_camera.FlagSet(ShipCamera.F_TARGET_MANDATORY | 
                ShipCamera.F_TARGET_LOCKED | ShipCamera.F_RELATIVE_ROTATION);
            Main.p.input_man.mouse.RegisterClient(ship_camera);
            Main.p.input_man.kbd.RegisterClient(ship_camera);

            // Position
            Vector3 cam_pos = new Vector3(Main.p.cfg.camera_pos)
                .scl(MathUtils.degRad, MathUtils.degRad, 1.0f);
            Vector3 cam_off = new Vector3(Main.p.cfg.camera_offset)
                .scl(MathUtils.degRad, MathUtils.degRad, 1.0f);
            Interfaces.IRigidBody cam_target = 
                (Main.p.cfg.camera_target == 0) ? ship_ctrl : sphere;

            ship_camera.SetTarget(cam_target);
            ship_camera.SetPosOffset(cam_pos, cam_off);

            //
            // Shooter
            //
            shooter.SetBullet(Main.p.ass_man.GetModel(Main.p.cfg.asset_model_bullet));

            //
            // Engine
            //
            Main.p.scheduler.RegisterClient(this);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnPrepare(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            Main.p.renderer.SetCamera(ship_camera);
        }
        return true;
    }

    // *************************************************************************
    // Interfaces.ISchedulerClient
    // *************************************************************************
    @Override public void OnEvent(Interfaces.IEvent event) {
        switch(event.GetEventId()) {
            // -----------------------------------------------------------------
            case Scheduler.EVT_CAMERA_UNLOCK:
                ship_camera.FlagSet(ShipCamera.F_TARGET_LOCKED);
            break;

            // -----------------------------------------------------------------
            case Scheduler.EVT_CAMERA_LOCK:
                ship_camera.FlagUnset(ShipCamera.F_TARGET_LOCKED);
            break;
        }
    }
}
