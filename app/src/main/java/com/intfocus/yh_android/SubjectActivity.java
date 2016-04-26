package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.TencentUtil;
import com.intfocus.yh_android.util.URLs;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class SubjectActivity extends BaseActivity implements OnPageChangeListener {
    private Boolean isInnerLink;
    private String reportID;
    private PDFView mPDFView;
    private File pdfFile;
    private String bannerName, link;
    private int objectID;
    private int objectType;
    private int groupID;
    private String userNum;
    private RelativeLayout bannerView;
    private IWXAPI wxApi;
    private static final int THUMB_SIZE = 150;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        String APP_ID = "wx5a37b4326f4dd280";
        wxApi = WXAPIFactory.createWXAPI(this, APP_ID, false);
        wxApi.registerApp(APP_ID);

        findViewById(R.id.back).setOnClickListener(mOnBackListener);
        findViewById(R.id.back_text).setOnClickListener(mOnBackListener);

//        mPDFView.setOnTouchListener(this);

        Button shareButton = (Button) findViewById(R.id.share);
        Button drawButton = (Button) findViewById(R.id.draw);

        /*
         * JSON Data
         */
        try {
            groupID = user.getInt("group_id");
            userNum = user.getString("user_num");
        } catch (JSONException e) {
            e.printStackTrace();
            groupID = -2;
            userNum = "mobile-not-get";
        }

        bannerView = (RelativeLayout) findViewById(R.id.actionBar);
        TextView mTitle = (TextView) findViewById(R.id.title);
        mPDFView = (PDFView) findViewById(R.id.pdfview);
        ImageView mComment = (ImageView) findViewById(R.id.comment);
        mComment.setOnClickListener(mOnCommentLister);
        mPDFView.setVisibility(View.INVISIBLE);

        pullToRefreshWebView = (PullToRefreshWebView) findViewById(R.id.webview);
        initRefreshWebView();

        mWebView.requestFocus();
        mWebView.setDrawingCacheEnabled(true);
        pullToRefreshWebView.setVisibility(View.VISIBLE);
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");
        mWebView.loadUrl(urlStringForLoading);

        // 刷新监听事件
        pullToRefreshWebView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<android.webkit.WebView>() {
            @Override
            public void onRefresh(PullToRefreshBase<android.webkit.WebView> refreshView) {
                // 模拟加载任务
                new pullToRefreshTask().execute();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String label = simpleDateFormat.format(System.currentTimeMillis());
                // 显示最后更新的时间
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
            }
        });

        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = captureWebViewVisibleSize(mWebView);
                Drawable drawable = new BitmapDrawable(bitmap);
                DrawView drawView = new DrawView(SubjectActivity.this);
                drawView.setBackground(drawable);
                setContentView(drawView);
                addContentView(drawView.btnEraseAll, drawView.btnEraseParams);
                addContentView(drawView.btnBlue, drawView.btnBlueParams);
                addContentView(drawView.btnRed, drawView.btnRedParams);
                addContentView(drawView.btnSave, drawView.btnSaveParams);
                addContentView(drawView.btnDisplay, drawView.btnDisplayParams);
                addContentView(drawView.btnCancel, drawView.btnCancelParams);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.back);

                WXImageObject imgObj = new WXImageObject(bmp);
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = imgObj;

                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
                bmp.recycle();
                msg.thumbData = TencentUtil.bmpToByteArray(thumbBmp, true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("img");
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneSession;
                wxApi.sendReq(req);
            }
        });

