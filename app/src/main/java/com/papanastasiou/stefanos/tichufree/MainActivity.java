package com.papanastasiou.stefanos.tichufree;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.content.Intent;
import android.widget.TextView;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;


import com.smartfoxserver.v2.exceptions.SFSException;

import java.util.List;
import java.util.ListIterator;


public class MainActivity extends AppCompatActivity implements IEventListener {
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";
    private EditText editNameText;
    public ProgressDialog progressDialog;
    private String authData = "";

    private final String TAG = this.getClass().getSimpleName();
    private final static String TAB_TAG_CHAT = "tChat";
    private final static String TAB_TAG_GAMES = "tGames";
    private final static String TAB_TAG_USERS = "tUsers";

    private final static String EXTENSION_ID = "tichu";
    private final static String EXTENSIONS_CLASS = "TichuServer.TichuExtension";
    private final static String GAME_ROOMS_GROUP_NAME = "games";

    private final static boolean VERBOSE_MODE = true;

    private final static String DEFAULT_SERVER_ADDRESS = "192.168.1.6";
    private final static String DEFAULT_SERVER_PORT = "9933";

    private final static int COLOR_GREEN = Color.parseColor("#99FF99");
    private final static int COLOR_BLUE = Color.parseColor("#99CCFF");
    private final static int COLOR_GRAY = Color.parseColor("#cccccc");
    private final static int COLOR_RED = Color.parseColor("#FF0000");
    private final static int COLOR_ORANGE = Color.parseColor("#f4aa0b");

    private enum Status {
        DISCONNECTED, CONNECTED, CONNECTING, CONNECTION_ERROR, CONNECTION_LOST, LOGGED, IN_A_ROOM
    }

    Status currentStatus = null;

    SmartFox sfsClient;

    EditText inputServerAddress, inputServerPort, inputUserNick, inputChatMessage, inputCreateGame;
    View buttonConnect, buttonLogin, buttonChatSend, buttonCreateGame, layoutConnector,
            layoutLogin, layoutGame;

