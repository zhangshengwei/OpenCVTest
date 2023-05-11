package com.rokid.opencvtest.nativetest;

public class NativeManager {
    static {
        System.loadLibrary("zq");
    }

    public native int add(int args, int args2);

    // 缺少 JNI 无法还无法直接使用


}
