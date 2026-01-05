package com.ednovas.donguatv;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private int statusBarHeight = 0;
    private ViewGroup webViewParent = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        
        // 设置状态栏背景色
        window.setStatusBarColor(0xFF141414);
        
        // 设置状态栏图标为浅色
        View decorView = window.getDecorView();
        int flags = decorView.getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(flags);
        
        // 获取状态栏高度
        statusBarHeight = getStatusBarHeight();
    }

    @Override
    public void onStart() {
        super.onStart();
        
        // 获取 WebView 并设置其父容器的 padding
        WebView webView = getBridge().getWebView();
        if (webView != null && webView.getParent() instanceof ViewGroup) {
            webViewParent = (ViewGroup) webView.getParent();
            
            // 设置父容器的顶部 padding
            webViewParent.setPadding(
                webViewParent.getPaddingLeft(),
                statusBarHeight,
                webViewParent.getPaddingRight(),
                webViewParent.getPaddingBottom()
            );
            
            // 设置背景色与应用一致
            webViewParent.setBackgroundColor(0xFF141414);
            
            // 添加 JavaScript 接口用于全屏控制
            webView.addJavascriptInterface(new FullscreenInterface(), "AndroidFullscreen");
        }
    }
    
    // 获取状态栏高度（像素）
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        // 如果获取失败，使用默认值
        if (result == 0) {
            result = (int) (24 * getResources().getDisplayMetrics().density);
        }
        return result;
    }
    
    // 进入全屏模式
    private void enterFullscreen() {
        runOnUiThread(() -> {
            // 移除 padding
            if (webViewParent != null) {
                webViewParent.setPadding(0, 0, 0, 0);
            }
            
            // 隐藏状态栏和导航栏
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            
            // 锁定横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        });
    }
    
    // 退出全屏模式
    private void exitFullscreen() {
        runOnUiThread(() -> {
            // 恢复 padding
            if (webViewParent != null) {
                webViewParent.setPadding(
                    webViewParent.getPaddingLeft(),
                    statusBarHeight,
                    webViewParent.getPaddingRight(),
                    webViewParent.getPaddingBottom()
                );
            }
            
            // 显示状态栏
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
            flags &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            flags &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(flags);
            
            // 解锁屏幕方向
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        });
    }
    
    // JavaScript 接口类
    public class FullscreenInterface {
        @JavascriptInterface
        public void enter() {
            enterFullscreen();
        }
        
        @JavascriptInterface
        public void exit() {
            exitFullscreen();
        }
    }
}
