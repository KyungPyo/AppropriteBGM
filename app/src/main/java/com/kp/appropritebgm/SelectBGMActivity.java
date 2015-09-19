package com.kp.appropritebgm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.kp.appropritebgm.DBControl.BGMList;
import com.kp.appropritebgm.DBControl.DBManager;

import java.util.ArrayList;

/**
 * Created by KP on 2015-08-28.
 */
public class SelectBGMActivity extends ActionBarActivity {

    private DBManager dbManager = null;
    private ListView listView = null;
    private ArrayList<BGMList> listdata = null;
    private BgmListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String accessTitle = null;
        int accessCode = 0;

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("AccessData");
        if(bundle == null){ // 넘어온 인텐트 안의 Bundle Data에 값이 없을 경우
            Toast.makeText(this, "잘못된 접근:NoData", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            accessTitle = bundle.getString("Title");
            setTitle(accessTitle + "에 추가할 브금 선택");
            accessCode = bundle.getInt("AccessCode");
        }
        setContentView(R.layout.activity_selectbgm);

        if(accessTitle != null && accessCode != 0) { // intent가 값을 정상적으로 받아온 접근일 경우에만
            dbManager = DBManager.getInstance(this);    // 내장DB 접속
            drawListView(accessCode);
        } else {
            Toast.makeText(this, "잘못된 접근:NoValues", Toast.LENGTH_SHORT).show();
            finish();   // 비정상일경우 이전 액티비티로
        }
    }

    private void drawListView(int accessCode){
        listdata = dbManager.getBGMList(1);             // 리스트뷰에 출력시킬 BGM 리스트 내장DB에서 읽어옴(전체:1)
        adapter = new BgmListAdapter(this, listdata, accessCode);

        listView=(ListView)findViewById(R.id.list_atv_select);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
    }

    private ListView.OnItemClickListener itemClickListener
            = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            listView.setSelection(position);            // listView.getSelectedItemPosition(); 사용해서 나중에 받아올꺼
            adapter.setSelectedItem(position);          // 화면에 highlight로 표시하기 위한 것
            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), adapter.getItem(position) + " " + position + " " + listView.isItemChecked(position), Toast.LENGTH_SHORT).show();
        }
    };
}
