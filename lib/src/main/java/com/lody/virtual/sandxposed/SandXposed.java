package com.lody.virtual.sandxposed;

import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.OSUtils;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.remote.InstalledAppInfo;
import com.swift.sandhook.HookLog;
import com.swift.sandhook.PendingHookHandler;
import com.swift.sandhook.SandHookConfig;
import com.swift.sandhook.utils.ReflectionUtils;
import com.swift.sandhook.xposedcompat.XposedCompat;

import java.io.File;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import mirror.dalvik.system.VMRuntime;

import static com.swift.sandhook.xposedcompat.utils.DexMakerUtils.MD5;

public class SandXposed {

    public static void init() {
        //Build.VERSION_CODES.P
        if (Build.VERSION.SDK_INT >= 28) {
            ReflectionUtils.passApiCheck();
        }
        SandHookConfig.DEBUG = VMRuntime.isJavaDebuggable == null ? false : VMRuntime.isJavaDebuggable.call(VMRuntime.getRuntime.call());
        HookLog.DEBUG = SandHookConfig.DEBUG;
        SandHookConfig.SDK_INT = OSUtils.getInstance().isAndroidQ() ? 29 : Build.VERSION.SDK_INT;
        SandHookConfig.compiler = SandHookConfig.SDK_INT < Build.VERSION_CODES.O;
        if (PendingHookHandler.canWork()) {
            Log.e("SandHook", "Pending Hook Mode!");
        }
    }

    public static void injectXposedModule(Context context, String packageName, String processName) {
        Log.d("yich", "hook application classloader : " + context.getClassLoader()+"id:"+context.hashCode()+"xposedBridge.classloader:"+XposedBridge.class.getClassLoader());
        if (BlackList.canNotInject(packageName, processName))
            return;
        List<InstalledAppInfo> appInfos = VirtualCore.get().getInstalledApps(InstalledAppInfo.FLAG_XPOSED_MODULE | InstalledAppInfo.FLAG_ENABLED_XPOSED_MODULE);
        ClassLoader classLoader = context.getClassLoader();

        for (InstalledAppInfo module:appInfos) {
            if (TextUtils.equals(packageName, module.packageName)) {
                Log.d("injectXposedModule", "injectSelf : " + processName);
            }
            XposedCompat.loadModule(module.getApkPath(), module.getOdexFile().getParent(), VEnvironment.getAppLibDirectory(module.packageName).getAbsolutePath(), XposedBridge.class.getClassLoader());
        }
        XposedCompat.context = context;
        XposedCompat.packageName = packageName;
        XposedCompat.processName = processName;
        XposedCompat.cacheDir = new File(context.getCacheDir(), MD5(processName));
        XposedCompat.classLoader = XposedCompat.getSandHookXposedClassLoader(classLoader, XposedBridge.class.getClassLoader());
        XposedCompat.isFirstApplication = true;

        SandHookHelper.initHookPolicy();
        EnvironmentSetup.init(context, packageName, processName);

        try {
            XposedCompat.callXposedModuleInit();
        } catch (Throwable throwable) {
            Log.e("SandHook", "hook module failed:"+throwable.getMessage());
            throwable.printStackTrace();
        }
    }



}
