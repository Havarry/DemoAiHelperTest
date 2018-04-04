package com.example.aihelpertest.activities;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.aihelpertest.R;
import com.example.aihelpertest.utils.HttpUtils;
import com.gzln.goba.AIHelperSdk;
import com.gzln.goba.aihelp.AIHelper;
import com.gzln.goba.aihelp.CallBackFuns;
import com.gzln.goba.baidutts.BDTtsUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private TextView tv_question;
//    private TextView tv_answer;
    private ListView lv_single_choice;
    private List<String> data;
    private TextView tv_selected;
    private Button btn_last;
    private Button btn_skip;
    private Button btn_next;
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
        class CallBackFunsImp implements CallBackFuns {
            @Override
            public void onVodInput(String msg) {
                onPostReq(msg);
            }
        }
        CallBackFunsImp funsImp = new CallBackFunsImp();
        aiHelper = new AIHelper(this, funsImp);
        bdTtsUtil = new BDTtsUtil(this);
        initView();
        data = new ArrayList<>();
        data.add("1、小学及以下");
        data.add("2、 初中");
        data.add("3、 高中(含中专)");
        data.add("4、 大专及以上");
        bdTtsUtil.speak(tv_question.getText().toString());
        delay();
        bdTtsUtil.speak("1、小学及以下\n2、初中\n3、 高中(含中专)\n4、 大专及以上");
        lv_single_choice.setAdapter(new ArrayAdapter<String>(this, R.layout.item_lv_single_choice, data));
        lv_single_choice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int checkedItemPosition = lv_single_choice.getCheckedItemPosition();
                bdTtsUtil.cancle();
                tv_selected.setText("您选择的选项为：" + data.get(checkedItemPosition));
                bdTtsUtil.speak("您选择的选项为：" + data.get(checkedItemPosition));
            }
        });
    }

    private void onPostReq(String msg) {
//        msg = URLEncoder.encode(msg);
        postUrl = "http://qa.birdbot.cn:7749/q/" + currQuesNum;
        final Map<String, String> params = new HashMap<String, String>();
        params.put("answer", msg);
        params.put("json", "1");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = HttpUtils.submitPostData(postUrl, params, "utf-8");
                    Log.e(TAG, "result: " + result);
                    Message msg = new Message();
                    msg.what = 1;
                    Bundle data = new Bundle();
                    data.putString("post", result);
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onGetReq(int curr) {
        final String getUrl = "http://qa.birdbot.cn:7749/q/" + curr+"?json=1";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = HttpUtils.submitGetData(getUrl, "utf-8");
                    Log.e(TAG, "result: " + result);
                    Message msg = new Message();
                    msg.what = 2;
                    Bundle data = new Bundle();
                    data.putString("get", result);
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //    对语音数据进行处理
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle result = msg.getData();
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String postText = result.getString("post");
                    postText = AIHelperSdk.decode(postText);
//                    Log.e(TAG, "handleMessage: " + postText);
                    if (postText !=null){
                        try {
                            JSONObject postObject = new JSONObject(postText);
                            String code = postObject.getString("code");
                            if (code.equals("answer")){
                                tv_selected.setText("您选择的选项为：" + postObject.getString("text"));
                                bdTtsUtil.speak("您选择的选项为：" + postObject.getString("text"));
                            }else {
                                next();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2:
                    String getText = result.getString("get");
                    getText = AIHelperSdk.decode(getText);
                    String answersText = "没明白您说什么，请再说一遍";
//                    Log.e(TAG, "handleMessage: " + postText);
                    try {
                        if (getText!=null){
                            JSONObject jsonObject = null;
                            jsonObject = new JSONObject(getText);
                            String question = jsonObject.getString("Q");
                            JSONArray answers = jsonObject.getJSONArray("A");
                            data.clear();
                            answersText = "";
                            for (int i=0;i<answers.length();i++){
                                int num = i+1;
                                answersText+=num+"、"+answers.getString(i)+"\n";
                                data.add(num+"、"+answers.getString(i));
                            }
                            tv_question.setText("第" + currQuesNum + "题、 " + question);
                            lv_single_choice.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.item_lv_single_choice, data));
                            bdTtsUtil.speak("第" + currQuesNum + "题、 " + question);
                            delay();
                            bdTtsUtil.speak(answersText);
                        }else {
                            bdTtsUtil.speak(answersText);
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
        img_help = findViewById(R.id.img_help);
        img_voice = findViewById(R.id.img_voice);
        img_write = findViewById(R.id.img_write);
        btn_send = findViewById(R.id.btn_send);
        btn_talk = findViewById(R.id.btn_talk);
        speakLayout = findViewById(R.id.speakLayout);
        img_speakingAnimation = findViewById(R.id.img_speakAnimation);
        tv_cancel = findViewById(R.id.tv_cancel);
        edi_content = findViewById(R.id.edi_write);
        tv_question = findViewById(R.id.tv_question);
        lv_single_choice = findViewById(R.id.lv_single_choice);
        tv_selected = findViewById(R.id.tv_selected);
        btn_last = findViewById(R.id.btn_last);
        btn_skip = findViewById(R.id.btn_skip);
        btn_next = findViewById(R.id.btn_next);
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
                bdTtsUtil.cancle();
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
                onPostReq(text);
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

        btn_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                last();
            }
        });
        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });
    }

    private void getHelp() {

    }

    private void last(){
        bdTtsUtil.cancle();
        if (currQuesNum == 1){
            bdTtsUtil.speak("亲，当前已处于调查问卷第1题");
            Toast.makeText(MainActivity.this, "亲，当前已处于调查问卷第1题" ,Toast.LENGTH_SHORT).show();
        }
        else {
            currQuesNum--;
            onGetReq(currQuesNum);
            tv_selected.setText("您选择的选项为：");
        }
    }

    private void next(){
        bdTtsUtil.cancle();
        if (currQuesNum == 10){
            bdTtsUtil.speak("亲，感谢您的参与，问卷已经结束");
            Toast.makeText(MainActivity.this, "感谢您的参与，问卷已经结束" ,Toast.LENGTH_SHORT).show();
        }
        else {
            currQuesNum++;
            onGetReq(currQuesNum);
            tv_selected.setText("您选择的选项为：");
        }
    }

    private void delay(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        bdTtsUtil.cancle();
    }
}
