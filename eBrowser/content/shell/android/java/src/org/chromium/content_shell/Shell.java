// Copyright 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.content_shell;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import org.chromium.base.annotations.CalledByNative;
import org.chromium.base.annotations.JNINamespace;
import org.chromium.content.browser.ActivityContentVideoViewEmbedder;
import org.chromium.content.browser.ContentVideoViewEmbedder;
import org.chromium.content.browser.ContentView;
import org.chromium.content.browser.ContentViewClient;
import org.chromium.content.browser.ContentViewCore;
import org.chromium.content.browser.ContentViewRenderView;
import org.chromium.content_public.browser.LoadUrlParams;
import org.chromium.content_public.browser.NavigationController;
import org.chromium.content_public.browser.WebContents;
import org.chromium.ui.base.ViewAndroidDelegate;
import org.chromium.ui.base.WindowAndroid;
import org.chromium.base.Log;

import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
/**
 * Container for the various UI components that make up a shell window.
 */
@JNINamespace("content")
public class Shell extends LinearLayout {

    private static final long COMPLETED_PROGRESS_TIMEOUT_MS = 200;
    private static final String TAG = "eBrowser.Shell";
    private boolean isPowerSaving = false;
    private final Runnable mClearProgressRunnable = new Runnable() {
        @Override
        public void run() {
            mProgressDrawable.setLevel(0);
        }
    };

    private ContentViewCore mContentViewCore;
    private WebContents mWebContents;
    private NavigationController mNavigationController;
    private ContentViewClient mContentViewClient;

    private EditText mUrlTextView;
    private ImageButton mPrevButton;
    private ImageButton mNextButton;
    private ImageButton mStopReloadButton;
    private Button mminusButton;
    private Button msavePowerButton;
    private Button mplusButton;
    private Button mresetButton;
    private TextView fpsText;
    private ClipDrawable mProgressDrawable;
    private Button bushuangButton;
    private Button feedbackButton;
    private TextView text_bushuang;

    private long mNativeShell;
    private ContentViewRenderView mContentViewRenderView;
    private WindowAndroid mWindow;

    private boolean mLoading = false;
    private boolean mIsFullscreen = false;
    private int initalFps = 10;
    private int initalBushuang = 0;
    private Context mContext;
    public static Handler handler;
    /**
     * Constructor for inflating via XML.
     */
    public Shell(Context context, AttributeSet attrs) {
        super(context, attrs);
	mContext = (Activity) getContext();
	if(getUUID(mContext).isEmpty()){
    	   setUUID(mContext);
	}
    }

