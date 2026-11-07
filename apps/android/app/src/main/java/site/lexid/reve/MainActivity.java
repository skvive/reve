package site.lexid.reve;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.http.SslError;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

/** RE:VE native navigation container for the verified HTTPS website. */
public class MainActivity extends Activity {
    private WebView webView;
    private final String base = "https://reve.lexid.site";
    private final String contentVersion = "20261107c";
    private final String[] paths = {"/", "/collection/", "/sound-lab/", "/journal/"};
    private final String[][] labels = {
        {"홈","제품","사운드랩","저널","공유"},
        {"Home","Products","Lab","Journal","Share"},
        {"首页","产品","声学","专栏","分享"},
        {"ホーム","製品","サウンド","記事","共有"}
    };
    private final Button[] tabButtons = new Button[4];
    private Button shareButton;
    private String currentLocale = "ko";
    private int accent = Color.rgb(220,185,145);
    private LinearLayout root;
    private LinearLayout tabBar;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(9,9,11));
        getWindow().setNavigationBarColor(Color.rgb(9,9,11));
        root = new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(9,9,11));
        webView=new WebView(this);webView.setBackgroundColor(Color.rgb(9,9,11));
        webView.getSettings().setJavaScriptEnabled(true);webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView,false);
        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest request){return route(request.getUrl());}
            @Override public void onReceivedSslError(WebView view,SslErrorHandler handler,SslError error){handler.cancel();Toast.makeText(MainActivity.this,"Secure connection error",Toast.LENGTH_LONG).show();}
            @Override public void onReceivedError(WebView v, WebResourceRequest req, WebResourceError err){if(req.isForMainFrame())Toast.makeText(MainActivity.this,"Network unavailable. Check your connection.",Toast.LENGTH_SHORT).show();}
            @Override public void onPageFinished(WebView view,String url){
                Uri uri=Uri.parse(url);
                if("https".equals(uri.getScheme())&&"reve.lexid.site".equals(uri.getHost()))
                    view.evaluateJavascript("(function(){try{return localStorage.getItem('reve.language')||'ko'}catch(e){return 'ko'}})()",MainActivity.this::updateLocale);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.setDownloadListener((url,userAgent,disposition,mimeType,size)->{
            Uri uri=Uri.parse(url);if(!"https".equals(uri.getScheme())||!"reve.lexid.site".equalsIgnoreCase(uri.getHost()))return;
            DownloadManager.Request request=new DownloadManager.Request(uri);
            request.setTitle("RE:VE Download");request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS,uri.getLastPathSegment());
            ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
        });
        root.addView(webView,new LinearLayout.LayoutParams(-1,0,1));
        tabBar=new LinearLayout(this);tabBar.setPadding(dp(5),dp(6),dp(5),dp(6));tabBar.setBackgroundColor(Color.rgb(19,18,20));
        for(int i=0;i<paths.length;i++){
            final String path=paths[i];
            Button btn=button(labels[0][i]);tabButtons[i]=btn;
            btn.setTextSize(11);btn.setMinWidth(0);btn.setPadding(0,0,0,0);
            btn.setOnClickListener(v->webView.evaluateJavascript("(function(){try{return localStorage.getItem('reve.language')||'ko'}catch(e){return 'ko'}})()",raw->{
                updateLocale(raw);
                webView.loadUrl(base+path+"?appCatalog="+contentVersion+("ko".equals(currentLocale)?"":"&lang="+currentLocale));
            }));
            tabBar.addView(btn,new LinearLayout.LayoutParams(0,dp(48),1));
        }
        shareButton=button("공유");shareButton.setTextSize(11);shareButton.setMinWidth(0);shareButton.setPadding(0,0,0,0);
        shareButton.setOnClickListener(v->{Intent in=new Intent(Intent.ACTION_SEND);in.setType("text/plain");in.putExtra(Intent.EXTRA_TEXT,webView.getUrl()==null?base:webView.getUrl());startActivity(Intent.createChooser(in,"Share RE:VE"));});
        tabBar.addView(shareButton,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(tabBar);setContentView(root);
        Uri launch=getIntent().getData();webView.loadUrl(launch!=null&&"reve.lexid.site".equals(launch.getHost())&&"https".equals(launch.getScheme())?launch.toString():base+"/?appCatalog="+contentVersion);
    }
    private void updateLocale(String raw){
        if(raw!=null&&raw.contains("zh-CN"))currentLocale="zh-CN";
        else if(raw!=null&&raw.contains("ja"))currentLocale="ja";
        else if(raw!=null&&raw.contains("en"))currentLocale="en";
        else currentLocale="ko";
        int k="en".equals(currentLocale)?1:"zh-CN".equals(currentLocale)?2:"ja".equals(currentLocale)?3:0;
        for(int i=0;i<tabButtons.length;i++){
            if(tabButtons[i]!=null){tabButtons[i].setText(labels[k][i]);tabButtons[i].setContentDescription(labels[k][i]);}
        }
        if(shareButton!=null){shareButton.setText(labels[k][4]);shareButton.setContentDescription(labels[k][4]);}
    }
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setTextColor(accent);b.setAllCaps(false);b.setBackgroundColor(Color.TRANSPARENT);return b;}
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+.5f);}
    private boolean route(Uri uri){if("https".equals(uri.getScheme())&&"reve.lexid.site".equals(uri.getHost()))return false;
        if("https".equals(uri.getScheme())||"mailto".equals(uri.getScheme())||"tel".equals(uri.getScheme())){try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(ActivityNotFoundException ex){Toast.makeText(this,"No app to open this link",Toast.LENGTH_SHORT).show();}}return true;}
    @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(webView!=null){root.removeView(webView);webView.destroy();webView=null;}super.onDestroy();}
}
