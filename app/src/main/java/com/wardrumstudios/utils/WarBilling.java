package com.wardrumstudios.utils;

import java.io.PrintStream;

public class WarBilling extends WarBase {
    public void AddSKU(String str) {
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**** AddSKU: ");
        stringBuilder.append(str);
        printStream.println(stringBuilder.toString());
    }

    public boolean InitBilling() {
        System.out.println("**** InitBilling()");
        return true;
    }

    public String LocalizedPrice(String str) {
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**** LocalizedPrice: ");
        stringBuilder.append(str);
        printStream.println(stringBuilder.toString());
        return "";
    }

    public boolean RequestPurchase(String str) {
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**** RequestPurchase: ");
        stringBuilder.append(str);
        printStream.println(stringBuilder.toString());
        return true;
    }

    public void SetBillingKey(String str) {
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**** SetBillingKey: ");
        stringBuilder.append(str);
        printStream.println(stringBuilder.toString());
    }

    public native void changeConnection(boolean z);

    public native void notifyChange(String str, int i);
}