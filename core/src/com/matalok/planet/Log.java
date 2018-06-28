// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
public class Log {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    private static java.text.DecimalFormat m_formatter = 
        new java.text.DecimalFormat("000000.0000");

    // -------------------------------------------------------------------------
    public static void Debug(String fmt, Object... args) {
        Log.Raw("DBG", fmt, args);
    }

    // -------------------------------------------------------------------------
    public static void Info(String fmt, Object... args) {
        Log.Raw("INF", fmt, args);
    }

    // -------------------------------------------------------------------------
    public static void Err(String fmt, Object... args) {
        Log.Raw("ERR", fmt, args);
    }

    // -------------------------------------------------------------------------
    public static void Raw(String lvl, String fmt, Object... args) {
        if(Main.p != null) {
            float time = Utils.MsecToSec(Main.p.time_man.GetCur());
            System.out.printf("%s :: %s :: ", m_formatter.format(time), lvl);
        }
        System.out.println(String.format(fmt, args));
    }
}
