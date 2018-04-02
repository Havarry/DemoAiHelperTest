package com.gzln.goba;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.icu.text.UnicodeSetSpanner;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import com.gzln.goba.activities.FullScreenActivity;
import com.gzln.goba.activities.HelpActivity;
import com.gzln.goba.aihelp.AIHelper;
import com.gzln.goba.aihelp.CallBackFuns;
import com.gzln.goba.aihelp.HyaiCallBack;
import com.gzln.goba.baidutts.BDTtsUtil;
import com.gzln.goba.utils.MyToast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by 火焰科技 on 2017/9/15.
 * AIHelper SDK 接口
 */

public class AIHelperSdk {
    private static final String TAG = "AIHelperSdk";
    public static final int FULL_MODE = 1;  //  全屏方案参数
    public static final int HALF_MODE = 2;  //  半屏方案参数
    public static final int NONE_MODE = 3;  //  无屏方案参数
    public static final int LOGO_LARGE = 4;  // 大图标
    public static final int LOGO_MEDIUM = 5; // 中图标
    public static final int LOGO_SMALL = 6;  // 小图标
    public static final int USE_TTS = 0;    //  使用百度语音合成TTS参数
    public static final int NOT_USE_TTS = -1;   //  不使用百度语音合成TTS参数
    private static int x1, x2, y1, y2;   // x1，y1按钮down时的坐标，x2，y2按钮up时的坐标
    private static int lastX, lastY;    // 移动按钮移动之前的位置
    private static int flag = 0;     //判断悬浮按钮是移动还是长按事件
    private static Context sContext;
    private static ViewGroup sViewGroup;
    private static int sMode;
    private static boolean sIsTranslucent;
    private static int sLogoSize;
    private static String sUserId;
    private static String sOrginid;
    private static String sToken;
    private static int sIsPlayVoice;
    private static HyaiCallBack _funs;
    private static HyaiListenCallBack _listenfuns = null;
    private static AIHelper aiHelper;
    private static BDTtsUtil bdTtsUtil;
    private static MyToast myToast;
    private static DisplayMetrics mDisplayMetrics;
    private static int screenWidth;
    private static int screenHeight;
    private static Button btn_FloatLogo;
    private static Button btn_FixedLogo;
    private static ImageView img_floatWindow;
    private static boolean isFloat = true, isFirst = true;
    private static ViewGroup halfLayout;
    private static ImageView half_img_voice;
    private static ImageView half_img_write;
    private static Button half_btn_talk;
    private static EditText half_edi_content;
    private static ImageView half_img_help;
    private static Button half_btn_send;
    private static WebView half_web_chat;
    private static RelativeLayout half_chat_web;
    private static ViewGroup halfAnimationLayout;
    private static ImageView half_img_speakAnimation;
    private static TextView half_tv_cancel;
    private static ProgressBar half_ProgressBar;
    private static boolean isShowHalfScreen = true;
    //定义浮动窗口布局
    private static WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    private static WindowManager mWindowManager;
    private static String srvUrl;
    private static String themeName;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static String helpData;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
//                    FloatButton长按启动语音
                    bdTtsUtil.cancle();
                    halfAnimationLayout.setVisibility(View.VISIBLE);
                    ((Animatable) half_img_speakAnimation.getDrawable()).start();
                    aiHelper.start();
                    break;
                case 2:
//                    读取语音转化成的字符串并回调
                    String text = data.get("sentence").toString();
                    text = decode(text);
                    Log.e(TAG, "handleMessage: " + text);
                    _funs.onCommand(text);
//                    try {
//                        JSONObject jsonObject = new JSONObject(text);
//                        int state = jsonObject.getInt("state");
//                        String dataSpeech = jsonObject.getString("data");
//                        if (state == 1) {
//                            _funs.onCommand(text);
//                            long returnDataTime = System.currentTimeMillis();
//                            PrintStepTime.print(returnDataTime, 3, "");
//                        } else if (state == 2) {
//                            if (sIsPlayVoice == USE_TTS) {
//                                 bdTtsUtil.speak(dataSpeech);
//                                half_web_chat.loadUrl("javascript:addMessageLeft('" + dataSpeech + "');");
//                                long returnDataTime = System.currentTimeMillis();
//                                PrintStepTime.print(returnDataTime, 3, dataSpeech);
//                            }
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    break;
                case 3:
                    if (isNetworkAvailable(sContext.getApplicationContext())) {
                        bdTtsUtil.cancle();
                        if (myToast != null) {
                            myToast.cancel();
                            showToast();
                        } else {
                            showToast();
                        }
                        aiHelper.start();
                    } else {
                        Toast.makeText(sContext, "没有检测到网络", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 4:
                    String toast = data.get("toast").toString();
                    if (myToast != null) {
                        myToast.cancel();
                        showToastText(toast);
                    } else {
                        showToastText(toast);
                    }
                    showToastText(toast);
                    break;
            }
        }
    };

    private static void showToast() {
        myToast = null;
        myToast = new MyToast(sContext);
        View view = LayoutInflater.from(sContext).inflate(R.layout.window_animation, null);
        ImageView image = (ImageView) view.findViewById(R.id.window_img_speakAnimation);
        image.setImageResource(R.drawable.animation_speaking);
        ((Animatable) image.getDrawable()).start();
        //设置Toast的显示位置,后两个参数是偏移量
        myToast.setGravity(Gravity.CENTER, 0, 0);
        myToast.setView(view);
        myToast.show(0);
    }

    private static void showToastText(String toast) {
        myToast = null;
        myToast = new MyToast(sContext);
        myToast.setText(toast);
        myToast.setDuration(1500);
        myToast.show(500);
    }

    /**
     * 检测当的网络
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 初始化可移动按钮方案
     *
     * @param context       上下文对象
     * @param viewGroup     上下文对象的父布局
     * @param mode          屏幕方案
     * @param isTranslucent 是否为沉浸式布局
     * @param logoSize      logo鸟的尺寸
     * @param userId
     * @param orginId
     * @param token
     * @param isPlayVoice   是否使用百度语音合成  AIHelper.TTS USE_TTS or AIHelper.NOT_USE_TTS
     * @param funs          回调函数
     */
    public static void initFloatButton(Context context, ViewGroup viewGroup, int mode, boolean isTranslucent, int logoSize, String userId, String orginId, String token, int isPlayVoice, HyaiCallBack funs) {
        sContext = context;
        sViewGroup = viewGroup;
        sMode = mode;
        sIsTranslucent = isTranslucent;
        sLogoSize = logoSize;
        sUserId = userId;
        sOrginid = orginId;
        sToken = token;
        sIsPlayVoice = isPlayVoice;
        _funs = funs;
        mDisplayMetrics = sContext.getResources().getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        if (sIsTranslucent) {
            screenHeight = mDisplayMetrics.heightPixels;
        } else {
            screenHeight = mDisplayMetrics.heightPixels - getStatusBarHeight();
        }
//        初始化AI语音助手和百度语音合成TTS
        class CallBackFunsImp implements CallBackFuns {
            @Override
            public void onVodInput(String msg) {
                onReq(msg);
            }
        }
        CallBackFunsImp funsImp = new CallBackFunsImp();
        aiHelper = new AIHelper(sContext, funsImp);
        bdTtsUtil = new BDTtsUtil(sContext);

//        设置悬浮按钮的属性
        btn_FloatLogo = new Button(sContext);
        setFloatButtonParams(btn_FloatLogo);

//        按住说话动画效果位置参数设置
        halfAnimationLayout = (ViewGroup) LayoutInflater.from(sContext).inflate(R.layout.animation_speak_layout, null);
        setHalfAnimationParams(halfAnimationLayout);
        sViewGroup.addView(btn_FloatLogo);
        sViewGroup.addView(halfAnimationLayout);

//                悬浮按钮方案点击事件
        btn_FloatLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sMode == FULL_MODE) {
                    sContext.startActivity(new Intent(sContext, FullScreenActivity.class));
                } else if (sMode == HALF_MODE) {
                    if (isShowHalfScreen) {
                        isFloat = true;
                        initHalfView(isFloat);
                        sViewGroup.addView(halfLayout);
                        btn_FloatLogo.bringToFront();
                        isShowHalfScreen = false;
                    } else {
                        halfLayout.setVisibility(View.GONE);
                        isShowHalfScreen = true;
                    }
                } else if (sMode == NONE_MODE) {
                    String themeName = AIHelperSdk.getThemeName();
                    themeName = URLEncoder.encode(themeName);
                    final String helpUrl = "http://54.223.180.3:8091/help/queryHelpScene.do?orgId=1&name=" + themeName;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(helpUrl);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setReadTimeout(5000);
                                connection.connect();
                                InputStream inputStream = connection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                                StringBuffer stringBuffer = new StringBuffer();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    stringBuffer.append(line);
                                }
                                Intent intent = new Intent(sContext, HelpActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("data", stringBuffer.toString());
                                intent.putExtras(bundle);
                                sContext.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }

        });
        btn_FloatLogo.setOnTouchListener(new View.OnTouchListener() {
            boolean isIntercept = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        long down_Time = System.currentTimeMillis();
//                        Log.e(TAG, "onTouch: " + down_Time);
                        PrintStepTime.print(down_Time, 1, "");
                        lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                        lastY = (int) event.getRawY();
                        x1 = (int) event.getRawX();//得到相对应屏幕左上角的坐标
                        y1 = (int) event.getRawY();
                        flag = 0;
//                        当按住bnt_logo超过400ms时调用讯飞语音
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(150);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (flag == 0) {
                                    Message msg = new Message();
                                    msg.what = 1;
                                    Bundle data = new Bundle();
                                    msg.setData(data);
                                    mHandler.sendMessage(msg);
                                    flag = 2;
                                }
                            }
                        }).start();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;

