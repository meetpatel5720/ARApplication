package com.mp.android.myarapp.Activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.mp.android.myarapp.R;

import static com.mp.android.myarapp.Misc.Constant.IS_PLANE_RENDER_VISIBLE;
import static com.mp.android.myarapp.Misc.Constant.SHARED_PREFS;

public class SettingActivity extends AppCompatActivity {

    Switch isPlaneRendererEnable;
    boolean isPlaneRenderVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        isPlaneRendererEnable = findViewById(R.id.plan_renderer_switch);

        isPlaneRendererEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                savePrefs();
            }

        });
        
        loadPrefs();
        updateView();
    }

    private void savePrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(IS_PLANE_RENDER_VISIBLE,isPlaneRendererEnable.isChecked());

        editor.apply();
    }

    private void loadPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        isPlaneRenderVisible = sharedPreferences.getBoolean(IS_PLANE_RENDER_VISIBLE,false);
    }

    private void updateView() {
        isPlaneRendererEnable.setChecked(isPlaneRenderVisible);
    }
}
