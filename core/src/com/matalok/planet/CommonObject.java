// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import java.util.LinkedList;
import java.util.ListIterator;
import com.afomins.planet.Interfaces.ICommonObject;

// -----------------------------------------------------------------------------
public class CommonObject implements Interfaces.ICommonObject {
    // *************************************************************************
    // FLAGS
    // *************************************************************************
    public static int F_EXPIRED    = 1 << 0;
    public static int F_NO_RENDER  = 1 << 1;

    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static int id = 0;

    // -------------------------------------------------------------------------
    public static final int STAGE_PRE         = 0;
    public static final int STAGE_POST        = 1;
    public static final String[] STAGE_NAMES  = new String[] {
        "pre ", "post"
    };

    public static final int PRIO_NONE         = -1;
    public static final int PRIO_LAST         = 999;

    public static final int MTH_CREATE        = 0;
    public static final int MTH_RESET         = 1;
    public static final int MTH_RESIZE        = 2;
    public static final int MTH_PREPARE       = 3;
    public static final int MTH_RENDER        = 4;
    public static final int MTH_SHAPE         = 5;
    public static final int MTH_PAUSE         = 6;
    public static final int MTH_RESUME        = 7;
    public static final int MTH_DISPOSE       = 8;
    public static final int MTH_NUM           = 9;
    public static final String[] MTH_NAMES    = new String[] {
        "create", "reset", "resize", "prepare", "render", "shape", 
        "pause", "resume", "dispose"
    };

    // *************************************************************************
    // FUNCTION
    // *************************************************************************
    public static boolean RunMethod(Interfaces.ICommonObject obj, int method_id, 
      int stage, Object[] args) {
        if(method_id != CommonObject.MTH_PREPARE && 
           method_id != CommonObject.MTH_RENDER &&
           method_id != CommonObject.MTH_SHAPE) {
            Log.Debug("Running method :: mth=%s(%s) obj=%s", 
                CommonObject.MTH_NAMES[method_id], 
                CommonObject.STAGE_NAMES[stage], obj.GetNameId());
        }

        boolean rc = false;
        switch(method_id) {
            case CommonObject.MTH_CREATE:   rc = obj.OnCreate(stage, args);     break;
            case CommonObject.MTH_RESET:    rc = obj.OnReset(stage, args);      break;
            case CommonObject.MTH_RESIZE:   rc = obj.OnResize(stage, args);     break;
            case CommonObject.MTH_PREPARE:  rc = obj.OnPrepare(stage, args);    break;
            case CommonObject.MTH_RENDER:   rc = obj.OnRender(stage, args);     break;
            case CommonObject.MTH_SHAPE:    rc = obj.OnShape(stage, args);      break;
            case CommonObject.MTH_PAUSE:    rc = obj.OnPause(stage, args);      break;
            case CommonObject.MTH_RESUME:   rc = obj.OnResume(stage, args);     break;
            case CommonObject.MTH_DISPOSE:  rc = obj.OnDispose(stage, args);    break;
            default:
                Utils.Assert(false, 
                    "Failed to run method, unknown method id :: obj=%s id=%d", 
                    obj.GetNameId(), method_id);
        }
        return rc;
    }

    // -------------------------------------------------------------------------
    public static void Walk(Interfaces.ICommonObject root, boolean forward, 
      int method_id, Object[] args) {
        // Run own method before children
        if(!CommonObject.RunMethod(root, method_id, CommonObject.STAGE_PRE, args)) {
            return;
        }

        // Walk children
        ListIterator<Interfaces.ICommonObject> it = root.GetChildrenIt(forward);
        for( ;; ) {
            // Get next element
            Interfaces.ICommonObject obj = null;
            if(forward && it.hasNext()) {
                obj = it.next();
            } else if (!forward && it.hasPrevious()) {
                obj = it.previous();
            }

            // Test EOL 
            if(obj == null) break;

            // Walk children hierarchy
            CommonObject.Walk(obj, forward, method_id, args);
        }

        // Run own method after children
        CommonObject.RunMethod(root, method_id, CommonObject.STAGE_POST, args);

        // Delete expired children when render is over
        CommonObject root_co = (CommonObject)root;
        if(method_id == CommonObject.MTH_RENDER && root_co.m_expire_num > 0) {
            Log.Debug("Deleting expired children :: name=%s expire_num=%d", 
                root.GetNameId(), root_co.m_expire_num);

            it = root.GetChildrenIt(true);
            while(it.hasNext()) {
                Interfaces.ICommonObject obj = it.next();
                if(!obj.IsExpired()) {
                    continue;
                }

                CommonObject.Walk(obj, true, CommonObject.MTH_DISPOSE, null);
                it.remove();
                root_co.m_expire_num--;
                Log.Debug("Deleting expired child :: name=%s", obj.GetNameId());
            }
            Utils.Assert(root_co.m_expire_num == 0, 
                "Failed to deleted all expired children :: name=%s expired=%d", 
                root.GetNameId(), root_co.m_expire_num);
        }
    }

