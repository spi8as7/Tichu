package com.papanastasiou.stefanos.tichufree;

import android.annotation.TargetApi;
import android.content.AsyncTaskLoader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Handler;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;

public class GameActivity extends AppCompatActivity implements IEventListener {
    private RelativeLayout mainLayout,waitLayout,gameLayout;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private int cardWidth;
    private int cardHeight;
    private int selectedCardId = -1;
    private int selectedCardIndex = -1;
    private CardView[] cardViewArray = new CardView[14];
    private CardView selectedCardView = null;
    private CardView tempCardView = null;
    private CardView temp1CardView = null;
    private CardView temp2CardView = null;
    private CardView temp3CardView = null;
   // private ImageView DeckCardView =null;
    //private ImageView onBoardCards ;

    private boolean grand=false;
    private ImageView reqNewCardView;
    private ImageView topCardView;
    private Button tmpButton;
    private Button bottomButton;
    //private TextView turnTextView;
    //private Dialog gameStatusDialog;
    //private Handler handler = new Handler();


    private boolean isUserTurn = true;
    private boolean isHand = true;
    private boolean isUserActionDone = false;
    Chronometer chronometer;

    private int GAME_STATUS =0;
    private int EXCHANGE_STATUS =0;

    private int TOP_CARD = -1;
    private int REQUESTED_CARD = -1;

    private ArrayList<CardView> VIEW_LIST = new ArrayList<CardView>();
    private ArrayList<Integer> MOVE_CARD = new ArrayList<Integer>();
    private ArrayList<Integer> DECK_CARDS = new ArrayList<Integer>();
    private ArrayList<ImageView> DECK_CARDS_VIEWS = new ArrayList<ImageView>();

    private ArrayList<Integer> USER_CARD = new ArrayList<Integer>();
    private ArrayList<Integer> OPP1_CARD = new ArrayList<Integer>();
    private ArrayList<Integer> OPP2_CARD = new ArrayList<Integer>();
    private ArrayList<Integer> TEAM_CARD = new ArrayList<Integer>();
    private ArrayList<Integer> DECK_ALL_CARDS = new ArrayList<Integer>();

    private int[][] TempSorting = new int[14][4];
    private  ArrayList<Integer> tempList = new ArrayList<Integer>();

    private int cardStartXX = 0;
    private int cardStartYY = 0;


    TextView turn_txt;

    Bitmap[] cards;

    boolean myTurn;

    public int TURN =0;
    public final int ONE_CARD =1;
    public final int TWO_CARDS =2;
    public final int THREE_CARDS =3;
    public final int STEPS =4;
    public final int FULL =5;
    public final int STRAIGHT =6;

    private boolean gameStarted;
    SmartFox sfsClient;

    enum moves {
        start, stop, move, win, tie;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if(display!=null){
            if(Build.VERSION.SDK_INT> Build.VERSION_CODES.HONEYCOMB_MR2){
                display.getSize(size);
            }else{
                size.x = display.getWidth();
                size.y = display.getHeight();
            }
        }
        SCREEN_WIDTH = size.x;
        SCREEN_HEIGHT = size.y;
        Log.d("SCREEN_WIDTH", String.valueOf(SCREEN_WIDTH));
        Log.d("SCREEN_HEIGHT", String.valueOf(SCREEN_HEIGHT));
        mainLayout = (RelativeLayout)findViewById(R.id.gameActivityView);
        initSmartFox();
        //waitForOpponent();
    }

    private void initSmartFox() {
        // Instantiate SmartFox client
        sfsClient = SFSController.getSFSClient();
        // Register to SmartFox events
        sfsClient.addEventListener(SFSEvent.EXTENSION_RESPONSE, this);
        sfsClient.addEventListener(SFSEvent.CONNECTION_LOST, this);
        sfsClient.addEventListener(SFSEvent.USER_EXIT_ROOM, this);

        // Tell extension I'm ready to play
        sfsClient
                .send(new ExtensionRequest("ready", new SFSObject(), sfsClient.getLastJoinedRoom()));


        if(sfsClient.getLastJoinedRoom().getUserList().size() == 1 ) {
            waitForOpponent();
        }
        gameStarted = true;

    }

    private void waitForOpponent() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                waitLayout = (RelativeLayout) findViewById(R.id.waitLayout);
                waitLayout.setVisibility(View.VISIBLE);

