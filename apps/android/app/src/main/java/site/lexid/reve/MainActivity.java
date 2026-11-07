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
import android.widget.FrameLayout;
import android.view.Gravity;
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
    private final String contentVersion = "20261107d";
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
    private LinearLayout errorPanel;
    private String lastRequested = "https://reve.lexid.site/";
    private boolean mainFrameFailed = false;

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
            @Override public void onReceivedSslError(WebView view,SslErrorHandler handler,SslError error){
                handler.cancel();
                MainActivity.this.showConnectionError(true);
            }
            @Override public void onReceivedError(WebView v, WebResourceRequest req, WebResourceError err){
                if(req.isForMainFrame()) MainActivity.this.showConnectionError(false);
            }
            @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap favicon){
                mainFrameFailed=false;
                if(errorPanel!=null)errorPanel.setVisibility(View.GONE);
            }
            @Override public void onPageFinished(WebView view,String url){
                Uri uri=Uri.parse(url);
                if("https".equals(uri.getScheme())&&"reve.lexid.site".equals(uri.getHost())&&!mainFrameFailed){
                    lastRequested=url;
                    view.evaluateJavascript("(function(){try{return localStorage.getItem('reve.language')||'ko'}catch(e){return 'ko'}})()",MainActivity.this::updateLocale);
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.setDownloadListener((url,userAgent,disposition,mimeType,size)->{
            Uri uri=Uri.parse(url);if(!"https".equals(uri.getScheme())||!"reve.lexid.site".equalsIgnoreCase(uri.getHost()))return;
            DownloadManager.Request request=new DownloadManager.Request(uri);
            request.setTitle("RE:VE Download");request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            String filename=uri.getLastPathSegment();
            if(filename==null||!filename.matches("[A-Za-z0-9._-]{1,140}"))filename="REVE-download";
            request.setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS,filename);
            ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
        });
        FrameLayout contentFrame=new FrameLayout(this);
        contentFrame.addView(webView,new FrameLayout.LayoutParams(-1,-1));
        errorPanel=new LinearLayout(this);
        errorPanel.setOrientation(LinearLayout.VERTICAL);
        errorPanel.setGravity(Gravity.CENTER);
        errorPanel.setPadding(dp(25),dp(35),dp(25),dp(35));
        errorPanel.setBackgroundColor(Color.rgb(9,9,11));
        TextView title=new TextView(this);
        title.setText("RE:VE");
        title.setTextColor(accent);
        title.setTextSize(29);
        title.setGravity(Gravity.CENTER);
        errorPanel.addView(title,new LinearLayout.LayoutParams(-1,dp(62)));
        TextView notice=new TextView(this);
        notice.setId(android.R.id.message);
        notice.setText("연결을 확인할 수 없습니다. 인터넷 연결 후 다시 시도해 주세요.");
        notice.setTextColor(Color.WHITE);
        notice.setTextSize(15);
        notice.setGravity(Gravity.CENTER);
        notice.setLineSpacing(dp(5),1f);
        errorPanel.addView(notice,new LinearLayout.LayoutParams(-1,dp(105)));
        Button retry=button("다시 연결");
        retry.setTextSize(17);
        retry.setMinHeight(dp(52));
        retry.setContentDescription("연결 다시 시도");
        retry.setOnClickListener(v->{errorPanel.setVisibility(View.GONE);webView.loadUrl(lastRequested);});
        errorPanel.addView(retry,new LinearLayout.LayoutParams(-1,dp(55)));
        Button browser=button("외부 브라우저에서 열기");
        browser.setTextSize(14);
        browser.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(base)));}catch(ActivityNotFoundException ex){Toast.makeText(this,"브라우저가 없습니다",Toast.LENGTH_SHORT).show();}});
        errorPanel.addView(browser,new LinearLayout.LayoutParams(-1,dp(48)));
        errorPanel.setVisibility(View.GONE);
        contentFrame.addView(errorPanel,new FrameLayout.LayoutParams(-1,-1));
        root.addView(contentFrame,new LinearLayout.LayoutParams(-1,0,1));
        tabBar=new LinearLayout(this);tabBar.setPadding(dp(5),dp(6),dp(5),dp(6));tabBar.setBackgroundColor(Color.rgb(19,18,20));
        for(int i=0;i<paths.length;i++){
            final String path=paths[i];
            Button btn=button(labels[0][i]);tabButtons[i]=btn;
            btn.setTextSize(11);btn.setMinWidth(0);btn.setPadding(0,0,0,0);
            btn.setOnClickListener(v->webView.evaluateJavascript("(function(){try{return localStorage.getItem('reve.language')||'ko'}catch(e){return 'ko'}})()",raw->{
                updateLocale(raw);
                lastRequested=base+path+"?appCatalog="+contentVersion+("ko".equals(currentLocale)?"":"&lang="+currentLocale);
                webView.loadUrl(lastRequested);
            }));
            tabBar.addView(btn,new LinearLayout.LayoutParams(0,dp(48),1));
        }
        shareButton=button("공유");shareButton.setTextSize(11);shareButton.setMinWidth(0);shareButton.setPadding(0,0,0,0);
        shareButton.setOnClickListener(v->{Intent in=new Intent(Intent.ACTION_SEND);in.setType("text/plain");in.putExtra(Intent.EXTRA_TEXT,webView.getUrl()==null?base:webView.getUrl());startActivity(Intent.createChooser(in,"Share RE:VE"));});
        tabBar.addView(shareButton,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(tabBar);setContentView(root);
        Uri launch=getIntent().getData();
        lastRequested=launch!=null&&"reve.lexid.site".equals(launch.getHost())&&"https".equals(launch.getScheme())?launch.toString():base+"/?appCatalog="+contentVersion;
        webView.loadUrl(lastRequested);
    }
    private void showConnectionError(boolean secure){
        mainFrameFailed=true;
        if(errorPanel==null)return;
        TextView notice=errorPanel.findViewById(android.R.id.message);
        if(notice!=null)notice.setText(secure?"보안 연결을 확인할 수 없습니다. 네트워크 또는 기기 시간을 확인해 주세요.":"연결을 확인할 수 없습니다. 인터넷 연결 후 다시 시도해 주세요.");
        errorPanel.setVisibility(View.VISIBLE);
        errorPanel.bringToFront();
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
    @Override protected void onDestroy(){if(webView!=null){webView.stopLoading();webView.destroy();webView=null;}super.onDestroy();}
}
