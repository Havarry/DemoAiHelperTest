package com.example.aihelpertest;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.aihelpertest.utils.HttpUtils;
import com.gzln.goba.AIHelperSdk;
import com.gzln.goba.aihelp.AIHelper;
import com.gzln.goba.baidutts.BDTtsUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private int currQuesNum = 1;
    private ImageView img_help;
    private ImageView img_voice;
    private ImageView img_write;
    private Button btn_send;
    private Button btn_talk;
    private EditText edi_content;
    private ImageView img_speakingAnimation;
    private TextView tv_qa;
    private RelativeLayout speakLayout;
    private TextView tv_cancel;
    private AIHelper aiHelper;
    private BDTtsUtil bdTtsUtil;
    private String postUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //        初始化AI语音助手和百度语音合成TTS
        class CallBackFunsImp implements com.gzln.goba.aihelp.CallBackFuns {
            @Override
            public void onVodInput(String msg) {
                onPostReq(msg);
            }
        }
        CallBackFunsImp funsImp = new CallBackFunsImp();
        aiHelper = new AIHelper(this, funsImp);
        bdTtsUtil = new BDTtsUtil(this);
        initView();
    }

    private void onPostReq(String msg) {
//        msg = URLEncoder.encode(msg);
        postUrl = "http://qa.birdbot.cn:7749/q/"+currQuesNum;
        final Map<String,String> params = new HashMap<String,String>();
        params.put("answer" , msg);
        params.put("json" , "1");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String  result = HttpUtils.submitPostData(postUrl, params, "utf-8");
                    Log.e(TAG, "result: " + result);
                    Message msg = new Message();
                    msg.what = 1;
                    Bundle data = new Bundle();
                    data.putString("msg", result);
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

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    // 初始化全屏方案布局
    private void initView() {
        img_help = findViewById(com.gzln.goba.R.id.img_help);
        img_voice = findViewById(com.gzln.goba.R.id.img_voice);
        img_write = findViewById(com.gzln.goba.R.id.img_write);
        btn_send = findViewById(com.gzln.goba.R.id.btn_send);
        btn_talk = findViewById(com.gzln.goba.R.id.btn_talk);
        speakLayout = findViewById(com.gzln.goba.R.id.speakLayout);
        img_speakingAnimation = findViewById(com.gzln.goba.R.id.img_speakAnimation);
        tv_cancel = findViewById(com.gzln.goba.R.id.tv_cancel);
        edi_content = findViewById(com.gzln.goba.R.id.edi_write);
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
                                img_speakingAnimation.setImageResource(com.gzln.goba.R.drawable.c_82);
                                tv_cancel.setText("松开手指，取消发送");
                                aiHelper.setCancel(isCancel);
                            }
                        } else {
                            if (isCancel) {
                                isCancel = false;
                                img_speakingAnimation.setImageResource(com.gzln.goba.R.drawable.animation_speaking);
                                ((Animatable) img_speakingAnimation.getDrawable()).start();
                                tv_cancel.setText("手指上滑，取消发送");
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isCancel) {
                            img_speakingAnimation.setImageResource(com.gzln.goba.R.drawable.animation_speaking);
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

//        发送按钮监听事件
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edi_content.getText().toString();

                edi_content.getText().clear();
            }
        });

//      帮助按钮点击事件监听
        img_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getHelp();
            }
        });
    }
}
