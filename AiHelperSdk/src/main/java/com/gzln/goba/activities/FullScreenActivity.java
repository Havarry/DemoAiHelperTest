package com.gzln.goba.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gzln.goba.AIHelperSdk;
import com.gzln.goba.R;
import com.gzln.goba.aihelp.AIHelper;
import com.gzln.goba.baidutts.BDTtsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by 火焰科技 on 2017/9/15.
 * AIHelper 全屏方案
 */
public class FullScreenActivity extends Activity {
    private static final String TAG = "MySdkActivity";
    private ImageView img_help;
    private ImageView img_voice;
    private ImageView img_write;
    private Button btn_send;
    private Button btn_talk;
    private ImageView img_back;
    private EditText edi_content;
    private ImageView img_speakingAnimation;
    private RelativeLayout speakLayout;
    private TextView tv_cancel;
    private ProgressBar full_ProgressBar;
    private WebView web_chat;
    private AIHelper aiHelper;
    private String srvUrl, help_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_layout);
        initView();
        setWebView();
        class CallBackFuns implements com.gzln.goba.aihelp.CallBackFuns {
            @Override
            public void onVodInput(String msg) {
                web_chat.loadUrl("javascript:addMessageRight('" + msg + "');");
                onReq(msg);
            }
        }
        CallBackFuns _funs = new CallBackFuns();
        aiHelper = new AIHelper(this, _funs);
    }

    //网络请求后台返回数据
    private void onReq(String msg) {
//        Toast.makeText(FullScreenActivity.this, msg, Toast.LENGTH_SHORT).show();
        msg = URLEncoder.encode(msg);
        srvUrl = AIHelperSdk.getSrvUrl();
        final String httpurl = srvUrl + "?text=" + msg + "&userid=" + AIHelperSdk.getUserId() + "&topic=&orginid=" + AIHelperSdk.getOrginId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(httpurl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setReadTimeout(5 * 1000);
                    conn.connect();
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                    StringBuffer sbf = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        sbf.append(line);
                    }
//                    Log.e(TAG, "result: " + sbf.toString());
                    Message msg = new Message();
                    msg.what = 1;
                    Bundle data = new Bundle();
                    data.putString("msg", sbf.toString());
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                }
            }
        }).start();
    }

    //    对语音数据进行处理
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String text = data.get("msg").toString();
                    text = AIHelperSdk.decode(text);
                    Log.e(TAG, "handleMessage: " + text);
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(text);
                        int state = jsonObject.getInt("state");
                        String dataSpeech = jsonObject.getString("data");
                        if (state == 1) {
                            web_chat.loadUrl("javascript:addMessageLeft('" + dataSpeech + "');");
                        } else if (state == 2) {
                            AIHelperSdk.speak(dataSpeech);
                            web_chat.loadUrl("javascript:addMessageLeft('" + dataSpeech + "');");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    // 初始化全屏方案布局
    private void initView() {
        img_help = (ImageView) findViewById(R.id.img_help);
        img_voice = (ImageView) findViewById(R.id.img_voice);
        img_write = (ImageView) findViewById(R.id.img_write);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_talk = (Button) findViewById(R.id.btn_talk);
        img_back = (ImageView) findViewById(R.id.img_back);
        web_chat = (WebView) findViewById(R.id.wbe_chat);
        full_ProgressBar = (ProgressBar) findViewById(R.id.full_progress);
        speakLayout = (RelativeLayout) findViewById(R.id.speakLayout);
        img_speakingAnimation = (ImageView) findViewById(R.id.img_speakAnimation);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        edi_content = (EditText) findViewById(R.id.edi_write);
        edi_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString() != null) {
                    img_help.setVisibility(View.GONE);
                    btn_send.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = edi_content.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    img_help.setVisibility(View.VISIBLE);
                    btn_send.setVisibility(View.GONE);
                } else {
                    img_help.setVisibility(View.GONE);
                    btn_send.setVisibility(View.VISIBLE);
                }
            }
        });
        //        从语音输入切换到打字输入
        img_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_write.setVisibility(View.GONE);
                img_voice.setVisibility(View.VISIBLE);
                btn_talk.setVisibility(View.GONE);
                edi_content.setVisibility(View.VISIBLE);
            }
        });

//        从打字输入切换到语音输入
        img_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(edi_content.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                img_voice.setVisibility(View.GONE);
                img_write.setVisibility(View.VISIBLE);
                btn_talk.setVisibility(View.VISIBLE);
                edi_content.setVisibility(View.GONE);
            }
        });

