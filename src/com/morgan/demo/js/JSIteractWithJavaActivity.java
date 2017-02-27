package com.morgan.demo.js;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.morgan.lib.base.BaseActionBarActivity;
import com.morgan.lib.util.ToastUtils;
import com.morgan.main.R;

/**
 * 一个js和java交互的例子。
 * 
 * @author JiGuoChao
 * 
 * @version 1.0
 * 
 * @date 2014-7-18
 */
public class JSIteractWithJavaActivity extends BaseActionBarActivity {

    private WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_js_interact_with_java);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new Object() {

            // 这个annotation用于兼容17以后的版本
            @JavascriptInterface
            public void onClick() {
                ToastUtils.toast(R.string.success);
                mWebView.loadUrl("javascript:updateHtml()");
            }
        }, "jsdemo");
        mWebView.loadUrl("file:///android_asset/demo_js_iteract_with_java.html");
    }
}