//                        当按钮处于长按时，判断上滑取消
                        if (flag == 2) {
//                            Log.e("长按事件", "onTouch: 上滑取消");
                            if (Math.abs(dy) > 100) {
                                half_img_speakAnimation.setImageResource(R.drawable.c_82);
                                half_tv_cancel.setText("松开手指，取消发送");
                                aiHelper.setCancel(true);

                            } else {
                                aiHelper.setCancel(false);
                                half_img_speakAnimation.setImageResource(R.drawable.animation_speaking);
                                ((Animatable) half_img_speakAnimation.getDrawable()).start();
                                half_tv_cancel.setText("手指上滑，取消发送");
                            }
                        }

//                        当按钮按下且在0—400ms内时若按钮移动时则将flag设为1，此时不调用讯飞语音
                        if ((Math.abs(dx) > 10 || Math.abs(dy) > 10) && flag == 0) {
                            flag = 1;
                        }

//                        当按钮处于移动状态时调用此方法
                        if (flag < 2) {
                            int l = v.getLeft() + dx;
                            int t = v.getTop() + dy;
                            int r = v.getRight() + dx;
                            int b = v.getBottom() + dy;
                            // 下面判断移动是否超出屏幕
                            if (l < 0) {
                                l = 0;
                                r = l + v.getWidth();
                            }
                            if (t < 0) {
                                t = 0;
                                b = t + v.getHeight();
                            }
                            if (r > screenWidth) {
                                r = screenWidth;
                                l = r - v.getWidth();
                            }
                            if (b > screenHeight) {
                                b = screenHeight;
                                t = b - v.getHeight();
                            }
                            v.layout(l, t, r, b);
                            lastX = (int) event.getRawX();
                            lastY = (int) event.getRawY();
                            v.postInvalidate();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
//                        当按钮处于长按输入语音时，UP时关闭语音
                        if (flag == 2) {
                            aiHelper.stop();
                            aiHelper.setCancel(false);
                            half_img_speakAnimation.setImageResource(R.drawable.animation_speaking);
                            half_tv_cancel.setText("手指上滑，取消发送");
                            halfAnimationLayout.setVisibility(View.GONE);
                            isIntercept = true;
                        } else {
                            flag = 1;
                            x2 = (int) event.getRawX();
                            y2 = (int) event.getRawY();
                            double distance = Math.sqrt(Math.abs(x1 - x2) * Math.abs(x1 - x2) + Math.abs(y1 - y2) * Math.abs(y1 - y2));//两点之间的距离
//                            Log.i("i", "x1 - x2>>>>>>" + distance);
                            if (distance < 10) { // 距离较小，当作click事件来处理
                                isIntercept = false;
                            } else {
                                isIntercept = true;
                            }
                        }
//                        当界面刷新时，移动的按钮回到原来的地方
                        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        buttonParams.leftMargin = v.getLeft();
                        if (sIsTranslucent) {
                            buttonParams.topMargin = v.getTop() - getStatusBarHeight();
                        } else {
                            buttonParams.topMargin = v.getTop();
                        }
                        v.setLayoutParams(buttonParams);
                        break;
                }
                return isIntercept;
            }
        });
    }

    /**
     * 初始化固定按钮方案
     * <p>
     * //     * @param context     上下文对象
     * //     * @param viewGroup   上下文对象的父布局
     * //     * @param mode        屏幕方案 FULL_SCREEN or HALF_SCREEEN
     * //     * @param userId      用户ID
     * //     * @param orginId
     * //     * @param token
     * //     * @param isPlayVoice 是否使用百度语音合成TTS
     * //     * @param funs        回调函数
     */
    public static void initFixedButton(Context context, ViewGroup viewGroup, int mode, int logoSize, String userId, String orginId, String token, int isPlayVoice, HyaiCallBack funs) {
        sContext = context;
        sViewGroup = viewGroup;
        sMode = mode;
        sLogoSize = logoSize;
        sUserId = userId;
        sOrginid = orginId;
        sToken = token;
        sIsPlayVoice = isPlayVoice;
        _funs = funs;
        mDisplayMetrics = sContext.getResources().getDisplayMetrics();

//        初始化AI助手
        class CallBackFunsImp implements CallBackFuns {
            @Override
            public void onVodInput(String msg) {
                onReq(msg);
            }
        }
        CallBackFunsImp funsImp = new CallBackFunsImp();
        aiHelper = new AIHelper(sContext, funsImp);
        bdTtsUtil = new BDTtsUtil(sContext);

//        设置悬浮按钮的属性
        btn_FixedLogo = new Button(sContext);
        setFixedButtonParams(btn_FixedLogo);

//         按住说话动画效果位置参数设置
        halfAnimationLayout = (ViewGroup) LayoutInflater.from(sContext).inflate(R.layout.animation_speak_layout, null);
        setHalfAnimationParams(halfAnimationLayout);

        sViewGroup.addView(btn_FixedLogo);
        sViewGroup.addView(halfAnimationLayout);

        //        固定按钮方案点击事件
        btn_FixedLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sMode == FULL_MODE) {
                    sContext.startActivity(new Intent(sContext, FullScreenActivity.class));
                } else if (sMode == HALF_MODE) {
                    if (isShowHalfScreen) {
                        isFloat = false;
                        initHalfView(isFloat);
                        sViewGroup.addView(halfLayout);
                        btn_FixedLogo.bringToFront();
                        isShowHalfScreen = false;
                    } else {
                        halfLayout.setVisibility(View.GONE);
                        isShowHalfScreen = true;
                    }
                } else if (sMode == NONE_MODE) {
                    Log.i(TAG, "加载无屏方案");
                }
            }
        });
        btn_FixedLogo.setOnTouchListener(new View.OnTouchListener() {
            boolean isIntercept = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                        lastY = (int) event.getRawY();
                        x1 = (int) event.getRawX();//得到相对应屏幕左上角的坐标
                        y1 = (int) event.getRawY();
                        flag = 0;
//                       当按住bnt_logo超过400ms时调用讯飞语音
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(150);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (flag == 0) {
                                    Message msg = new Message();
                                    msg.what = 1;
                                    Bundle data = new Bundle();
                                    msg.setData(data);
                                    mHandler.sendMessage(msg);
                                    flag = 2;
                                }
                            }
                        }).start();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;

