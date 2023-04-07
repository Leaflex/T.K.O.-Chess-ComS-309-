package com.example.tko_chess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class TKOHostOrJoinActivity extends AppCompatActivity {

    Button hostBtn;
    Button joinBtn;
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tko_host_or_join);
        backBtn = findViewById(R.id.backBtn9);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TKOHostOrJoinActivity.this, MainMenuActivity.class);
                startActivity(intent);
            }
        });

        hostBtn = findViewById(R.id.hostBtn3);
        hostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TKOHostOrJoinActivity.this, TKOLobbyActivity.class);
                startActivity(intent);
            }
        });

        joinBtn = findViewById(R.id.joinBtn);
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TKOHostOrJoinActivity.this, ChessLobbyPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}