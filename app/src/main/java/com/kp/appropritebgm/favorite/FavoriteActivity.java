package com.kp.appropritebgm.favorite;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.kp.appropritebgm.R;

/**
 * Created by KP on 2015-08-24.
 */
public class FavoriteActivity extends ActionBarActivity {

    private int focusButton = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("즐겨찾기 설정");
        setContentView(R.layout.activity_favorite);
        Intent intent = getIntent();
    }

    public void onClickSelectFavorite(View view){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        String title = "번 즐겨찾기";

        // 선택한 버튼의 번호를 받아서 저장해놓고 BGM선택 액티비티로 값을 넘겨준다.
        // 나중에 다시 받아올 때 해당 번호를 사용하여 DB와 화면을 갱신해야한다.
        switch (view.getId()){
            case R.id.btn_favorite1:{
                title = "1"+title;
                focusButton = 1;
                break;
            }
        }
        bundle.putString("Title", title);
        bundle.putInt("AccessCode", 2);
        intent.putExtra("AccessData", bundle);

        ComponentName componentName = new ComponentName("com.kp.appropritebgm", "com.kp.appropritebgm.SelectBGMActivity");
        intent.setComponent(componentName);
        startActivity(intent);
    }
}