//                        当按钮处于长按时，判断上滑取消
                        if (flag == 2) {
                            Log.e("长按事件", "onTouch: 上滑取消");
                            if (Math.abs(dy) > 100) {
                                half_img_speakAnimation.setImageResource(R.drawable.c_82);
                                half_tv_cancel.setText("松开手指，取消发送");
                                aiHelper.setCancel(true);

                            } else {
                                aiHelper.setCancel(false);
                                half_img_speakAnimation.setImageResource(R.drawable.animation_speaking);
                                ((Animatable) half_img_speakAnimation.getDrawable()).start();
                                half_tv_cancel.setText("手指上滑，取消发送");
                            }
                        }
//                        当按钮按下且在0—400ms内时若按钮移动时则将flag设为1，此时不调用讯飞语音
                        if ((Math.abs(dx) > 10 || Math.abs(dy) > 10) && flag == 0) {
                            flag = 1;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
//                        当按钮处于长按输入语音时，UP时关闭语音
                        if (flag == 2) {
                            aiHelper.stop();
                            aiHelper.setCancel(false);
                            half_img_speakAnimation.setImageResource(R.drawable.animation_speaking);
                            half_tv_cancel.setText("手指上滑，取消发送");
                            halfAnimationLayout.setVisibility(View.GONE);
                            isIntercept = true;
                        } else {
                            flag = 1;
                            x2 = (int) event.getRawX();
                            y2 = (int) event.getRawY();
                            double distance = Math.sqrt(Math.abs(x1 - x2) * Math.abs(x1 - x2) + Math.abs(y1 - y2) * Math.abs(y1 - y2));//两点之间的距离
                            Log.i("i", "x1 - x2>>>>>>" + distance);
                            if (distance < 10) { // 距离较小，当作click事件来处理
                                isIntercept = false;
                            } else {
                                isIntercept = true;
                            }
                        }
                        break;
                }
                return isIntercept;
            }
        });
    }

    public static void setLinstenCallback(HyaiListenCallBack funs) {
        _listenfuns = funs;
    }

    public static void initFloatWindow(Context context, String userId, String orginId, String token, int isPlayVoice, HyaiCallBack funs) {
        sContext = context;
//        sLogoSize = logoSize;
        sUserId = userId;
        sOrginid = orginId;
        sToken = token;
        sIsPlayVoice = isPlayVoice;
        _funs = funs;
        mDisplayMetrics = sContext.getResources().getDisplayMetrics();
        class CallBackFunsImp implements CallBackFuns {
            @Override
            public void onVodInput(String msg) {
                onReq(msg);
            }
        }
        CallBackFunsImp funsImp = new CallBackFunsImp();
        aiHelper = new AIHelper(sContext, funsImp);
        bdTtsUtil = new BDTtsUtil(sContext);
        if (img_floatWindow == null) {
            createFloatView();
        }
    }

    private static void createFloatView() {
        int screenWidth = mDisplayMetrics.widthPixels;
        int screenHeight = mDisplayMetrics.heightPixels;
        wmParams = new WindowManager.LayoutParams();
        //通过getApplication获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) sContext.getApplicationContext().getSystemService(WINDOW_SERVICE);
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.START | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = screenWidth / 4 * 3;
        wmParams.y = screenHeight / 4 * 3;
        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        img_floatWindow = new ImageView(sContext);
        img_floatWindow.setImageResource(R.drawable.icon_logo_l);
        mWindowManager.addView(img_floatWindow, wmParams);

        //设置监听浮动窗口的触摸移动
        img_floatWindow.setOnTouchListener(new View.OnTouchListener() {
            boolean isClick;
            int lastX;
            int lastY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isClick = false;
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        flag = 0;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(150);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (flag == 0) {
                                    Message msg = new Message();
                                    msg.what = 3;
                                    Bundle data = new Bundle();
                                    msg.setData(data);
                                    mHandler.sendMessage(msg);
                                    flag = 2;
                                }
                            }
                        }).start();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        isClick = true;
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;

                        if ((Math.abs(dx) > 30 || Math.abs(dy) > 30) && flag == 0) {
                            flag = 1;
//                            Toast.makeText(sContext,""+dx+"---dy:"+dy,Toast.LENGTH_SHORT).show();

                        }
                        if (flag == 1) {
                            // getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                            wmParams.x = (int) event.getRawX()
                                    - img_floatWindow.getMeasuredWidth() / 2;
                            // 减去状态栏的高度
                            wmParams.y = (int) event.getRawY()
                                    - img_floatWindow.getMeasuredHeight() / 2 - getStatusBarHeight();
                            // 刷新
                            mWindowManager.updateViewLayout(img_floatWindow, wmParams);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        boolean isCancel = false;
                        if (flag == 2) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            aiHelper.stop();
                            isCancel = myToast.cancel();
                        } else {
                            flag = 1;
                        }
                        if (isCancel) {
                            myToast.cancel();
                        }
                        break;
                    default:
                        break;
                }
                return isClick;
            }
        });
    }

    //网络请求后台返回数据
    public static void onReq(String msg) {
        if (_listenfuns != null) {
            msg = _listenfuns.onListen(msg);
        }
//        Toast.makeText(sContext, msg, Toast.LENGTH_SHORT).show();
//        final String finalMsg = msg;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Message toast = new Message();
//                toast.what = 4;
//                Bundle data = new Bundle();
//                data.putString("toast", finalMsg);
//                toast.setData(data);
//                mHandler.sendMessage(toast);
//            }
//        }).start();
        long returnVoice = System.currentTimeMillis();
        PrintStepTime.print(returnVoice, 2, msg);
        msg = URLEncoder.encode(msg);
        srvUrl = getSrvUrl();
//        setSrvUrl("http://bi.birdbot.cn:8184/req");
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
                    Message msg = new Message();
                    msg.what = 2;
                    Bundle data = new Bundle();
                    data.putString("sentence", sbf.toString());
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                }
            }
        }).start();
    }

