package com.approsoft.tictactoe;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GameActivity extends AppCompatActivity {

//    Button btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9;
//    Button btnNew,btnReset;
    ArrayList<Button> buttonsList = new ArrayList<>();
    TextView tvXwins, tvDraws, tvOwins, tvTurn;
    AlertDialog alertDialog;
    int xWins = 0;
    int oWins = 0;
    int d = 0;
    int flag = 0;
    int draw = 0;
    int numberOfPlayers = 0;
    int btnNumber;
    String moveLabel;
    String tempKey, username, serverName, my_email, player1Email, player2Email;
    HashMap<String, Object> map;
    HashMap<String, Object> move;
    boolean iAmX = false;

    int[] buttonIDs = {R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
            R.id.btn8, R.id.btn9};

    DatabaseReference serverRef, playerSectionRef, player1Ref, player2Ref, gameActivityRef;
    DatabaseReference game_servers = FirebaseDatabase.getInstance()
            .getReference().getRoot().child("game_servers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();

        alertDialog = new AlertDialog.Builder(GameActivity.this).create();
        alertDialog.setTitle("Tic Tac Toe");
        alertDialog.setMessage("Waiting for another player...");
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit Server", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(GameActivity.this, ServerActivity.class));
                GameActivity.this.finish();
            }
        });
        alertDialog.show();
        alertDialog.setCancelable(false);

        serverName = getIntent().getStringExtra("serverName");
        my_email = getIntent().getStringExtra("my_email");

        serverRef = game_servers.child(serverName);
        gameActivityRef = serverRef.child("gameActivity");
        playerSectionRef = serverRef.child("players");
        player1Ref = playerSectionRef.child("player1");
        player2Ref = playerSectionRef.child("player2");

        buttonsList.clear();
        for(int i=0; i<9; i++){
            Button button = (Button) findViewById(buttonIDs[i]);
            buttonsList.add(i, button);
        }
//        btn1 = (Button) findViewById(R.id.btn1);
//        btn2 = (Button) findViewById(R.id.btn2);
//        btn3 = (Button) findViewById(R.id.btn3);
//        btn4 = (Button) findViewById(R.id.btn4);
//        btn5 = (Button) findViewById(R.id.btn5);
//        btn6 = (Button) findViewById(R.id.btn6);
//        btn7 = (Button) findViewById(R.id.btn7);
//        btn8 = (Button) findViewById(R.id.btn8);
//        btn9 = (Button) findViewById(R.id.btn9);