//        为按住说话语音输入设置监听器
        btn_talk.setOnTouchListener(new View.OnTouchListener() {
            int lastY = 0;
            int distanceY = 0;
            boolean isCancel = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastY = (int) event.getRawY();
                        isCancel = false;
                        speakLayout.setVisibility(View.VISIBLE);
                        ((Animatable) img_speakingAnimation.getDrawable()).start();
                        aiHelper.start();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        distanceY = (int) (lastY - event.getRawY());
                        if (distanceY > 100) {
                            if (!isCancel) {
                                isCancel = true;
                                img_speakingAnimation.setImageResource(R.drawable.c_82);
                                tv_cancel.setText("松开手指，取消发送");
                                aiHelper.setCancel(isCancel);
                            }
                        } else {
                            if (isCancel) {
                                isCancel = false;
                                img_speakingAnimation.setImageResource(R.drawable.animation_speaking);
                                ((Animatable) img_speakingAnimation.getDrawable()).start();
                                tv_cancel.setText("手指上滑，取消发送");
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isCancel) {
                            img_speakingAnimation.setImageResource(R.drawable.animation_speaking);
                            tv_cancel.setText("手指上滑，取消发送");
                            speakLayout.setVisibility(View.GONE);
                            aiHelper.stop();
                            isCancel = false;
                            aiHelper.setCancel(isCancel);
                            return false;
                        } else {
                            speakLayout.setVisibility(View.GONE);
                            aiHelper.setCancel(isCancel);
                            aiHelper.stop();
                            break;
                        }
                }
                return false;
            }
        });

//        返回监听事件
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        发送按钮监听事件
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edi_content.getText().toString();
                onReq(text);
                web_chat.loadUrl("javascript:addMessageRight('" + text + "');");
                edi_content.getText().clear();
            }
        });

//      帮助按钮点击事件监听
        img_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHelp();
            }
        });
    }

    //     WebView的设置
    private void setWebView() {
        //启用支持JavaScript
        web_chat.getSettings().setJavaScriptEnabled(true);
        //启用支持DOM Storage
        web_chat.getSettings().setDomStorageEnabled(true);
        //加载web资源
        long t = new Date().getTime();
        web_chat.loadUrl("http://bi.birdbot.cn:15180/hy.html?t=" + t);
        web_chat.setWebChromeClient(new WebChromeClient() {//监听网页加载
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    // 网页加载完成
                    full_ProgressBar.setVisibility(View.GONE);
                } else {
                    if (full_ProgressBar.getVisibility() == View.GONE) {
                        full_ProgressBar.setVisibility(View.VISIBLE);
                    }
                    // 加载中
                    full_ProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        web_chat.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                web_chat.loadUrl("javascript:addMessageLeft('" + "请问有什么可以帮到您呢" + "');");
                super.onPageFinished(view, url);
            }
        });
        //自适应屏幕
        web_chat.getSettings().setUseWideViewPort(true);
        web_chat.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        web_chat.getSettings().setSupportZoom(true);
        web_chat.setInitialScale(100);
    }

    private void getHelp() {
        String themeName = "", help = "";
        String[] caseLists = null;
        String name = AIHelperSdk.getThemeName();
        name = URLEncoder.encode(name);
        final String helpUrl = "http://54.223.180.3:8091/help/queryHelpScene.do?orgId=1&name=" + name;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(helpUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.connect();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    StringBuffer stringBuffer = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuffer.append(line);
                    }
                    help_data = stringBuffer.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        if (help_data != null) {
            try {
                JSONObject all = new JSONObject(help_data);
                JSONObject data = all.has("data") ? all.getJSONObject("data") : null;
                themeName = data.has("sname") ? data.getString("sname") : "";
                JSONArray caseList = data.has("caseList") ? data.optJSONArray("caseList") : null;
                caseLists = new String[caseList.length()];
                for (int i = 0; i < caseList.length(); i++) {
                    JSONObject object = caseList.optJSONObject(i);
                    caseLists[i] = object.has("cname") ? object.getString("cname") : "";
//                    Log.e(TAG, "onCreate:1----" + caseLists[i]);
                    JSONArray contentList = object.has("contentList") ? object.optJSONArray("contentList") : null;
                    for (int j = 0; j < contentList.length(); j++) {
                        JSONObject content = contentList.optJSONObject(j);
                        caseLists[i] += content.has("content") ? "<br>  " + content.getString("content") : "";
//                        Log.e(TAG, "onCreate:2----" + caseLists[i]);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!themeName.equals("") && caseLists != null) {
                AIHelperSdk.speak(themeName + "使用帮助如下");
                for (int i = 0; i < caseLists.length; i++) {
                    help += caseLists[i] + "<br>";
                }
                web_chat.loadUrl("javascript:addMessageLeft('" + themeName + "使用帮助如下：<br>" + help + "');");
            }
        }
    }
}