//    PDA 网络请求后台返回数据
//    public static void onReq(String msg) {
//        if (_listenfuns != null) {
//            msg = _listenfuns.onListen(msg);
//        }
////        Toast.makeText(sContext, msg, Toast.LENGTH_SHORT).show();
//        final String httpurl0 = srvUrl.replace("/req","/isen") + "?text=" + URLEncoder.encode(msg);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    URL url = new URL(httpurl0);
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setRequestMethod("GET");
//                    conn.setReadTimeout(5 * 1000);
//                    conn.connect();
//                    InputStream in = conn.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
//                    StringBuffer sbf = new StringBuffer();
//                    String line = "";
//                    while ((line = reader.readLine()) != null) {
//                        sbf.append(line);
//                    }
//                    String imsg = sbf.toString();
//
//                    final String finalMsg = imsg;
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Message toast = new Message();
//                            toast.what = 4;
//                            Bundle data = new Bundle();
//                            data.putString("toast", finalMsg);
//                            toast.setData(data);
//                            mHandler.sendMessage(toast);
//                        }
//                    }).start();
//                    long returnVoice = System.currentTimeMillis();
//                    PrintStepTime.print(returnVoice, 2, imsg);
//                    imsg = URLEncoder.encode(imsg);
//
//                    final String httpurl = srvUrl + "?text=" + imsg + "&userid=" + AIHelperSdk.getUserId() + "&topic=&orginid=" + AIHelperSdk.getOrginId();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                URL url = new URL(httpurl);
//                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                                conn.setRequestMethod("GET");
//                                conn.setReadTimeout(5 * 1000);
//                                conn.connect();
//                                InputStream in = conn.getInputStream();
//                                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
//                                StringBuffer sbf = new StringBuffer();
//                                String line = "";
//                                while ((line = reader.readLine()) != null) {
//                                    sbf.append(line);
//                                }
//                                Message msg = new Message();
//                                msg.what = 2;
//                                Bundle data = new Bundle();
//                                data.putString("sentence", sbf.toString());
//                                msg.setData(data);
//                                mHandler.sendMessage(msg);
//                            } catch (Exception e) {
//                            }
//                        }
//                    }).start();
//                } catch (Exception e) {
//                }
//            }
//        }).start();
//    }

    private static class PrintStepTime {
        public static void print(long time, int step, String msg) {
            msg = URLEncoder.encode(msg);
            final String httptime = "http://vrai.birdbot.cn:8082/stat?wid=" + AIHelperSdk.getOrginId() + "_" + AIHelperSdk.getUserId() + "_" + time + "&step=" + step + "&msg=" + msg;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(httptime);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setReadTimeout(5 * 1000);
                        connection.connect();
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                        StringBuffer sbf = new StringBuffer();
                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            sbf.append(line);
                        }
//                        Log.e("PrintStepTime", "run: " + sbf.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void sync(String data) {
        final String tdata = URLEncoder.encode(data);
        final String httpurl = "http://bi.birdbot.cn:8184/sync";//?data=" + data + "&userid=" + AIHelperSdk.getUserId() + "&orginid=" + AIHelperSdk.getToken();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(httpurl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setReadTimeout(5 * 1000);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.connect();
                    PrintWriter printWriter = new PrintWriter(conn.getOutputStream());
                    // 发送请求参数
                    printWriter.write("data=" + tdata + "&userid=" + AIHelperSdk.getUserId() + "&orginid=" + AIHelperSdk.getToken());//post的参数 xx=xx&yy=yy
                    // flush输出流的缓冲
                    printWriter.flush();

                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                    StringBuffer sbf = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        sbf.append(line);
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }

    /**
     * 获取手机状态栏高度
     *
     * @return 状态栏高度
     */
    private static int getStatusBarHeight() {
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = sContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = sContext.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 获取底部导航栏高度
     *
     * @return
     */
    public static int getNavigationBarHeight() {
        int navigationHeight = 0;
        Resources resources = sContext.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        //获取NavigationBar的高度
        navigationHeight = resources.getDimensionPixelSize(resourceId);
        return navigationHeight;
    }

    //    初始化长按说话动画布局
    private static void setHalfAnimationParams(ViewGroup halfAnimationLayout) {
        RelativeLayout.LayoutParams halfAnimationParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        halfAnimationParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        halfAnimationLayout.setLayoutParams(halfAnimationParams);
        half_img_speakAnimation = (ImageView) halfAnimationLayout.findViewById(R.id.half_img_speakAnimation);
        half_tv_cancel = (TextView) halfAnimationLayout.findViewById(R.id.half_tv_cancel);
    }

    // 设置悬浮按钮位置
    private static void setFloatButtonParams(Button btn_floatLogo) {
        if (sLogoSize == LOGO_LARGE) {
            btn_floatLogo.setBackgroundResource(R.drawable.icon_logo_l);
        } else if (sLogoSize == LOGO_MEDIUM) {
            btn_floatLogo.setBackgroundResource(R.drawable.icon_logo_m);
        } else if (sLogoSize == LOGO_SMALL) {
            btn_floatLogo.setBackgroundResource(R.drawable.icon_logo_s);
        }
        btn_floatLogo.setMinHeight(0);
        btn_floatLogo.setMinWidth(0);
        btn_floatLogo.setId(R.id.btn_FloatLogo);
        RelativeLayout.LayoutParams paramsFloat = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsFloat.leftMargin = mDisplayMetrics.widthPixels / 4 * 3;
        paramsFloat.topMargin = mDisplayMetrics.heightPixels / 4 * 3;
        btn_floatLogo.setLayoutParams(paramsFloat);
    }

    //设置固定按钮位置
    private static void setFixedButtonParams(Button btn_fixedLogo) {
        if (sLogoSize == LOGO_LARGE) {
            btn_fixedLogo.setBackgroundResource(R.drawable.icon_logo_l);
        } else if (sLogoSize == LOGO_MEDIUM) {
            btn_fixedLogo.setBackgroundResource(R.drawable.icon_logo_m);
        } else if (sLogoSize == LOGO_SMALL) {
            btn_fixedLogo.setBackgroundResource(R.drawable.icon_logo_s);
        }
        btn_fixedLogo.setMinHeight(0);
        btn_fixedLogo.setMinWidth(0);
        btn_fixedLogo.setId(R.id.btn_FixedLogo);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_fixedLogo.setLayoutParams(params);
    }

    // 初始化半屏布局
    private static void initHalfView(boolean isfloat) {
        halfLayout = (ViewGroup) LayoutInflater.from(sContext).inflate(R.layout.half_screen_layout, null);
//             半屏halfLayout位置参数设置
        RelativeLayout.LayoutParams halfLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (isfloat) {
            halfLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else {
            halfLayoutParams.addRule(RelativeLayout.ABOVE, R.id.btn_FixedLogo);
        }
        halfLayout.setLayoutParams(halfLayoutParams);
        half_img_voice = (ImageView) halfLayout.findViewById(R.id.half_img_voice);
        half_img_write = (ImageView) halfLayout.findViewById(R.id.half_img_write);
        half_btn_talk = (Button) halfLayout.findViewById(R.id.half_btn_talk);
        half_edi_content = (EditText) halfLayout.findViewById(R.id.half_edi_write);
        half_img_help = (ImageView) halfLayout.findViewById(R.id.half_img_help);
        half_btn_send = (Button) halfLayout.findViewById(R.id.half_btn_send);
        half_web_chat = (WebView) halfLayout.findViewById(R.id.half_web_chat);
        half_chat_web = (RelativeLayout) halfLayout.findViewById(R.id.half_chat_web);
        half_ProgressBar = (ProgressBar) halfLayout.findViewById(R.id.half_progress);
        preferences = sContext.getSharedPreferences(TAG, MODE_PRIVATE);
        editor = preferences.edit();
        isFirst = preferences.getBoolean("isFirst", true);
        //  WebView的设置  启用支持JavaScript
        half_web_chat.getSettings().setJavaScriptEnabled(true);
        //启用支持DOM Storage
        half_web_chat.getSettings().setDomStorageEnabled(true);
        //加载web资源
        long t = new Date().getTime();
        half_web_chat.loadUrl("http://bi.birdbot.cn:15180/hy.html?t=" + t);
        //自适应屏幕
        half_web_chat.getSettings().setUseWideViewPort(true);
        half_web_chat.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        half_web_chat.getSettings().setSupportZoom(true);
        half_web_chat.setInitialScale(100);
        half_web_chat.setWebChromeClient(new WebChromeClient() {//监听网页加载
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    // 网页加载完成
                    half_ProgressBar.setVisibility(View.GONE);
                } else {
                    if (half_ProgressBar.getVisibility() == View.GONE) {
                        half_ProgressBar.setVisibility(View.VISIBLE);
                    }
                    // 加载中
                    half_ProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        half_web_chat.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            //在WebView 加载完成后才能调用JS
            @Override
            public void onPageFinished(WebView view, String url) {
                if (isFirst) {
                    getHelp();
                    editor.putBoolean("isFirst", false);
                    editor.commit();
                } else {
                    half_web_chat.loadUrl("javascript:addMessageLeft('" + "请问有什么可以帮到您呢" + "');");
                }
                super.onPageFinished(view, url);
            }
        });

//        半屏方案从语音输入切换到打字输入
        half_img_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                half_img_write.setVisibility(View.GONE);
                half_img_voice.setVisibility(View.VISIBLE);
                half_btn_talk.setVisibility(View.GONE);
                half_edi_content.setVisibility(View.VISIBLE);
            }
        });

