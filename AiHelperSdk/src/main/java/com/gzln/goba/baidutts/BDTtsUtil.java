package com.gzln.goba.baidutts;

import android.content.Context;
import android.util.Log;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

/**
 * Created by 火焰科技 on 2017/9/20.
 * 百度语音在线合成工具类
 */

public class BDTtsUtil implements SpeechSynthesizerListener {
    private static final String TAG = BDTtsUtil.class.getSimpleName();
    private Context mContext;
    // 语音合成客户端
    private SpeechSynthesizer speechSynthesizer;

    public BDTtsUtil(Context context) {
        this.mContext = context;
        init();
    }

    // 初始化语音合成客户端并启动
    private void init() {
        // 获取语音合成对象实例
        speechSynthesizer = SpeechSynthesizer.getInstance();
        // 设置context
        speechSynthesizer.setContext(mContext);
        // 设置语音合成状态监听器
        speechSynthesizer.setSpeechSynthesizerListener(this);
        // 设置在线语音合成授权，需要填入从百度语音官网申请的api_key和secret_key
        speechSynthesizer.setApiKey("gWrEBa08Ma6baG3cGVkFg0ds", "e619f3e48dffe1564b8f50139c365b47");
        // 设置离线语音合成授权，需要填入从百度语音官网申请的app_id
        speechSynthesizer.setAppId("10165413");
        // 获取语音合成授权信息
        AuthInfo authInfo = speechSynthesizer.auth(TtsMode.ONLINE);
        // 判断授权信息是否正确，如果正确则初始化语音合成器并开始语音合成，如果失败则做错误处理
        if (authInfo.isSuccess()) {
            speechSynthesizer.initTts(TtsMode.ONLINE);
        } else {
            Log.e(TAG, "百度语音合成授权失败");
        }
        setParams();
    }

    //    为语音合成器设置相关参数
    private void setParams() {
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");//发音人，目前支持女声(0)和男声(1)
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");//音量，取值范围[0, 9]，数值越大，音量越大
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");//朗读语速，取值范围[0, 9]，数值越大，语速越快
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");//音调，取值范围[0, 9]，数值越大，音量越高
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_ENCODE,
                SpeechSynthesizer.AUDIO_ENCODE_AMR);//音频格式，支持bv/amr/opus/mp3，取值详见随后常量声明
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_RATE,
                SpeechSynthesizer.AUDIO_BITRATE_AMR_15K85);//音频比特率，各音频格式支持的比特率详见随后常量声明
    }

    public void speak(final String content) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = speechSynthesizer.speak(content);
                if (ret != 0) {
                    Log.e(TAG, "开始合成器失败: " + ret);
                }
            }
        }).start();
    }

    //    暂停文本朗读，如果没有调用speak(String)方法或者合成器初始化失败，该方法将无任何效果
    public void cancle() {
        speechSynthesizer.pause();
    }

    //    继续文本朗读，如果没有调用speak(String)方法或者合成器初始化失败，该方法将无任何效果
    public void resume() {
        speechSynthesizer.resume();
    }

    @Override
    public void onSynthesizeStart(String s) {

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {

    }

    @Override
    public void onSpeechStart(String s) {
        Log.i(TAG, "朗读开始");
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        Log.i(TAG, "朗读已停止");
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        Log.e(TAG, "onError: "+s+"SpeechError:"+speechError );
    }
}

