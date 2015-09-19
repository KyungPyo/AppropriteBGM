package com.kp.appropritebgm;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.kp.appropritebgm.DBControl.BGMList;

import java.util.ArrayList;

/**
 * Created by KP on 2015-08-20.
 */
public class BgmListAdapter extends BaseAdapter {

    private Context mContext = null;
    private ArrayList<BGMList> mData = null;
    private LayoutInflater mLayoutInflater = null;
    private int accessCode = 0;
    private int selectedItem = -1;

    class ViewHolder{
        ImageView itemIcon;
        TextView itemBgmName;
        CheckBox itemCheckBox;
        boolean isSelected = false;
    }

    public BgmListAdapter(Context context, ArrayList<BGMList> data, int code){
        mContext = context;
        mData = data;
        mLayoutInflater = LayoutInflater.from(context);
        accessCode = code;
        // 접근하는 곳에 따라서 코드가 다름. 해당 코드에 맞는 뷰를 제공
        // 0:비정상 접근  1:MainActivity  2:FavoriteActivity  3:CategoryActivity
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position).getName();
    }

    @Override
    public long getItemId(int position) { return mData.get(position).getId(); }

    public int getBgmId(int position) { return mData.get(position).getId();}
    public String getBgmName(int position){ return mData.get(position).getName(); }
    public String getBgmPath(int position){ return mData.get(position).getPath(); }
    public int getBgmInnerFileCode(int position){ return mData.get(position).getInnerFileCode(); }
    public int getBgmCategoryId(int position){ return mData.get(position).getCategoryId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();
        View itemLayout = convertView;  // 재사용할 뷰
        ViewHolder viewHolder = null;   // 자식 뷰 저장해놓을 뷰홀더

        // 재사용할 뷰가 없으면 새로 생성. 리스트의 한 항목에 해당하는 레이아웃
        if(itemLayout == null) {
            itemLayout = mLayoutInflater.inflate(R.layout.listitem_mainactivity, null);
            // View Holder를 생성하여 저장해두고 다음에 리스트아이템을 재사용할 때 참조한다.

            viewHolder = setLayoutType(itemLayout, accessCode);

            itemLayout.setTag(viewHolder);  // 사용자 정의 데이터 보관

        } else {    // 뷰를 재사용하는 경우
            viewHolder = (ViewHolder)itemLayout.getTag();
        }

        // 레이아웃 갱신
        viewHolder.itemBgmName.setText(mData.get(position).getName());
        if (accessCode == 2){   // FavoriteActivity에서 호출했으면 아이템 선택기능 추가
            highlightItem(position, itemLayout, viewHolder);
        }


//        // 리스트 아이템에 이벤트 리스너 등록
//        itemLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("BgmListAdapter", "OnClickListener");
//                Toast.makeText(context, getItem(pos) + " " + pos, Toast.LENGTH_SHORT).show();
//            }
//        });
        return itemLayout;
    }

    public ViewHolder setLayoutType(View item, int code){
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.itemIcon = (ImageView)item.findViewById(R.id.mainlist_item_icon);
        viewHolder.itemBgmName = (TextView)item.findViewById(R.id.mainlist_item_bgmname);
        viewHolder.itemCheckBox = (CheckBox)item.findViewById(R.id.mainlist_item_checkbox);

        switch (code){
            case 0: {   // 잘못된 접근
                break;
            }
            case 1: {   // MainActivity
                break;
            }
            case 2: {   // FavoriteActivity
                viewHolder.itemCheckBox.setEnabled(false);
                viewHolder.itemCheckBox.setVisibility(View.INVISIBLE);
                viewHolder.itemCheckBox.setChecked(false);
                break;
            }
            case 3: {   // CategoryActivity
                break;
            }
        }

        return viewHolder;
    }


    public void setSelectedItem(int position) {
        selectedItem = position;
    }

    private void highlightItem(int position, View item, ViewHolder viewHolder){
        Log.i("highlight", "excuted");
        // selectedItem을 어댑터를 사용하는 액티비티의 아이템선택 리스너에서 변경해주어야한다.
        // 선택한 아이템과 position이 일치하고 아직 선택이 안되어있으면 선택상태로
        if (position == selectedItem) {
            item.setBackgroundResource(R.color.list_select);
        } else {
            item.setBackgroundResource(R.drawable.item_selector);
        }
    }

    public void refreshData(ArrayList<BGMList> newData){
        mData = newData;       // 바뀐 데이터 적용하고
        notifyDataSetChanged(); // 리스트 새로고침
    }
}
