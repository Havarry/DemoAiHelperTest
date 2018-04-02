package com.gzln.goba.aihelp;

import android.webkit.JavascriptInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by sam on 2017/8/24.
 */

public class ChattingJs {

    private CallBackFuns _funs;
    private final static int TO_START_VODINPUT = 0x1001; // 隐藏菜单按钮
    private final static int TO_STOP_VODINPUT = 0x1002; // 隐藏菜单按钮
    private final static int TO_COMMAND = 0x1003; // 隐藏菜单按钮
    public ChattingJs(CallBackFuns funs)
    {
        _funs = funs;
    }

    @JavascriptInterface
    public void onReqVodInputStart() {
        Message msg = new Message();
        msg.what = TO_START_VODINPUT;
        handler.sendMessage(msg);
    }
    @JavascriptInterface
    public void onReqVodInputStop() {
        Message msg = new Message();
        msg.what = TO_STOP_VODINPUT;
        handler.sendMessage(msg);
    }

    @JavascriptInterface
    public void onCommand(String parm) {
        Message msg = new Message();
        msg.what = TO_COMMAND;
        Bundle data = new Bundle();
        data.putString("cmd", parm);
        msg.setData(data);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what) {
                case TO_START_VODINPUT:

                    break;
            }
        }
    };
}
