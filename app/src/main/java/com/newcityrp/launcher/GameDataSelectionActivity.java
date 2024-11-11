package com.newcityrp.launcher;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import android.content.Context;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;

public class GameDataSelectionActivity extends AppCompatActivity {

    Button liteButton, fullButton;
    SharedPreferences apppref;
    UtilManager utilManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_game_type_selection);
        utilManager = new UtilManager(this);

        apppref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        liteButton = findViewById(R.id.dataSelectionLiteButton);
        fullButton = findViewById(R.id.dataSelectionFullButton);

        liteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = apppref.edit();
                editor.putString("gameType", "lite");
                editor.apply();
                utilManager.launchMainActivityFreshly(GameDataSelectionActivity.this);
            }
        });

        fullButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = apppref.edit();
                editor.putString("gameType", "full");
                editor.apply();
                utilManager.launchMainActivityFreshly(GameDataSelectionActivity.this);
            }
        });
    }
}