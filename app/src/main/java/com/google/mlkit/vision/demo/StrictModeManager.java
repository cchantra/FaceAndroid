package com.google.mlkit.vision.demo;


import android.os.StrictMode;

public class StrictModeManager {

    private static boolean enabled = false;

    private StrictModeManager() {}

    public static void enableStrictMode() {
        enabled = true;
        StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .penaltyDropBox();

        StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
                .penaltyDropBox()
                .penaltyDeath();

        StrictMode.setThreadPolicy(threadPolicyBuilder.build());
        StrictMode.setVmPolicy(vmPolicyBuilder.build());

    }

    public static void allowDiskReads(Runnable runnable) {
        StrictMode.ThreadPolicy oldThreadPolicy = null;
        if (enabled) {
            oldThreadPolicy = StrictMode.getThreadPolicy();
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(oldThreadPolicy).permitDiskReads().build());
        }
        runnable.run();
        if (oldThreadPolicy != null) StrictMode.setThreadPolicy(oldThreadPolicy);
    }
}