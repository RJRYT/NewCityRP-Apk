package com.wardrumstudios.utils;

import android.view.ViewParent;

public class WarGamepad extends WarBilling {
    public float GetGamepadAxis(int i, int i2) {
        System.out.println("**** GetGamepadAxis()");
        return 0.0f;
    }

    public int GetGamepadButtons(int i) {
        System.out.println("**** GetGamepadButtons()");
        return 0;
    }

    public int GetGamepadTrack(int i, int i2, int i3) {
        System.out.println("**** GetGamepadTrack()");
        return 0;
    }

    public int GetGamepadType(int i) {
        return -1;
    }

    public native boolean processTouchpadAsPointer(ViewParent viewParent, boolean z);
}