    TextView labelStatus, labelTagUsers, labelTagGames;
    //CheckBox checkUseBlueBox;
    //ListView listUsers, listGames, listMessages;
    ArrayAdapter<String> adapterUsers, adapterGames;
    //MessagesAdapter adapterMessages;
    //TabHost mTabHost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editNameText = (EditText)findViewById(R.id.editNameText);
        initSmartFox();
        //init(); // sundesi me ton server
    }

    private void initSmartFox() {

        // Instantiate SmartFox client
        sfsClient = SFSController.getSFSClient();

        // Add event listeners
        sfsClient.addEventListener(SFSEvent.CONNECTION, this);
        sfsClient.addEventListener(SFSEvent.CONNECTION_LOST, this);
        sfsClient.addEventListener(SFSEvent.LOGIN, this);
        sfsClient.addEventListener(SFSEvent.LOGIN_ERROR, this);
        sfsClient.addEventListener(SFSEvent.ROOM_JOIN, this);
        sfsClient.addEventListener(SFSEvent.USER_ENTER_ROOM, this);
        sfsClient.addEventListener(SFSEvent.USER_EXIT_ROOM, this);
        sfsClient.addEventListener(SFSEvent.PUBLIC_MESSAGE, this);
        sfsClient.addEventListener(SFSEvent.ROOM_ADD, this);
        sfsClient.addEventListener(SFSEvent.ROOM_REMOVE, this);
        sfsClient.addEventListener(SFSEvent.ROOM_JOIN_ERROR, this);
        sfsClient.addEventListener(SFSEvent.EXTENSION_RESPONSE,this);

        if (VERBOSE_MODE)
            Log.v(TAG, "SmartFox created:" + sfsClient.isConnected() + " BlueBox enabled="
                    + sfsClient.useBlueBox());

        sfsClient.connect(Constants.SERVER_, Integer.parseInt(Constants.PORT));
        setStatus(Status.CONNECTING);
    }


    public void onPlayGameClicked(View view){
        if(editNameText.getText().length()==0){
            //Utils.showToastAlert(this, getApplicationContext().getString(R.string.enterName));
            return;
        }
        //Intent intent = new Intent(this, RoomActivity.class);
        String userName = editNameText.getText().toString().trim();
        String zoneName = getString(R.string.example_zone);
        //Log.d(userName.toString(), zoneName.toString());
        sfsClient.send(new LoginRequest(userName, "", zoneName));
        //intent.putExtra(EXTRA_MESSAGE,userName);
        //startActivity(intent);
    }


    public void goToRoomList() {
        Log.d("goToRoomList", "goToRoomList called");
        Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
        startActivity(intent);
    }

    private void setStatus(Status status, String... params) {
        if (status == currentStatus) {
            // If there is no status change ignore it
            return;
        }
        if (VERBOSE_MODE) Log.v(TAG, "New status= " + status);
        currentStatus = status;
        final String message;
        final int messageColor;
        final boolean connectButtonEnabled;
        switch (status) {
            case CONNECTING:
                message = getString(R.string.connecting);
                messageColor = COLOR_BLUE;
                connectButtonEnabled = true;
                break;
            case DISCONNECTED:
                message = getString(R.string.disconnected);
                messageColor = COLOR_GRAY;
                connectButtonEnabled = true;
                break;
            case CONNECTION_ERROR:
                message = getString(R.string.connection_error);
                messageColor = COLOR_RED;
                connectButtonEnabled = true;
                break;
            case CONNECTED:
                message = getString(R.string.connected) + ": " + params[0];
                messageColor = COLOR_GREEN;
                connectButtonEnabled = false;
                break;
            case CONNECTION_LOST:
                message = getString(R.string.connection_lost);
                messageColor = COLOR_ORANGE;
                connectButtonEnabled = true;
                break;
            case LOGGED:
                message = getString(R.string.logged_into) + "'" + params[0] /*
																		 * zone name
																		 */
                        + "' zone";
                messageColor = COLOR_GREEN;
                connectButtonEnabled = false;
                break;
            case IN_A_ROOM:
                message = getString(R.string.joined_to_room) + params[0] /* room name */
                        + "'";
                messageColor = COLOR_GREEN;
                connectButtonEnabled = true;
                break;
            default:
                connectButtonEnabled = true;
                messageColor = 0;
                message = null;
        }
        Log.d("DEBUG", message);
    }

    private void disconnect() {
        if (VERBOSE_MODE) Log.v(TAG, "Disconnect");
        if (sfsClient.isConnected()) {
            if (VERBOSE_MODE) Log.v(TAG, "Disconnect: Disconnecting client");
            sfsClient.disconnect();
        }
    }


    public void updateGamesList() {
        //adapterGames.clear();
        List<Room> gameList = sfsClient.getRoomListFromGroup(GAME_ROOMS_GROUP_NAME);
        ListIterator<Room> gameRoomIterator = gameList.listIterator();
        while (gameRoomIterator.hasNext()) {
            Room room = gameRoomIterator.next();
            // Add each room back into the adapter with player count
            //adapterGames.add(room.getName() + "         Users: " + room.getUserCount() + "/"
              //      + room.getMaxUsers());
        }

        //updateGamesTabLabel();
    }



    @Override
    public void dispatch(final BaseEvent event) throws SFSException {
        runOnUiThread(new Runnable() {
            public void run() {
                if (VERBOSE_MODE)
                    Log.v(TAG,
                            "Dispatching " + event.getType() + " (arguments="
                                    + event.getArguments() + ")");
                if (event.getType().equalsIgnoreCase(SFSEvent.CONNECTION)) {
                    if (event.getArguments().get("success").equals(true)) {
                        setStatus(Status.CONNECTED, sfsClient.getConnectionMode());
                        // Login as guest in current zone
                        //showLayout(layoutLogin)
                    } else {
                        setStatus(Status.CONNECTION_ERROR);
                        //showLayout(layoutConnector);
                    }
                } else if (event.getType().equalsIgnoreCase(SFSEvent.CONNECTION_LOST)) {
                    setStatus(Status.CONNECTION_LOST);
                    //adapterMessages.clear();
                    //adapterUsers.clear();
                    disconnect();
                    //showLayout(layoutConnector);
                } else if (event.getType().equalsIgnoreCase(SFSEvent.LOGIN)) {
                    setStatus(Status.LOGGED, sfsClient.getCurrentZone());
                    sfsClient.send(new JoinRoomRequest(getString(R.string.example_lobby)));
                    goToRoomList();
                }
            }
        });
    }
}


