package com.nvidia.devtech;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.nvidia.devtech.HeightProvider.HeightListener;
import com.nvidia.devtech.InputManager.InputListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL11;

import androidx.appcompat.app.AppCompatActivity;
import com.newcityrp.launcher.R;



public abstract class NvEventQueueActivity extends AppCompatActivity implements SensorEventListener, InputListener, OnTouchListener, HeightListener {
    private static final int EGL_CONTEXT_CLIENT_VERSION = 12440;
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int EGL_OPENGL_ES3_BIT = 64;
    private static final int EGL_RENDERABLE_TYPE = 12352;
    private boolean GameIsFocused = false;
    private boolean HasGLExtensions = false;
    protected boolean ResumeEventDone = false;
    private int SwapBufferSkip = 0;
    private boolean Use2Touches = true;
    protected int alphaSize = 0;
    private AssetManager assetMgr = null;
    private boolean bInitDone = false;
    protected int blueSize = 5;
    protected SurfaceHolder cachedSurfaceHolder = null;
    protected int[] configAttrs = null;
    protected int[] contextAttrs = null;
    protected int depthSize = 16;
    protected Display display = null;
    EGL10 egl = null;
    protected EGLConfig eglConfig = null;
    protected EGLContext eglContext = null;
    protected EGLDisplay eglDisplay = null;
    protected EGLSurface eglSurface = null;
    private int fixedHeight = 0;
    private int fixedWidth = 0;
    GL11 gl = null;
    private String glExtensions = null;
    private String glRenderer = null;
    private String glVendor = null;
    private String glVersion = null;
    protected int greenSize = 6;
    protected Handler handler = null;
    protected ClipboardManager mClipboardManager = null;
    private HeightProvider mHeightProvider = null;
    private InputManager mInputManager = null;
    private FrameLayout mRootFrame = null;
    protected int mSensorDelay = 1;
    protected SensorManager mSensorManager = null;
    private SurfaceView mSurfaceView = null;
    private int mUseFullscreen = 0;
    protected boolean paused = false;
    private boolean ranInit = false;
    protected int redSize = 5;
    protected int stencilSize = 0;
    private int surfaceHeight = 0;
    private int surfaceWidth = 0;
    private boolean viewIsActive = false;
    public boolean waitForPermissions = false;
    protected boolean wantsAccelerometer = false;
    protected boolean wantsMultitouch = false;

    public class RawData {
        public byte[] data;
        public int length;
    }

    public class RawTexture extends RawData {
        public int height;
        public int width;

        public RawTexture() {
            super();
        }
    }

    class a implements OnSystemUiVisibilityChangeListener {
        a() {
        }

        public void onSystemUiVisibilityChange(int i) {
            if ((i & NvEventQueueActivity.EGL_OPENGL_ES2_BIT) == 0) {
                NvEventQueueActivity.this.hideSystemUI();
            }
        }
    }

    class c implements Callback {
        final /* synthetic */ NvEventQueueActivity b;