    // *************************************************************************
    // CommonObject
    // *************************************************************************
    private int m_flags_co;
    private int m_id, m_priority, m_expire_num;
    private String m_name, m_name_id;
    private LinkedList<Interfaces.ICommonObject> m_children;
    private CommonObject m_parent;

    // -------------------------------------------------------------------------
    public CommonObject(String name) {
        this(name, CommonObject.PRIO_LAST);
    }

    // -------------------------------------------------------------------------
    public CommonObject(String name, int priority) {
        m_id = id++;
        m_name = name;
        m_priority = priority;
        m_name_id = String.format("%s-%d", name, m_id);

        // List of children
        m_children = new LinkedList<Interfaces.ICommonObject>();
    }

    // -------------------------------------------------------------------------
    public boolean HasChild(Interfaces.ICommonObject obj) {
        for(Interfaces.ICommonObject c : m_children) {
            if(c.IsSame(obj)) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    public CommonObject AddChild(CommonObject obj) {
        Utils.Assert(!HasChild(obj), 
            "Failed to add child, child already present :: l=%s r=%s",
            GetNameId(), obj.GetNameId());

        int priority = obj.GetPriority();

        // Append to the end
        if(priority >= CommonObject.PRIO_LAST || 
          m_children.size() == 0 || 
          m_children.getLast().GetPriority() <= priority) {
            m_children.add(obj);

        // Add to the middle
        } else {
            ListIterator<Interfaces.ICommonObject> it = m_children.listIterator();
            while(it.hasNext()) {
                Interfaces.ICommonObject c = it.next();
                if(priority <= c.GetPriority()) {
                    it.previous();
                    it.add(obj);
                    return obj;
                }
            }
            Utils.Assert(false, "Failed to add child :: l=%s", obj.GetNameId());
        }

        obj.m_parent = this;
        return obj;
    }

    // *************************************************************************
    // Interfaces.ICommonObject
    // *************************************************************************
    @Override public boolean OnCreate(int stage, Object[] args) { return true; }
    @Override public boolean OnReset(int stage, Object[] args) { return true; }
    @Override public boolean OnResize(int stage, Object[] args) { return true; }
    @Override public boolean OnPrepare(int stage, Object[] args) { return true; }
    @Override public boolean OnRender(int stage, Object[] args) { return true; }
    @Override public boolean OnShape(int stage, Object[] args) { return true; }
    @Override public boolean OnPause(int stage, Object[] args) { return true; }
    @Override public boolean OnResume(int stage, Object[] args) { return true; }
    @Override public boolean OnDispose(int stage, Object[] args) { return true; }

    // -------------------------------------------------------------------------
    @Override public boolean IsSame(ICommonObject obj) {
        if(obj.GetId() == GetId()) {
            Utils.Assert(
                obj.equals(this), "Different objects share same id :: l=%s(%s) r=%s(%s)",
                GetNameId(), toString(), obj.GetNameId(), obj.toString());
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    @Override public int GetPriority() { 
        return m_priority; 
    }

    // -------------------------------------------------------------------------
    @Override public int GetId() { 
        return m_id; 
    }

    // -------------------------------------------------------------------------
    @Override public String GetName() { 
        return m_name; 
    }

    // -------------------------------------------------------------------------
    @Override public String GetNameId() { 
        return m_name_id; 
    }

    // -------------------------------------------------------------------------
    @Override public ListIterator<Interfaces.ICommonObject> GetChildrenIt(boolean from_beginning) {
        return m_children.listIterator(
            (from_beginning) ? 0 : m_children.size());
    }

    // -------------------------------------------------------------------------
    @Override public int GetChildNum() {
        return m_children.size();
    }

    // -------------------------------------------------------------------------
    @Override public void Expire() {
        m_flags_co = UtilsFlags.SetFlag(m_flags_co, CommonObject.F_EXPIRED);
        if(m_parent != null) {
            m_parent.m_expire_num++;
        }
        Log.Debug("Expiring object :: name=%s", GetNameId());
    }

    // -------------------------------------------------------------------------
    @Override public boolean IsExpired() {
        return UtilsFlags.TestFlag(m_flags_co, CommonObject.F_EXPIRED);
    }
}
