package com.example.tko_chess;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.tko_chess.ultils.Const;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Lex Somers
 */
public class SingletonUser extends AppCompatActivity {

    // Static variable reference of CurrUser
    // of type SingletonUser
    private static SingletonUser UserInstance = null;

    // JSONObject that holds the currently logged in user's username and password
    private static JSONObject UserObject = null;



    // Static method
    // Static method to create instance of Singleton class or "login" user.
    // Needs to be provided a JSONObject
    public static SingletonUser login(JSONObject user) throws JSONException {
        if (UserInstance == null) {
            UserInstance = new SingletonUser(user);
        }

        return UserInstance;
    }



    // Constructor
    // Here we will be creating private constructor
    // restricted to this class itself
    private SingletonUser(JSONObject user) throws JSONException {
        //Instantiate UserObject
        updateUserObject(user.get("username").toString());
    }



    //Instantiates the UserObejct variable with a JSONObject representation of the user trying to login
    public void updateUserObject(String user) {

        //Get user object from backend
        RequestQueue queue = Volley.newRequestQueue(SingletonUser.this);
        JsonObjectRequest GetUserReq = new JsonObjectRequest(Request.Method.GET, Const.URL_SERVER_GETUSER + user, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                UserObject = response;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        queue.add(GetUserReq);
    }



    // Static method
    // Static method to log user out by nulling the static instance of the class and JSONObject variable
    public static void logout()
    {
        UserInstance = null;
        UserObject = null;
    }



    // Static method
    // Returns an instance of this singleton class.
    public static synchronized SingletonUser getInstance()
    {
        return UserInstance;
    }



    //Returns a JSONObject of the currently logged in user.
    public JSONObject getUserObject() {
        return UserObject;
    }



    //Returns a String with the username of the currently logged in user.
    public String getUsername()  {
        try {
            return (String) UserObject.get("username");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }



    //Returns a String with the password of the currently logged in user.
    public String getPassword() throws JSONException {
        return (String) UserObject.get("password");
    }



    //Returns a JSONArray with Strings of the username and password of the currently logged in user.
    public JSONArray getUserArray() {
        //Instantiate UserArray
        JSONArray UserArray = new JSONArray();
        try {
            UserArray.put(UserObject.get("username"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            UserArray.put(UserObject.get("password"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return UserArray;
    }



    //Returns a JSONArray with Strings of the friends of the currently logged in user.
    public JSONArray getListOfFriends() {
        try {
            return (JSONArray) UserObject.get("friends");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }



    //Returns a JSONArray with Strings of the incoming friend requests of the currently logged in user.
    public JSONArray getListOfIncomingFriendReq() {
        try {
            return (JSONArray) UserObject.get("incomingFriendRequests");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }



    //Returns a JSONArray with Strings of the out going friend requests of the currently logged in user.
    public JSONArray getListOfOutGoingFriendReq() {
        try {
            return (JSONArray) UserObject.get("outgoingFriendRequests");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
