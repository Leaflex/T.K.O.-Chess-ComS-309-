package com.example.tko_chess;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.tko_chess.ultils.Const;

public class LobbyActivity extends AppCompatActivity {

	//Text Declarations
	TextView LobbyName;
	TextView ReadyStatus;
	TextView HostOptions;
	TextView LobbyCode;
	TextView LeaveLobbyError;
	TextView LobbyError;
	TextView LobbyEvent;

	//Button Declarations
	ImageButton LobbyToHostJoin;
	Button Player1Btn;
	Button Player2Btn;
	Button SpectatorBtn;
	ImageButton NotReadyBtn;
	ImageButton ReadyBtn;
	Button StartGameBtn;
	Button GameSettingsBtn;

	//String Declarations
	String GameMode;
	String lobbyCode;
	String HostOrJoin;
	String PlayerOrSpectator = "Spectator";
	String WhoPlayer1;
	String WhoPlayer2;
	String lobbyMessage;

	String URLConcatenation;

	//LinearLayout Declarations
	LinearLayout LobbyOverlay;
	LinearLayout LobbyMembersLayout;

	//JSONArray Declarations
	JSONArray LobbyMembers;

	//User ready tracker
	boolean UserReady = false;

	//Currently logged in user.
	SingletonUser currUser = SingletonUser.getInstance();

