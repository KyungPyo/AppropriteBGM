package com.kp.appropritebgm.record;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kp.appropritebgm.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by KP on 2015-08-05.
 */
public class RecordActivity extends ActionBarActivity {
    // 녹음 관련
    private RecordManager recordManager = null;
    private RecordTask recordTask = null;

    // 재생 관련
    private MediaPlayer music = null;
    private PlayTask playTask = null;
    private Uri uri = null;
    private final int PROGRESS_INTERVAL = 50;     // 재생 progress바 갱신주기
    private int maxTime = 0;    // 현재 녹음된 파일 재생길이

    // 화면 출력 관련
    private int currentRecordTimeMs = 0, currentPlayTimeMs = 0;
    private SeekBar recordProgressBar = null;
    private TextView recordMaxTimeText = null;
    private TextView recordPlayTimeText = null;
    private ImageView btnPlay = null;
    private ImageButton btnRecord = null;
    private ImageView btnSave = null;
    private ImageView btnStop = null;

    // 저장 관련(팝업)
    private View mPopupLayout = null;
    private PopupWindow mPopupWindow = null;
    private Button popSaveBtn = null;
    private Button popCancelBtn = null;
    private EditText filenameEt = null;
    private Spinner categorySel = null;
    private int selectCategory = 0;

    /***** 작업스레드 AsyncTask 상속받아서 클래스 생성 - 녹음 스레드 *****/
    private class RecordTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {     // 작업스레드 사전 처리작업
            currentRecordTimeMs = 0;
            btnPlay.setEnabled(false);  // 녹음중엔 재생버튼을 누를 수 없다.
            btnSave.setEnabled(false);  // 저장버튼도
            btnStop.setEnabled(false);  // 정지버튼도

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {      // 실제 작업스레드 동작
            recordManager.start();
            while (true) {
                /** 녹음시간제한 **
                if (!recordManager.isRecording() || currentRecordTimeMs > 180*1000 ) {
                    recordManager.stop();
                    return true;
                }*/
                if ( isCancelled() ) {  // 작업이 취소되었으면
                    if(recordManager.isRecording())
                        recordManager.stop();
                    return null;
                }
                try {
                    Thread.sleep(PROGRESS_INTERVAL);                         // cancel되면 이부분에서 Exception이 발생해 catch로 넘어간다
                    currentRecordTimeMs += PROGRESS_INTERVAL;
                    publishProgress(currentRecordTimeMs);   // 현재 녹음시간 메인스레드로 전달
                } catch (InterruptedException e) {
                    if(recordManager.isRecording())
                        recordManager.stop();
                    return null;
                }
            }
        }

        @Override
        protected void onCancelled() {
            if(recordManager.isRecording())
                recordManager.stop();

            btnPlay.setEnabled(true);   // 재생버튼 클릭가능
            btnSave.setEnabled(true);   // 저장버튼 클릭가능
            btnStop.setEnabled(true);   // 정지버튼 클릭가능
            prepareRecordFileToPlay();
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int currentTime = values[0];

            // 현재 녹음 진행상황 화면에 표시
            // Seekbar 설정
            recordProgressBar.setMax(currentTime);
            recordProgressBar.setProgress(currentTime);

            setTimeText(recordMaxTimeText, currentTime);
            setTimeText(recordPlayTimeText, currentTime);

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            btnPlay.setEnabled(true);   // 재생버튼 클릭가능
            prepareRecordFileToPlay();

            super.onPostExecute(aVoid);
        }
    }
    /***** 녹음 스레드 *****/

