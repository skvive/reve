package site.lexid.reve;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.ColorDrawable;
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
    private final String[][] compactLabels = {
        {"홈","제품","사운드","저널","공유"},
        {"Home","Shop","Lab","News","Share"},
        {"首页","产品","声学","专栏","分享"},
        {"ホーム","製品","音響","記事","共有"}
    };
    private final String[][] connectionLabels = {
        {"연결을 확인할 수 없습니다. 인터넷 연결 후 다시 시도해 주세요.","보안 연결을 확인할 수 없습니다. 네트워크 또는 기기 시간을 확인해 주세요.","다시 연결","외부 브라우저에서 열기"},
        {"Connection unavailable. Check your internet and retry.","Secure connection failed. Check your network or device time.","Retry","Open in browser"},
        {"连接失败。请检查网络后重试。","安全连接失败。请检查网络或设备时间。","重试","在浏览器中打开"},
        {"接続できません。通信状況を確認して再試行してください。","安全な接続に失敗しました。通信状況や端末の時刻をご確認ください。","再試行","ブラウザで開く"}
    };
    private final Button[] tabButtons = new Button[4];
    private TextView errorNotice;
    private Button retryButton;
    private Button browserButton;
    private boolean secureError = false;
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
        errorNotice=notice;
        notice.setId(android.R.id.message);
        notice.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        notice.setText("연결을 확인할 수 없습니다. 인터넷 연결 후 다시 시도해 주세요.");
        notice.setTextColor(Color.WHITE);
        notice.setTextSize(15);
        notice.setGravity(Gravity.CENTER);
        notice.setLineSpacing(dp(5),1f);
        errorPanel.addView(notice,new LinearLayout.LayoutParams(-1,dp(105)));
        Button retry=button("다시 연결");
        retryButton=retry;
        retry.setTextSize(17);
        retry.setMinHeight(dp(52));
        retry.setContentDescription("연결 다시 시도");
        retry.setOnClickListener(v->{errorPanel.setVisibility(View.GONE);webView.loadUrl(lastRequested);});
        errorPanel.addView(retry,new LinearLayout.LayoutParams(-1,dp(55)));
        Button browser=button("외부 브라우저에서 열기");
        browserButton=browser;
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
            btn.setTextSize(12);btn.setMinWidth(0);btn.setMinHeight(dp(48));btn.setPadding(dp(2),0,dp(2),0);
            btn.setGravity(Gravity.CENTER);btn.setMaxLines(2);
            btn.setOnClickListener(v->webView.evaluateJavascript("(function(){try{return localStorage.getItem('reve.language')||'ko'}catch(e){return 'ko'}})()",raw->{
                updateLocale(raw);
                lastRequested=base+path+"?appCatalog="+contentVersion+("ko".equals(currentLocale)?"":"&lang="+currentLocale);
                webView.loadUrl(lastRequested);
            }));
            tabBar.addView(btn,new LinearLayout.LayoutParams(0,dp(48),1));
        }
        shareButton=button("공유");shareButton.setTextSize(12);shareButton.setMinWidth(0);shareButton.setMinHeight(dp(48));shareButton.setPadding(dp(2),0,dp(2),0);shareButton.setGravity(Gravity.CENTER);shareButton.setMaxLines(2);
        shareButton.setOnClickListener(v->{Intent in=new Intent(Intent.ACTION_SEND);in.setType("text/plain");in.putExtra(Intent.EXTRA_TEXT,webView.getUrl()==null?base:webView.getUrl());startActivity(Intent.createChooser(in,"Share RE:VE"));});
        tabBar.addView(shareButton,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(tabBar);setContentView(root);
        updateNavigationSizing(getResources().getConfiguration());
        Uri launch=getIntent().getData();
        String initial=launch!=null&&isOwnHttpsUri(launch)?launch.toString():base+"/?appCatalog="+contentVersion;
        String restored=state==null?null:state.getString("reve.lastUrl");
        lastRequested=restored!=null&&isOwnHttpsUri(Uri.parse(restored))?restored:initial;
        webView.loadUrl(lastRequested);
    }
    private void showConnectionError(boolean secure){
        mainFrameFailed=true;
        secureError=secure;
        if(errorPanel==null)return;
        updateConnectionLocale();
        errorPanel.setVisibility(View.VISIBLE);
        errorPanel.bringToFront();
    }
    private void updateLocale(String raw){
        if(raw!=null&&raw.contains("zh-CN"))currentLocale="zh-CN";
        else if(raw!=null&&raw.contains("ja"))currentLocale="ja";
        else if(raw!=null&&raw.contains("en"))currentLocale="en";
        else currentLocale="ko";
        int k=localeIndex();
        boolean compact=getResources().getConfiguration().fontScale>=1.4f;
        for(int i=0;i<tabButtons.length;i++){
            if(tabButtons[i]!=null){tabButtons[i].setText(compact?compactLabels[k][i]:labels[k][i]);tabButtons[i].setContentDescription(labels[k][i]);}
        }
        if(shareButton!=null){shareButton.setText(compact?compactLabels[k][4]:labels[k][4]);shareButton.setContentDescription(labels[k][4]);}
        updateConnectionLocale();
    }
    private int localeIndex(){return "en".equals(currentLocale)?1:"zh-CN".equals(currentLocale)?2:"ja".equals(currentLocale)?3:0;}
    private void updateConnectionLocale(){
        int k=localeIndex();
        if(errorNotice!=null)errorNotice.setText(connectionLabels[k][secureError?1:0]);
        if(retryButton!=null){retryButton.setText(connectionLabels[k][2]);retryButton.setContentDescription(connectionLabels[k][2]);}
        if(browserButton!=null){browserButton.setText(connectionLabels[k][3]);browserButton.setContentDescription(connectionLabels[k][3]);}
    }
    private void updateNavigationSizing(Configuration config){
        boolean enlarged=config.fontScale>=1.4f;
        int height=dp(enlarged?72:52);
        for(Button button:tabButtons){if(button!=null){LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)button.getLayoutParams();p.height=height;button.setLayoutParams(p);}}
        if(shareButton!=null){LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)shareButton.getLayoutParams();p.height=height;shareButton.setLayoutParams(p);}
        updateLocale(currentLocale);
    }
    private boolean isOwnHttpsUri(Uri uri){return "https".equals(uri.getScheme())&&"reve.lexid.site".equalsIgnoreCase(uri.getHost());}
    @Override public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
        updateNavigationSizing(config);
    }
    @Override protected void onSaveInstanceState(Bundle state){
        String current=webView==null?null:webView.getUrl();
        state.putString("reve.lastUrl",current!=null&&isOwnHttpsUri(Uri.parse(current))?current:lastRequested);
        super.onSaveInstanceState(state);
    }
    private Button button(String label){
        Button b=new Button(this);b.setText(label);b.setTextColor(accent);b.setAllCaps(false);
        GradientDrawable focus=new GradientDrawable();focus.setColor(Color.rgb(45,39,33));focus.setCornerRadius(dp(8));
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused},focus);
        states.addState(new int[]{android.R.attr.state_pressed},focus);
        states.addState(new int[]{},new ColorDrawable(Color.TRANSPARENT));
        b.setBackground(states);return b;
    }
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+.5f);}
    private boolean route(Uri uri){if("https".equals(uri.getScheme())&&"reve.lexid.site".equals(uri.getHost()))return false;
        if("https".equals(uri.getScheme())||"mailto".equals(uri.getScheme())||"tel".equals(uri.getScheme())){try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(ActivityNotFoundException ex){Toast.makeText(this,"No app to open this link",Toast.LENGTH_SHORT).show();}}return true;}
    @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(webView!=null){webView.stopLoading();webView.destroy();webView=null;}super.onDestroy();}
}