//        半屏方案从打字输入切换到语音输入
        half_img_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) sContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(half_edi_content.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                half_img_voice.setVisibility(View.GONE);
                half_img_write.setVisibility(View.VISIBLE);
                half_btn_talk.setVisibility(View.VISIBLE);
                half_edi_content.setVisibility(View.GONE);
            }
        });

//        半屏方案为按住说话语音输入设置监听器
        half_btn_talk.setOnTouchListener(new View.OnTouchListener() {
            int lastY = 0;
            int distanceY = 0;
            boolean isCancel = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastY = (int) event.getRawY();
                        isCancel = false;
                        halfAnimationLayout.setVisibility(View.VISIBLE);
                        ((Animatable) half_img_speakAnimation.getDrawable()).start();
                        aiHelper.start();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        distanceY = (int) (lastY - event.getRawY());
                        if (distanceY > 100) {
                            if (!isCancel) {
                                isCancel = true;
                                half_img_speakAnimation.setImageResource(R.drawable.c_82);
                                half_tv_cancel.setText("松开手指，取消发送");
                                aiHelper.setCancel(isCancel);
                            }
                        } else {
                            if (isCancel) {
                                isCancel = false;
                                half_img_speakAnimation.setImageResource(R.drawable.animation_speaking);
                                ((Animatable) half_img_speakAnimation.getDrawable()).start();
                                half_tv_cancel.setText("手指上滑，取消发送");
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isCancel) {
                            half_img_speakAnimation.setImageResource(R.drawable.animation_speaking);
                            half_tv_cancel.setText("手指上滑，取消发送");
                            halfAnimationLayout.setVisibility(View.GONE);
                            aiHelper.stop();
                            isCancel = false;
                            aiHelper.setCancel(isCancel);
                            return false;
                        } else {
                            halfAnimationLayout.setVisibility(View.GONE);
                            aiHelper.setCancel(isCancel);
                            aiHelper.stop();
                            break;
                        }
                }
                return false;
            }
        });

