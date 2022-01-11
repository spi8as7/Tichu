package com.papanastasiou.stefanos.tichufree;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.CreateRoomRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.PublicMessageRequest;
import sfs2x.client.requests.RoomExtension;
import sfs2x.client.requests.RoomSettings;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.smartfoxserver.v2.exceptions.SFSException;

import java.util.List;
import java.util.ListIterator;

public class RoomActivity extends AppCompatActivity implements IEventListener {

    private final String TAG = this.getClass().getSimpleName();
    private final static String EXTENSION_ID = "tichu";
    private final static String EXTENSIONS_CLASS = "TichuServer.TichuExtension";
    private final static String GAME_ROOMS_GROUP_NAME = "games";
    private final static String TAB_TAG_CREATE_GAME = "Create Game";
    private final static String TAB_TAG_GAMES = "tGames";
    private final static String TAB_TAG_USERS = "tUsers";
    private final static String TAB_TAG_CHAT = "tChat";
    private final static boolean VERBOSE_MODE = true;

    private RelativeLayout mainLayout;
    ListView listUsers, listGames, listMessages;
    ArrayAdapter<String> adapterUsers, adapterGames;
    EditText inputCreateGame,inputChatMessage;
    View buttonCreateGame,buttonChatSend,layoutConnector,
            layoutLogin, layoutGame;
    TabHost mTabHost;
    SmartFox sfsClient;
    TextView labelTagUsers,labelTagGames;
    ListView list1;
    ListView list2;
    MessagesAdapter adapterMessages;
    Status currentStatus = null;

    private final static int COLOR_GREEN = Color.parseColor("#99FF99");
    private final static int COLOR_BLUE = Color.parseColor("#99CCFF");
    private final static int COLOR_GRAY = Color.parseColor("#cccccc");
    private final static int COLOR_RED = Color.parseColor("#FF0000");
    private final static int COLOR_ORANGE = Color.parseColor("#f4aa0b");

    private enum Status {
        DISCONNECTED, CONNECTED, CONNECTING, CONNECTION_ERROR, CONNECTION_LOST, LOGGED, IN_A_ROOM
    }

    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry","WebOS","Ubuntu","Windows7","Max OS X"};
    String[] mobileArray2 = {"Nikos","Kwstas","Giannis","Giannis","Alexis","Nikos","Kwstas","Giannis","Alexis","Nikos","Kwstas","Giannis","Alexis"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);


        mainLayout = (RelativeLayout)findViewById(R.id.roomActivityView);

