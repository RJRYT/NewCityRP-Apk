package com.nvidia.devtech;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.PopupWindow;

public class HeightProvider extends PopupWindow implements OnGlobalLayoutListener {
    private int heightMaxHorizontal;
    private int heightMaxVertical;
    private HeightListener listener;
    private Activity mActivity;
    private View rootView;

    public interface HeightListener {
        void onHeightChanged(int i, int i2);
    }

    class a implements Runnable {
        final /* synthetic */ View b;

        a(View view) {
            this.b = view;
        }

        public void run() {
            HeightProvider.this.showAtLocation(this.b, 0, 0, 0);
        }
    }

    public HeightProvider(Activity activity) {
        super(activity);
        this.mActivity = activity;
        View view = new View(activity);
        this.rootView = view;
        setContentView(view);
        this.rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        setBackgroundDrawable(new ColorDrawable(0));
        setWidth(0);
        setHeight(-1);
        setSoftInputMode(16);
        setInputMethodMode(1);
    }

    public HeightProvider init(View view) {
        if (!isShowing()) {
            view.post(new a(view));
        }
        return this;
    }

    public void onGlobalLayout() {
        Rect rect = new Rect();
        this.rootView.getWindowVisibleDisplayFrame(rect);
        int i = rect.bottom;
        if (i > rect.right) {
            if (i > this.heightMaxVertical) {
                this.heightMaxVertical = i;
            }
            i = this.heightMaxVertical;
        } else {
            if (i > this.heightMaxHorizontal) {
                this.heightMaxHorizontal = i;
            }
            i = this.heightMaxHorizontal;
        }
        i -= rect.bottom;
        HeightListener heightListener = this.listener;
        if (heightListener != null) {
            heightListener.onHeightChanged(this.mActivity.getResources().getConfiguration().orientation, i);
        }
    }

    public HeightProvider setHeightListener(HeightListener heightListener) {
        this.listener = heightListener;
        return this;
    }
}