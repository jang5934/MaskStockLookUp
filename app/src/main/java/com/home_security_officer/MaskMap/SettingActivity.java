package com.home_security_officer.MaskMap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

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