        //buttonCreateGame = findViewById(R.id.button_create_game);
        //inputCreateGame = (EditText) findViewById(R.id.input_create_game);
        initUI();
    }

    public void onJoinRoomClicked(){
        Intent intent = new Intent(this, GameActivity.class);
        //loginToAppWarp(userName, "");
        startActivity(intent);
    }

    public void initUI(){

        //listGames = (ListView) findViewById(R.id.list_games);
        //buttonCreateGame = findViewById(R.id.button_create_game);
        // inputCreateGame = (EditText) findViewById(R.id.input_create_game);
        //listUsers = (ListView) findViewById(R.id.list_users);
        //mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        listUsers = (ListView) findViewById(R.id.list_users);
        //listMessages = (ListView) findViewById(R.id.list_chat);
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
       // inputChatMessage = (EditText) findViewById(R.id.input_chat_message);
        listGames = (ListView) findViewById(R.id.list_games);
        buttonCreateGame = findViewById(R.id.button_create_game);
        inputCreateGame = (EditText) findViewById(R.id.input_create_game);
       // buttonChatSend = findViewById(R.id.button_chat_send);
        layoutGame = findViewById(R.id.gameActivityView);

        sfsClient = SFSController.getSFSClient();
        sfsClient.addEventListener(SFSEvent.LOGIN, this);
        sfsClient.addEventListener(SFSEvent.ROOM_JOIN, this);
        sfsClient.addEventListener(SFSEvent.USER_ENTER_ROOM, this);
        sfsClient.addEventListener(SFSEvent.USER_EXIT_ROOM, this);
        sfsClient.addEventListener(SFSEvent.ROOM_ADD, this);
        sfsClient.addEventListener(SFSEvent.ROOM_CREATION_ERROR, this);


        // The list of users
        adapterUsers = new ArrayAdapter<String>(this, R.layout.row_user);
        listUsers.setAdapter(adapterUsers);

        // The list of messages
       // adapterMessages = new MessagesAdapter(this);
        //listMessages.setAdapter(adapterMessages);
        // Enable auto scroll
       // listMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //listMessages.setStackFromBottom(true);

        // The list of games
        adapterGames = new ArrayAdapter<String>(this, R.layout.row_game);
        listGames.setAdapter(adapterGames);
        listGames.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Join the selected game
                String listItem = (String) ((TextView) view).getText();
                final String roomToJoin = listItem.substring(0, listItem.indexOf(" "));
                sfsClient.send(new JoinRoomRequest(roomToJoin));
            }
        });
        buttonCreateGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Create a new game with the specified name
                createGameRoom(inputCreateGame.getText().toString());

            }
        });

        // The tabs
        mTabHost.setup();
       // mTabHost.addTab(newTab(TAB_TAG_CHAT, R.string.chat, R.id.tab1));
        mTabHost.addTab(newTab(TAB_TAG_GAMES, R.string.games, R.id.tab2));
        mTabHost.addTab(newTab(TAB_TAG_USERS, R.string.users, R.id.tab3));
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equalsIgnoreCase(TAB_TAG_GAMES)) {
                    updateGamesList();
                }
            }
        });

        listUsers.setVisibility(View.VISIBLE);
        //showLayout(layoutConnector);
    }
        // The list of users
        //adapterUsers = new ArrayAdapter<String>(this, R.layout.row_user);
        //listUsers.setAdapter(adapterUsers);

        // The list of games
        //adapterGames = new ArrayAdapter<String>(this, R.layout.row_game);
        //listGames.setAdapter(adapterGames);
        //listGames.setOnItemClickListener(new AdapterView.OnItemClickListener() {

           // @Override
           // public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
           //     // Join the selected game
           //     String listItem = (String) ((TextView) view).getText();
           //     final String roomToJoin = listItem.substring(0, listItem.indexOf(" "));
           //     sfsClient.send(new JoinRoomRequest(roomToJoin));
           // }
       // });




        //showLayout(layoutConnector);

    /**
     * Send the create room request to the server and add room name to game list
     */
    public void createGameRoom(String roomName) {

            RoomExtension extension = new RoomExtension(EXTENSION_ID, EXTENSIONS_CLASS);
            RoomSettings settings = new RoomSettings(roomName);
            settings.setGroupId(GAME_ROOMS_GROUP_NAME);
            settings.setGame(true);
            settings.setMaxUsers(4);
            settings.setMaxSpectators(0);
            settings.setExtension(extension);
            sfsClient.send(new CreateRoomRequest(settings, true, sfsClient.getLastJoinedRoom()));

    }

    private void updateUsersTabLabel() {
        labelTagUsers.setText(getString(R.string.users) + " (" + adapterUsers.getCount() + ")");
    }

    private void updateGamesTabLabel() {
        labelTagGames.setText(getString(R.string.games) + " (" + adapterGames.getCount() + ")");
    }

    public void onCreateRoomClicked (View view) {
        //createGameRoom();
        //onJoinRoomClicked();
    }

    public void onUsersClicked (View view) {
        list1.setVisibility(View.INVISIBLE);
        list2.setVisibility(View.VISIBLE);
    }

    public void onGamesClicked (View view) {
        list1.setVisibility(View.VISIBLE);
        list2.setVisibility(View.INVISIBLE);
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

    /**
     * Update game list
     */
    public void updateGamesList() {
        adapterGames.clear();
        List<Room> gameList = sfsClient.getRoomListFromGroup(GAME_ROOMS_GROUP_NAME);
        ListIterator<Room> gameRoomIterator = gameList.listIterator();
        while (gameRoomIterator.hasNext()) {
            Room room = gameRoomIterator.next();
            // Add each room back into the adapter with player count
            adapterGames.add(room.getName() + "         Users: " + room.getUserCount() + "/"
                    + room.getMaxUsers());
        }

        updateGamesTabLabel();
    }

    @Override
    public void dispatch(BaseEvent event) throws SFSException {
        if (event.getType().equalsIgnoreCase(SFSEvent.ROOM_JOIN)) {
            Log.d("ROOM","TRYING");
            setStatus(Status.IN_A_ROOM, sfsClient.getLastJoinedRoom().getName());
            //showLayout(layoutGame);
            startActivity(new Intent(this, GameActivity.class));
            Room room = (Room) event.getArguments().get("room");
            adapterUsers.clear();
            for (User user : room.getUserList()) {
                //adapterUsers.add(user.getName());
                //updateUsersTabLabel();
            }
            //adapterMessages.add(new ChatMessage("Room [" + room.getName() + "] joined"));

            //if (room.isGame()) {
                //startTrisGame();
              //  startActivity(new Intent(this, GameActivity.class));
            //}

        }// When a user enter the room the user list is updated
        else if (event.getType().equals(SFSEvent.USER_ENTER_ROOM)) {
            final User user = (User) event.getArguments().get("user");
            final Room room = (Room) event.getArguments().get("room");
            if (VERBOSE_MODE) Log.v(TAG, "User '" + user.getName() + "' joined the room");
            adapterUsers.add(user.getName());
            //updateUsersTabLabel();
            //adapterMessages.add(new ChatMessage("User '" + user.getName()
              //      + "' joined the room"));
            if (room.isGame()) {
               // updateGamesList();
            }
        }
        // When a user leave the room the user list is updated
        else if (event.getType().equals(SFSEvent.USER_EXIT_ROOM)) {
            final User user = (User) event.getArguments().get("user");
            final Room room = (Room) event.getArguments().get("room");
            if (VERBOSE_MODE) Log.v(TAG, "User '" + user.getName() + "' left the room");
            adapterUsers.remove(user.getName());
            //updateUsersTabLabel();
            //adapterMessages.add(new ChatMessage("User '" + user.getName()
              //      + "' left the room"));
            if (room.isGame()) {
                //updateGamesList();
            }
        } else if (event.getType().equalsIgnoreCase(SFSEvent.LOGIN)) {
            Log.d("ROOM","TRYING");
            setStatus(Status.LOGGED, sfsClient.getCurrentZone());
            sfsClient.send(new JoinRoomRequest(getString(R.string.example_lobby)));
        } else if (event.getType().equalsIgnoreCase(SFSEvent.ROOM_ADD)) {
            Log.d("ROOM","DONE");
            //setStatus(Status.LOGGED, sfsClient.getCurrentZone());
            //sfsClient.send(new JoinRoomRequest(getString(R.string.example_lobby)));
        } else if (event.getType().equalsIgnoreCase(SFSEvent.ROOM_CREATION_ERROR)) {
            Log.d("ROOM","ERROR");
            //setStatus(Status.LOGGED, sfsClient.getCurrentZone());
            //sfsClient.send(new JoinRoomRequest(getString(R.string.example_lobby)));
        }

    }

    /**
     * Create a TabSpec with the given tag, label and content
     *
     * @param tag
     * @param labelId
     * @param tabContentId
     * @return
     */
    private TabSpec newTab(String tag, int labelId, int tabContentId) {
        View indicator = LayoutInflater.from(this).inflate(R.layout.tab_header,
                (ViewGroup) findViewById(android.R.id.tabs), false);
        TextView label = (TextView) indicator.findViewById(android.R.id.title);
        label.setText(labelId);
        if (TAB_TAG_USERS.equals(tag)) {
            labelTagUsers = label;
        } else if (TAB_TAG_GAMES.equals(tag)) {
            labelTagGames = label;
        }
        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(tabContentId);
        return tabSpec;
    }

    private void showLayout(final View layoutToShow) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // Show the layout selected and hide the others
                for (View layout : new View[]{layoutGame, layoutConnector, layoutLogin}) {
                    if (layoutToShow == layout) {
                        layout.setVisibility(View.VISIBLE);
                    } else {
                        layout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}
