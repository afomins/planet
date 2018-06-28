// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;

// -----------------------------------------------------------------------------
public class AssMan 
  extends CommonObject {
    // *************************************************************************
    // AssMan
    // *************************************************************************
    private AssetManager m_ass_man;

    // -------------------------------------------------------------------------
    public AssMan() {
        super("ass-man");
    }

    // -------------------------------------------------------------------------
    public Model GetModel(String name) {
        return (Model)m_ass_man.get(name);
    }

    // -------------------------------------------------------------------------
    public Pixmap GetPixmap(String name) {
        return new Pixmap(Gdx.files.internal(name));
    }

    // -------------------------------------------------------------------------
    public TextureAtlas GetTextureAtlas(String name) {
        return (TextureAtlas)m_ass_man.get(name);
    }

    // -------------------------------------------------------------------------
    public FileHandle GetFile(String name) {
        return Gdx.files.internal(name);
    }

    // -------------------------------------------------------------------------
    private synchronized <T> void Load(Class<T> type, String[] names) {
        if(names == null) {
            return;
        }
        for(String name : names) {
            m_ass_man.load(name, type);
        }
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    @Override public boolean OnCreate(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_PRE) {
            m_ass_man = new AssetManager();
            Load(Model.class, Main.p.cfg.ass_man.get("model"));
            Load(TextureAtlas.class, Main.p.cfg.ass_man.get("ta"));

            while(!m_ass_man.update(424242)) {
                Log.Debug("Still loading assets...");
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnDispose(int stage, Object[] args) {
        if(stage == CommonObject.STAGE_POST) {
            m_ass_man.dispose();
        }
        return true;
    }
}
