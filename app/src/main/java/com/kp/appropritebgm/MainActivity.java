package com.kp.appropritebgm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kp.appropritebgm.DBControl.BGMList;
import com.kp.appropritebgm.DBControl.DBManager;
import com.kp.appropritebgm.DBControl.ScanFileList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    static final int MAIN_ACTIVITY = 1;
    // 화면출력 관련
    private View menu_layout = null;
    private ListView listView = null;
    private TextView playTimeText = null;
    private SeekBar progressBar = null;
    private TextView maxTimeText = null;
    private ImageView btnPlay = null;
    private ImageView btnPause = null;
    private ImageView btnStop = null;

    // 데이터 관련
    private DBManager dbManager = null;
    private ArrayList<BGMList> listdata = null;
    private BgmListAdapter adapter = null;
    private ScanFileList scan = null;

    // 재생 관련
    private MediaPlayer music = null;
    private PlayTask playTask = null;
    private Uri uri = null;
    private final int PROGRESS_INTERVAL = 50;     // 재생 progress바 갱신주기
    private int maxTime = 0;    // 현재 녹음된 파일 재생길이
    private boolean isPauesd = false;   // 일시정지 여부

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
                    Thread.sleep(PROGRESS_INTERVAL);                         // cancel되면 이부분에서 Exception이 발생해 catch로 넘어간다
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
//            recordProgressBar.setProgress(0);
//            setTimeText(recordPlayTimeText, 0);

            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int currentTime = values[0];

            if(music.isPlaying()) {     // 재생중이면 재생바 갱신
                progressBar.setProgress(currentTime);
                setTimeText(playTimeText, currentTime);
            }

            super.onProgressUpdate(values);
        }
    }
    /***** 재생 스레드 *****/

    /***** 재생 준비/동작 *****/
    private void resetPlay(){
        if (music != null && music.isPlaying()) { // 재생중이면 정지/초기화 후 다시 재생준비
            music.stop();
            try {
                music.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            music.seekTo(0);
        }
        // 재생 툴 초기화
        progressBar.setProgress(0);
        setTimeText(playTimeText, 0);
        isPauesd = false;   // 일시정지 여부 초기화
    }

    private void prepareFileToPlay(int position){
        resetPlay();
        // 내장파일 여부 검사 (0:외장 그외 숫자가 들어가있으면 외장. 그외 숫자는 리소스코드번호임)
        if(adapter.getBgmInnerFileCode(position) == 0){
            Log.e("aaaaaa", adapter.getBgmPath(position));
            uri = Uri.fromFile(new File(adapter.getBgmPath(position)));
            music = MediaPlayer.create(this, uri);
        } else {
            music = MediaPlayer.create(this, adapter.getBgmInnerFileCode(position));
        }
        music.setLooping(false);

        // 재생관련 화면 표시값 초기설정
        maxTime = music.getDuration() - (music.getDuration() % PROGRESS_INTERVAL);
        progressBar.setMax(maxTime);
        progressBar.setProgress(0);
        setTimeText(maxTimeText, maxTime);
        setTimeText(playTimeText, 0);
    }

    private void playBgm(){     // 처음부터 재생하기
        resetPlay();
        music.start();
    }
    /***** 재생 준비/동작 *****/

    /***** 액티비티 *****/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("적절한 브금");
        setContentView(R.layout.activity_main);

        dbManager = DBManager.getInstance(this);    // 내장DB 접속
        drawListView(MAIN_ACTIVITY);    // MainActivity code = 1

        initMenuLayout();       // 메뉴 레이아웃 준비

        playTimeText = (TextView)findViewById(R.id.text_playtime);
        progressBar = (SeekBar)findViewById(R.id.seekbar_main);
        maxTimeText = (TextView)findViewById(R.id.text_maxtime);
        btnPlay = (ImageView)findViewById(R.id.btn_play);
        btnPause = (ImageView)findViewById(R.id.btn_pause);
        btnStop = (ImageView)findViewById(R.id.btn_stop);

        progressBar.setOnSeekBarChangeListener(seekBarChangeListener);  // seekbar 이벤트 등록
    }

    @Override
    protected void onPause() {
        resetPlay();  // 액티비티 빠져나갈 때 음악 정지
        super.onPause();
    }

    private void drawListView(int accessCode){
        scan = new ScanFileList(this);
        scan.refreshBgmList();  // 탐색범위 안에서 파일을 다시 검색하고 DB 갱신
        listdata = dbManager.getBGMList(1);             // 리스트뷰에 출력시킬 BGM 리스트 내장DB에서 읽어옴(전체:1)
        adapter = new BgmListAdapter(this, listdata, accessCode);    // MainActivity code = 1

        listView=(ListView)findViewById(R.id.list_atv_main);
        listView.setAdapter(adapter);
//        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(itemClickListener);
    }

    private void initMenuLayout(){
        // 메뉴 레이아웃 겹치기
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout menuInflater = (LinearLayout)inflater.inflate(R.layout.menu, null);

        LinearLayout.LayoutParams menuParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        addContentView(menuInflater, menuParam);

        menu_layout = findViewById(R.id.layout_menu);
        menu_layout.setVisibility(View.INVISIBLE);
        menu_layout.setEnabled(false);
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

    /*** 이벤트 ***/
    private ListView.OnItemClickListener itemClickListener
            = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // 리스트 아이템 선택하면 해당 음원 재생 준비
            prepareFileToPlay(position);
            playBgm();  // 처음부터 재생
            // 재생하면 화면 처리할 스레드 시작
            playTask = new PlayTask();
            playTask.execute();
            //Toast.makeText(getApplicationContext(), adapter.getItem(position) + " " + position + " " + listView.isItemChecked(position), Toast.LENGTH_SHORT).show();
        }
    };

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

    public void onClickPlayTools(View view){
        if(music != null){      // 재생 할 음악이 선택되었을 경우에만 실행가능
            switch (view.getId()) {
                case R.id.btn_play:{
                    if(isPauesd) {      // 일시정지 되어있으면
                        music.start();  // 그대로 다시 재생
                        isPauesd = false;
                    } else {            // 아니면
                        Log.e("dd", "처음부터 재생");
                        playBgm();       // 처음부터 다시 재생
                    }
                    // 재생하면 화면 처리할 스레드 시작
                    playTask = new PlayTask();
                    playTask.execute();
                    break;
                }
                case R.id.btn_stop:{
                    resetPlay();      // 음악 선택여부도 체크하면서 정지시켜서 대기상태로 만듬
                    break;
                }
                case R.id.btn_pause:{
                    if(isPauesd){    // 일시정지 중이면 재생
                        music.start();
                        isPauesd = false;
                        // 재생하면 화면 처리할 스레드 시작
                        playTask = new PlayTask();
                        playTask.execute();
                    } else if(music.isPlaying()){   // 일시정지 중이 아니고 재생중이면
                        music.pause();
                        isPauesd = true;
                    }
                    break;
                }
            }
        }
    }

    // 리스트 새로고침
    public void refreshBgmList(int category){
        scan = new ScanFileList(this);
        scan.refreshBgmList();  // 탐색범위 안에서 파일을 다시 검색하고 DB 갱신
        listdata = dbManager.getBGMList(category);     // 갱신된 DB 다시 불러옴
        adapter.refreshData(listdata);          // 갱신된 브금 리스트 다시 적용
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String txt = null;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings && !menu_layout.isShown()) {
            menu_layout.setVisibility(View.VISIBLE);
            menu_layout.setEnabled(true);
        } else if (id == R.id.action_settings && menu_layout.isShown()) {
            menu_layout.setVisibility(View.INVISIBLE);
            menu_layout.setEnabled(false);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:{
                // 메뉴가 보이는 상태에서 뒤로버튼을 눌렀을 경우
                if(menu_layout.isShown()){
                    // 메뉴를 닫음
                    menu_layout.setVisibility(View.INVISIBLE);
                    menu_layout.setEnabled(false);
                    return true;
                }
                break;
            }

            case KeyEvent.KEYCODE_MENU:{
                // 메뉴가 보이는 상태에서 메뉴버튼 눌렀을 경우
                if(menu_layout.isShown()){
                    // 메뉴를 닫음
                    menu_layout.setVisibility(View.INVISIBLE);
                    menu_layout.setEnabled(false);
                    return true;
                } else {
                    // 아니면 메뉴를 열음
                    menu_layout.setVisibility(View.VISIBLE);
                    menu_layout.setEnabled(true);
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onClickMain(View v){
        Intent intent = new Intent();
        ComponentName componentName = null;
        switch (v.getId()){
            case R.id.btn_atv_record:{
                componentName = new ComponentName("com.kp.appropritebgm", "com.kp.appropritebgm.record.RecordActivity");
                intent.setComponent(componentName);
                menu_layout.setVisibility(View.INVISIBLE);
                menu_layout.setEnabled(false);
                startActivity(intent);
                break;
            }
            case R.id.btn_atv_favorite:{
                componentName = new ComponentName("com.kp.appropritebgm", "com.kp.appropritebgm.favorite.FavoriteActivity");
                intent.setComponent(componentName);
                menu_layout.setVisibility(View.INVISIBLE);
                menu_layout.setEnabled(false);
                startActivity(intent);
                break;
            }
            case R.id.btn_atv_category:{
                componentName = new ComponentName("com.kp.appropritebgm", "com.kp.appropritebgm.category.CategoryActivity");
                intent.setComponent(componentName);
                menu_layout.setVisibility(View.INVISIBLE);
                menu_layout.setEnabled(false);
                startActivity(intent);
                break;
            }
            case R.id.btn_atv_settings:{
                componentName = new ComponentName("com.kp.appropritebgm", "com.kp.appropritebgm.SettingActivity");
                intent.setComponent(componentName);
                menu_layout.setVisibility(View.INVISIBLE);
                menu_layout.setEnabled(false);
                startActivity(intent);
                break;
            }
            case R.id.close_menu:{
                menu_layout.setVisibility(View.INVISIBLE);
                menu_layout.setEnabled(false);
                break;
            }
            case R.id.btn_refresh:{
                refreshBgmList(1);
                break;
            }
        }
    }
    /***** 액티비티 *****/
}