        c(NvEventQueueActivity nvEventQueueActivity) {
            this.b = nvEventQueueActivity;
        }

        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Surface changed: ");
            stringBuilder.append(i2);
            stringBuilder.append(", ");
            stringBuilder.append(i3);
            printStream.println(stringBuilder.toString());
            NvEventQueueActivity.this.surfaceWidth = i2;
            NvEventQueueActivity.this.surfaceHeight = i3;
            NvEventQueueActivity nvEventQueueActivity = NvEventQueueActivity.this;
            nvEventQueueActivity.setWindowSize(nvEventQueueActivity.surfaceWidth, NvEventQueueActivity.this.surfaceHeight);
        }

        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            System.out.println("systemInit.surfaceCreated");
            Object obj = NvEventQueueActivity.this.cachedSurfaceHolder == null ? 1 : null;
            NvEventQueueActivity nvEventQueueActivity = NvEventQueueActivity.this;
            nvEventQueueActivity.cachedSurfaceHolder = surfaceHolder;
            if (!(nvEventQueueActivity.fixedWidth == 0 || NvEventQueueActivity.this.fixedHeight == 0)) {
                System.out.println("Setting fixed window size");
                surfaceHolder.setFixedSize(NvEventQueueActivity.this.fixedWidth, NvEventQueueActivity.this.fixedHeight);
            }
            NvEventQueueActivity.this.ranInit = true;
            if (!(NvEventQueueActivity.this.bInitDone || NvEventQueueActivity.this.init(true))) {
                NvEventQueueActivity.this.handler.post(new Runnable()
                                 {
                                     public void run()
                                     {
                                         new AlertDialog.Builder(NvEventQueueActivity.this)
                                                 .setMessage("Application initialization failed. The application will exit.")
                                                 .setPositiveButton("Ok",
                                                         new DialogInterface.OnClickListener ()
                                                         {
                                                             public void onClick(DialogInterface i, int a)
                                                             {
                                                                 finish();
                                                             }
                                                         }
                                                 )
                                                 .setCancelable(false)
                                                 .show();
                                     }
                                 });
            }
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("surfaceCreated: w:");
            stringBuilder.append(NvEventQueueActivity.this.surfaceWidth);
            stringBuilder.append(", h:");
            stringBuilder.append(NvEventQueueActivity.this.surfaceHeight);
            printStream.println(stringBuilder.toString());
            if (obj == null && NvEventQueueActivity.this.ResumeEventDone) {
                System.out.println("entering resumeEvent");
                NvEventQueueActivity.this.resumeEvent();
                System.out.println("returned from resumeEvent");
            }
            NvEventQueueActivity nvEventQueueActivity2 = NvEventQueueActivity.this;
            nvEventQueueActivity2.setWindowSize(nvEventQueueActivity2.surfaceWidth, NvEventQueueActivity.this.surfaceHeight);
        }

        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            System.out.println("systemInit.surfaceDestroyed");
            NvEventQueueActivity.this.viewIsActive = false;
            NvEventQueueActivity.this.pauseEvent();
            NvEventQueueActivity.this.destroyEGLSurface();
        }
    }

    class d implements Runnable {
        final /* synthetic */ int b;

        d(int i) {
            this.b = i;
        }

        public void run() {
            String str = this.b == 1 ? "com.jekmant.perfectlauncher" : "none";
            if (this.b == 2) {
                str = "com.jekmant.matlauncher";
            }
            Intent launchIntentForPackage = NvEventQueueActivity.this.getPackageManager().getLaunchIntentForPackage(str);
            if (launchIntentForPackage != null) {
                launchIntentForPackage.putExtra("minimize", true);
                NvEventQueueActivity nvEventQueueActivity = NvEventQueueActivity.this;
                if (nvEventQueueActivity.ResumeEventDone) {
                    nvEventQueueActivity.pauseEvent();
                }
                System.out.println("Calling launcher activity");
                NvEventQueueActivity.this.startActivity(launchIntentForPackage);
                System.out.println("Called launcher activity");
            }
        }
    }

    private native void onInputEnd(byte[] bArr);

    private void processCutout() {
        if (VERSION.SDK_INT >= 28 && this.mUseFullscreen == 1) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = 1;
        }
    }

    public void DoResumeEvent()
    {
        new Thread(new Runnable() {
            public void run() {
                while (NvEventQueueActivity.this.cachedSurfaceHolder == null)
                {
                    NvEventQueueActivity.this.mSleep(1000);
                }
                System.out.println("Call from DoResumeEvent");
                NvEventQueueActivity.this.resumeEvent();
                NvEventQueueActivity.this.ResumeEventDone = true;
            }
        }).start();
    }

    public void GetGLExtensions() {
        if (!this.HasGLExtensions) {
            GL11 gl11 = this.gl;
            if (gl11 != null && this.cachedSurfaceHolder != null) {
                this.glVendor = gl11.glGetString(7936);
                this.glExtensions = this.gl.glGetString(7939);
                this.glRenderer = this.gl.glGetString(7937);
                this.glVersion = this.gl.glGetString(7938);
                PrintStream printStream = System.out;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Vendor: ");
                stringBuilder.append(this.glVendor);
                printStream.println(stringBuilder.toString());
                printStream = System.out;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Extensions ");
                stringBuilder.append(this.glExtensions);
                printStream.println(stringBuilder.toString());
                printStream = System.out;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Renderer: ");
                stringBuilder.append(this.glRenderer);
                printStream.println(stringBuilder.toString());
                printStream = System.out;
                stringBuilder = new StringBuilder();
                stringBuilder.append("GIVersion: ");
                stringBuilder.append(this.glVersion);
                printStream.println(stringBuilder.toString());
                if (this.glVendor != null) {
                    this.HasGLExtensions = true;
                }
            }
        }
    }

    public SurfaceView GetSurfaceView() {
        return this.mSurfaceView;
    }

    public boolean InitEGLAndGLES2(int i) {
        PrintStream printStream;
        String str;
        System.out.println("lnitEGLAndGLES2");
        if (this.cachedSurfaceHolder == null) {
            printStream = System.out;
            str = "InitEGLAndGLES2 failed, cachedSurfaceHoIder is null";
        } else {
            if (this.eglContext == null ? initEGL() : true) {
                System.out.println("Should we create a surface?");
                if (!this.viewIsActive) {
                    System.out.println("Yes! Calling create surface");
                    createEGLSurface(this.cachedSurfaceHolder);
                    System.out.println("Done creating surface");
                }
                this.viewIsActive = true;
                this.SwapBufferSkip = 1;
                return true;
            }
            printStream = System.out;
            str = "initEGlAndGLES2 failed, core EGL init failure";
        }
        printStream.println(str);
        return false;
    }

    public void OnInputEnd(String str) {
        byte[] bytes;
        try {
            bytes = str.getBytes("windows-1251");
        } catch (UnsupportedEncodingException unused) {
            bytes = null;
        }
        onInputEnd(bytes);
    }

    public native boolean accelerometerEvent(float f, float f2, float f3);

    public void callLauncherActivity(int i) {
        runOnUiThread(new d(i));
    }

    public native void cleanup();

    protected void cleanupEGL() {
        System.out.println("cleanupEGL");
        destroyEGLSurface();
        EGLDisplay eGLDisplay = this.eglDisplay;
        if (eGLDisplay != null) {
            EGL10 egl10 = this.egl;
            EGLSurface eGLSurface = EGL10.EGL_NO_SURFACE;
            egl10.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, EGL10.EGL_NO_CONTEXT);
        }
        EGLContext eGLContext = this.eglContext;
        if (eGLContext != null) {
            this.egl.eglDestroyContext(this.eglDisplay, eGLContext);
        }
        eGLDisplay = this.eglDisplay;
        if (eGLDisplay != null) {
            this.egl.eglTerminate(eGLDisplay);
        }
        this.eglDisplay = null;
        this.eglContext = null;
        this.eglSurface = null;
        this.ranInit = false;
        this.eglConfig = null;
        this.cachedSurfaceHolder = null;
        this.surfaceWidth = 0;
        this.surfaceHeight = 0;
    }

    protected boolean createEGLSurface(SurfaceHolder surfaceHolder) {
        this.eglSurface = this.egl.eglCreateWindowSurface(this.eglDisplay, this.eglConfig, surfaceHolder, null);
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("eglSurface: ");
        stringBuilder.append(this.eglSurface);
        stringBuilder.append(", err: ");
        stringBuilder.append(this.egl.eglGetError());
        printStream.println(stringBuilder.toString());
        int[] iArr = new int[1];
        this.egl.eglQuerySurface(this.eglDisplay, this.eglSurface, 12375, iArr);
        this.surfaceWidth = iArr[0];
        this.egl.eglQuerySurface(this.eglDisplay, this.eglSurface, 12374, iArr);
        this.surfaceHeight = iArr[0];
        System.out.println("checking glVendor == null?");
        if (this.glVendor == null) {
            System.out.println("Making current and back");
            makeCurrent();
            unMakeCurrent();
        }
        System.out.println("Done create EGL surface");
        return true;
    }

    protected void destroyEGLSurface() {
        System.out.println("*** destroyEGLSurface");
        EGLDisplay eGLDisplay = this.eglDisplay;
        if (!(eGLDisplay == null || this.eglSurface == null)) {
            EGL10 egl10 = this.egl;
            EGLSurface eGLSurface = EGL10.EGL_NO_SURFACE;
            egl10.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, EGL10.EGL_NO_CONTEXT);
        }
        EGLSurface eGLSurface2 = this.eglSurface;
        if (eGLSurface2 != null) {
            this.egl.eglDestroySurface(this.eglDisplay, eGLSurface2);
        }
        this.eglSurface = null;
    }

    public byte[] getClipboardText()
    {
        String retn = " ";

        if(mClipboardManager.getPrimaryClip() != null)
        {
            ClipData.Item item = mClipboardManager.getPrimaryClip().getItemAt(0);
            if(item != null)
            {
                CharSequence sequence = item.getText();
                if(sequence != null)
                {
                    retn = sequence.toString();
                }
            }
        }

        byte[] toReturn = null;
        try
        {
            toReturn = retn.getBytes("windows-1251");
        }
        catch(UnsupportedEncodingException e)
        {

        }

        return toReturn;
    }

    public int getOrientation() {
        return this.display.getOrientation();
    }

    public boolean getSupportPauseResume() {
        return true;
    }

    public int getSurfaceHeight() {
        return this.surfaceHeight;
    }

    public int getSurfaceWidth() {
        return this.surfaceWidth;
    }

    public void hideInputLayout()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInputManager.HideInputLayout();
            }
        });
    }

    public void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(7942);
    }

    public native void imeClosed();

    public native boolean init(boolean z);

    protected boolean initEGL() {
        int i = 1;
        if (this.configAttrs == null) {
            this.configAttrs = new int[]{12344};
        }
        int[] iArr = this.configAttrs;
        this.configAttrs = new int[((iArr.length + 3) - 1)];
        int i2 = 0;
        while (i2 < iArr.length - 1) {
            this.configAttrs[i2] = iArr[i2];
            i2++;
        }
        int[] iArr2 = this.configAttrs;
        int i3 = i2 + 1;
        iArr2[i2] = EGL_RENDERABLE_TYPE;
        i2 = i3 + 1;
        iArr2[i3] = EGL_OPENGL_ES2_BIT;
        iArr2[i2] = 12344;
        this.contextAttrs = new int[]{EGL_CONTEXT_CLIENT_VERSION, 2, 12344};
        if (iArr2 == null) {
            this.configAttrs = new int[]{12344};
        }
        int[] iArr3 = this.configAttrs;
        this.configAttrs = new int[((iArr3.length + 13) - 1)];
        int i4 = 0;
        while (i4 < iArr3.length - 1) {
            this.configAttrs[i4] = iArr3[i4];
            i4++;
        }
        iArr3 = this.configAttrs;
        int i5 = i4 + 1;
        i3 = 12324;
        iArr3[i4] = 12324;
        i4 = i5 + 1;
        iArr3[i5] = this.redSize;
        i5 = i4 + 1;
        int i6 = 12323;
        iArr3[i4] = 12323;
        i4 = i5 + 1;
        iArr3[i5] = this.greenSize;
        i5 = i4 + 1;
        iArr3[i4] = 12322;
        i4 = i5 + 1;
        iArr3[i5] = this.blueSize;
        i5 = i4 + 1;
        iArr3[i4] = 12321;
        i4 = i5 + 1;
        iArr3[i5] = this.alphaSize;
        i5 = i4 + 1;
        iArr3[i4] = 12326;
        i4 = i5 + 1;
        iArr3[i5] = this.stencilSize;
        i5 = i4 + 1;
        iArr3[i4] = 12325;
        i4 = i5 + 1;
        iArr3[i5] = this.depthSize;
        iArr3[i4] = 12344;
        EGL10 egl10 = (EGL10) EGLContext.getEGL();
        this.egl = egl10;
        egl10.eglGetError();
        this.eglDisplay = this.egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("eglDisplay: ");
        stringBuilder.append(this.eglDisplay);
        stringBuilder.append(", errr: ");
        stringBuilder.append(this.egl.eglGetError());
        printStream.println(stringBuilder.toString());
        boolean eglInitialize = this.egl.eglInitialize(this.eglDisplay, new int[2]);
        PrintStream printStream2 = System.out;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("EGLInitialize returned: ");
        stringBuilder2.append(eglInitialize);
        printStream2.println(stringBuilder2.toString());
        if (!eglInitialize) {
            return false;
        }
        int eglGetError = this.egl.eglGetError();
        if (eglGetError != 12288) {
            return false;
        }
        printStream2 = System.out;
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append("eglInitialize err: ");
        stringBuilder2.append(eglGetError);
        printStream2.println(stringBuilder2.toString());
        EGLConfig[] eGLConfigArr = new EGLConfig[20];
        iArr3 = new int[1];
        this.egl.eglChooseConfig(this.eglDisplay, this.configAttrs, eGLConfigArr, 20, iArr3);
        PrintStream printStream3 = System.out;
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("eglChooseConfig err: ");
        stringBuilder3.append(this.egl.eglGetError());
        printStream3.println(stringBuilder3.toString());
        i4 = 16777216;
        iArr2 = new int[1];
        int i7 = 0;
        while (i7 < iArr3[0]) {
            int i8;
            Object obj;
            int[] iArr4;
            for (int i9 = 0; i9 < ((iArr.length - i) >> i); i9++) {
                int i10 = i9 * 2;
                this.egl.eglGetConfigAttrib(this.eglDisplay, eGLConfigArr[i7], this.configAttrs[i10], iArr2);
                i8 = iArr2[0];
                int[] iArr5 = this.configAttrs;
                i10++;
                if ((i8 & iArr5[i10]) != iArr5[i10]) {
                    obj = null;
                    break;
                }
            }
            obj = 1;
            if (obj == null) {
                iArr4 = iArr;
            } else {
                this.egl.eglGetConfigAttrib(this.eglDisplay, eGLConfigArr[i7], i3, iArr2);
                i8 = iArr2[0];
                this.egl.eglGetConfigAttrib(this.eglDisplay, eGLConfigArr[i7], i6, iArr2);
                int i11 = iArr2[0];
                this.egl.eglGetConfigAttrib(this.eglDisplay, eGLConfigArr[i7], 12322, iArr2);
                int i12 = iArr2[0];
                this.egl.eglGetConfigAttrib(this.eglDisplay, eGLConfigArr[i7], 12321, iArr2);
                i3 = iArr2[0];
                this.egl.eglGetConfigAttrib(this.eglDisplay, eGLConfigArr[i7], 12325, iArr2);
                i6 = iArr2[0];
                iArr4 = iArr;
                this.egl.eglGetConfigAttrib(this.eglDisplay, eGLConfigArr[i7], 12326, iArr2);
                i = iArr2[0];
                PrintStream printStream4 = System.out;
                StringBuilder stringBuilder4 = new StringBuilder();
                stringBuilder4.append(">>> EGL Config [");
                stringBuilder4.append(i7);
                stringBuilder4.append("] R");
                stringBuilder4.append(i8);
                stringBuilder4.append("G");
                stringBuilder4.append(i11);
                stringBuilder4.append("B");
                stringBuilder4.append(i12);
                stringBuilder4.append("A");
                stringBuilder4.append(i3);
                stringBuilder4.append(" D");
                stringBuilder4.append(i6);
                stringBuilder4.append("S");
                stringBuilder4.append(i);
                printStream4.println(stringBuilder4.toString());
                int abs = (((((Math.abs(i8 - this.redSize) + Math.abs(i11 - this.greenSize)) + Math.abs(i12 - this.blueSize)) + Math.abs(i3 - this.alphaSize)) << 16) + (Math.abs(i6 - this.depthSize) << 8)) + Math.abs(i - this.stencilSize);
                if (abs < i4) {
                    System.out.println("--------------------------");
                    PrintStream printStream5 = System.out;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("New config chosen: ");
                    stringBuilder2.append(i7);
                    printStream5.println(stringBuilder2.toString());
                    i = 0;
                    while (true) {
                        int[] iArr6 = this.configAttrs;
                        if (i >= ((iArr6.length - 1) >> 1)) {
                            break;
                        }
                        i12 = i * 2;
                        this.egl.eglGetConfigAttrib(this.eglDisplay, eGLConfigArr[i7], iArr6[i12], iArr2);
                        if (iArr2[0] >= this.configAttrs[i12 + 1]) {
                            printStream3 = System.out;
                            StringBuilder stringBuilder5 = new StringBuilder();
                            stringBuilder5.append("setting ");
                            stringBuilder5.append(i);
                            stringBuilder5.append(", matches: ");
                            stringBuilder5.append(iArr2[0]);
                            printStream3.println(stringBuilder5.toString());
                        }
                        i++;
                    }
                    this.eglConfig = eGLConfigArr[i7];
                    i4 = abs;
                }
            }
            i7++;
            iArr = iArr4;
            i = 1;
            i3 = 12324;
            i6 = 12323;
        }
        this.eglContext = this.egl.eglCreateContext(this.eglDisplay, this.eglConfig, EGL10.EGL_NO_CONTEXT, this.contextAttrs);
        PrintStream printStream6 = System.out;
        StringBuilder stringBuilder6 = new StringBuilder();
        stringBuilder6.append("eglCreateContext: ");
        stringBuilder6.append(this.egl.eglGetError());
        printStream6.println(stringBuilder6.toString());
        this.gl = (GL11) this.eglContext.getGL();
        return true;
    }

    public void initGame() {
        if (!this.waitForPermissions) {
            systemInit();
        }
    }

    public native void jniNvAPKInit(Object obj);

    public native boolean keyEvent(int i, int i2, int i3, int i4, KeyEvent keyEvent);

    /*  JADX ERROR: NullPointerException in pass: e
        java.lang.NullPointerException: Attempt to invoke virtual method 'boolean jadx.core.c.d.a.a(jadx.core.c.a.b)' on a null object reference
        	at jadx.core.c.g.a.e.a(BlockSplitter.java:246)
        	at jadx.core.c.g.a.e.a(BlockSplitter.java:229)
        	at jadx.core.c.g.a.e.d(BlockSplitter.java:119)
        	at jadx.core.c.g.a.e.a(BlockSplitter.java:49)
        	at jadx.core.c.g.g.a(DepthTraversal.java:29)
        	at jadx.core.c.g.g.a(DepthTraversal.java:16)
        	at jadx.core.b.a(ProcessClass.java:32)
        	at jadx.a.d.a(JadxDecompiler.java:300)
        	at jadx.a.e.b(JavaClass.java:63)
        */
    public RawData loadFile(String filename)
    {
        InputStream is = null;
        RawData ret = new RawData();
        try {
            try
            {
                is = new FileInputStream("/data/" + filename);
            }
            catch (Exception e)
            {
                try
                {
                    is = getAssets().open(filename); 
                }
                catch (Exception e2)
                {
                }
            }
            int size = is.available();
            ret.length = size;
            ret.data = new byte[size];
            is.read(ret.data);
        }
        catch (IOException ioe)
        {
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Exception e) {}
            }
        }
        return ret;
    }

    /*  JADX ERROR: NullPointerException in pass: e
        java.lang.NullPointerException: Attempt to invoke virtual method 'boolean jadx.core.c.d.a.a(jadx.core.c.a.b)' on a null object reference
        	at jadx.core.c.g.a.e.a(BlockSplitter.java:246)
        	at jadx.core.c.g.a.e.a(BlockSplitter.java:229)
        	at jadx.core.c.g.a.e.d(BlockSplitter.java:119)
        	at jadx.core.c.g.a.e.a(BlockSplitter.java:49)
        	at jadx.core.c.g.g.a(DepthTraversal.java:29)
        	at jadx.core.c.g.g.a(DepthTraversal.java:16)
        	at jadx.core.b.a(ProcessClass.java:32)
        	at jadx.a.d.a(JadxDecompiler.java:300)
        	at jadx.a.e.b(JavaClass.java:63)
        */
    public RawTexture loadTexture(String filename)
    {
        RawTexture ret = new RawTexture();
        try {
            InputStream is = null;
            try
            {
                is = new FileInputStream("/data/" + filename);
            }
            catch (Exception e)
            {
                try
                {
                    is = getAssets().open(filename); 
                }
                catch (Exception e2)
                {
                }
            }
            
            Bitmap bmp = BitmapFactory.decodeStream(is);
            ret.width = bmp.getWidth();
            ret.height = bmp.getHeight();
            int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    
            // Flip texture
            int[] tmp = new int[bmp.getWidth()];
            final int w = bmp.getWidth(); 
            final int h = bmp.getHeight();
            for (int i = 0; i < h>>1; i++)
            {
                System.arraycopy(pixels, i*w, tmp, 0, w);
                System.arraycopy(pixels, (h-1-i)*w, pixels, i*w, w);
                System.arraycopy(tmp, 0, pixels, (h-1-i)*w, w);
            }
    
            // Convert from ARGB -> RGBA and put into the byte array
            ret.length = pixels.length * 4;
            ret.data = new byte[ret.length];
            int pos = 0;
            int bpos = 0;
            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++, pos++)
                {
                    int p = pixels[pos];
                    ret.data[bpos++] = (byte) ((p>>16)&0xff);
                    ret.data[bpos++] = (byte) ((p>> 8)&0xff);
                    ret.data[bpos++] = (byte) ((p>> 0)&0xff);
                    ret.data[bpos++] = (byte) ((p>>24)&0xff);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    public native void lowMemoryEvent();

    public void mSleep(long j) {
        try {
            Thread.sleep(j);
        } catch (InterruptedException unused) {
        }
    }

    public boolean makeCurrent() {
        PrintStream printStream;
        String str;
        EGLContext eGLContext = this.eglContext;
        if (eGLContext == null) {
            printStream = System.out;
            str = "eglContext is NULL";
        } else {
            EGLSurface eGLSurface = this.eglSurface;
            if (eGLSurface == null) {
                printStream = System.out;
                str = "eglSurface is NULL";
            } else {
                if (!this.egl.eglMakeCurrent(this.eglDisplay, eGLSurface, eGLSurface, eGLContext)) {
                    EGL10 egl10 = this.egl;
                    EGLDisplay eGLDisplay = this.eglDisplay;
                    EGLSurface eGLSurface2 = this.eglSurface;
                    if (!egl10.eglMakeCurrent(eGLDisplay, eGLSurface2, eGLSurface2, this.eglContext)) {
                        printStream = System.out;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("eglMakeCurrent err: ");
                        stringBuilder.append(this.egl.eglGetError());
                        str = stringBuilder.toString();
                    }
                }
                GetGLExtensions();
                return true;
            }
        }
        printStream.println(str);
        return false;
    }

    public native boolean multiTouchEvent(int i, int i2, int i3, int i4, int i5, int i6, MotionEvent motionEvent);

    public native boolean multiTouchEvent4(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, MotionEvent motionEvent);

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public void onCreate(Bundle bundle) {
        System.out.println("**** onCreate");
        super.onCreate(bundle);
        this.handler = new Handler();
        if (this.wantsAccelerometer && this.mSensorManager == null) {
            this.mSensorManager = (SensorManager) getSystemService("sensor");
        }
        this.mClipboardManager = (ClipboardManager) getSystemService("clipboard");
        NvUtil.getInstance().setActivity(this);
        NvAPKFileHelper.getInstance().setContext(this);
        new NvAPKFile().is = null;
        try {
            AssetManager assets = getAssets();
            this.assetMgr = assets;
            jniNvAPKInit(assets);
        } catch (UnsatisfiedLinkError unused) {
        }
        this.display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
        getWindow().addFlags(1024);
        setRequestedOrientation(6);
        initGame();
        hideSystemUI();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if ((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    hideSystemUI();
                } else {
                    // TODO: The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
                }

            }
        });
        processCutout();
    }

    public void onDestroy() {
        System.out.println("**** onDestroy");
        HeightProvider heightProvider = this.mHeightProvider;
        if (heightProvider != null) {
            heightProvider.dismiss();
        }
        quitAndWait();
        finish();
        super.onDestroy();
        systemCleanup();
    }

    public void onHeightChanged(int i, int i2) {
        InputManager inputManager = this.mInputManager;
        if (inputManager != null) {
            inputManager.onHeightChanged(i2);
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i != 24) {
            if (i != 25) {
                boolean z = false;
                if (!(i == 89 || i == 85)) {
                    if (i != 90) {
                        if (!(i == 82 || i == EGL_OPENGL_ES2_BIT)) {
                            z = super.onKeyDown(i, keyEvent);
                        }
                        if (!z) {
                            z = keyEvent(keyEvent.getAction(), i, keyEvent.getUnicodeChar(), keyEvent.getMetaState(), keyEvent);
                        }
                    }
                }
                return z;
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 115 && VERSION.SDK_INT >= 11) {
            keyEvent(keyEvent.isCapsLockOn() ? 3 : EGL_OPENGL_ES2_BIT, 115, 0, 0, keyEvent);
        }
        if (!(i == 89 || i == 85)) {
            if (i != 90) {
                boolean onKeyUp = super.onKeyUp(i, keyEvent);
                if (onKeyUp) {
                    return onKeyUp;
                }
                return keyEvent(keyEvent.getAction(), i, keyEvent.getUnicodeChar(), keyEvent.getMetaState(), keyEvent);
            }
        }
        return false;
    }

    protected void onPause() {
        System.out.println("**** onPause");
        super.onPause();
        this.paused = true;
        if (this.ResumeEventDone) {
            System.out.println("java is invoking pauseEvent(), this will block until\nthe client calls NVEventPauseProcessed");
            pauseEvent();
            System.out.println("pauseEvent() returned");
        }
    }

    protected void onRestart() {
        System.out.println("**** onRestart");
        super.onRestart();
    }

    protected void onResume() {
        System.out.println("**** onResume");
        super.onResume();
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(1), this.mSensorDelay);
        }
        this.paused = false;
        HeightProvider heightProvider = this.mHeightProvider;
        if (heightProvider != null) {
            heightProvider.init(this.mRootFrame);
        }
        if (this.viewIsActive && this.ResumeEventDone) {
            resumeEvent();
            SurfaceHolder surfaceHolder = this.cachedSurfaceHolder;
            if (surfaceHolder != null) {
                surfaceHolder.setKeepScreenOn(true);
            }
        }
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == 1) {
            float f;
            int rotation = this.display.getRotation();
            float f2 = 0.0f;
            float[] fArr;
            if (rotation == 0) {
                fArr = sensorEvent.values;
                f2 = -fArr[0];
                f = fArr[1];
            } else if (rotation == 1) {
                fArr = sensorEvent.values;
                f2 = fArr[1];
                f = fArr[0];
            } else if (rotation == 2) {
                fArr = sensorEvent.values;
                f2 = fArr[0];
                f = fArr[1];
            } else if (rotation != 3) {
                f = 0.0f;
            } else {
                fArr = sensorEvent.values;
                f2 = -fArr[1];
                f = fArr[0];
            }
            accelerometerEvent(f2, f, sensorEvent.values[2]);
        }
    }

    protected void onStop() {
        System.out.println("**** onStop");
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        super.onStop();
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        MotionEvent motionEvent2 = motionEvent;
        int i = 0;
        if (view != this.mRootFrame) {
            return false;
        }
        if (!this.wantsMultitouch) {
            return touchEvent(motionEvent.getAction(), (int) motionEvent.getX(), (int) motionEvent.getY(), motionEvent2);
        }
        int pointerCount = motionEvent.getPointerCount();
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        int i10 = 0;
        while (i < pointerCount) {
            if (i2 == 0) {
                i3 = (int) motionEvent2.getX(i);
                i4 = (int) motionEvent2.getY(i);
            } else if (i2 == 1) {
                i5 = (int) motionEvent2.getX(i);
                i6 = (int) motionEvent2.getY(i);
            } else if (this.Use2Touches || i2 != 2) {
                if (!this.Use2Touches && i2 == 3) {
                    i9 = (int) motionEvent2.getX(i);
                    i10 = (int) motionEvent2.getY(i);
                }
                i++;
            } else {
                i7 = (int) motionEvent2.getX(i);
                i8 = (int) motionEvent2.getY(i);
            }
            i2++;
            i++;
        }
        boolean z = this.Use2Touches;
        i = motionEvent.getAction();
        return z ? multiTouchEvent(i, i2, i3, i4, i5, i6, motionEvent) : multiTouchEvent4(i, i2, i3, i4, i5, i6, i7, i8, i9, i10, motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return super.onTouchEvent(motionEvent);
    }

    public void onWindowFocusChanged(boolean z) {
        if (this.ResumeEventDone && this.viewIsActive && !this.paused) {
            if (this.GameIsFocused && !z) {
                InputManager inputManager = this.mInputManager;
                if (inputManager == null || !inputManager.IsShowing()) {
                    pauseEvent();
                }
            } else if (!this.GameIsFocused && z) {
                resumeEvent();
            }
            this.GameIsFocused = z;
        }
        super.onWindowFocusChanged(z);
        if (z) {
            hideSystemUI();
        }
    }

    public native void pauseEvent();

    public native void quitAndWait();

    public native void resumeEvent();

    public void setFixedSize(int i, int i2) {
        this.fixedWidth = i;
        this.fixedHeight = i2;
    }

    public void setUseFullscreen(int i) {
        this.mUseFullscreen = i;
    }

    public native void setWindowSize(int i, int i2);

    public void showInputLayout()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInputManager.ShowInputLayout();
            }
        });
    }

    public boolean swapBuffers() {
        int i = this.SwapBufferSkip;
        if (i > 0) {
            this.SwapBufferSkip = i - 1;
            System.out.println("swapBuffer wait");
            return true;
        }
        PrintStream printStream;
        String str;
        EGLSurface eGLSurface = this.eglSurface;
        if (eGLSurface == null) {
            printStream = System.out;
            str = "eglSurface is NULL";
        } else if (this.egl.eglSwapBuffers(this.eglDisplay, eGLSurface)) {
            return true;
        } else {
            printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("eglSwapBufferrr: ");
            stringBuilder.append(this.egl.eglGetError());
            str = stringBuilder.toString();
        }
        printStream.println(str);
        return false;
    }

    protected void systemCleanup() {
        if (this.ranInit) {
            cleanup();
        }
        cleanupEGL();
    }

    protected boolean systemInit() {
        System.out.println("ln systemInit");
        System.out.println("Calling init(false)");
        this.bInitDone = init(false);
        System.out.println("Called");
        setContentView(R.layout.main_render_screen);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.main_sv);
        this.mSurfaceView = surfaceView;
        this.mRootFrame = (FrameLayout) findViewById(R.id.main_fl_root);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(2);
        holder.setKeepScreenOn(true);
        surfaceView.setFocusable(true);
        surfaceView.setFocusableInTouchMode(true);
        this.mRootFrame.setOnTouchListener(this);
        this.mInputManager = new InputManager(this);
        this.mHeightProvider = new HeightProvider(this).init(this.mRootFrame).setHeightListener(this);
        DoResumeEvent();
        holder.addCallback(new c(this));
        return true;
    }

    public native boolean touchEvent(int i, int i2, int i3, MotionEvent motionEvent);

    public boolean unMakeCurrent() {
        EGL10 egl10 = this.egl;
        EGLDisplay eGLDisplay = this.eglDisplay;
        EGLSurface eGLSurface = EGL10.EGL_NO_SURFACE;
        if (egl10.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, EGL10.EGL_NO_CONTEXT)) {
            return true;
        }
        PrintStream printStream = System.out;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("egl(Un)MakeCurrent err: ");
        stringBuilder.append(this.egl.eglGetError());
        printStream.println(stringBuilder.toString());
        return false;
    }
}