    /***** 재생 스레드 *****/
    private class PlayTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            while(music.isPlaying()) {
                if (isCancelled()) {  // 작업이 취소되었으면
                    return null;
                }
                try {
                    publishProgress(music.getCurrentPosition());   // 현재 재생시간 메인스레드로 전달
                    Thread.sleep(PROGRESS_INTERVAL);             // cancel되면 이부분에서 Exception이 발생해 catch로 넘어간다
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            recordProgressBar.setProgress(0);
            setTimeText(recordPlayTimeText, 0);

            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int currentTime = values[0];

            if(music.isPlaying()) {     // 재생중이면 재생바 갱신
                recordProgressBar.setProgress(currentTime);
                setTimeText(recordPlayTimeText, currentTime);
            } else {    // 스레드 cancel 되지않고 재생 끝났으면(100ms 안의 오차일 경우 대비)
                Log.e("111", "ok");
                btnPlay.setImageResource(R.drawable.btn_play);  // 일시정지 버튼을 재생버튼으로 변경
            }

            if(currentTime >= maxTime){ // 재생시간이 최대시간을 넘기면
                Log.e("222", "ok");
                btnPlay.setImageResource(R.drawable.btn_play);  // 일시정지 버튼을 재생버튼으로 변경
            }

            super.onProgressUpdate(values);
        }
    }
    /***** 재생 스레드 *****/

    /***** 액티비티 *****/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("녹음하기");
        setContentView(R.layout.activity_record);

        Intent intent = getIntent();
        // 저장될 디렉토리명을 app_name으로 설정
        recordManager = new RecordManager(getString(R.string.app_name));
        recordProgressBar = (SeekBar)findViewById(R.id.seekbar_record); // 재생 바
        recordMaxTimeText = (TextView)findViewById(R.id.text_maxtime_atvrecord);    // 재생할 파일 최대길이
        recordPlayTimeText = (TextView)findViewById(R.id.text_playtime_atvrecord);  // 재생하는 파일 현재시간
        btnPlay = (ImageView)findViewById(R.id.btn_play_atvrecord);   // 재생버튼
        btnRecord = (ImageButton)findViewById(R.id.btn_startRecord);    // 녹음시작/중지 버튼
        btnSave = (ImageView)findViewById(R.id.btn_save_atvrecord);        // 저장버튼
        btnStop = (ImageView)findViewById(R.id.btn_stop_atvrecord);        // 정지버튼

        recordProgressBar.setOnSeekBarChangeListener(seekBarChangeListener);    // seekbar 이벤트 등록


        btnPlay.setEnabled(false);  // 녹음하기전엔(녹음된 파일이 없으면) 재생버튼을 누를 수 없다.
        btnSave.setEnabled(false);  // 저장버튼도
        btnStop.setEnabled(false);  // 정지버튼도

        setPopupWindow();
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                music.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void prepareRecordFileToPlay(){
        uri = Uri.fromFile(new File(recordManager.getPath()));
        music = MediaPlayer.create(this, uri);
        music.setLooping(false);

        maxTime = music.getDuration() - (music.getDuration() % PROGRESS_INTERVAL);  // 100ms 아래 값은 버린다
        recordProgressBar.setMax(maxTime);
        recordProgressBar.setProgress(0);
        setTimeText(recordMaxTimeText, maxTime);
        setTimeText(recordPlayTimeText, 0);
    }

    // 녹음버튼 클릭
    public void onClick_startRecord(View v){
        if (v.getId() == R.id.btn_startRecord){

            if (music != null && music.isPlaying()) {    // 재생중이면
                music.stop();           // 재생중이던거 정지하고
                btnPlay.setImageResource(R.drawable.btn_play);  // 일시정지 버튼 다시 재생버튼으로 바꾸고
            }

            if(!recordManager.isRecording()){   // 녹음중이 아니면
                recordTask = new RecordTask();
                recordTask.execute();  // 녹음 시작
                v.setBackgroundResource(R.drawable.btn_stoprecord_selector);   // 녹음버튼의 이미지를 녹음중으로 변경


            } else {     // 녹음중이면
                recordTask.cancel(true);   // 녹음 중지
                v.setBackgroundResource(R.drawable.btn_startrecord_selector);     // 녹음버튼의 이미지를 녹음 준비중으로 변경

            }

        }
    }

