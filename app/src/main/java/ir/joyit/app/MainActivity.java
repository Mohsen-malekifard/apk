package ir.joyit.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    // Change this URL to point the app at a different website.
    private static final String HOME_URL = "https://joyit.ir/";

    private int backgroundColor;
    private int accentColor;
    private int secondaryTextColor;

    private FrameLayout root;
    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorPanel;
    private boolean hasMainFrameError = false;
    private String lastRequestedUrl = HOME_URL;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backgroundColor = getColor(R.color.joyit_background);
        accentColor = getColor(R.color.joyit_accent);
        secondaryTextColor = getColor(R.color.joyit_text_secondary);

        Window window = getWindow();
        window.setStatusBarColor(backgroundColor);
        window.setNavigationBarColor(backgroundColor);
        window.getDecorView().setSystemUiVisibility(0);

        // WebView debugging is available in debug builds only.
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);

        root = new FrameLayout(this);
        root.setBackgroundColor(backgroundColor);
        setContentView(root);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);

        configureWebView(webView);

        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        root.addView(webView, webParams);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setIndeterminate(false);
        progressBar.setProgressTintList(ColorStateList.valueOf(accentColor));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        progressBar.setElevation(dp(8));
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3), Gravity.TOP
        );
        root.addView(progressBar, progressParams);

        buildErrorPanel();
        installWebViewClients();

        if (savedInstanceState != null && webView.restoreState(savedInstanceState) != null) {
            // Restore the previous in-app navigation state after configuration changes.
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView(WebView target) {
        WebSettings settings = target.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(true);
        // Keep playback user-initiated; games can still play media after a tap.
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        // Some embedded games and sign-in flows rely on third-party cookies.
        cookieManager.setAcceptThirdPartyCookies(target, true);
    }

    private void installWebViewClients() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleNavigation(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleNavigation(Uri.parse(url));
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url != null && !url.isEmpty()) {
                    lastRequestedUrl = url;
                }
                hasMainFrameError = false;
                errorPanel.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(5);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
                if (!hasMainFrameError) {
                    errorPanel.setVisibility(View.GONE);
                }
                progressBar.setProgress(100);
                progressBar.postDelayed(() -> progressBar.setVisibility(View.GONE), 180);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    hasMainFrameError = true;
                    lastRequestedUrl = request.getUrl().toString();
                    showErrorPanel();
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                           android.net.http.SslError error) {
                // Never bypass certificate errors. WebView's default is to cancel.
                handler.cancel();
                hasMainFrameError = true;
                showErrorPanel();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress >= 100) {
                    progressBar.postDelayed(() -> progressBar.setVisibility(View.GONE), 120);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            // Redirect links that request a new tab back into the app's main WebView.
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                WebView popup = new WebView(MainActivity.this);
                configureWebView(popup);
                popup.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        Uri uri = request.getUrl();
                        if (isWebUrl(uri)) {
                            webView.loadUrl(uri.toString());
                        } else {
                            handleNavigation(uri);
                        }
                        popup.post(popup::destroy);
                        return true;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Uri uri = Uri.parse(url);
                        if (isWebUrl(uri)) {
                            webView.loadUrl(uri.toString());
                        } else {
                            handleNavigation(uri);
                        }
                        popup.post(popup::destroy);
                        return true;
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        if (isWebUrl(Uri.parse(url))) {
                            webView.loadUrl(url);
                            popup.post(popup::destroy);
                        }
                    }
                });

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popup);
                resultMsg.sendToTarget();
                return true;
            }
        });
    }

    private boolean handleNavigation(Uri uri) {
        if (isWebUrl(uri)) {
            return false;
        }

        String scheme = uri.getScheme();
        if (scheme == null) {
            return true;
        }

        // Only well-known user-facing schemes are handed to another app.
        switch (scheme.toLowerCase()) {
            case "mailto":
            case "tel":
            case "sms":
            case "smsto":
            case "geo":
            case "market":
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException ignored) {
                    // The device has no compatible app; keep Joyit running safely.
                }
                return true;
            default:
                // Block file://, content://, javascript:, and unknown URL schemes.
                return true;
        }
    }

    private boolean isWebUrl(Uri uri) {
        String scheme = uri.getScheme();
        return "https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme);
    }

    private void buildErrorPanel() {
        errorPanel = new LinearLayout(this);
        errorPanel.setOrientation(LinearLayout.VERTICAL);
        errorPanel.setGravity(Gravity.CENTER);
        errorPanel.setPadding(dp(28), dp(24), dp(28), dp(24));
        errorPanel.setBackgroundColor(backgroundColor);
        errorPanel.setVisibility(View.GONE);
        errorPanel.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

        TextView title = new TextView(this);
        title.setText(R.string.error_title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView detail = new TextView(this);
        detail.setText(R.string.error_message);
        detail.setTextColor(secondaryTextColor);
        detail.setTextSize(14);
        detail.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        detailParams.topMargin = dp(12);

        TextView retry = new TextView(this);
        retry.setText(R.string.retry_connection);
        retry.setTextColor(Color.WHITE);
        retry.setTextSize(15);
        retry.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        retry.setGravity(Gravity.CENTER);
        retry.setPadding(dp(28), dp(13), dp(28), dp(13));
        retry.setFocusable(true);
        retry.setClickable(true);
        retry.setContentDescription(getString(R.string.retry_connection));
        GradientDrawable buttonBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(126, 76, 235), Color.rgb(181, 91, 245)}
        );
        buttonBg.setCornerRadius(dp(14));
        retry.setBackground(buttonBg);
        retry.setOnClickListener(v -> {
            hasMainFrameError = false;
            errorPanel.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            webView.loadUrl(lastRequestedUrl == null ? HOME_URL : lastRequestedUrl);
        });
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        retryParams.topMargin = dp(22);

        errorPanel.addView(title);
        errorPanel.addView(detail, detailParams);
        errorPanel.addView(retry, retryParams);
        root.addView(errorPanel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private void showErrorPanel() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            errorPanel.setVisibility(View.VISIBLE);
        });
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
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
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) {
            webView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
