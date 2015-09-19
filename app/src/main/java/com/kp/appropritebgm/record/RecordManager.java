package com.kp.appropritebgm.record;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by KP on 2015-08-05.
 */
public class RecordManager {
    private MediaRecorder mr = null;
    private String path, filename;
    private String dirName;
    private boolean isRecording = false;

    public RecordManager(){
        dirName = "AppropriteBGM";
        init();
    }
    public RecordManager(String appName) {
        // 디렉토리명은 외부에서 생성자를 호출할 때 string.xml의 app_name을 넘겨준다.
        dirName = appName;
        init();
    }

    private void init() {
        // 외장메모리 기본경로/어플명 에 녹음한 파일을 저장한다.
        path = Environment.getExternalStorageDirectory() + File.separator + dirName;
        Log.i("dir_path", path);
        File file = new File(path);
        filename = "record_temp.mp3";   // 임시 파일명
        // 디렉토리가 존재하지 않으면 생성
        if ( !file.exists() ) {
            file.mkdirs();
            Log.i("make dir", "excuted : " + file.exists());
        }
    }

    // 녹음 시작
    public void start() {

        Log.i("Rec", "prepare to recording(RecordManager)");
        // 처음 녹음을 시작하면 MediaRecorder 객체 생성
        if (mr == null)
            mr = new MediaRecorder();

        mr.reset();
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);   // 마이크로 녹음
        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mr.setOutputFile(path + File.separator + filename);    // 파일 경로 및 파일명으로 녹음한 파일 생성준비

        try {
            isRecording = true;  // 녹음시작
            Log.i("Rec", "start to recording(RecordManager)");
            mr.prepare();
            mr.start();
        } catch (IOException e) {
            e.printStackTrace();
            isRecording = false; //녹음 실패
        }
    }

    // 녹음 중지
    public void stop() {
        Log.i("Rec", "stop recording(RecordManager)");
        isRecording = false;
        if (mr == null) return;
        try {
            Log.i("Rec", "stop method(RecordManager)");
            mr.stop();
        } catch (Exception e) {
        } finally {
            Log.i("Rec", "release method(RecordManager)");
            mr.release();
            mr = null;
        }
    }

    // 녹음중입니까?
    public boolean isRecording() {
        return isRecording;
    }

    // 녹음된 파일이 저장된 곳은 어디? (파일명 포함해서 넘겨준다)
    public String getPath() { return path + File.separator + filename; }
    // 녹음된 파일 저장된 디렉토리 경로만 넘겨준다
    public String getDirPath() { return path; }
}