//      半屏方案当输入框有内容时，UI界面发生改变
        half_edi_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString() != null) {
                    half_img_help.setVisibility(View.GONE);
                    half_btn_send.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = half_edi_content.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    half_img_help.setVisibility(View.VISIBLE);
                    half_btn_send.setVisibility(View.GONE);
                } else {
                    half_img_help.setVisibility(View.GONE);
                    half_btn_send.setVisibility(View.VISIBLE);
                }
            }
        });
//       半屏发送按钮点击事件监听
        half_btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = half_edi_content.getText().toString();
                half_web_chat.loadUrl("javascript:addMessageRight('" + text + "');");
                onReq(text);
                half_edi_content.getText().clear();
            }
        });

//                半屏帮助按钮点击事件监听
        half_img_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHelp();
            }
        });
    }

    private static void getHelp() {
        String themeName = "", help = "";
        String[] caseLists = null;
        String name = getThemeName();
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
                    helpData = stringBuffer.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        if (helpData != null) {
            try {
                JSONObject all = new JSONObject(helpData);
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
                AIHelperSdk.speak("使用帮助如下");
                for (int i = 0; i < caseLists.length; i++) {
                    help += caseLists[i] + "<br>";
                }
                half_web_chat.loadUrl("javascript:addMessageLeft('" + "语音帮助：<br>" + help + "');");
            }
        }
    }

    //将16进制的Unicode码转为中文
    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer retBuf = new StringBuffer();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5) && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr.charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else
                    retBuf.append(unicodeStr.charAt(i));
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }

    public static String getUserId() {
        return sUserId;
    }

    public static String getOrginId() {
        return sOrginid;
    }

    public static String getToken() {
        return sToken;
    }

    public static void setUserId(String userId) {
        sUserId = userId;
    }

    public static void setOrginId(String orginId) {
        sOrginid = orginId;
    }

    public static void setToken(String token) {
        sToken = token;
    }

    public static void setSrvUrl(String url) {
        srvUrl = url;
    }

    public static String getSrvUrl() {
        return srvUrl;
    }

    public static String getThemeName() {
        return themeName;
    }

    public static void setThemeName(String name) {
        themeName = name;
    }

    public static void speak(String speech) {
        bdTtsUtil.speak(speech);
    }

    public static void cancelTts() {
        bdTtsUtil.cancle();
    }
}
