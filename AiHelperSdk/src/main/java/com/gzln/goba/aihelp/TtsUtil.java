package com.gzln.goba.aihelp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 讯飞语音在线合成工具类
 * @author hzw
 */
public class TtsUtil {

	private static final String TAG = TtsUtil.class.getSimpleName();

	/** 合成的语音文件保存路径 */
	private static final String AUDIO_PATH = AssetsCopyer.getAppDir2()+"/cache.wav";

	private SpeechSynthesizer mTts;

	private Callback callback;

	private FragmentActivity parent;

	public interface Callback{
		public void onSynthesizeToUriCompleated(Bundle data);
		public void onSpeakBegin();
	}

	public TtsUtil(Callback callback) {
		this.callback = callback;
		init();
	}

	private void init() {

		mTts = SpeechSynthesizer.createSynthesizer(parent, null);
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
		mTts.setParameter(SpeechConstant.SPEED, "50"); // 语速 0~100
		mTts.setParameter(SpeechConstant.PITCH, "50"); // 语调 0~100
		mTts.setParameter(SpeechConstant.VOLUME, "80"); // 音量 0~100
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 引擎
		// 设置文件保存格式
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		//设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
		//mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, AssetsCopyer.getAppDir2()+"/cache.wav");
	}

	private SynthesizerListener mSynListener = new SynthesizerListener() {

		@Override
		public void onSpeakResumed() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSpeakProgress(int arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSpeakPaused() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSpeakBegin() {
			// TODO Auto-generated method stub
			//Log.i(TAG, "onSpeakBegin...");
			callback.onSpeakBegin();
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCompleted(SpeechError arg0) {
			// TODO Auto-generated method stub
			//Log.i("讯飞语音合成", "合成compleated");
			Bundle data = new Bundle();
			data.putString("audioPath", AUDIO_PATH);
			callback.onSynthesizeToUriCompleated(data);
		}

		@Override
		public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * 开始将文字合成语音
	 * @param text
	 */
	public void startSpeaking(String text){
		String language = setupLanguage();
		mTts.setParameter(SpeechConstant.SPEED, "50");
		if(language.equals("zh_cn") && text.contains("="))
			text = text.replaceAll("=", "等于");
		mTts.startSpeaking(text, mSynListener);

	}

	/**
	 * 立即停止说话
	 */
	public void stopSpeaking(){

		mTts.stopSpeaking();
	}

	/**
	 * 将文本合成音频文件，不播放
	 * @param text
	 */
	public void synthesizeToUri(String text){

		setupLanguage();
		mTts.synthesizeToUri(text, AUDIO_PATH, mSynListener);
	}

	/**
	 * 将文本合成音频文件，不播放，只合成中文
	 * @param text
	 */
	public void synthesizeToUriOnlyChinese(String text){
		mTts.setParameter(SpeechConstant.VOICE_NAME,"xiaoyan"); // 小妍（中英文，普通话）
		mTts.setParameter(SpeechConstant.SPEED, "35"); // 语速
		mTts.synthesizeToUri(text, AUDIO_PATH, mSynListener);
	}

	/**
	 * 设置语言
	 */
	private String setupLanguage() {
		String currentOutLanguage = "zh_cn";
		if(currentOutLanguage.equals("en_us")){
			mTts.setParameter(SpeechConstant.VOICE_NAME,"vimary"); // 玛丽（英文）
		}
		if(currentOutLanguage.equals("zh_cn")){
			mTts.setParameter(SpeechConstant.VOICE_NAME,"xiaoyan"); // 小妍（中英文，普通话）
		}
		return currentOutLanguage;
	}
}
