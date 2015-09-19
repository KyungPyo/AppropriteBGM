package com.kp.appropritebgm;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by KP on 2015-08-26.
 */
public class SettingActivity extends ActionBarActivity {

    private ListView listView = null;
    private ArrayAdapter<String> adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("환경설정");
        setContentView(R.layout.activity_setting);

        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);
        listView = (ListView)findViewById(R.id.list_atv_setting);
        listView.setAdapter(adapter);

        adapter.add("파일 탐색범위 설정");
        adapter.add("버전 정보 확인");

        listView.setOnItemClickListener(onItemClickListener);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), adapter.getItem(position)+" "+position, Toast.LENGTH_SHORT).show();
        }
    };
}
