package com.home_security_officer.MaskMap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class DetailedInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_information);

        Intent intent = getIntent();

        TextView tv = (TextView)findViewById(R.id.seller_name);
        tv.setText(intent.getStringExtra("name"));

        tv = (TextView)findViewById(R.id.seller_type);
        if(intent.getStringExtra("type").equals("01"))
            tv.setText("약국");
        else if(intent.getStringExtra("type").equals("02"))
            tv.setText("우체국");
        else
            tv.setText("농협");

        tv = (TextView)findViewById(R.id.seller_addr);
        tv.setText(intent.getStringExtra("addr"));

        tv = (TextView)findViewById(R.id.seller_stock_at);
        tv.setText(intent.getStringExtra("stock_at"));

        tv = (TextView)findViewById(R.id.seller_remain_stat);
        if(intent.getStringExtra("remain_stat").equals("plenty"))
            tv.setText("100개 이상");
        else if(intent.getStringExtra("remain_stat").equals("some"))
            tv.setText("30개 이상 100개 미만");
        else if(intent.getStringExtra("remain_stat").equals("few"))
            tv.setText("2개 이상 30개 미만");
        else if(intent.getStringExtra("remain_stat").equals("empty"))
            tv.setText("1개 이하");
        else
            tv.setText("판매 중지");

        tv = (TextView)findViewById(R.id.seller_created_at);
        tv.setText(intent.getStringExtra("created_at"));

        ActionBar actionBar = getSupportActionBar() ;
        actionBar.setTitle("판매처 상세 정보");
    }
}