    // 재생관련 버튼
    public void onClick_playRecord(View v) {
        switch (v.getId()) {
            // 재생버튼
            case R.id.btn_play_atvrecord : {
                if (!music.isPlaying() && !recordManager.isRecording()) {   // 재생중과 녹음중이 아니면
                    music.start();
                    btnPlay.setImageResource(R.drawable.btn_pause);
                    playTask = new PlayTask();
                    playTask.execute(); // 재생 시작 후 seekbar&textview 처리
                } else {
                    music.pause();
                    btnPlay.setImageResource(R.drawable.btn_play);
                }
                break;
            }

            // 정지(임시)
            case R.id.btn_stop_atvrecord : {
                if (music.isPlaying()){ // 재생중이면
                    music.stop();
                    try {
                        music.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    btnPlay.setImageResource(R.drawable.btn_play);  // 일시정지 다시 재생으로
                }
                setTimeText(recordPlayTimeText, 0);
                music.seekTo(0);
                recordProgressBar.setProgress(0);

                break;
            }
        }
    }

    // Record Activity에서 저장버튼 클릭
    public void onClick_save(View v){
        if (v.getId() == R.id.btn_save_atvrecord) {
            selectCategory = 0;     // 혹시나 저장됐던 카테고리번호 초기화
            // 팝업윈도우 출력
            mPopupWindow.showAtLocation(mPopupLayout, Gravity.CENTER, 0, 0);
        }
    }

    // 재생시간 표시하는 텍스트뷰 설정
    public void setTimeText(TextView targetView, int timeMs) {
        int sec = 0, min = 0;
        String timeText = "";
        // TextView 설정
        min = timeMs / 60000;
        timeMs = timeMs % 60000;
        sec = timeMs / 1000;

        if (min < 10)
            timeText = "0";
        timeText = timeText + min + ":";
        if (sec < 10)
            timeText = timeText + "0";
        timeText = timeText + sec;

        targetView.setText(timeText);
    }

    // 저장버튼을 눌렀을 때 출력되는 팝업윈도우 설정(버튼 이벤트리스너 포함)
    public void setPopupWindow() {
        mPopupLayout = getLayoutInflater().inflate(R.layout.popup_saverecord, null);    // 팝업으로 띄울 xml 연결

        // 팝업윈도우
        popSaveBtn = (Button)mPopupLayout.findViewById(R.id.btn_save_ok);
        popCancelBtn = (Button)mPopupLayout.findViewById(R.id.btn_save_cancel);
        filenameEt = (EditText)mPopupLayout.findViewById(R.id.et_save_filename);
        categorySel = (Spinner)mPopupLayout.findViewById(R.id.sel_save_category);

        // 팝업 윈도우 생성 popup_saverecord.xml 파일
        mPopupWindow = new PopupWindow(mPopupLayout,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, true);
        // 백그라운드를 설정해야 사용자가 팝업윈도우 밖을 클릭했을때 종료된다.
        mPopupWindow.setBackgroundDrawable( new ColorDrawable() );

        // 카테고리 스피너에 아이템 추가 (SQLLite select문 추가필요)
        ArrayList<String> spinnerItem = new ArrayList<>();
        spinnerItem.add("카테고리를 선택하세요");
        for(int i=1; i<20; i++)
            spinnerItem.add("카테고리"+i);
        final ArrayAdapter<String> mAdapter;
        mAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, spinnerItem);

        categorySel.setAdapter(mAdapter);

        // 카테고리 선택 스피너 선택 이벤트
        categorySel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectCategory = (int)id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /***** (팝업윈도우 내 클릭이벤트) 팝업윈도우의 저장버튼을 클릭 ****/
        popSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newFileName = filenameEt.getText().toString();  // 입력한 파일명 받아오기

                if(newFileName == "" || newFileName == null){     // 파일명 입력확인
                    Toast.makeText(RecordActivity.this, "파일명을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if (selectCategory == 0) {   // 카테고리 선택확인
                    Toast.makeText(RecordActivity.this, "카테고리를 선택해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    // (SQLLite insert문 추가필요)
                    String category = mAdapter.getItem(selectCategory);

                    File file = new File(recordManager.getPath());
                    File renamedFile = new File(recordManager.getDirPath() + File.separator + newFileName + ".mp3");
                    file.renameTo(renamedFile);
                    // 파일을 이름변경해서 남기고 액티비티 종료(메인 액티비티로 돌아간다)
                    finish();
                }
            }
        });

        /***** (팝업윈도우 내 클릭이벤트) 팝업윈도우의 취소버튼을 클릭 ****/
        popCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
    }
    /***** 액티비티 *****/
}
