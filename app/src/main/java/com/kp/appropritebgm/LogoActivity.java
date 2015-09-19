package com.kp.appropritebgm;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import com.kp.appropritebgm.DBControl.DBManager;
import com.kp.appropritebgm.record.RecordManager;

/**
 * Created by KP on 2015-08-17.
 */


public class LogoActivity extends ActionBarActivity {

    private CheckPref mPref = null;
    private DBManager dbManager = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPref = new CheckPref(this);    // 공유 프레퍼런스 객체
        setContentView(R.layout.activity_logo);

        final TextView message = (TextView)findViewById(R.id.logo_message);   // 현재 진행상황 표시 텍스트

        // 최초 실행이면 SQLite DB 초기설정을 한다.
        if(mPref.getFirstExcute()){
            message.setText("앱 초기 설정중입니다.");
            Log.i("First Excute!!", "okok");
            mPref.setFirstExcute();
        }
        // 싱글톤 객체를 받아오면서 상속받은 SQLiteOpenHelper 클래스를 이용하여 DB를 생성/수정/열기 한다.
        dbManager = DBManager.getInstance(this);

        // RecordManager 객체의 초기설정을 이용하여 기본 디렉토리가 존재하지 않으면 생성한다.
        RecordManager recordManager = new RecordManager(getString(R.string.app_name));
        recordManager = null;

        // 로고화면에서 잠시 대기하면서 초기설정을 하고 메인 액티비티로 전환
        Thread waitThread = new Thread("Wait and Start Thread"){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(1000);
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.kp.appropritebgm", "com.kp.appropritebgm.MainActivity");
                    intent.setComponent(componentName);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };

        waitThread.start();
        message.setText("시작하는 중");
    }
}
