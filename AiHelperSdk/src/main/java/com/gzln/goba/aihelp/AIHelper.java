package com.gzln.goba.aihelp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.gzln.goba.R;
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
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
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
        String contents = "{\"userword\":[{\"name\":\"云货通名词\",\"words\":[";
        contents += "\"快速开单\",";
        contents += "\"开单\",";
        contents += "\"零售客户\",";
        contents += "\"批发客户\",";
        contents += "\"百里屠苏\",";
        contents += "\"风晴雪\",";
        contents += "\"方兰生\",";
        contents += "\"襄铃\",";
        contents += "\"红玉\",";
        contents += "\"太子长琴\",";
        contents += "\"尹千觞\",";
        contents += "\"何生\",";
        contents += "\"韩版时尚围巾\",";
        contents += "\"韩版时尚卫衣\",";
        contents += "\"日版潮流棒球帽\",";
        contents += "\"杏色\",";
        contents += "\"玫瑰红\",";
        contents += "\"棕色\",";
        contents += "\"咖啡\",";
        contents += "\"深蓝色\",";
        contents += "\"浅蓝色\",";
        contents += "\"蓝色\",";
        contents += "\"浅绿色\",";
        contents += "\"军绿色\",";
        contents += "\"绿色\",";
        contents += "\"紫罗兰\",";
        contents += "\"深紫色\",";
        contents += "\"紫色\",";
        contents += "\"酒红色\",";
        contents += "\"红色\",";
        contents += "\"粉红色\",";
        contents += "\"巧克力色\",";
        contents += "\"卡其色\",";
        contents += "\"褐色\",";
        contents += "\"橘色\",";
        contents += "\"浅黄色\",";
        contents += "\"黄色\",";
        contents += "\"深灰色\",";
        contents += "\"浅白色\",";
        contents += "\"白色\",";
        contents += "\"黑色\",";
        contents += "\"XS\",";
        contents += "\"S\",";
        contents += "\"M\",";
        contents += "\"L\",";
        contents += "\"XL\",";
        contents += "\"XXL\",";
        contents += "\"XXXL\",";
        contents += "\"均码\",";
        contents += "\"蔡伟盛\",";
        contents += "\"卢光焕\",";
        contents += "\"网店\",";
        contents += "\"门店\",";
        contents += "\"工厂\",";
        contents += "\"订单\",";
        contents += "\"发起\",";
        contents += "\"一审\",";
        contents += "\"二审\",";
        contents += "\"三审\",";
        contents += "\"四审\",";
        contents += "\"五审\",";
        contents += "\"六审\",";
        contents += "\"七审\",";
        contents += "\"通过\",";
        contents += "\"作废\"";
        contents += "]}]}";
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
}
