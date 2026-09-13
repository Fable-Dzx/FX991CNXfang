package com.fx991cnx.fang;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on, immersive fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // 刘海屏适配：内容延伸到刘海区域，实现真正的全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(false);

        final AssetManager assets = getAssets();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                // Only intercept local asset-style requests; let https/http pass through (fonts etc.)
                if ("file".equals(scheme) || "https".equals(scheme) || "http".equals(scheme)) {
                    String path = uri.getPath();
                    if (path != null && path.startsWith("/")) {
                        String assetPath = path.substring(1);
                        // file:///android_asset/index.html -> getPath()=/android_asset/index.html
                        // 剥掉 android_asset/ 前缀，assets.open() 才找得到
                        if (assetPath.startsWith("android_asset/")) {
                            assetPath = assetPath.substring("android_asset/".length());
                        }
                        // strip any trailing query / default to index
                        if (assetPath.isEmpty() || assetPath.endsWith("/")) {
                            assetPath = assetPath + "index.html";
                        }
                        try {
                            InputStream is = assets.open(assetPath);
                            String mime = mimeFor(assetPath);
                            // 对 HTML 注入刘海屏适配样式：覆盖网页自带的 safe-area 顶部留白，
                            // 让计算器主体真正铺满到刘海区域（网页源码保持不变）
                            if (mime.startsWith("text/html")) {
                                byte[] raw = readAll(is);
                                String html = new String(raw, StandardCharsets.UTF_8);
                                String style = "<style>"
                                        + "[class*=\"mainMain\"]{"
                                        + "padding-top:0 !important;"
                                        + "padding-bottom:env(safe-area-inset-bottom) !important;"
                                        + "}"
                                        + "</style>";
                                if (html.contains("</head>")) {
                                    html = html.replace("</head>", style + "</head>");
                                } else {
                                    html = style + html;
                                }
                                WebResourceResponse resp = new WebResourceResponse(
                                        mime, "utf-8",
                                        new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
                                return resp;
                            }
                            WebResourceResponse resp = new WebResourceResponse(mime, "utf-8", is);
                            return resp;
                        } catch (IOException e) {
                            // not an asset file -> let WebView handle (network)
                            return null;
                        }
                    }
                }
                return null;
            }

            private byte[] readAll(InputStream is) throws IOException {
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1) {
                    bos.write(buf, 0, n);
                }
                is.close();
                return bos.toByteArray();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // 兜底：页面加载后强制清除计算器主容器的 safe-area 顶部留白，
                // 使内容铺满刘海区域（CSS module 类名含 mainMain 子串）
                String js = "try{var e=document.querySelector('[class*=\"mainMain\"]');"
                        + "if(e){e.style.paddingTop='0px';e.style.paddingBottom='env(safe-area-inset-bottom)';}"
                        + "}catch(err){}";
                view.evaluateJavascript(js, null);
                super.onPageFinished(view, url);
            }
        });

        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    private String mimeFor(String path) {
        String p = path.toLowerCase();
        if (p.endsWith(".js")) return "application/javascript";
        if (p.endsWith(".mjs")) return "application/javascript";
        if (p.endsWith(".css")) return "text/css";
        if (p.endsWith(".html") || p.endsWith(".htm")) return "text/html";
        if (p.endsWith(".json")) return "application/json";
        if (p.endsWith(".ico")) return "image/x-icon";
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return "image/jpeg";
        if (p.endsWith(".svg")) return "image/svg+xml";
        if (p.endsWith(".woff2")) return "font/woff2";
        if (p.endsWith(".woff")) return "font/woff";
        if (p.endsWith(".ttf")) return "font/ttf";
        if (p.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
