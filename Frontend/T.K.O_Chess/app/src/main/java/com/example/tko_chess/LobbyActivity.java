package com.example.tko_chess;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.Image;
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
import org.w3c.dom.Text;

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
	TextView LobbyCodeText;
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
	String GameMode = "";
	String LobbyCode = "";
	String HostOrJoin = "";
	String PlayerOrSpectator = "Spectator";
	String WhoPlayer1 = "";
	String WhoPlayer2 = "";
	String lobbyMessage = "";

	String URLConcatenation = "";

	//LinearLayout Declarations
	LinearLayout LobbyOverlay;
	LinearLayout LobbyMembersLayout;

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
		LobbyCodeText = findViewById(R.id.LobbyCodeText);
		LeaveLobbyError = findViewById(R.id.LeaveLobbyErrorText);
		LobbyError = findViewById(R.id.LobbyErrorText);
		LobbyEvent = findViewById(R.id.LobbyEventText);

		//String Initializations
		GameMode = getIntent().getExtras().getString("Gamemode");
		LobbyCode = getIntent().getExtras().getString("LobbyCode");
		HostOrJoin = getIntent().getExtras().getString("HostOrJoin");
		URLConcatenation = currUser.getUsername() + "/" + HostOrJoin + "/" + LobbyCode;

		//LinearLayout Initializationss
		LobbyOverlay = findViewById(R.id.LobbyOverlayLinearLayout);
		LobbyMembersLayout = findViewById(R.id.LobbyLinearLayout);

		//Display lobby code if spectator.
		if (!HostOrJoin.equals("host")) {
			LobbyCodeText.setText(LobbyCode);
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
						case "JustJoined":
							String[] temp = message.split(" ");
							String[] members = temp[1].split("#");

							//Updates lobby display
							displayLobbyMembers(members);

							//Exit switch statement
							break;


						case "LobbyCode":
							//Display lobby code
							LobbyCode = strings[1];
							LobbyCodeText.setText(LobbyCode);

							//Exit switch statement
							break;


						//Starts the game for everyone
						case "StartGame":
							//Disconnects from lobby
							WebSocket.close();

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

							//Exit switch statement
							break;


						//Updates screen with new ready status of user specified by strings[1]
						case "Ready":
							//Updates client side info
							userReadied(strings);

							//If user is ready, disable leaving the lobby.
							disableLeaveLobby();

							//TODO Delete this if new method works
							//Updates lobby display
							//getLobbyMembers();

							//Exit switch statement
							break;


						//Updates screen with new ready status of the specified user (strings[1]).
						case "UnReady":
							//Updates client side info
							userUnReadied(strings);

							//If user is not ready, enable leaving the lobby.
							enableLeaveLobby();


							//TODO Delete this if new method works
							//Updates lobby display
							//getLobbyMembers();

							//Exit switch statement
							break;


						//Updates screen with new PlayerType of the specified user (strings[1]).
						case "Switch":
							//Updates client side info
							switchPlayerType(strings);

							//TODO Delete this if new method works
							//Updates lobby display
							//getLobbyMembers();

							//Hides or displays ready status buttons
							hideOrShowViews();

							//Exit switch statement
							break;


						//Updates screen by removing the user who left.
						case "PlayerLeft":
							//Updates client side info
							playerLeftLobby(strings);

							//TODO Delete this if new method works
							//Updates lobby display
							//getLobbyMembers();

							//Exit switch statement
							break;


						//Updates screen by adding the new user that joined. User will be spectator by default.
						case "Spectator":
							//Tells users spectator joined
							spectatorJoined(strings);

							//TODO Delete this if new method works
							//Updates lobby display
							//getLobbyMembers();

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
							//Tells host they can start the game
							hostCanStart(strings);

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
		hideOrShowViews();
	}



	//Hides views on screen depending on the user's role in the lobby
	private void hideOrShowViews() {
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
		if (PlayerOrSpectator.equals("Spectator")) {
			//Hides ready status options from user
			ReadyStatus.setVisibility(View.INVISIBLE);
			NotReadyBtn.setVisibility(View.INVISIBLE);
			ReadyBtn.setVisibility(View.INVISIBLE);

			NotReadyBtn.setClickable(false);
			ReadyBtn.setClickable(false);
		} else

		if (PlayerOrSpectator.equals("Player1") || PlayerOrSpectator.equals("Player2")) {
			//Hides ready status options from user
			ReadyStatus.setVisibility(View.VISIBLE);
			NotReadyBtn.setVisibility(View.VISIBLE);
			ReadyBtn.setVisibility(View.VISIBLE);

			NotReadyBtn.setClickable(true);
			ReadyBtn.setClickable(true);
		}
	}



	//Disables and hides all buttons on screen
	private void hideAllButtons() {
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



	//Displays the host left overlay
	private void displayExitLobbyOverlay(String message) {
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



	//Enables start game button
	private void enableStartGame() {
		StartGameBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.soft_blue)));
		StartGameBtn.setClickable(true);
	}



	//Disables start game button
	private void disableStartGame() {
		StartGameBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.faded_soft_blue)));
		StartGameBtn.setClickable(false);
}



	//Disables leave lobby button
	private  void disableLeaveLobby() {
		LobbyToHostJoin.setClickable(false);
	}



	//Disables leave lobby button
	private void enableLeaveLobby() {
		LobbyToHostJoin.setClickable(true);
	}



	//Switches client side data for role that was switched
	private void switchPlayerType(String[] strings) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//Tell users who switched to what
				lobbyMessage = strings[3] + " (" + strings[1] + ")" + " switched to " + strings[2] + ".";
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

				//Change lobby member's playerType icon
				int numChildren = LobbyMembersLayout.getChildCount();
				for (int i = 0; i < numChildren; i++) {
					LinearLayout member = (LinearLayout) LobbyMembersLayout.getChildAt(i);
					TextView memberName = (TextView) member.getChildAt(0);

					//If this member object is the member who switched, then...
					if (memberName.getText().equals(strings[3])) {
						ImageView readyOrSpectator = (ImageView) member.getChildAt(1);

						//If user is switching to a player, change spectator image to not ready image.
						if (strings[2].equals("Player1") || strings[2].equals("Player2")) {
							readyOrSpectator.setImageResource(R.drawable.notreadystatus);
						} else

						//If user is switching to spectator change ready image to spectator image.
						if (strings[2].equals("Spectator")) {
							readyOrSpectator.setImageResource(R.drawable.spectator);
						}

					}
				}
			}
		});
	}



	//Updates client info about user ready status
	private void userUnReadied(String[] strings) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//Tell users who unreadied
				lobbyMessage = strings[1] + " has un-readied.";
				LobbyEvent.setText(lobbyMessage);

				//If user who changed ready status was currUser, store their new ready status.
				if (strings[1].equals(currUser.getUsername())) {
					UserReady = false;
				}

				//Change lobby member's playerType icon
				int numChildren = LobbyMembersLayout.getChildCount();
				for (int i = 0; i < numChildren; i++) {
					LinearLayout member = (LinearLayout) LobbyMembersLayout.getChildAt(i);
					TextView memberName = (TextView) member.getChildAt(0);

					//If this member object is the member who unreadied, then...
					if (memberName.getText().equals(strings[1])) {
						ImageView readyStatus = (ImageView) member.getChildAt(1);
						readyStatus.setImageResource(R.drawable.notreadystatus);
					}
				}
			}
		});
	}



	//Updates client info about user ready status
	private void userReadied(String[] strings) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//Tell users who readied up
				lobbyMessage = strings[1] + " has readied-up.";
				LobbyEvent.setText(lobbyMessage);

				//If user who changed ready status was currUser, store their new ready status.
				if (strings[1].equals(currUser.getUsername())) {
					UserReady = true;
				}

				//Change lobby member's playerType icon
				int numChildren = LobbyMembersLayout.getChildCount();
				for (int i = 0; i < numChildren; i++) {
					LinearLayout member = (LinearLayout) LobbyMembersLayout.getChildAt(i);
					TextView memberName = (TextView) member.getChildAt(0);

					//If this member object is the member who readied, then...
					if (memberName.getText().equals(strings[1])) {
						ImageView readyStatus = (ImageView) member.getChildAt(1);
						readyStatus.setImageResource(R.drawable.readystatus);
					}
				}
			}
		});
	}



	//Updates client info about players
	private void playerLeftLobby(String[] strings) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//Tell users who left
				lobbyMessage = strings[2] + " (" + strings[1] + ")" + " has left.";
				LobbyEvent.setText(lobbyMessage);

				//Updates who is player 1
				if (strings[1].equals("Player1")) {
					WhoPlayer1 = "";
				} else

					//Updates who is player 2
					if (strings[1].equals("Player2")) {
						WhoPlayer2 = "";
					}

				//Remove member visual from lobby
				int numChildren = LobbyMembersLayout.getChildCount();
				for (int i = 0; i < numChildren; i++) {
					LinearLayout member = (LinearLayout) LobbyMembersLayout.getChildAt(i);
					TextView memberName = (TextView) member.getChildAt(0);

					//If this member object is the member who left, then...
					if (memberName.getText().equals(strings[2])) {
						LobbyMembersLayout.removeViewAt(i);
					}
				}
			}
		});
	}



	//Updates client that spectator has joined
	private void spectatorJoined(String[] strings) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//Tell users who joined
				lobbyMessage = strings[1] + " has joined.";
				LobbyEvent.setText(lobbyMessage);

				//Add host visual
				if (HostOrJoin.equals("host")) {
					//Create member object
					View newMember = getLayoutInflater().inflate(R.layout.lobby_host_layout, null, false);
					TextView MemberNameText = (TextView) newMember.findViewById(R.id.MemberNameTextView);
					ImageView MemberReadyStatus = (ImageView) newMember.findViewById(R.id.ReadyStatusImageView);
					Button KickMemberBtn = (Button) newMember.findViewById(R.id.KickMemberBtn);

					//Set name and spectator image
					MemberNameText.setText(strings[1]);
					MemberReadyStatus.setImageResource(R.drawable.spectator);

					//Kicks user from lobby
					KickMemberBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							WebSocket.send("Kick " + strings[1]);
						}
					});

					//Add the new member object to the screen.
					LobbyMembersLayout.addView(newMember);
				} else

				//Add member visual
				if (HostOrJoin.equals("join")) {
					//Create member object
					View newMember = getLayoutInflater().inflate(R.layout.lobby_host_layout, null, false);
					TextView MemberNameText = (TextView) newMember.findViewById(R.id.MemberNameTextView);
					ImageView MemberReadyStatus = (ImageView) newMember.findViewById(R.id.ReadyStatusImageView);

					//Set name and spectator image
					MemberNameText.setText(strings[1]);
					MemberReadyStatus.setImageResource(R.drawable.spectator);

					//Add the new member object to the screen.
					LobbyMembersLayout.addView(newMember);
				}
			}
		});
	}



	//Updates host that they can start the game
	private void hostCanStart(String[] response) {
		//Tell host both players are ready
		lobbyMessage = "Both players ready.";
		LobbyEvent.setText(lobbyMessage);
	}



	//Gets array of all members in the lobby and their information
	private void displayLobbyMembers(String[] listMembers) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < listMembers.length; i++) {
					String[] member = listMembers[i].split("[.]");

					//Display members for host
					if (HostOrJoin.equals("host")) {
						//Create member object
						View newMember = getLayoutInflater().inflate(R.layout.lobby_host_layout, null, false);
						TextView MemberNameText = (TextView) newMember.findViewById(R.id.MemberNameTextView);
						ImageView MemberReadyStatus = (ImageView) newMember.findViewById(R.id.ReadyStatusImageView);
						Button KickMemberBtn = (Button) newMember.findViewById(R.id.KickMemberBtn);

						//Display name of member
						MemberNameText.setText(member[0]);

						//Display spectator image
						if (member[1].equals("Spectator")) {
							MemberReadyStatus.setImageResource(R.drawable.spectator);
						} else

						//Display ready or not ready image
						if (member[1].equals("Player1") || member[1].equals("Player2")) {
							if (member[2].equals("NotReady")) {
								MemberReadyStatus.setImageResource(R.drawable.notreadystatus);
							} else

							if (member[2].equals("Ready")) {
								MemberReadyStatus.setImageResource(R.drawable.readystatus);
							}
						}

						//Kicks user from lobby
						KickMemberBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								WebSocket.send("Kick " + member[0]);
							}
						});

						//Add the new member object to the screen.
						LobbyMembersLayout.addView(newMember);
					} else

					//Display members for member
					if (HostOrJoin.equals("join")) {
						//Create member object
						View newMember = getLayoutInflater().inflate(R.layout.lobby_host_layout, null, false);
						TextView MemberNameText = (TextView) newMember.findViewById(R.id.MemberNameTextView);
						ImageView MemberReadyStatus = (ImageView) newMember.findViewById(R.id.ReadyStatusImageView);

						//Display name of member
						MemberNameText.setText(member[0]);

						//Display spectator image
						if (member[1].equals("Spectator")) {
							MemberReadyStatus.setImageResource(R.drawable.spectator);
						} else

							//Display ready or not ready image
							if (member[1].equals("Player1") || member[1].equals("Player2")) {
								if (member[2].equals("NotReady")) {
									MemberReadyStatus.setImageResource(R.drawable.notreadystatus);
								} else

								if (member[2].equals("Ready")) {
									MemberReadyStatus.setImageResource(R.drawable.readystatus);
								}
							}

						//Add the new member object to the screen.
						LobbyMembersLayout.addView(newMember);
					}
				}
			}
		});
	}
}