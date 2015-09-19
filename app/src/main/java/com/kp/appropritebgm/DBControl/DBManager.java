package com.kp.appropritebgm.DBControl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kp.appropritebgm.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by KP on 2015-08-17.
 */
// SQLiteOpenHelper 클래스 상속받아서 사용 (onCreate, onOpen, onUpgrade override)
public class DBManager extends SQLiteOpenHelper {

    static final String DB_NAME = "AppropriteBGM_DB";
    static final int DB_VERSION = 1;

    Context mContext = null;
    private static DBManager mDBManager = null;
    private SQLiteDatabase mDataBase = null;

    public static DBManager getInstance (Context context) {
        if (mDBManager == null){
            mDBManager = new DBManager(context, DB_NAME, null, DB_VERSION);
        }

        return mDBManager;
    }

    private DBManager (Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dbName, factory, version);
        mContext = context;

        // DB 생성 및 열기
        mDataBase = getWritableDatabase();
    }

    // DB 최초 생성 이벤트
    @Override
    public void onCreate(SQLiteDatabase db) {
        String[] SQLquery = null;
        // raw 에 있는 테이블 생성 SQL문이 저장되어있는 Text파일 불러오기(sqlite_create.txt)
        InputStream inputStream = mContext.getResources().openRawResource(R.raw.sqlite_create);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            // 파일 내용을 읽어서 byte스트림에 저장
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }

            // 파일을 읽어서 String형으로 저장한 후 ; 를 기준으로 문장을 나눠 저장한다.
            String temp = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
            SQLquery = temp.split(";");
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 파일 내용이 있으면 쿼리문 실행
        if (SQLquery != null) {
            // 초기 설정파일 sqlite_create.txt 파일 읽어온 내용을 실행
            for( int n=0; n<SQLquery.length-1; n++) // split 후 맨 뒤에 아무내용없는 값 제외 -1
                db.execSQL(SQLquery[n]);

            // 내장 BGM 파일 DB 등록
            insertInnerBGM(db);

            Log.i("query!!", "init success");

        } else {
            // 파일 내용이 없으면 종료
            Log.e("query!!", "SQLquery is null");
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DB 테이블 변경이 있는 경우
        if(oldVersion < newVersion) {
            String query = "DROP TABLE IF EXISTS ";
            db.execSQL(query + "Search");
            db.execSQL(query + "Favorite");
            db.execSQL(query + "BGMList");
            db.execSQL(query + "Category");
            onCreate(db);
        }
    }

    // 내장브금 DB에 추가하는 메소드
    private void insertInnerBGM(SQLiteDatabase db){
        String query;

        query = "INSERT INTO BGMList(bgm_name, bgm_path, innerfile) VALUES ('인간극장', 'innerfile', #)";
        query = query.replace("#", Integer.toString(R.raw.human_cinema));
        db.execSQL(query);
//        Log.i("innerfile", query + "\n" + Integer.toString(R.raw.human_cinema));
        query = "INSERT INTO BGMList(bgm_name, bgm_path, innerfile) VALUES ('함정카드', 'innerfile', #)";
        query = query.replace("#", Integer.toString(R.raw.trapcard));
        db.execSQL(query);
    }

    // 브금목록 가져오는 메소드
    public ArrayList<BGMList> getBGMList(int category){
        ArrayList<BGMList> data = new ArrayList<>();    // 결과 저장할 리스트
        BGMList tuple = null;   // 쿼리결과 저장할 각각의 튜플
        String query = null;    // 쿼리문
        Cursor result = null;   // 쿼리결과

        if (category == 1) {  // 카테고리 [전체] 선택
            query = "SELECT * FROM BGMList ORDER BY bgm_name";
        } else {    // 그 외 카테고리 선택
            query = "SELECT * FROM BGMList WHERE category_id="+category+" ORDER BY bgm_name";
        }
        result = mDataBase.rawQuery(query, null);   // 쿼리 실행

        if (result != null){
            while (result.moveToNext()){
                tuple = new BGMList();  // 결과 한줄 저장
                tuple.id = result.getInt(0);
                tuple.name = result.getString(1);
                tuple.path = result.getString(2);
                tuple.innerFileCode = result.getInt(3);
                tuple.categoryId = result.getInt(4);

                data.add(tuple);    // 결과 한줄 리스트에 추가
            }
        }

        return data;
    }

    // 음악파일 탐색범위 가져오는 메소드
    public ArrayList<String> getScanRange(){
        ArrayList<String> data = new ArrayList<>();     // 쿼리결과 저장
        String query = null;    // 쿼리문
        Cursor result = null;   // 쿼리결과

        query = "SELECT scan_path FROM Scan ORDER BY scan_id";
        result = mDataBase.rawQuery(query, null);   // 쿼리 실행

        if (result != null){
            while (result.moveToNext()){
                data.add(result.getString(0));    // 결과 한줄 리스트에 추가
            }
        }

        return data;
    }

    // 검색된 음악파일 개수가 DB에 저장된 개수와 일치하는지 확인하는 메소드
    public boolean checkRecordExist(ArrayList<String> filename){
        int count = filename.size();
        String query = null;
        Cursor result = null;

        query = "SELECT COUNT(*) FROM BGMList WHERE bgm_name in (";
        for(int i=0; i<count; i++){
            // 파일명에서 확장자 부분만 뺀 값이 bgm_name에 저장 되어있다.
            query += "'"+filename.get(i).substring(0, filename.get(i).length()-4)+"',";
        }
        query = query.substring(0, query.length()-1) + ")";   // 맨 끝에 추가된 쉼표(,) 제거하고 괄호닫음

        result = mDataBase.rawQuery(query, null);   // 쿼리 실행
        if(result.moveToNext()){    // 값이 있으면
            return result.getInt(0)==count;     // 검색된 수와 DB에 등록된 수가 같은지 확인
        } else {
            Log.e("DBManager", "checkRecordExist:No result error");
            return false;
        }
    }

    // BGMList 테이블에 레코드 추가하는 메소드
    public void insertBgmList(String nameWithExt, String path){
        String query = null;
        String name = nameWithExt.substring(0, nameWithExt.length()-4);     // 확장자를 뺀 부분
        String fullpath = path + File.separator + nameWithExt;          // 파일까지 합한 절대경로

        // 브금 이름과 경로만 설정해 주면 나머지는 기본값(innerfile코드는 0, 카테고리는 [분류안됨]의 id인 2)
        query = "INSERT INTO BGMList(bgm_name, bgm_path) VALUES ('"+name+"', '"+fullpath+"')";
        mDataBase.execSQL(query);
    }

    // 파일리스트를 넘겨받아서 DB등록 체크한 후 안되어있으면 추가해주는 메소드
    public void checkAndInsertBgmList(ArrayList<String> filelist, String path){
        if (!checkRecordExist(filelist)) {  // 넘겨받은 파일 개수와 DB에 등록된 파일 개수가 안맞을 경우
            String query = null;
            Cursor result = null;
            String filename = null;
            for (int i=0; i<filelist.size(); i++) { // filelist 개수만큼 반복
                filename = filelist.get(i).substring(0, filelist.get(i).length()-4);    // 확장자를 뺀 부분이 filename
                query = "SELECT bgm_id FROM BGMList WHERE bgm_name='"+filename+"'";
                result = mDataBase.rawQuery(query, null);
                if (!result.moveToNext()) {  // 검색된 값이 존재하지 않으면
                    insertBgmList(filelist.get(i), path);   // insert실행
                }
            }
        }
    }
}