//        wxApi.handleIntent(getIntent(), this);

            /*
             * Intent Data || JSON Data
             */
        Intent intent = getIntent();
        link = intent.getStringExtra("link");

        bannerName = intent.getStringExtra("bannerName");
        objectID = intent.getIntExtra("objectID", -1);
        objectType = intent.getIntExtra("objectType", -1);
        isInnerLink = !(link.startsWith("http://") || link.startsWith("https://"));

        mTitle.setText(bannerName);

        checkInterfaceOrientation(this.getResources().getConfiguration());

        List<ImageView> colorViews = new ArrayList<>();
        colorViews.add((ImageView) findViewById(R.id.colorView0));
        colorViews.add((ImageView) findViewById(R.id.colorView1));
        colorViews.add((ImageView) findViewById(R.id.colorView2));
        colorViews.add((ImageView) findViewById(R.id.colorView3));
        colorViews.add((ImageView) findViewById(R.id.colorView4));

        initColorView(colorViews);
    }

    private Bitmap captureWebViewVisibleSize(WebView webView) {
        Bitmap bmp = Bitmap.createBitmap(webView.getDrawingCache());
        return bmp;
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // 横屏时隐藏标题栏、导航栏
        checkInterfaceOrientation(newConfig);
    }

    private void checkInterfaceOrientation(Configuration config) {
        Boolean isLandscape = (config.orientation == Configuration.ORIENTATION_LANDSCAPE);

        bannerView.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        if (isLandscape) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        dealWithURL();
    }

    private void dealWithURL() {
        if (isInnerLink) {
            reportID = TextUtils.split(link, "/")[3];
            String urlPath = format(link.replace("%@", "%d"), groupID);
            urlString = String.format("%s%s", URLs.HOST, urlPath);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ApiHelper.reportData(mContext, String.format("%d", groupID), reportID);

                    new Thread(mRunnableForDetecting).start();
                }
            }).start();
        } else {
            urlString = link;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (urlString.toLowerCase().endsWith(".pdf")) {
                        new Thread(mRunnableForPDF).start();
                    } else {
                        /*
                         * 外部链接传参: userNum, timestamp
                         */
                        String appendParams = String.format("?user_num=%s&timestamp=%s", userNum, URLs.TimeStamp);

                        if (!urlString.contains("?")) {
                            urlString = String.format("%s%s", urlString, appendParams);
                        } else {
                            urlString = urlString.replace("?", appendParams);
                        }
                        Log.i("OutLink", urlString);
                        mWebView.loadUrl(urlString);
                    }
                }
            });
        }
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private final Handler mHandlerForPDF = new Handler() {
        public void handleMessage(Message message) {

            //Log.i("PDF", pdfFile.getAbsolutePath());
            if (pdfFile.exists()) {
                mPDFView.fromFile(pdfFile)
                        .showMinimap(true)
                        .enableSwipe(true)
                        .swipeVertical(true)
                        .load();
                mWebView.setVisibility(View.INVISIBLE);
                mPDFView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(SubjectActivity.this, "加载PDF失败", Toast.LENGTH_SHORT).show();
            }

        }
    };


    @Override
    public void onPageChanged(int page, int pageCount) {
        Log.i("onPageChanged", String.format("page: %d, count: %d", page, pageCount));
    }

    private final Runnable mRunnableForPDF = new Runnable() {
        @Override
        public void run() {
            String outputPath = String.format("%s/%s/%s.pdf", FileUtil.basePath(mContext), URLs.CACHED_DIRNAME, URLs.MD5(urlString));
            pdfFile = new File(outputPath);
            ApiHelper.downloadFile(mContext, urlString, pdfFile);

            Message message = mHandlerForPDF.obtainMessage();
            mHandlerForPDF.sendMessage(message);
        }
    };

    private final View.OnClickListener mOnCommentLister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("bannerName", bannerName);
            intent.putExtra("objectID", objectID);
            intent.putExtra("objectType", objectType);

            mContext.startActivity(intent);

                /*
                 * 用户行为记录, 单独异常处理，不可影响用户体验
                 */
            try {
                logParams = new JSONObject();
                logParams.put("action", "点击/主题页面/评论");
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private final View.OnClickListener mOnBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SubjectActivity.this.onBackPressed();
        }
    };

    private class pullToRefreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // 如果这个地方不使用线程休息的话，刷新就不会显示在那个 PullToRefreshListView 的 UpdatedLabel 上面

            /*
             *  下拉浏览器刷新时，删除响应头文件，相当于无缓存刷新
             */
            if (isInnerLink) {
                String urlKey;
                if (urlString != null && !urlString.isEmpty()) {
                    urlKey = urlString.contains("?") ? TextUtils.split(urlString, "?")[0] : urlString;
                    ApiHelper.clearResponseHeader(urlKey, assetsPath);
                }
                urlKey = String.format(URLs.API_DATA_PATH, URLs.HOST, groupID, reportID);
                ApiHelper.clearResponseHeader(urlKey, FileUtil.sharedPath(mContext));

                ApiHelper.reportData(mContext, String.format("%d", groupID), reportID);
                new Thread(mRunnableForDetecting).start();


                /*
                 * 用户行为记录, 单独异常处理，不可影响用户体验
                 */
                try {
                    logParams = new JSONObject();
                    logParams.put("action", "刷新/浏览器");
                    logParams.put("obj_title", urlString);
                    new Thread(mRunnableForLogger).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            dealWithURL();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Call onRefreshComplete when the list has been refreshed. 如果没有下面的函数那么刷新将不会停
            pullToRefreshWebView.onRefreshComplete();
        }
    }

    private class JavaScriptInterface extends JavaScriptBase {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void storeTabIndex(final String pageName, final int tabIndex) {
            try {
                String filePath = FileUtil.dirPath(mContext, URLs.CONFIG_DIRNAME, URLs.TABINDEX_CONFIG_FILENAME);

                JSONObject config = new JSONObject();
                if ((new File(filePath).exists())) {
                    String fileContent = FileUtil.readFile(filePath);
                    config = new JSONObject(fileContent);
                }
                config.put(pageName, tabIndex);

                FileUtil.writeFile(filePath, config.toString());
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public int restoreTabIndex(final String pageName) {
            int tabIndex = 0;
            try {
                String filePath = FileUtil.dirPath(mContext, URLs.CONFIG_DIRNAME, URLs.TABINDEX_CONFIG_FILENAME);

                JSONObject config = new JSONObject();
                if ((new File(filePath).exists())) {
                    String fileContent = FileUtil.readFile(filePath);
                    config = new JSONObject(fileContent);
                }
                tabIndex = config.getInt(pageName);
            } catch (JSONException e) {
                //e.printStackTrace();
            }

            return tabIndex < 0 ? 0 : tabIndex;
        }

        @JavascriptInterface
        public void jsException(final String ex) {
            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "JS异常");
                logParams.put("obj_id", objectID);
                logParams.put("obj_type", objectType);
                logParams.put("obj_title", String.format("主题页面/%s/%s", bannerName, ex));
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