    /**
     * Set the SurfaceView being renderered to as soon as it is available.
     */
    //ContentViewRenderView is SurfaceView?
    public void setContentViewRenderView(ContentViewRenderView contentViewRenderView) {
        FrameLayout contentViewHolder = (FrameLayout) findViewById(R.id.contentview_holder);
        if (contentViewRenderView == null) {
            if (mContentViewRenderView != null) {
                contentViewHolder.removeView(mContentViewRenderView);
            }
        } else {
            contentViewHolder.addView(contentViewRenderView,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));
        }
        mContentViewRenderView = contentViewRenderView;
    }

    /**
     * Initializes the Shell for use.
     *
     * @param nativeShell The pointer to the native Shell object.
     * @param window The owning window for this shell.
     * @param client The {@link ContentViewClient} to be bound to any current or new
     *               {@link ContentViewCore}s associated with this shell.
     */
    public void initialize(long nativeShell, WindowAndroid window, ContentViewClient client) {
        mNativeShell = nativeShell;
        mWindow = window;
        mContentViewClient = client;
    }

    /**
     * Closes the shell and cleans up the native instance, which will handle destroying all
     * dependencies.
     */
    public void close() {
        if (mNativeShell == 0) return;
        nativeCloseShell(mNativeShell);
    }

    @CalledByNative
    private void onNativeDestroyed() {
        mWindow = null;
        mNativeShell = 0;
        mContentViewCore.destroy();
    }

    /**
     * @return Whether the Shell has been destroyed.
     * @see #onNativeDestroyed()
     */
    public boolean isDestroyed() {
        return mNativeShell == 0;
    }

    /**
     * @return Whether or not the Shell is loading content.
     */
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //loading page progressbar
        mProgressDrawable = (ClipDrawable) findViewById(R.id.toolbar).getBackground();

        initializeUrlField();
        initializeNavigationButtons();
    }

    private void initializeUrlField() {
        mUrlTextView = (EditText) findViewById(R.id.url);
        mUrlTextView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId != EditorInfo.IME_ACTION_GO) && (event == null
                        || event.getKeyCode() != KeyEvent.KEYCODE_ENTER
                        || event.getAction() != KeyEvent.ACTION_DOWN)) {
                    return false;
                }
                loadUrl(mUrlTextView.getText().toString());
                setKeyboardVisibilityForUrl(false);
                mContentViewCore.getContainerView().requestFocus();
                return true;
            }
        });
        mUrlTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setKeyboardVisibilityForUrl(hasFocus);
                //mNextButton.setVisibility(hasFocus ? GONE : VISIBLE);
                //mPrevButton.setVisibility(hasFocus ? GONE : VISIBLE);
                //mStopReloadButton.setVisibility(hasFocus ? GONE : VISIBLE); //输入网址时隐藏前进后退刷新按钮
		//bushuangButton.setVisibility(hasFocus ? GONE : VISIBLE);
		msavePowerButton.setVisibility(hasFocus ? GONE : VISIBLE);
                if (!hasFocus) {
                    mUrlTextView.setText(mWebContents.getUrl());
                }
            }
        });
        mUrlTextView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mContentViewCore.getContainerView().requestFocus();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Loads an URL.  This will perform minimal amounts of sanitizing of the URL to attempt to
     * make it valid.
     *
     * @param url The URL to be loaded by the shell.
     */
    public void loadUrl(String url) {
        if (url == null) return;

        if (TextUtils.equals(url, mWebContents.getUrl())) {
            mNavigationController.reload(true);
        } else {
            mNavigationController.loadUrl(new LoadUrlParams(sanitizeUrl(url)));
        }
        mUrlTextView.clearFocus();
        // TODO(aurimas): Remove this when crbug.com/174541 is fixed.
        mContentViewCore.getContainerView().clearFocus();
        mContentViewCore.getContainerView().requestFocus();
    }

    /**
     * Given an URL, this performs minimal sanitizing to ensure it will be valid.
     * @param url The url to be sanitized.
     * @return The sanitized URL.
     */
    public static String sanitizeUrl(String url) {
        if (url == null) return null;
        if (url.startsWith("www.") || url.indexOf(":") == -1) url = "http://" + url;
        return url;
    }

    private void initializeNavigationButtons() {
        mPrevButton = (ImageButton) findViewById(R.id.prev);
        mPrevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNavigationController.canGoBack()) mNavigationController.goBack();
            }
        });

        mNextButton = (ImageButton) findViewById(R.id.next);
        mNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNavigationController.canGoForward()) mNavigationController.goForward();
            }
        });
        mStopReloadButton = (ImageButton) findViewById(R.id.stop_reload_button);
        mStopReloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoading) mWebContents.stop();
                else mNavigationController.reload(true);
            }
        });
