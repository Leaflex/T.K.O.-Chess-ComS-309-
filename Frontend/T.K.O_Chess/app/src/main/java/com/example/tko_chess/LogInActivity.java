package com.example.tko_chess;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.tko_chess.ultils.Const;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Lex Somers
 */
public class LogInActivity extends AppCompatActivity {

    EditText Username, Password;
    TextView LoginError;
    Button Login, LoginToRegister;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Register button that takes user to register page so they can make an account.
        LoginToRegister = (Button) findViewById(R.id.toRegisterBtn);
        LoginToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, RegisterActivity.class));
            }
        });



        //Login button that takes users to main menu after inputting a username and password
        Login = (Button) findViewById(R.id.LoginButton);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Text fields for users to enter username/password for their account
                Username = (EditText) findViewById(R.id.UsernameText);
                Password = (EditText) findViewById(R.id.PasswordText);
                LoginError = (TextView) findViewById(R.id.LoginErrorText);

                //Strings containing username/password. Used to check that user does exist in database.
                String username = Username.getText().toString();
                String password = Username.getText().toString();

                //JSONObejct containing login info that will be send to login for a login request
                JSONObject user = new JSONObject();
                try {
                    user.put("username", username);
                    user.put("password", password);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                //Create a Request Que for the JsonObjectRequest
                RequestQueue queue = Volley.newRequestQueue(LogInActivity.this);

                //Checks to see if there is a user that matches the input username and login.
                JsonObjectRequest userObjectReq = new JsonObjectRequest(Request.Method.POST, Const.URL_SERVER_LOGIN, user,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                String temp;

                                //Get confirmation/failure of login message from backend. Throw error if response is not string
                                try {
                                    temp = (String) response.get("message");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }

                                //If login was "success", take user to main menu screen.
                                if (temp.equals("true")) {
                                    SingletonUser currUser = SingletonUser.getInstance();
                                    try {
                                        currUser.updateUserObject(user.get("username").toString(), context);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }

                                    Intent intent = new Intent(LogInActivity.this, MainMenuActivity.class);
                                    startActivity(intent);
                                }

                                //else, show error message
                                else {
                                    try {
                                        LoginError.setText(response.get("message").toString());
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println(error.toString());
                                LoginError.setText("An error occurred.");
                            }
                        });

                queue.add(userObjectReq);
            }
        });
    }
}