	//WebSocket declarations
	private WebSocketClient WebSocket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);

		//Button Initializations
		LobbyToHostJoin = findViewById(R.id.LobbyToHostJoinBtn);
		Player1Btn = findViewById(R.id.Player1Btn);
		Player2Btn = findViewById(R.id.Player2Btn);
		SpectatorBtn = findViewById(R.id.SpectatorBtn);
		NotReadyBtn = findViewById(R.id.NotReadyBtn);
		ReadyBtn = findViewById(R.id.ReadyBtn);
		StartGameBtn = findViewById(R.id.StartGameBtn);
		GameSettingsBtn = findViewById(R.id.GameSettingsBtn);

		//Text Initializations
		LobbyName = findViewById(R.id.LobbyCodeText);
		ReadyStatus = findViewById(R.id.ReadyStatusText);
		HostOptions = findViewById(R.id.HostOptionsText);
		LobbyCode = findViewById(R.id.LobbyCodeText);
		LeaveLobbyError = findViewById(R.id.LeaveLobbyErrorText);
		LobbyError = findViewById(R.id.LobbyErrorText);
		LobbyEvent = findViewById(R.id.LobbyEventText);

		//String Initializations
		GameMode = getIntent().getExtras().getString("Gamemode");
		lobbyCode = getIntent().getExtras().getString("LobbyCode");
		HostOrJoin = getIntent().getExtras().getString("HostOrJoin");
		URLConcatenation = currUser.getUsername() + "/" + HostOrJoin + "/" + LobbyCode;

		//LinearLayout Initializationss
		LobbyOverlay = findViewById(R.id.LobbyOverlayLinearLayout);

		//Display lobby code if spectator.
		if (!HostOrJoin.equals("host")) {
			LobbyCode.setText(lobbyCode);
		}

		//Disable the StartGameBtn initially until host gets CanStart message
		disableStartGame();

		//TODO ///Disable game settings button until we implement something for it./// DELETE AFTER IMPLEMENTING
		GameSettingsBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.faded_soft_blue)));
		GameSettingsBtn.setClickable(false);
		//TODO ///Disable game settings button until we implement something for it./// DELETE AFTER IMPLEMENTING

		//Beginning of WebSocket code
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		Draft[] drafts = {
				new Draft_6455()
		};

		try {
			WebSocket = new WebSocketClient(new URI(Const.URL_SERVER_WEBSOCKETLOBBY + URLConcatenation), (Draft)drafts[0]) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					Log.d("OPEN", "run() returned: " + "is connecting");
					System.out.println("onOpen returned");
				}

				@Override
				public void onMessage(String message) {
					Log.d("", "run() returned: " + message);
					String[] strings = message.split(" ");

					//Clears latest error messages
					LobbyError.setText("");
					LeaveLobbyError.setText("");
					LobbyEvent.setText("");

					switch (strings[0]) {
						case "LobbyCode":
							//Display lobby code
							LobbyCode.setText(strings[1]);

							//Exit switch statement
							break;


						//Starts the game for everyone
						case "StartGame":
							//TODO. Don't forget to close the websocket
							//Take user to chess game
							if (GameMode.equals("Chess")) {
								Intent intent = new Intent(LobbyActivity.this, ChessActivity.class);

								//Sending extra info about type of game and user's role in that game (Spectator or player)
								intent.putExtra("UserRole", PlayerOrSpectator);
								intent.putExtra("Gamemode", GameMode);

								startActivity(intent);
							} else

							//Take user to chess boxing game
							if (GameMode.equals("ChessBoxing")) {
								Intent intent = new Intent(LobbyActivity.this, ChessActivity.class);

								//Sending extra info about type of game and user's role in that game (Spectator or player)
								intent.putExtra("UserRole", PlayerOrSpectator);
								intent.putExtra("Gamemode", GameMode);

								startActivity(intent);
							} else

							//Take user to boxing game
							if (GameMode.equals("Boxing")) {
								Intent intent = new Intent(LobbyActivity.this, BoxingActivity.class);

								//Sending extra info about type of game and user's role in that game (Spectator or player)
								intent.putExtra("UserRole", PlayerOrSpectator);
								intent.putExtra("Gamemode", GameMode);

								startActivity(intent);
							}

							//Disconnects from lobby
							WebSocket.close();

							//Exit switch statement
							break;


						//Updates screen with new ready status of user specified by strings[1]
						case "Ready":
							//Tell users who readied up
							lobbyMessage = strings[1] + "has readied-up.";
							LobbyEvent.setText(lobbyMessage);

							//If user who changed ready status was currUser, store their new ready status.
							if (strings[1].equals(currUser.getUsername())) {
								UserReady = true;
							}

							//Updates lobby display
							getLobbyMembers();
							displayLobbyMembers();

							//Exit switch statement
							break;


						//Updates screen with new ready status of the specified user (strings[1]).
						case "UnReady":
							//Tell users who unreadied
							lobbyMessage = strings[1] + "has un-readied.";
							LobbyEvent.setText(lobbyMessage);

							//If user who changed ready status was currUser, store their new ready status.
							if (strings[1].equals(currUser.getUsername())) {
								UserReady = false;
							}

							//Updates lobby display
							getLobbyMembers();
							displayLobbyMembers();

							//Exit switch statement
							break;


						//Updates screen with new PlayerType of the specified user (strings[1]).
						case "Switch":
							//Tell users who switched to what
							lobbyMessage = strings[3] + "(" + strings[1] + ")" + "switched to " + strings[2] + ".";
							LobbyEvent.setText(lobbyMessage);

							//If user who changed PlayerType was currUser, store their new PlayerType.
							if (strings[3].equals(currUser.getUsername())) {
								PlayerOrSpectator = strings[2];
							}

							//If player 1 is the user changing their PlayerType, then update WhoPlayer1
							if (strings[3].equals(WhoPlayer1)) {
								WhoPlayer1 = "";
							}

							//If player 2 is the user changing their PlayerType, then update WhoPlayer2
							if (strings[3].equals(WhoPlayer2)) {
								WhoPlayer2 = "";
							}

							//If user is changing to player 1, update WhoPlayer1
							if (strings[2].equals("Player1")) {
								WhoPlayer1 = strings[3];
							}

							//If user is changing to player 2, update WhoPlayer2
							if (strings[2].equals("Player2")) {
								WhoPlayer2 = strings[3];
							}

							//Updates lobby display
							getLobbyMembers();
							displayLobbyMembers();

							//Exit switch statement
							break;


						//Updates screen by removing the user who left.
						case "PlayerLeft":
							//Tell users who left
							lobbyMessage = strings[2] + "(" + strings[1] + ")" + "has left.";
							LobbyEvent.setText(lobbyMessage);

							//Updates who is player 1
							if (strings[1].equals("Player1")) {
								WhoPlayer1 = "";
							} else

								//Updates who is player 2
								if (strings[1].equals("Player2")) {
									WhoPlayer2 = "";
								}

							//Updates lobby display
							getLobbyMembers();
							displayLobbyMembers();

							//Exit switch statement
							break;


						//Updates screen by adding the new user that joined. User will be spectator by default.
						case "Spectator":
							//Tell users who joined
							lobbyMessage = strings[1] + "has joined.";
							LobbyEvent.setText(lobbyMessage);

							//Updates lobby display
							getLobbyMembers();
							displayLobbyMembers();

							//Exit switch statement
							break;


						//Closes websocket and displays lobby exit overlay
						case "Kicked":
						case "HostLeft":
							WebSocket.close();

							//Hides and disables all buttons
							hideAllButtons();

							//Displays overlay for host leaving
							displayExitLobbyOverlay(strings[0]);

							//Exit switch statement
							break;


						//Enables the start game button
						case "CanStart":
							//Tell host both players are ready
							lobbyMessage = "Both players ready.";
							LobbyEvent.setText(lobbyMessage);

							enableStartGame();

							//Exit switch statement
							break;


						//Enables the start game button
						case "CannotStart":
							disableStartGame();

							//Exit switch statement
							break;
					}
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					Log.d("CLOSE", "onClose() returned: " + reason);
					System.out.println("onClose returned");
				}

				@Override
				public void onError(Exception e) {
					Log.d("Exception:", e.getMessage().toString());
					LobbyError.setText("An error occurred.");

				}
			};
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		}

		//Connects to the websocket
		WebSocket.connect();
		//End of WebSocket code
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


		//Takes user back to host or join screen
		LobbyToHostJoin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Clears latest error messages
				LobbyError.setText("");
				LeaveLobbyError.setText("");
				LobbyEvent.setText("");

				//If User is a player, and are readied up, then don't leave lobby.
				if ((!PlayerOrSpectator.equals("Spectator")) && (UserReady)) {
					LeaveLobbyError.setText("Please unready before leaving.");
				}
				//If user is a spectator or a player who is not readied up, then leave the lobby.
				else {
					WebSocket.close();

					Intent intent = new Intent(LobbyActivity.this, HostJoinActivity.class);
					intent.putExtra("Gamemode", GameMode);
					startActivity(intent);
				}
			}
		});



		//Changes user's role in the lobby to player 1 if possible.
		Player1Btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Clears latest error messages
				LobbyError.setText("");
				LeaveLobbyError.setText("");
				LobbyEvent.setText("");

				//Change user PlayerType to player 1 if user is not already player 1
				if (!WhoPlayer1.equals(currUser.getUsername())) {
					WebSocket.send("SwitchToP1");
				}
			}
		});



		//Changes user's role in the lobby to player 2 if possible.
		Player2Btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Clears latest error messages
				LobbyError.setText("");
				LeaveLobbyError.setText("");
				LobbyEvent.setText("");

				//Change user PlayerType to player 2 if user is not already player 2
				if (!WhoPlayer2.equals(currUser.getUsername())) {
					WebSocket.send("SwitchToP2");
				}
			}
		});



		//Changes user's role in the lobby to spectator.
		SpectatorBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Clears latest error messages
				LobbyError.setText("");
				LeaveLobbyError.setText("");
				LobbyEvent.setText("");

				//Change user PlayerType to spectator if user is not already spectator
				if (!PlayerOrSpectator.equals("Spectator")) {
					WebSocket.send("SwitchToSpectate"); //TODO check that message should not be "SwitchToSpectator" instead of "SwitchToSpectate"
				}
			}
		});



		//Changes status of user to not ready
		NotReadyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Clears latest error messages
				LobbyError.setText("");
				LeaveLobbyError.setText("");
				LobbyEvent.setText("");

				//Change user's ready status to unready if user is not already "not ready".
				if (UserReady) {
					WebSocket.send("UnReady");

					//If user is not ready, enable leaving the lobby.
					enableLeaveLobby();
				}
			}
		});



		//Changes status of user to ready
		ReadyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Clears latest error messages
				LobbyError.setText("");
				LeaveLobbyError.setText("");
				LobbyEvent.setText("");

				//Change user's ready status to ready if user is not already "ready".
				if (UserReady) {
					WebSocket.send("Ready");

					//If user is ready, disable leaving the lobby.
					disableLeaveLobby();
				}
			}
		});



		//Starts games for all players and spectators
		StartGameBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Clears latest error messages
				LobbyError.setText("");
				LeaveLobbyError.setText("");
				LobbyEvent.setText("");

				WebSocket.send("Start " + GameMode);
			}
		});



		//Hide and disable host options/ready status
		hideViews();
	}



	//Hides views on screen depending on the user's role in the lobby
	private void hideViews() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//If user is not host, hide host options
				if (HostOrJoin.equals("join")) {
					//Hides host options from user.
					HostOptions.setVisibility(View.INVISIBLE);
					StartGameBtn.setVisibility(View.INVISIBLE);
					GameSettingsBtn.setVisibility(View.INVISIBLE);

					//Disables host option buttons
					StartGameBtn.setClickable(false);
					GameSettingsBtn.setClickable(false);
				} else

				//If user is spectator, hide ready status options
				if (PlayerOrSpectator.equals("spectator")) {
					//Hides ready status options from user
					ReadyStatus.setVisibility(View.INVISIBLE);
					NotReadyBtn.setVisibility(View.INVISIBLE);
					ReadyBtn.setVisibility(View.INVISIBLE);

					NotReadyBtn.setClickable(false);
					ReadyBtn.setClickable(false);
				}
			}
		});
	}



	//Disables and hides all buttons on screen
	private void hideAllButtons() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LobbyToHostJoin.setVisibility(View.INVISIBLE);
				Player1Btn.setVisibility(View.INVISIBLE);
				Player2Btn.setVisibility(View.INVISIBLE);
				SpectatorBtn.setVisibility(View.INVISIBLE);
				NotReadyBtn.setVisibility(View.INVISIBLE);
				ReadyBtn.setVisibility(View.INVISIBLE);
				StartGameBtn.setVisibility(View.INVISIBLE);
				GameSettingsBtn.setVisibility(View.INVISIBLE);

				LobbyToHostJoin.setClickable(false);
				Player1Btn.setClickable(false);
				Player2Btn.setClickable(false);
				SpectatorBtn.setClickable(false);
				NotReadyBtn.setClickable(false);
				ReadyBtn.setClickable(false);
				StartGameBtn.setClickable(false);
				GameSettingsBtn.setClickable(false);
			}
		});
	}



	//Displays the host left overlay
	private void displayExitLobbyOverlay(String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				View inflatedLayout = getLayoutInflater().inflate(R.layout.lobby_exitlobby_layout, null, false);
				Button LobbyToMenuBtn = (Button) inflatedLayout.findViewById(R.id.LobbyToMenuBtn);
				TextView ExitLobbyText = (TextView) inflatedLayout.findViewById(R.id.ExitLobbyText);

				//If host left, display host left message
				if (message.equals("HostLeft")) {
					ExitLobbyText.setText("The host has left the lobby.");
				} else

				//If kicked, display kicked message
				if (message.equals("Kicked")) {
					ExitLobbyText.setText("You have been kicked.");
				}


				LobbyToMenuBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						//Returns user to main menu
						Intent intent = new Intent(LobbyActivity.this, MainMenuActivity.class);
						startActivity(intent);
					}
				});

				LobbyOverlay.addView(inflatedLayout);
			}
		});
	}



	//Enables start game button
	private void enableStartGame() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				StartGameBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.soft_blue)));
				StartGameBtn.setClickable(true);
			}
		});
	}



	//Disables start game button
	private void disableStartGame() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				StartGameBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.faded_soft_blue)));
				StartGameBtn.setClickable(false);
			}
		});
	}



	//Disables leave lobby button
	private  void disableLeaveLobby() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LobbyToHostJoin.setClickable(false);
			}
		});
	}



	//Disables leave lobby button
	private  void enableLeaveLobby() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LobbyToHostJoin.setClickable(true);
			}
		});
	}



	//Gets array of all members in the lobby and their information
	private void getLobbyMembers() {
		RequestQueue queue = Volley.newRequestQueue(LobbyActivity.this);
		JsonArrayRequest lobbyMembersReq = new JsonArrayRequest(Request.Method.GET, Const.URL_SERVER_GETLOBBY + LobbyCode, null, new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray response) {
				LobbyMembers = response;
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});

		//Send the request we created
		queue.add(lobbyMembersReq);
	}



	//Displays the lobby's current members and related info.
	private void displayLobbyMembers() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LobbyMembersLayout.removeAllViews();
				LobbyMembersLayout = findViewById(R.id.LobbyLinearLayout);

				for (int i = 0; i < LobbyMembers.length(); i++) {
					//Get lobby member's username, player type, and ready status (for spectators, ready status default is NotReady)
					String[] memberInfo;
					try {
						memberInfo = LobbyMembers.get(i).toString().split(" ");
					} catch (JSONException e) {
						throw new RuntimeException(e);
					}

					//Display members in lobby for host (with kick button)
					if (HostOrJoin.equals("host")) {
						View inflatedLayout = getLayoutInflater().inflate(R.layout.lobby_host_layout, null, false);
						TextView MemberNameText = (TextView) inflatedLayout.findViewById(R.id.MemberNameTextView);
						ImageView MemberReadyStatus = (ImageView) inflatedLayout.findViewById(R.id.ReadyStatusImageView);
						Button KickMemberBtn = (Button) inflatedLayout.findViewById(R.id.KickMemberBtn);

						//Display member's username
						MemberNameText.setText(memberInfo[1]);

						//Display member as spectator
						if (memberInfo[2].equals("Spectator")) {
							MemberReadyStatus.setImageResource(R.drawable.spectator);
						} else

							//Display member's ready status if they are a player
							if (memberInfo[2].equals("Player1") || memberInfo[2].equals("Player2")) {
								if (memberInfo[3].equals("Ready")) {
									MemberReadyStatus.setImageResource(R.drawable.readystatus);
								} else if (memberInfo[3].equals("NotReady")) {
									MemberReadyStatus.setImageResource(R.drawable.notreadystatus);
								}
							}

						//Kicks user from lobby
						KickMemberBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								WebSocket.send("Kick " + memberInfo[1]);
							}
						});

						//Adds member object to screen
						LobbyMembersLayout.addView(inflatedLayout);

					} else

						//Display members in lobby for member (without kick button)
						if (HostOrJoin.equals("join")) {
							View inflatedLayout = getLayoutInflater().inflate(R.layout.lobby_member_layout, null, false);
							TextView MemberNameText = (TextView) inflatedLayout.findViewById(R.id.MemberTextView);
							ImageView MemberReadyStatus = (ImageView) inflatedLayout.findViewById(R.id.PlayerStatusImageView);

							//Display member's username
							MemberNameText.setText(memberInfo[1]);

							//Display member as spectator
							if (memberInfo[2].equals("Spectator")) {
								MemberReadyStatus.setImageResource(R.drawable.spectator);
							} else

								//Display member's ready status if they are a player
								if (memberInfo[2].equals("Player1") || memberInfo[2].equals("Player2")) {
									if (memberInfo[3].equals("Ready")) {
										MemberReadyStatus.setImageResource(R.drawable.readystatus);
									} else if (memberInfo[3].equals("NotReady")) {
										MemberReadyStatus.setImageResource(R.drawable.notreadystatus);
									}
								}

							//Adds member object to screen
							LobbyMembersLayout.addView(inflatedLayout);
						}
				}
			}
		});
	}
}