//        btnNew = (Button) findViewById(R.id.btnNew);
//        btnReset = (Button) findViewById(R.id.btnReset);

        disableButtons();

        player1Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                player1Email = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        player2Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                player2Email = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        playerSectionRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                numberOfPlayers++;
                if(numberOfPlayers==2){
                    alertDialog.cancel();
                    Map<String, Object> map = new HashMap<>();
                    map.put("gameActivity", "");
                    serverRef.updateChildren(map);
                    gameActivityRef = serverRef.child("gameActivity");

                    if(my_email.equals(player1Email)){
                        iAmX = true;
                        myTurn();
                    }else{
                        iAmX = false;
                        opponentsTurn();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                --numberOfPlayers;
                if(numberOfPlayers<2){
                    startActivity(new Intent(GameActivity.this, ServerActivity.class));
                    GameActivity.this.finish();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        gameActivityRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext()){
                    String moveLocation = (String) ((DataSnapshot) i.next()).getValue();
                    String moveSymbol = (String) ((DataSnapshot) i.next()).getValue();
                    int moveLocationInt = Integer.valueOf(moveLocation);
                    Button button = buttonsList.get(moveLocationInt);
                    button.setText(moveSymbol);
                    button.setEnabled(false);
                    draw++;
                    checkWin();
                    checkDraw();
                    if(iAmX){
                        if(moveSymbol.equals("X")){
                            //Todo : I sent this move
                            opponentsTurn();
                        }else{
                            //Todo : Opponent sent this move
                            myTurn();
                        }
                    }else{
                        if(moveSymbol.equals("O")){
                            //Todo : I sent this move
                            opponentsTurn();
                        }else{
                            //Todo : Opponent sent this move
                            myTurn();
                        }
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        tvDraws = (TextView) findViewById(R.id.tvDraws);
        tvXwins = (TextView) findViewById(R.id.tvXwins);
        tvOwins = (TextView) findViewById(R.id.tvOwins);
        tvTurn = (TextView) findViewById(R.id.tvTurn);

//        btnNew.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                newGame();
//            }
//        });
//        btnReset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                xWins = 0;
//                tvXwins.setText("X Wins:");
//                oWins = 0;
//                tvOwins.setText("O Wins:");
//                d = 0;
//                tvDraws.setText("Draws:");
//                newGame();
//            }
//        });

        for(int i=0; i<9; i++){
            final Button button = buttonsList.get(i);
            final String index = String.valueOf(i);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(iAmX){
                        button.setText("X");

                        Map<String, Object> map = new HashMap<>();
                        String tempKey = gameActivityRef.push().getKey();
                        gameActivityRef.updateChildren(map);

                        DatabaseReference moveRoot = gameActivityRef.child(tempKey);
                        Map<String, Object> move = new HashMap<>();
                        move.put("location", index);
                        move.put("symbol", "X");
                        moveRoot.updateChildren(move);

                    }else{
                        button.setText("O");

                        Map<String, Object> map = new HashMap<>();
                        String tempKey = gameActivityRef.push().getKey();
                        gameActivityRef.updateChildren(map);

                        DatabaseReference moveRoot = gameActivityRef.child(tempKey);
                        Map<String, Object> move = new HashMap<>();
                        move.put("location", index);
                        move.put("symbol", "O");
                        moveRoot.updateChildren(move);

                    }
                    opponentsTurn();
                    button.setEnabled(false);
//                    draw++;
//                    checkWin();
//                    checkDraw();
                }
            });
        }

//        btn1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(iAmX){
//                    btn1.setText("X");
//                    Map<String, Object> move = new HashMap<>();
//                    move.put("location", "1");
//                    move.put("symbol", "X");
//                    gameActivityRef.updateChildren(move);
//                    opponentsTurn();
//                }else{
//                    btn1.setText("O");
//                    Map<String, Object> move = new HashMap<>();
//                    move.put("location", "1");
//                    move.put("symbol", "O");
//                    gameActivityRef.updateChildren(move);
//                    opponentsTurn();
//                }
//
////                if(flag ==0){
////                    btn1.setText("X");
////                    tvTurn.setText("Turn: O");
////                    flag = 1;
////                }
////                else{
////                    btn1.setText("O");
////                    tvTurn.setText("Turn: X");
////                    flag = 0;
////                }
//
//
//                btn1.setEnabled(false);
//                draw++;
//                checkWin();
//                checkDraw();
//            }
//        });
//        btn2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(flag ==0){
//                    btn2.setText("X");
//                    tvTurn.setText("Turn: O");
//                    flag = 1;
//                }
//                else{
//                    btn2.setText("O");
//                    tvTurn.setText("Turn: X");
//                    flag = 0;
//                }
//                btn2.setEnabled(false);
//                draw++;
//                checkWin();
//                checkDraw();
//            }
//        });
//        btn3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(flag ==0){
//                    btn3.setText("X");
//                    tvTurn.setText("Turn: O");
//                    flag = 1;
//                }
//                else{
//                    btn3.setText("O");
//                    tvTurn.setText("Turn: X");
//                    flag = 0;
//                }
//                btn3.setEnabled(false);
//                draw++;
//                checkWin();
//                checkDraw();
//            }
//        });
//        btn4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(flag ==0){
//                    btn4.setText("X");
//                    tvTurn.setText("Turn: O");
//                    flag = 1;
//                }
//                else{
//                    btn4.setText("O");
//                    tvTurn.setText("Turn: X");
//                    flag = 0;
//                }
//                btn4.setEnabled(false);
//                draw++;
//                checkWin();
//                checkDraw();
//            }
//        });
//        btn5.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(flag ==0){
//                    btn5.setText("X");
//                    tvTurn.setText("Turn: O");
//                    flag = 1;
//                }
//                else{
//                    btn5.setText("O");
//                    tvTurn.setText("Turn: X");
//                    flag = 0;
//                }
//                btn5.setEnabled(false);
//                draw++;
//                checkWin();
//                checkDraw();
//            }
//        });
//        btn6.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(flag ==0){
//                    btn6.setText("X");
//                    tvTurn.setText("Turn: O");
//                    flag = 1;
//                }
//                else{
//                    btn6.setText("O");
//                    tvTurn.setText("Turn: X");
//                    flag = 0;
//                }
//                btn6.setEnabled(false);
//                draw++;
//                checkWin();
//                checkDraw();
//            }
//        });
//        btn7.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(flag ==0){
//                    btn7.setText("X");
//                    tvTurn.setText("Turn: O");
//                    flag = 1;
//                }
//                else{
//                    btn7.setText("O");
//                    tvTurn.setText("Turn: X");
//                    flag = 0;
//                }
//                btn7.setEnabled(false);
//                draw++;
//                checkWin();
//                checkDraw();
//            }
//        });
//        btn8.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(flag ==0){
//                    btn8.setText("X");
//                    tvTurn.setText("Turn: O");
//                    flag = 1;
//                }
//                else{
//                    btn8.setText("O");
//                    tvTurn.setText("Turn: X");
//                    flag = 0;
//                }
//                btn8.setEnabled(false);
//                draw++;
//                checkWin();
//                checkDraw();
//            }
//        });
//        btn9.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(flag ==0){
//                    btn9.setText("X");
//                    tvTurn.setText("Turn: O");
//                    flag = 1;
//                }
//                else{
//                    btn9.setText("O");
//                    tvTurn.setText("Turn: X");
//                    flag = 0;
//                }
//                btn9.setEnabled(false);
//                draw++;
//                checkWin();
//                checkDraw();
//            }
//        });


    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are You Sure?");
        builder.setMessage("Are you sure you want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Todo: Remove this player from server and then move back to ServerActivity
                GameActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void myTurn(){
        tvTurn.setText("Your Turn");
        tvTurn.setTextColor(Color.GREEN);
        enableEmptyButtons();
    }
    private void opponentsTurn(){
        tvTurn.setText("Opponents Turn");
        tvTurn.setTextColor(Color.RED);
        disableButtons();
    }

    private void enableEmptyButtons(){
        for(int i=0; i<9; i++){
            Button button = buttonsList.get(i);
            if(TextUtils.isEmpty(button.getText().toString())){
                button.setEnabled(true);
            }else
                button.setEnabled(false);
        }

//        String b1 = btn1.getText().toString();
//        if(b1==null || TextUtils.isEmpty(b1)){
//            btn1.setEnabled(true);
//        }else{
//            btn1.setEnabled(false);
//        }
//        String b2 = btn2.getText().toString();
//        if(b2==null || TextUtils.isEmpty(b2)){
//            btn2.setEnabled(true);
//        }else{
//            btn2.setEnabled(false);
//        }
//        String b3 = btn3.getText().toString();
//        if(b3==null || TextUtils.isEmpty(b3)){
//            btn3.setEnabled(true);
//        }else{
//            btn3.setEnabled(false);
//        }
//        String b4 = btn4.getText().toString();
//        if(b4==null || TextUtils.isEmpty(b4)){
//            btn4.setEnabled(true);
//        }else{
//            btn4.setEnabled(false);
//        }
//        String b5 = btn5.getText().toString();
//        if(b5==null || TextUtils.isEmpty(b5)){
//            btn5.setEnabled(true);
//        }else{
//            btn5.setEnabled(false);
//        }
//        String b6 = btn6.getText().toString();
//        if(b6==null || TextUtils.isEmpty(b6)){
//            btn6.setEnabled(true);
//        }else{
//            btn6.setEnabled(false);
//        }
//        String b7 = btn7.getText().toString();
//        if(b7==null || TextUtils.isEmpty(b7)){
//            btn7.setEnabled(true);
//        }else{
//            btn7.setEnabled(false);
//        }
//        String b8 = btn8.getText().toString();
//        if(b8==null || TextUtils.isEmpty(b8)){
//            btn8.setEnabled(true);
//        }else{
//            btn8.setEnabled(false);
//        }
//        String b9 = btn9.getText().toString();
//        if(b9==null || TextUtils.isEmpty(b9)){
//            btn9.setEnabled(true);
//        }else{
//            btn9.setEnabled(false);
//        }

    }

    private void checkWin(){
        String b1 = buttonsList.get(0).getText().toString();
        String b2 = buttonsList.get(1).getText().toString();
        String b3 = buttonsList.get(2).getText().toString();
        String b4 = buttonsList.get(3).getText().toString();
        String b5 = buttonsList.get(4).getText().toString();
        String b6 = buttonsList.get(5).getText().toString();
        String b7 = buttonsList.get(6).getText().toString();
        String b8 = buttonsList.get(7).getText().toString();
        String b9 = buttonsList.get(8).getText().toString();

//        String b1 = btn1.getText().toString();
//        String b2 = btn2.getText().toString();
//        String b3 = btn3.getText().toString();
//        String b4 = btn4.getText().toString();
//        String b5 = btn5.getText().toString();
//        String b6 = btn6.getText().toString();
//        String b7 = btn7.getText().toString();
//        String b8 = btn8.getText().toString();
//        String b9 = btn9.getText().toString();

        //Row 1
        if(b1.equals(b2) && b2.equals(b3) && !b1.equals("")){
            Toast.makeText(this, b1 + " wins.", Toast.LENGTH_SHORT).show();
            if(b1.equals("X")){
                xWins++;
                tvXwins.setText("X Wins: "+xWins);
            }else{
                oWins++;
                tvOwins.setText("O Wins: "+oWins);
            }
            newGame();
        }
        //Row 2
        if(b4.equals(b5) && b5.equals(b6) && !b4.equals("")){
            Toast.makeText(this, b4 + " wins.", Toast.LENGTH_SHORT).show();
            if(b4.equals("X")){
                xWins++;
                tvXwins.setText("X Wins: "+xWins);
            }else{
                oWins++;
                tvOwins.setText("O Wins: "+oWins);
            }
            newGame();
        }
        //Row 3
        if(b7.equals(b8) && b8.equals(b9) && !b7.equals("")){
            Toast.makeText(this, b7 + " wins.", Toast.LENGTH_SHORT).show();
            if(b7.equals("X")){
                xWins++;
                tvXwins.setText("X Wins: "+xWins);
            }else{
                oWins++;
                tvOwins.setText("O Wins: "+oWins);
            }
            newGame();
        }
        //Column 1
        if(b1.equals(b4) && b4.equals(b7) && !b1.equals("")){
            Toast.makeText(this, b1 + " wins.", Toast.LENGTH_SHORT).show();
            if(b1.equals("X")){
                xWins++;
                tvXwins.setText("X Wins: "+xWins);
            }else{
                oWins++;
                tvOwins.setText("O Wins: "+oWins);
            }
            newGame();
        }
        //Column 2
        if(b2.equals(b5) && b5.equals(b8) && !b2.equals("")){
            Toast.makeText(this, b2 + " wins.", Toast.LENGTH_SHORT).show();
            if(b2.equals("X")){
                xWins++;
                tvXwins.setText("X Wins: "+xWins);
            }else{
                oWins++;
                tvOwins.setText("O Wins: "+oWins);
            }
            newGame();
        }
        //Column 3
        if(b3.equals(b6) && b6.equals(b9) && !b3.equals("")){
            Toast.makeText(this, b3 + " wins.", Toast.LENGTH_SHORT).show();
            if(b3.equals("X")){
                xWins++;
                tvXwins.setText("X Wins: "+xWins);
            }else{
                oWins++;
                tvOwins.setText("O Wins: "+oWins);
            }
            newGame();
        }
        //Diagonal 1 to 9
        if(b1.equals(b5) && b5.equals(b9) && !b1.equals("")){
            Toast.makeText(this, b1 + " wins.", Toast.LENGTH_SHORT).show();
            if(b1.equals("X")){
                xWins++;
                tvXwins.setText("X Wins: "+xWins);
            }else{
                oWins++;
                tvOwins.setText("O Wins: "+oWins);
            }
            newGame();
        }
        //Diagonal 3 to 7
        if(b3.equals(b5) && b5.equals(b7) && !b3.equals("")){
            Toast.makeText(this, b3 + " wins.", Toast.LENGTH_SHORT).show();
            if(b3.equals("X")){
                xWins++;
                tvXwins.setText("X Wins: "+xWins);
            }else{
                oWins++;
                tvOwins.setText("O Wins: "+oWins);
            }
            newGame();
        }


    }

    private void newGame(){
        for(int i=0; i<9; i++){
            Button button = buttonsList.get(i);
            button.setEnabled(true);
            button.setText("");
        }
        gameActivityRef.setValue(null);
        Map<String, Object> map = new HashMap<>();
        map.put("gameActivity", "");
        serverRef.updateChildren(map);
        gameActivityRef = serverRef.child("gameActivity");

//        btn1.setEnabled(true);
//        btn1.setText("");
//        btn2.setEnabled(true);
//        btn2.setText("");
//        btn3.setEnabled(true);
//        btn3.setText("");
//        btn4.setEnabled(true);
//        btn4.setText("");
//        btn5.setEnabled(true);
//        btn5.setText("");
//        btn6.setEnabled(true);
//        btn6.setText("");
//        btn7.setEnabled(true);
//        btn7.setText("");
//        btn8.setEnabled(true);
//        btn8.setText("");
//        btn9.setEnabled(true);
//        btn9.setText("");
        draw = 0;
    }

    private void checkDraw(){
        if(draw==9){
            Toast.makeText(GameActivity.this, "Game Drawn", Toast.LENGTH_SHORT).show();
            d++;
            tvDraws.setText("Draws: "+d);
            newGame();
        }
    }

    private void disableButtons(){
        for(int i=0; i<9; i++){
            Button button = buttonsList.get(i);
            button.setEnabled(false);
        }
//        btn1.setEnabled(false);
//        btn2.setEnabled(false);
//        btn3.setEnabled(false);
//        btn4.setEnabled(false);
//        btn5.setEnabled(false);
//        btn6.setEnabled(false);
//        btn7.setEnabled(false);
//        btn8.setEnabled(false);
//        btn9.setEnabled(false);

    }
    private void enableButtons(){
        for(int i=0; i<9; i++){
            Button button = buttonsList.get(i);
            button.setEnabled(true);
        }

//        btn1.setEnabled(true);
//        btn2.setEnabled(true);
//        btn3.setEnabled(true);
//        btn4.setEnabled(true);
//        btn5.setEnabled(true);
//        btn6.setEnabled(true);
//        btn7.setEnabled(true);
//        btn8.setEnabled(true);
//        btn9.setEnabled(true);

    }
}
