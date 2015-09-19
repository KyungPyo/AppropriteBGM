package com.kp.appropritebgm.DBControl;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by KP on 2015-09-07.
 */
public class ScanFileList implements FilenameFilter {

    private Context mContext = null;
    private DBManager dbManager = null;
    private ArrayList<String> scanRangeList = null;
    final static String ROOT = Environment.getExternalStorageDirectory().getPath();

    public ScanFileList(Context context) {
        mContext = context;
    }

    public void refreshBgmList(){
        dbManager = DBManager.getInstance(mContext);

        scanRangeList = dbManager.getScanRange();   // DB에서 탐색범위 목록을 가져옴
        for(int i=0; i<scanRangeList.size(); i++){  // 탐색범위 개수만큼 반복
            ArrayList<String> fileList;
            fileList = getMusicFileList(scanRangeList.get(i));
            if(fileList.size()>0){  // 검색된 음악파일이 있는 경우
                dbManager.checkAndInsertBgmList(fileList, ROOT+scanRangeList.get(i));
            }
        }
    }

    @Override
    public boolean accept(File dir, String filename) {
        // 재생 허용가능한 확장자들
        boolean result = filename.endsWith(".mp3") | filename.endsWith(".ogg") | filename.endsWith(".wma");
        //Log.i(filename, "file?"+result);
        return result;
    }

    public ArrayList<String> getMusicFileList(String path){
        File dir = new File(ROOT+path);
        if (!dir.isDirectory()){    // 해당 경로가 디렉토리가 아니면 검색안함
            return null;
        }

        ArrayList<String> result = new ArrayList<>();   // 결과 저장할 ArrayList
        String[] fileList = dir.list();   // 파일목록을 구한다.
        for (int i=0; i<fileList.length; i++){
            if(accept(dir, fileList[i])) {    // 음악 파일인지 확인
                result.add(fileList[i]);     // 음악 파일이면 결과에 추가
            }
        }

        return result;
    }
}
