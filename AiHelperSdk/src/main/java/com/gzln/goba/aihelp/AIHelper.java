package com.gzln.goba.aihelp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import com.gzln.goba.utils.MyToast;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by sam on 2017/7/13
 */

public class AIHelper {
    //    private TtsUtil mTts;
    private SpeechRecognizer mIat;
    private Context context;
    private MyToast myToast;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private boolean isCancel = false;
    private CallBackFuns _funs;
    private int accent_flag = 1;

    public AIHelper(Context context, CallBackFuns funs) {
        this.context = context;
        _funs = funs;
        myToast=new MyToast(context);
        init();
    }

//    private TtsUtil.Callback ttsUtilCallback = new TtsUtil.Callback() {
//
//        @Override
//        public void onSynthesizeToUriCompleated(Bundle data) {
//
//        }
//
//        @Override
//        public void onSpeakBegin(){
//
//        }
//    };

    /**
     * 上传联系人/词表监听器。
     */
    private LexiconListener mLexiconListener = new LexiconListener() {
        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError error) {
            if (error != null) {
                Log.e("讯飞Error", error.toString());
            } else {
                Log.i("讯飞", "讯飞-上传用户词表成功！");
            }
        }
    };

    private void init() {

        //mTts = new TtsUtil(ttsUtilCallback);
        SpeechUtility su = SpeechUtility.createUtility(context, SpeechConstant.APPID + "=572fee6b");
        // 讯飞语音听写
        mIat = SpeechRecognizer.createRecognizer(context, null);
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        setAccent(1);
        mIat.setParameter(SpeechConstant.ASR_PTT, "0"); // 去除标点
        mIat.setParameter(SpeechConstant.VAD_ENABLE, "1"); // 禁止静音抑制,默认为1开启，0关闭
        mIat.setParameter(SpeechConstant.VAD_EOS, "10000"); // 后端点超时，最大值
        mIat.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "40000");
        // 设置语言
        // 上传自定义词表
        // String contents = FucUtil.readFile(context, "userwords", "utf-8");
        // 指定引擎类型
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        // 函数调用返回值
        String contents = "";
        int ret = mIat.updateLexicon("userword", contents, mLexiconListener);
        if (ret != ErrorCode.SUCCESS)
            Log.e("科大讯飞Error", "上传热词失败,错误码：" + ret);

    }

    private RecognizerListener mRecoListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int arg0, byte[] arg1) {

        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {

            String text = JsonParserXunfei.parseIatResult(results.getResultString());

            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);

            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }

            if (isLast) {
                String sentence = resultBuffer.toString();
                if (sentence.equals("")) {
                    return;
                }
                Log.e("讯飞结果", sentence);
                Log.i("tag", "听写结果：" + sentence);
                _funs.onVodInput(sentence);
                sentence = "";
            }

        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {

        }

        @Override
        public void onError(SpeechError arg0) {
            Log.e("ERROR", arg0.toString());
//            if (arg0.getErrorCode() == ErrorCode.ERROR_NO_NETWORK) {
//                Toast.makeText(context, "没有检测到网络", Toast.LENGTH_SHORT).show();
//                AlertDialog.Builder normalDialog =
//                        new AlertDialog.Builder(context.getApplicationContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
//                normalDialog.setTitle("网络异常");
//                normalDialog.setMessage("没有连接网络");
//                normalDialog.setPositiveButton("确定",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //...To-do
//                            }
//                        });
//                normalDialog.show();
//            }
            if (arg0.getErrorCode() == ErrorCode.MSP_ERROR_NO_DATA) {
                showToastText("您好像没有说话哦");
            }
        }

        @Override
        public void onEndOfSpeech() {
            mIat.stopListening();
        }

        @Override
        public void onBeginOfSpeech() {
            mIatResults.clear();//说话之前清空之前的map
        }
    };

    public void start() {
        mIat.stopListening();
        int rt = mIat.startListening(mRecoListener);
        if (rt != ErrorCode.SUCCESS) {
            mIat.startListening(mRecoListener);
        }
    }

    public void stop() {
        if (isCancel) {
            mIat.cancel();
            return;
        }
        mIat.stopListening();
    }

    public void setCancel(boolean isCancel) {
        this.isCancel = isCancel;
    }

    private void showToastText(String toast) {
        myToast = null;
        myToast = new MyToast(context);
        myToast.setText(toast);
        myToast.setDuration(1500);
        myToast.show(500);
    }

    private void setAccent(int lang){
        switch (lang){
            case 1:
                mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
                break;
            case 2:
                mIat.setParameter(SpeechConstant.ACCENT, "cantonese");
                break;
        }
    }
}