//------------
       fpsText =  (TextView) findViewById(R.id.text_fps);
       text_bushuang =  (TextView) findViewById(R.id.text_bushuang);
       mminusButton = (Button) findViewById(R.id.minus);
        mminusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(initalFps<=2){return;}
                initalFps -= 2;
                fpsText.setText(String.valueOf(initalFps));
                mContentViewCore.changeFps(initalFps);
            }
        });
       mplusButton = (Button) findViewById(R.id.plus);
        mplusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(initalFps>=60){return;}
                initalFps += 2;
                fpsText.setText(String.valueOf(initalFps));
                mContentViewCore.changeFps(initalFps);
            }
        });

        bushuangButton = (Button) findViewById(R.id.bushuang);//feedback of scroll
	feedbackButton = (Button) findViewById(R.id.feedback);//feedback of pinch
        bushuangButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
		if(!isPowerSaving){return;}
                Log.w(TAG, "speeeeeeeeeeeeeeeeeeed-shell: %s ", ContentView.lastScrollAvgSpeed);
		Log.w(TAG, "start-time: %s ", System.currentTimeMillis());
		String ipAddr = "http://139.224.10.197:8080";
		final String urlSave = ipAddr+"/save?deviceId="+getUUID(mContext)+"&speed="+(ContentView.lastScrollAvgSpeed/50)+"&step=1";
		final String urlTrain = ipAddr+"/train?deviceId="+getUUID(mContext);
		final String urlDownload = ipAddr+"/download?fileName="+getUUID(mContext);
		Toast.makeText(mContext,"反馈",Toast.LENGTH_SHORT).show();
                new HttpPostThread(urlSave, mContext, handler).start();
                int lastCount = getCount(mContext);
		lastCount += 1;
                Log.w(TAG,"已反馈次数: %s",lastCount);
                doCount(mContext,lastCount);
		if(lastCount % 5 == 0){
			Toast.makeText(mContext,"训练",Toast.LENGTH_SHORT).show();
			new HttpPostThread(urlTrain, mContext, handler).start();
		}
		if(lastCount % 6 == 0){
			Toast.makeText(mContext,"下载",Toast.LENGTH_SHORT).show();
			new HttpDownloadThread(urlDownload, mContext, handler).start();
		}
		ContentView.lastScrollAvgSpeed = 0;
		return;
            }
        });
        feedbackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
		if(ContentView.lastScrollAvgSpeed<=0){return;}
                long tempSpeed = ContentView.lastScrollAvgSpeed;
		String tempFps = fpsText.getText().toString();
                initalBushuang += 1;
                text_bushuang.setText(String.valueOf(initalBushuang));
		String ipAddr = "http://139.224.10.197:8080";
		final String urlSave0 = ipAddr+"/pinch?deviceId="+getUUID(mContext)+"&speed="+tempSpeed+"&fps="+tempFps;
		new HttpPostThread(urlSave0, mContext, handler).start();
		Toast.makeText(mContext,"速度："+ContentView.lastScrollAvgSpeed+" FPS:"+tempFps,Toast.LENGTH_SHORT).show();
		ContentView.lastScrollAvgSpeed = 0;
		return;
            }
        });


      msavePowerButton = (Button) findViewById(R.id.savePowerBtn);
        msavePowerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
	        isPowerSaving = !isPowerSaving;
		if(isPowerSaving){msavePowerButton.setText("关闭");}else{msavePowerButton.setText("启用");}
                //if(initalFps>=60){return;}
                if(!isPowerSaving){
			mContentViewCore.switchChangeFps("stop",0);
			return;
		}
                //initalFps += 2;
	      String systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	      String appFolderPath = systemPath+"libsvm/";
	      String modelPath0 = appFolderPath+getUUID(mContext);
	      String modelPath = appFolderPath+"model";
	      String temp = "";
	      String line = null;
	      StringBuilder text = new StringBuilder();
      try {	
           File file = new File(modelPath0);
	   if(!file.exists()){file = new File(modelPath);}
           BufferedReader buf = new BufferedReader(new FileReader(file));	     
           while((line=buf.readLine())!=null)
	   {
		if(text.length()>0){
		    text.append("\n");
   		}
		text.append(line.trim());
           }
           buf.close();
	}
	catch (IOException e2)
	{
           throw new RuntimeException("读取文件异常");
	}
        final String modelStr = text.toString();
        //fpsText.setText(String.valueOf(initalFps));		
        mContentViewCore.switchChangeFps(modelStr,0);
        }
        });

      mresetButton = (Button) findViewById(R.id.reset);
      mresetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               initalBushuang = 0;
               text_bushuang.setText(String.valueOf(initalBushuang));
               initalFps = 10;
               fpsText.setText(String.valueOf(initalFps));
               mContentViewCore.changeFps(initalFps);
            }
        });
    }

    @SuppressWarnings("unused")
    @CalledByNative
    private void onUpdateUrl(String url) {
        mUrlTextView.setText(url);
    }

    @SuppressWarnings("unused")
    @CalledByNative
    private void onLoadProgressChanged(double progress) {
        removeCallbacks(mClearProgressRunnable);
        mProgressDrawable.setLevel((int) (10000.0 * progress));
        if (progress == 1.0) postDelayed(mClearProgressRunnable, COMPLETED_PROGRESS_TIMEOUT_MS);
    }

    @CalledByNative
    private void toggleFullscreenModeForTab(boolean enterFullscreen) {
        mIsFullscreen = enterFullscreen;
        LinearLayout toolBar = (LinearLayout) findViewById(R.id.toolbar);
        toolBar.setVisibility(enterFullscreen ? GONE : VISIBLE);
    }

    @CalledByNative
    private boolean isFullscreenForTabOrPending() {
        return mIsFullscreen;
    }

    @SuppressWarnings("unused")
    @CalledByNative
    private void setIsLoading(boolean loading) {
        mLoading = loading;
        if (mLoading) {
            mStopReloadButton
                    .setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            mStopReloadButton.setImageResource(R.drawable.ic_refresh);
        }
    }

    /**
     * Initializes the ContentView based on the native tab contents pointer passed in.
     * @param webContents A {@link WebContents} object.
     */
    @SuppressWarnings("unused")
    @CalledByNative
    private void initFromNativeTabContents(WebContents webContents) {
        Context context = getContext();
        mContentViewCore = new ContentViewCore(context, "");
        ContentView cv = ContentView.createContentView(context, mContentViewCore);
        mContentViewCore.initialize(ViewAndroidDelegate.createBasicDelegate(cv), cv,
                webContents, mWindow);
        mContentViewCore.setContentViewClient(mContentViewClient);
        mWebContents = mContentViewCore.getWebContents();
        mNavigationController = mWebContents.getNavigationController();
        if (getParent() != null) mContentViewCore.onShow();
        if (mWebContents.getUrl() != null) {
            mUrlTextView.setText(mWebContents.getUrl());
        }
        ((FrameLayout) findViewById(R.id.contentview_holder)).addView(cv,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
        cv.requestFocus();
        mContentViewRenderView.setCurrentContentViewCore(mContentViewCore);
    }

    @CalledByNative
    public ContentVideoViewEmbedder getContentVideoViewEmbedder() {
        return new ActivityContentVideoViewEmbedder((Activity) getContext()) {
            @Override
            public void enterFullscreenVideo(View view, boolean isVideoLoaded) {
                super.enterFullscreenVideo(view, isVideoLoaded);
                mContentViewRenderView.setOverlayVideoMode(true);
            }

            @Override
            public void exitFullscreenVideo() {
                super.exitFullscreenVideo();
                mContentViewRenderView.setOverlayVideoMode(false);
            }
        };
    }

    /**
     * Enable/Disable navigation(Prev/Next) button if navigation is allowed/disallowed
     * in respective direction.
     * @param controlId Id of button to update
     * @param enabled enable/disable value
     */
    @CalledByNative
    private void enableUiControl(int controlId, boolean enabled) {
        if (controlId == 0) {
            mPrevButton.setEnabled(enabled);
        } else if (controlId == 1) {
            mNextButton.setEnabled(enabled);
        }
    }

    /**
     * @return The {@link ViewGroup} currently shown by this Shell.
     */
    public ViewGroup getContentView() {
        return mContentViewCore.getContainerView();
    }

    /**
     * @return The {@link ContentViewCore} currently managing the view shown by this Shell.
     */
    public ContentViewCore getContentViewCore() {
        return mContentViewCore;
    }

     /**
     * @return The {@link WebContents} currently managing the content shown by this Shell.
     */
    public WebContents getWebContents() {
        return mWebContents;
    }

    private void setKeyboardVisibilityForUrl(boolean visible) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (visible) {
            imm.showSoftInput(mUrlTextView, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(mUrlTextView.getWindowToken(), 0);
        }
    }

    private int getCount(Context context){
        SharedPreferences sp = context.getSharedPreferences("config",context.MODE_PRIVATE);//参数1是xml文件名
        return sp.getInt("count",0);
    }
    private void doCount(Context context,int value){
        SharedPreferences sp = context.getSharedPreferences("config",context.MODE_PRIVATE);//参数1是xml文件名
        //参数2是读写文件的权限模式是只有本应用程序可以读写
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("count",value);
        editor.apply();
    }

public String getUUID(Context context){
    SharedPreferences sp = context.getSharedPreferences("config",context.MODE_PRIVATE);
    return sp.getString("uuid","");
}
public void setUUID(Context context){
    SharedPreferences sp = context.getSharedPreferences("config",context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sp.edit();
    editor.putString("uuid", UUID.randomUUID().toString());
    editor.apply();
}



    private static native void nativeCloseShell(long shellPtr);
}
