package com.gzln.goba.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gzln.goba.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HelpActivity extends Activity {
    private static final String TAG = "HelpActivity";
    protected TextView tv_help;
    protected TextView tv_themename;
    protected ImageView img_help_back;
    protected String themeName, help = "";
    protected String[] caseLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        tv_help = (TextView) findViewById(R.id.tv_help);
        tv_themename = (TextView) findViewById(R.id.tv_themename);
        img_help_back = (ImageView) findViewById(R.id.img_help_back);
        String helpData = getIntent().getExtras().getString("data");
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
                        caseLists[i] += content.has("content") ? "\n  " + content.getString("content") : "";
//                        Log.e(TAG, "onCreate:2----" + caseLists[i]);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (themeName != null && caseLists != null && !themeName.equals("")) {
                tv_themename.setText("语音使用帮助");
                for (int i = 0; i < caseLists.length; i++) {
                    help += caseLists[i] + "\n\n";
                }
                tv_help.setText(help);
            } else {
                tv_help.setText("帮助内容为空，请先在后台设置帮助内容");
            }
        } else {
            tv_help.setText("没有该场景的使用帮助，请先到后台设置");
        }
        img_help_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