                TextView waitText = (TextView) waitLayout.findViewById(R.id.waitingText);
                waitText.setText("Players Joined" + sfsClient.getLastJoinedRoom().getUserList().size() + " /3");
            }
        });


    }

    private void initScreen(){
        int cardBlockWidth = SCREEN_WIDTH/17; //maximum 14 fula kai thelw kai xwro gia ta koumpia
        cards = Utils.getCardsBitmapArray(this, cardBlockWidth);
        cardWidth = cards[0].getWidth();
        cardHeight = cards[0].getHeight();
        Log.d("cardWidth", String.valueOf(cardWidth));
        Log.d("cardHeight", String.valueOf(cardHeight));
        cardWidth = cardBlockWidth;
        waitLayout = (RelativeLayout) findViewById(R.id.waitLayout);
        waitLayout.setVisibility(View.INVISIBLE);
        gameLayout = (RelativeLayout) findViewById(R.id.relativeLayout1);
        gameLayout.setVisibility(View.VISIBLE);
        //for(int i=0; i < 14 ; i++) {
          //  USER_CARD.add(i);
        //}
        //dealNewCards();
        USER_CARD.add(13);
        USER_CARD.add(0);
        USER_CARD.add(26);
        USER_CARD.add(39);
        USER_CARD.add(51);
        USER_CARD.add(18);
        USER_CARD.add(30);
        USER_CARD.add(43);
        USER_CARD.add(7);
        USER_CARD.add(8);
        USER_CARD.add(51);
        USER_CARD.add(38);
        USER_CARD.add(21);
        USER_CARD.add(52);

        //DECK_CARDS.add(10);
        //DECK_CARDS.add(23);
        //DECK_CARDS.add(49);
        //DECK_CARDS.add(1);
        //DECK_CARDS.add(14);
        //SortArray(USER_CARD);
        addCardsToHand(8, 2, USER_CARD);
        TextView text = new TextView(this);
        //int pos;
        //for( pos =0; pos < sfsClient.getLastJoinedRoom().getPlayerList().size() ; pos++) {
            //if (sfsClient.getLastJoinedRoom().getPlayerList().get(pos).isItMe()) break;

       // }
        Log.d("Player1", String.valueOf(sfsClient.getLastJoinedRoom().getPlayerList().size()));
        Log.d("Player1", String.valueOf(sfsClient.getLastJoinedRoom().getPlayerList()));
        Log.d("Player1", String.valueOf(sfsClient.getLastJoinedRoom().getUserList()));
        Log.d("Player1", String.valueOf(sfsClient.getMySelf()));
        Log.d("Player1", String.valueOf(sfsClient.getLastJoinedRoom().getPlayerList().indexOf(sfsClient.getMySelf())));

        int thesi = sfsClient.getLastJoinedRoom().getPlayerList().indexOf(sfsClient.getMySelf());

        text.setText( sfsClient.getLastJoinedRoom().getPlayerList().get((thesi+1)%3).getName());
        text.setX(SCREEN_WIDTH - 200);
        text.setY(SCREEN_HEIGHT / 3);
        text.setBackgroundColor(Color.WHITE);
        text.setTextSize(20);
        mainLayout.addView(text);

        TextView text2 = new TextView(this);
        text2.setText( sfsClient.getLastJoinedRoom().getPlayerList().get((thesi+3)%3).getName());
        text2.setX(30);
        text2.setY(SCREEN_HEIGHT / 3);
        text2.setBackgroundColor(Color.WHITE);
        text2.setTextSize(20);
        mainLayout.addView(text2);

        TextView text3 = new TextView(this);
        text3.setText( sfsClient.getLastJoinedRoom().getPlayerList().get((thesi+2)%3).getName());
        text3.setBackgroundColor(Color.WHITE);
        text3.setX(SCREEN_WIDTH / 2);
        text3.setY(0);
        text3.setTextSize(20);
        mainLayout.addView(text3);
       // enemiesCards();
        turn_txt= new TextView(this);
        turn_txt.setX(1600);
        turn_txt.setY(50);
        turn_txt.setTextColor(Color.WHITE);
        turn_txt.setTextSize(20);
        turn_txt.setVisibility(View.INVISIBLE);
        mainLayout.addView(turn_txt);


    }

    private void setBitmapInImageView(ImageView view, Bitmap bmp){
        BitmapDrawable b = new BitmapDrawable(bmp);
        view.setBackgroundDrawable(b);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public boolean onTouchEvent(MotionEvent me){
        if(!isUserTurn){
            Utils.showToastAlert(this, Constants.ALERT_INV_MOVE);
            return false;
        }
        if(me.getAction()== MotionEvent.ACTION_DOWN) {
			/*
			 * when user select any card from existing card list
			 */
            if (GAME_STATUS > 0 && TURN ==0){
                if (selectedCardIndex != -1) {
                    Bitmap bitmap = ((BitmapDrawable) cardViewArray[selectedCardIndex].getDrawable()).getBitmap();
                    selectedCardView = new CardView(this, 0, (int) cardViewArray[selectedCardIndex].getX(), (int) cardViewArray[selectedCardIndex].getY());
                    selectedCardView.setImageBitmap(bitmap);
                    mainLayout.addView(selectedCardView);
                }
            }
        }else if(me.getAction()==MotionEvent.ACTION_UP){
            if(GAME_STATUS > 0 && TURN ==0) {
                int releaseCardIndex = -1;
                if (selectedCardIndex != -1 || TOP_CARD != -1 || REQUESTED_CARD != -1) {
                    //check for valid release index
                    for (int i = 0; i < cardViewArray.length; i++) {
                        if (me.getX() > (cardStartXX + (i * cardWidth)) && me.getX() < ((i * cardWidth) + cardWidth + cardStartXX) &&
                                me.getY() > cardStartYY && me.getY() < (cardStartYY + cardHeight)) {
                            releaseCardIndex = i;
                            break;
                        }
                    }
                    for (int j = 0; j < USER_CARD.size(); j++) {
                        if (USER_CARD.get(j) == selectedCardId && !(MOVE_CARD.contains(USER_CARD.get(j)))) {
                            //if( (selectedCardView.getX() <= tablo_kartas.getWidth() )&& (selectedCardView.getX() >= tablo_kartas.getX() )&& (selectedCardView.getY() <= tablo_kartas.getHeight() )&& (selectedCardView.getY() >= tablo_kartas.getY() )  ) {
                           // Log.d("debug1", USER_CARD.get(j).toString());
                            //Log.d("debug2",USER_CARD.get(selectedCardIndex).toString());
                            MOVE_CARD.add(USER_CARD.get(j));
                            Log.d("MOVE CARD", MOVE_CARD.toString());
                           // Log.d("USER CARD", USER_CARD.toString());
                            //USER_CARD.remove(j);
                        }
                    }
                    //Log.d("MOVE CARD", MOVE_CARD.toString());
                    //Log.d("CARD X:", String.valueOf( selectedCardView.getX()));
                    // Log.d("CARD Y:", String.valueOf(selectedCardView.getY()));
                    // Log.d("Tablo X:", String.valueOf( tablo_kartas.getX()));
                    // Log.d("Tablo Y:", String.valueOf(tablo_kartas.getY()));
                    //Log.d("Tablo Width:", String.valueOf( tablo_kartas.getWidth()));
                    //Log.d("Tablo Height:", String.valueOf(tablo_kartas.getHeight()));
                    mainLayout.removeView(selectedCardView); //eksafanizei tin karta
                    Bitmap bitmap = ((BitmapDrawable) cardViewArray[selectedCardIndex].getDrawable()).getBitmap();
                    if (EXCHANGE_STATUS == 0) {
                        temp1CardView = new CardView(this, 0, (int) cardViewArray[selectedCardIndex].getX(), (int) cardViewArray[selectedCardIndex].getY() - 300);
                        temp1CardView.animate().rotation(-90);
                        temp1CardView.setX(600);
                        temp1CardView.setImageBitmap(bitmap);
                        mainLayout.addView(temp1CardView);
                        VIEW_LIST.add(temp1CardView);
                        EXCHANGE_STATUS = 1;
                    } else if (EXCHANGE_STATUS == 1) {
                        temp2CardView = new CardView(this, 0, (int) cardViewArray[selectedCardIndex].getX(), (int) cardViewArray[selectedCardIndex].getY() - 300);
                        temp2CardView.setX(800);
                        temp2CardView.setImageBitmap(bitmap);
                        mainLayout.addView(temp2CardView);
                        VIEW_LIST.add(temp2CardView);
                        EXCHANGE_STATUS = 2;
                    } else if (EXCHANGE_STATUS == 2) {
                        temp3CardView = new CardView(this, 0, (int) cardViewArray[selectedCardIndex].getX(), (int) cardViewArray[selectedCardIndex].getY() - 300);
                        temp3CardView.animate().rotation(90);
                        temp3CardView.setX(1000);
                        temp3CardView.setImageBitmap(bitmap);
                        mainLayout.addView(temp3CardView);
                        VIEW_LIST.add(temp3CardView);
                        EXCHANGE_STATUS = 4;
                    } else if (EXCHANGE_STATUS == 3) {
                        tempCardView = new CardView(this, 0, (int) cardViewArray[selectedCardIndex].getX(), (int) cardViewArray[selectedCardIndex].getY() - 60);
                        tempCardView.setImageBitmap(bitmap);
                        mainLayout.addView(tempCardView);
                        VIEW_LIST.add(tempCardView);
                    }
                }
            }
        }else if(me.getAction()==MotionEvent.ACTION_MOVE){
            if(selectedCardView!=null){
                selectedCardView.setX(me.getX());
                selectedCardView.setY(me.getY());
            }
        }

        return true;
    }

    private void resetCard(int id){
        if(id!=-1){
            cardViewArray[id].setX(cardViewArray[id].getInitX());
            cardViewArray[id].setY(cardViewArray[id].getInitY());
        }
    }

    private void refreshList() {
        //Log.d("VIEW LIST", VIEW_LIST.toString());
        for(int i=0;i<VIEW_LIST.size() ;i++) {
            mainLayout.removeView(VIEW_LIST.get(i));
        }
        for(int i=0;i<USER_CARD.size() ;i++) {
          //  Log.d("Counter", String.valueOf(i));
           // Log.d("Cards", String.valueOf(cardViewArray[i]));
            mainLayout.removeView(cardViewArray[i]);
        }
        //addCardsToHand(14, 2);

    }

    public void onTopClicked (View view) {
        if(GAME_STATUS > 0) {
            refreshList();
            addCardsToHand(USER_CARD.size(), 2, USER_CARD);
            MOVE_CARD.clear();
            if(GAME_STATUS ==1) {
                EXCHANGE_STATUS = 0;
            }
            //Log.d("UNDO BUTTON", "patithike");
        }
    }

    public void onMiddleClicked(View view){
        if(GAME_STATUS == 0) {
            tmpButton = (Button) findViewById(R.id.button3);
            tmpButton.setVisibility(View.INVISIBLE);
            tmpButton = (Button) findViewById(R.id.button2);
            tmpButton.setText("DONE");
            tmpButton = (Button) findViewById(R.id.button5);
            tmpButton.setVisibility(View.VISIBLE);
            GAME_STATUS=1;
            //SortArray(USER_CARD);
            addCardsToHand(14, 2,USER_CARD);
        }else {
            tmpButton = (Button) findViewById(R.id.button3);
            tmpButton.setVisibility(View.INVISIBLE);
        }
    }

    public void onBottomClicked(View view){
       // refreshList();
        //bottomButton
        if(GAME_STATUS == 0) {
            tmpButton = (Button) findViewById(R.id.button2);
            tmpButton.setText("DONE");
            tmpButton = (Button) findViewById(R.id.button3);
            tmpButton.setText("TICHU");
            tmpButton = (Button) findViewById(R.id.button5);
            tmpButton.setVisibility(View.VISIBLE);
            GAME_STATUS=1;
           // SortArray(USER_CARD);
            addCardsToHand(USER_CARD.size(), 2,USER_CARD);
        } else if(GAME_STATUS == 1) {
            if(MOVE_CARD.size() != 3) {
                refreshList();
                //SortArray(USER_CARD);
                addCardsToHand(USER_CARD.size(), 2, USER_CARD);
                EXCHANGE_STATUS=0;
                MOVE_CARD.clear();
                Utils.showToastAlert(this, Constants.ALERT_EXCHANGE_CARD);
            } else {
                sendMove(MOVE_CARD);
               // refreshList();
               // USER_CARD.removeAll(MOVE_CARD);
               // Utils.showToastAlert(this, "Wait other players");
               // USER_CARD.add(37);
               // USER_CARD.add(36);
               // USER_CARD.add(35);
               // addCardsToHand(USER_CARD.size(), 2, USER_CARD);
               // GAME_STATUS=2;
               // EXCHANGE_STATUS=3;
            }
        } else if (GAME_STATUS == 2 ) {
            //ImageView DeckCardView = (ImageView) findViewById(R.id.imageView2);
            //Collections.sort(MOVE_CARD); //i detect gia na doulepsei thelei sorted list
            SortArray(MOVE_CARD);
            Log.d("Detect Status",String.valueOf(detect(MOVE_CARD)));
            if (detect(MOVE_CARD) != -1 ) {
                if(DECK_CARDS.size() > 0) {
                    Log.d("Deck Cards", DECK_CARDS.toString());
                    Log.d("Move Cards", MOVE_CARD.toString());
                    Log.d("MOVE Cards", String.valueOf(MOVE_CARD.get(0)));
                    Log.d("Deck Cards",String.valueOf(DECK_CARDS.get(0)));
                    typeIsSame(DECK_CARDS,MOVE_CARD);
                //if((detect(MOVE_CARD) != detect(DECK_CARDS) ) || (MOVE_CARD.size() != DECK_CARDS.size()) || (detect(MOVE_CARD)== -1)) {
                    if( ( (detect(MOVE_CARD) == detect(DECK_CARDS) ) && isGreater(MOVE_CARD.get(0),DECK_CARDS.get(0)) ) )  {
                        //refreshList();
                        //MOVE_CARD.clear();
                        //addCardsToHand(USER_CARD.size(), 2, USER_CARD);
                        makeMove();
                        //TURN = TURN + 1;
                        if (TURN == 0) {
                            turn_txt.setText("Steve Is Playing");
                        }if (TURN == 1) {
                            turn_txt.setText("Nikos Is Playing");
                        }if (TURN == 2) {
                            turn_txt.setText("Giannis Is Playing");
                        }if (TURN == 3) {
                            turn_txt.setText("Kostas Is Playing");
                        }
                        Utils.showToastAlert(this, "valid combination");
                    } else {
                        Utils.showToastAlert(this, "InValid combination");
                    }
                } else {
                    makeMove();
                    //TURN = TURN + 1;
                    if (TURN == 0) {
                        turn_txt.setText("Steve Is Playing");
                    }if (TURN == 1) {
                        turn_txt.setText("Nikos Is Playing");
                    }if (TURN == 2) {
                        turn_txt.setText("Giannis Is Playing");
                    }if (TURN == 3) {
                        turn_txt.setText("Kostas Is Playing");
                    }
                    //Log.d("USER_CARD", USER_CARD.toString());

                    //addCardsToHand(MOVE_CARD.size(),4, MOVE_CARD);
                    //Log.d("USER CARDS", USER_CARD.toString());
                    // Log.d("MOVE CARDS", MOVE_CARD.toString());
                }
            } else {
                Log.d("MOVE CARDS", MOVE_CARD.toString());
                refreshList();
                MOVE_CARD.clear();
               // SortArray(USER_CARD);
                addCardsToHand(USER_CARD.size(), 2, USER_CARD);
                Utils.showToastAlert(this, "Invalid combination");
            }
            Log.d("MOVE CARDS", MOVE_CARD.toString());
        }
    }



    private void addCardsToHand(int size,int height,ArrayList<Integer> list) {
        int i;
        refreshList();
        for( i=0;i < size;i++){  // Drawing from Bottom Left
            cardStartYY = SCREEN_HEIGHT-(height*cardHeight);
            //Log.d("cardHeight", String.valueOf(cardStartYY));
            final int selectedIndex = i;
            cardViewArray[i] = new CardView(this, list.get(i), (i*cardWidth), cardStartYY);
           // Log.d("CardViewArray", cardViewArray[i].toString());
            cardViewArray[i].setOnTouchListener(new View.OnTouchListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (!isUserTurn) {
                        Utils.showToastAlert(GameActivity.this, Constants.ALERT_INV_MOVE);
                        return false;
                    }
                    CardView cardView = (CardView) v;
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        selectedCardIndex = selectedIndex;
                        selectedCardId = cardView.getId();
                        Log.d("selectedCardId", String.valueOf(selectedCardId));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    }
                    return false;
                }
            });
            //cardViewArray[i].setImageBitmap();
            if( cardViewArray[i].getId() == 53) {
                cardViewArray[i].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dragon));
            } else if (cardViewArray[i].getId() == 54){
                cardViewArray[i].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mayong));
            } else if(cardViewArray[i].getId() == 55) {
                cardViewArray[i].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dogs));
            } else if(cardViewArray[i].getId() == 56) {
                cardViewArray[i].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.phoenix));
            } else {
                if( i > 0) cardViewArray[i].setImageBitmap(cards[(list.get(i-1))]);
                else cardViewArray[i].setImageBitmap(cards[(list.get(i))]);

            }

            // image array starts from zero
            mainLayout.addView(cardViewArray[i]);
        }
    }

    public int detect(ArrayList<Integer> input) {
        int mikos;
        mikos = input.size();
        if( mikos == 1)  //monofulia
            return ONE_CARD;
        else if(mikos == 2 ) {  //difulia
            int a = input.get(0);
            int b = input.get(1);
            int y = b - a;
            //System.out.println(a);
            // System.out.println(b);
            // System.out.println(y);
            if( (y == 13) || (y == 26) ||( y == 39 )  )
                return TWO_CARDS;
        }
        else if (mikos == 3) { //trifulia
            int a = input.get(0);
            int b = input.get(1);
            int c = input.get(2);
            int y = b - a;
            int z = c - b;
            System.out.println(y);
            System.out.println(z);
            if( ( ( y == 13 ) || (y == 26) ) && ( ( z == 13 ) || ( z == 26) ) )
                return THREE_CARDS;
        }
        else if(mikos == 4) { //vomva i steps
            int a = input.get(0);
            int b = input.get(1);
            int c = input.get(2);
            int d = input.get(3);
            int y = b - a;
            int z = d - c;
            int w = abs(b-c); // steps 4ada mono
            if( ( ( y == 13 ) || (y == 26)  || (y == 39) ) && ( ( z == 13 ) || ( z == 26)  || ( z == 39) ) && ( (w==1) || (w==12) || (w==14) || (w==25) || (w==38)  ) )
                return STEPS;
            if(  (y == 13) && (z == 13) && (w == 13) ) //vomva 4 fula
                return STEPS;
        }
        else if(mikos == 5) {
            int a = input.get(0);
            int b = input.get(1);
            int c = input.get(2);
            int d = input.get(3);
            int e = input.get(4);
            int x = b - a;
            int w = c - b;
            int y = d - c;
            int z = e - d;
            //2ada,3ada
            if (((x == 13) || (x == 26) || (x == 39)) && (((y == 13) || (y == 26)) && ((z == 13) || (z == 26)))) {
                //Log.d("IF", "1o");
                return FULL;
            }
            //3ada,2ada
            if ((((x == 13) || (x == 26)) && ((w == 13) || (w == 26))) && (z == 13) || (z == 26) || (z == 39)){
                //Log.d("IF", "2o");
                return FULL;
            }
            a = a%13;
            //if (a == 0) a=13;
            b = b%13;
            //if (b == 0) b=13;
            c = c%13;
           // if (c == 0) c=13;
            d = d%13;
           // if (d == 0) d=13;
            e = e%13;
            //if (e == 0) e=13;
            int x2 = b - a;
            int w2 = c - b;
            int y2 = d - c;
            int z2 = e - d;
            //kenta
            if ( (x2 == 1 ) && (w2 == 1 ) && (y2 == 1 ) && (z2 == 1 )  )
                return STRAIGHT;
            //kenta xrwma-> bomb
            if ( (x2 == 1 ) && (w2 == 1 ) && (y2 == 1 ) && (z2 == 1 ) && (x == 1 ) && (w == 1 ) && (y == 1 ) && (z == 1 )  )
                return STRAIGHT;
        }
        else {
            int temp1;
            int temp2;
            int temp3=0;
            int temp4=0,temp5;
            if((mikos%2) == 0) {
                for(int i=0; i < mikos ;i=i+2) {
                    temp1 = input.get(i);
                    temp2 = input.get(i+1);
                    temp1 = temp1 %13;
                    //if(temp1 == 0) temp1=13;        //pollapla steps
                    temp2 = temp2 %13;
                    //if(temp2 == 0) temp2=13;
                    temp5=temp1;
                    if ( i > 0) {
                        if((temp1==temp2) && (temp5-temp4 == 1)) temp3=1;
                        else {
                            temp3=0;
                            break;
                        }
                    }
                    temp4=temp5;
                }
                if (temp3 == 1) return STEPS;
            }
            temp1=input.get(0);
            for(int i=1 ; i < mikos; i++) {
                temp2=input.get(i);
                temp1 = temp1 %13;
                //if(temp1 == 0) temp1=13;        //kenta me fulla > 5
                temp2 = temp2 %13;
                //if(temp2 == 0) temp2=13;
                if( (temp2 - temp1) != 1 ) return -1;
                temp1=input.get(i);
            }
            return STRAIGHT;
        }
        return -1;

    }

    private void initiateSortingArray(){

        for(int i=0;i<14;i++) {
            for(int j=0;j<4;j++) {
                TempSorting[i][j]= -1;
            }
        }
    }

    private void SortArray(ArrayList<Integer> list) {
        initiateSortingArray();
        for(int i=0; i < list.size(); i++){
            int mod = list.get(i) % 13;
            int div = list.get(i) / 13;
            Log.d("Mod",String.valueOf(mod));
            Log.d("Div",String.valueOf(div));
            if(div == 4) {
                TempSorting[13][mod] = list.get(i);
            }else {
                TempSorting[mod][div] = list.get(i);
            }
            Log.d("MOVE_CARD",  String.valueOf(list.get(i)));
           // Log.d("Mod", String.valueOf(mod));
           // Log.d("Div",  String.valueOf(div));
        }

        list.clear();
        tempList.clear();
        for(int i=0; i <14; i++) {
            for (int j = 0; j < 4; j++)
                if (TempSorting[i][j] != -1) {
                    tempList.add(TempSorting[i][j]);
                }
        }
            Log.d("TempList", tempList.toString());
            if(tempList != null) {
                //append oxi add giati tis svinei
                //for(int k=0;k < tempList.size();k++){
                    //tempList.add
                list.addAll(tempList);
                //}
            }
            tempList.clear();
            Log.d("MOVE_CARD",  MOVE_CARD.toString());
    }

    public boolean isGreater(int x , int y) {
        int temp1 = x % 13;
        int temp2 = y % 13;

        Log.d("X",String.valueOf(temp1));
        Log.d("Y", String.valueOf(temp2));
        if( temp1 > temp2 ) return true;
        return false;
    }

    public void makeMove(){
        for (int i = 0; i < DECK_CARDS_VIEWS.size(); i++) {
            mainLayout.removeView(DECK_CARDS_VIEWS.get(i));
        }
        for (int i = 0; i < MOVE_CARD.size(); i++) {
            ImageView DeckCardView = new ImageView(this);
            //cardViewArray[i].setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dogs))
            if( MOVE_CARD.get(i) == 53) {
                DeckCardView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dragon));
            } else if( MOVE_CARD.get(i) == 54) {
                DeckCardView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mayong));
            }else if( MOVE_CARD.get(i) == 55) {
                DeckCardView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dogs));
            }else if( MOVE_CARD.get(i) == 56) {
                DeckCardView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.phoenix));
            } else {
                DeckCardView.setImageBitmap(cards[MOVE_CARD.get(i)]);
                //Log.d("SCREEN HEIGHT", String.valueOf(SCREEN_HEIGHT));
            }

            DeckCardView.setY(300);
            DeckCardView.setX(500 + i * 50);
            DeckCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
                });
                mainLayout.addView(DeckCardView);
                DECK_CARDS_VIEWS.add(DeckCardView);
        }
        DECK_CARDS.clear();
        DECK_CARDS.addAll(MOVE_CARD);
        refreshList();
        USER_CARD.removeAll(MOVE_CARD);
        MOVE_CARD.clear();
        if (USER_CARD.size() == 0) {
            Utils.showToastAlert(this, "You are out");
            tmpButton = (Button) findViewById(R.id.button2);
            tmpButton.setVisibility(View.INVISIBLE);
            tmpButton = (Button) findViewById(R.id.button5);
            tmpButton.setVisibility(View.INVISIBLE);
            for (int i = 0; i < DECK_CARDS_VIEWS.size(); i++) {
                mainLayout.removeView(DECK_CARDS_VIEWS.get(i));
            }
        }
        tmpButton = (Button) findViewById(R.id.button3);
        tmpButton.setVisibility(View.INVISIBLE);
        //SortArray(USER_CARD);
        addCardsToHand(USER_CARD.size(), 2, USER_CARD);
    }

    private boolean typeIsSame(ArrayList<Integer> list1,ArrayList<Integer> list2){

        if(detect(list1) == detect(list2)) {
            if(detect(list1) == 5) {
                for(int i=0; i < 5;i++) {
                    int temp =list1.get(i)%13;
                    if (temp == 0 ) temp=13;
                    list1.set(i,temp);
                    temp = list2.get(i)%13;
                    if (temp == 0 ) temp=13;
                    list2.set(i,temp);
                }

                Log.d("LIST1",list1.toString());
                Log.d("List2",list2.toString());

                if(list1.get(0)==list1.get(1) ) {
                }

            }
            return true;
        }
        return false;
    }

    private void enemiesCards() {

        //left
       ImageView img = new ImageView(this);
       img.setImageResource(R.drawable.backtichu);
       img.setX(30);
       img.setY(370);
       mainLayout.addView(img);
       TextView text = new TextView(this);
       text.setText("14");
       text.setTextColor(Color.WHITE);
       text.setTextSize(30);
       text.setX(30);
       text.setY(390);
        mainLayout.addView(text);

        //right
        ImageView img2 = new ImageView(this);
        img2.setImageResource(R.drawable.backtichu);
        img2.setX(1700);
        img2.setY(370);
        mainLayout.addView(img2);
        TextView text2 = new TextView(this);
        text2.setText("14");
        text2.setTextColor(Color.WHITE);
        text2.setTextSize(30);
        text2.setX(1700);
        text2.setY(390);
        mainLayout.addView(text2);

        //top
        ImageView img3 = new ImageView(this);
        img3.setImageResource(R.drawable.backtichu);
        img3.setX(850);
        img3.setY(50);
        mainLayout.addView(img3);
        TextView text3 = new TextView(this);
        text3.setText("14");
        text3.setTextColor(Color.WHITE);
        text3.setTextSize(30);
        text3.setX(850);
        text3.setY(70);
        mainLayout.addView(text3);

    }

    private void dealNewCards(){
        for(int i=0;i<= 55;i++){
            DECK_ALL_CARDS.add(i+1);
        }
        Collections.shuffle(DECK_ALL_CARDS);
        for(int i=0;i<14;i++){
            USER_CARD.add(DECK_ALL_CARDS.remove(0));
            OPP1_CARD.add(DECK_ALL_CARDS.remove(0));
            OPP2_CARD.add(DECK_ALL_CARDS.remove(0));
            TEAM_CARD.add(DECK_ALL_CARDS.remove(0));
        }
        Log.d("User Card",USER_CARD.toString());
        Log.d("Opp1 Card",OPP1_CARD.toString());
        Log.d("Opp2 Card",OPP2_CARD.toString());
        Log.d("Team Card",TEAM_CARD.toString());
    }

    /**
     * Show or hide the "waiting for opponent" dialog depending on who's turn it is.
     */
    private void setTurn() {
        if (gameStarted) {
            if (myTurn) {
                Log.d("MyTurn",sfsClient.getMySelf().toString());
            } else {
                Log.d("NotMyTurn",sfsClient.getMySelf().toString());
            }
        }
    }


    private int userHasMayong() {
        for(int i=0; i < 14 ; i++) {
            if(USER_CARD.get(i) == 53 ) {
                return 0;
            } else if(OPP1_CARD.get(i) == 53 ) {
                return 1;
            } else if(OPP2_CARD.get(i) == 53 ) {
                return 3;
            } else if(TEAM_CARD.get(i) == 53 ) {
                return 2;
            }
        }
        return -1;
    }

    /**
     * Set who's turn it is and start the game
     *
     * @param resObj
     */
    private void startGame(ISFSObject resObj) {
        //gameStarted = true;
        int firstTurn = resObj.getInt("t");
        myTurn = sfsClient.getMySelf().getPlayerId() == firstTurn ? true : false;
        //board.clear();
        //setTurn();
        //initScreen();
       // waitText.setVisibility(View.INVISIBLE);
        Log.d("start", "game") ;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                initScreen();
            }
        });

        gameStarted = true;
    }

    private void sendMove(ArrayList<Integer> move) {
        ISFSObject sfso = new SFSObject();
        sfso.putIntArray("x", move);
        sfsClient.send(new ExtensionRequest("move", sfso, sfsClient.getLastJoinedRoom()));
    }

    private void moveReceived(ISFSObject resObj) {
         ArrayList<Integer> move = (ArrayList<Integer>) resObj.getIntArray("x");
         refreshList();
         USER_CARD.removeAll(move);
         //Utils.showToastAlert(this, "Wait other players");
         USER_CARD.add(move.get(0));
         USER_CARD.add(move.get(1));
         USER_CARD.add(move.get(2));
         addCardsToHand(USER_CARD.size(), 2, USER_CARD);
         GAME_STATUS=2;
         EXCHANGE_STATUS=3;
    }
    @Override
    public void dispatch(final BaseEvent event) throws SFSException {
// If connection is lost switch back to main activity
        if (event.getType().equalsIgnoreCase(SFSEvent.CONNECTION_LOST)) {
            //showMessage(getString(R.string.connection_lost)
                   // + "\n"
                   // + getString(R.string.dialog_connection_lost_message,
                   // event.getArguments().get("reason").toString()));
            Log.d("CONNECTION LOST",sfsClient.getMySelf().toString());
        }
        if (event.getType().equalsIgnoreCase(SFSEvent.EXTENSION_RESPONSE)) {
            String cmd = event.getArguments().get("cmd").toString();
            ISFSObject resObj = new SFSObject();
            resObj = (ISFSObject) event.getArguments().get("params");

            switch (moves.valueOf(cmd)) {
                case start:
                    startGame(resObj);
                    break;

                case move:
                    moveReceived(resObj);
                    break;

                case win:
                case tie:
                   // showWinner(cmd, resObj);
                    break;
            }
        }
        // Handle a user leaving the room - display winners message to remaining player if game is
        // not yet over
        if (event.getType().equalsIgnoreCase(SFSEvent.USER_EXIT_ROOM)) {
            Room room = (Room) event.getArguments().get("room");
            if (room.isGame() && gameStarted) {
                User user = (User) event.getArguments().get("user");
                ISFSObject obj = new SFSObject();
                obj.putUtfString("q", user.getName());
                //showWinner("earlyExit", obj);
            }
        }

    }

}


