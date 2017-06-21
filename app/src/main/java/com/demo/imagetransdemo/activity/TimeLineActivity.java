package com.demo.imagetransdemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.demo.imagetransdemo.ItemData;
import com.demo.imagetransdemo.MyApplication;
import com.demo.imagetransdemo.R;
import com.demo.imagetransdemo.adapter.TimeLineAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuting on 17/6/20.
 */

public class TimeLineActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TimeLineAdapter timeLineAdapter;
    List<ItemData> datas = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initData();
        timeLineAdapter = new TimeLineAdapter(datas, this);
        recyclerView.setAdapter(timeLineAdapter);
    }

    private void initData() {
        String response = MyApplication.getFromAssets(this,"data.json");
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i=0;i<jsonArray.length();i++){
                ItemData itemData = new ItemData();
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                itemData.text = jsonObject.optString("text");
                JSONArray images = jsonObject.optJSONArray("images");
                for (int j = 0;j<images.length();j++){
                    String url = images.optString(j);
                    itemData.images.add(url);
                }
                datas.add(itemData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
