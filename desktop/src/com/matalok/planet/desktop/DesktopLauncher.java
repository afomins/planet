// -----------------------------------------------------------------------------
package com.matalok.planet.desktop;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.matalok.planet.Main;

// -----------------------------------------------------------------------------
public class DesktopLauncher {
    // -------------------------------------------------------------------------
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "planet";
//        config.width = 800;
//        config.height = 600;
        config.width = 480;
        config.height = 800;
        new LwjglApplication(new Main(), config);
    }
}
