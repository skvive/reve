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
    private final String contentVersion = "20261107b";
    private final String[] paths = {"/", "/collection/", "/sound-lab/", "/journal/"};
    private final String[] labels = {"Home", "Products", "Lab", "Journal"};
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
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.setDownloadListener((url,userAgent,disposition,mimeType,size)->{
            Uri uri=Uri.parse(url);if(!"https".equals(uri.getScheme()))return;
            DownloadManager.Request request=new DownloadManager.Request(uri);
            request.setTitle("RE:VE Download");request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS,uri.getLastPathSegment());
            ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
        });
        root.addView(webView,new LinearLayout.LayoutParams(-1,0,1));
        tabBar=new LinearLayout(this);tabBar.setPadding(dp(5),dp(6),dp(5),dp(6));tabBar.setBackgroundColor(Color.rgb(19,18,20));
        for(int i=0;i<paths.length;i++){final String path=paths[i];Button btn=button(labels[i]);btn.setTextSize(12);btn.setOnClickListener(v->webView.loadUrl(base+path+"?appCatalog="+contentVersion));tabBar.addView(btn,new LinearLayout.LayoutParams(0,dp(44),1));}
        Button share=button("Share");share.setTextSize(12);
        share.setOnClickListener(v->{Intent in=new Intent(Intent.ACTION_SEND);in.setType("text/plain");in.putExtra(Intent.EXTRA_TEXT,webView.getUrl());startActivity(Intent.createChooser(in,"Share RE:VE"));});
        tabBar.addView(share,new LinearLayout.LayoutParams(0,dp(44),1));
        root.addView(tabBar);setContentView(root);
        Uri launch=getIntent().getData();webView.loadUrl(launch!=null&&"reve.lexid.site".equals(launch.getHost())&&"https".equals(launch.getScheme())?launch.toString():base+"/?appCatalog="+contentVersion);
    }
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setTextColor(accent);b.setAllCaps(false);b.setBackgroundColor(Color.TRANSPARENT);return b;}
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+.5f);}
    private boolean route(Uri uri){if("https".equals(uri.getScheme())&&"reve.lexid.site".equals(uri.getHost()))return false;
        if("https".equals(uri.getScheme())||"mailto".equals(uri.getScheme())||"tel".equals(uri.getScheme())){try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(ActivityNotFoundException ex){Toast.makeText(this,"No app to open this link",Toast.LENGTH_SHORT).show();}}return true;}
    @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(webView!=null){root.removeView(webView);webView.destroy();webView=null;}super.onDestroy();}
}
