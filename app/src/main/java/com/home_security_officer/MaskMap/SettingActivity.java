package com.home_security_officer.MaskMap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getSupportActionBar() ;
        actionBar.setTitle("설정");

        Intent intent = getIntent();
        EditText editText = findViewById(R.id.edit_range);
        editText.setText(intent.getStringExtra("range"));

        TextView hyperLink = findViewById(R.id.check_rule_hyper_link);
        SpannableString content = new SpannableString("앱 마켓 모바일콘텐츠 결제 가이드라인 확인하기");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        hyperLink.setText(content);

        Linkify.TransformFilter mTransform = new Linkify.TransformFilter() {
            @Override public String transformUrl(Matcher match, String url) {
                return "";
            }
        };
        Pattern pattern1 = Pattern.compile("앱 마켓 모바일콘텐츠 결제 가이드라인 확인하기");
        Linkify.addLinks(hyperLink, pattern1, "https://github.com/jang5934/MaskStockLookUp/raw/master/mobile.pdf",null,mTransform);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.setting_save :
                EditText range_edit = (EditText) findViewById(R.id.edit_range);

                if(range_edit.getText().toString() == null) {
                    Toast.makeText(SettingActivity.this, "값을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                if(Integer.parseInt(range_edit.getText().toString()) > 5000) {
                    Toast.makeText(SettingActivity.this, "검색 범위는 5000m(5Km) 이내여야 합니다.", Toast.LENGTH_SHORT).show();
                    break;
                }

                intent = new Intent();
                intent.putExtra("range", range_edit.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }
}
