package com.example.tko_chess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.tko_chess.ultils.Const;


public class TKOLobbyKeyActivity extends AppCompatActivity {

    EditText lobbyKey;
    Button joinBtn;
    ImageButton backBtn;
    TextView error;
    String URLConcatenation = "";
    SingletonUser currUser = SingletonUser.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tko_lobby_key);

        backBtn = findViewById(R.id.backBtn10);

        //Goes back to Host or Join Screen
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TKOLobbyKeyActivity.this, ChessHostOrJoinActivity.class);
                startActivity(intent);
            }
        });

        joinBtn = findViewById(R.id.joinBtn3);

//        //Takes user to joined lobby
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lobbyKey = (EditText) findViewById(R.id.lobbyKey);
                error = (TextView) findViewById(R.id.loginErrorText);

                String lobbyPassword = lobbyKey.getText().toString();

                URLConcatenation = lobbyPassword + "/" + currUser.getUsername();

                RequestQueue queue = Volley.newRequestQueue(TKOLobbyKeyActivity.this);

                StringRequest joinLobby = new StringRequest(Request.Method.PUT,Const.URL_SERVER_LOBBYKEY + URLConcatenation,
                        new Response.Listener<String>(){
                            @Override
                            public void onResponse(String response){
                                if(response.equals("success")){
                                    Intent intent = new Intent(TKOLobbyKeyActivity.this, TKOLobbyActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError Error){
                        System.out.println(error.toString());
                        error.setText("Uh Oh SpaghettiOs");
                    }
                });
                queue.add(joinLobby);
            }
        });

    }
}