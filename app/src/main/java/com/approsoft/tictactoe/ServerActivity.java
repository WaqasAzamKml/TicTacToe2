package com.approsoft.tictactoe;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ServerActivity extends AppCompatActivity {

    Button btnLogout, btnCreateServer, btnJoinServer;

    DatabaseReference game_servers = FirebaseDatabase.getInstance().getReference().getRoot().child("game_servers");

    String my_server_name, my_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    ArrayList<String> availableServers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnCreateServer = (Button) findViewById(R.id.btnCreateServer);
        btnJoinServer = (Button) findViewById(R.id.btnJoinServer);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance().signOut(ServerActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ServerActivity.this.finish();
                    }
                });
            }
        });

        btnCreateServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateDialog();
            }
        });

        btnJoinServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJoinDialog();
            }
        });

        game_servers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext()){
                    set.add(((DataSnapshot)i.next()).getKey());
                }
                availableServers.clear();
                availableServers.addAll(set);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void showCreateDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Server Name");
        final EditText etServerName = new EditText(this);
        builder.setView(etServerName);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(etServerName.getText())){
                    etServerName.setError("Required");
                    etServerName.requestFocus();
                }else{
                    Map<String, Object> server_name = new HashMap<>();
                    my_server_name = etServerName.getText().toString();
                    server_name.put(my_server_name,"");
                    game_servers.updateChildren(server_name);

                    DatabaseReference my_server = game_servers.child(my_server_name);
//                            FirebaseDatabase.getInstance().getReference()
//                            .getRoot().child("game_servers").child(my_server_name);
                    Map<String, Object> players = new HashMap<>();
                    players.put("players","");
                    my_server.updateChildren(players);

                    DatabaseReference players_reference = my_server.child("players");
//                            FirebaseDatabase.getInstance().getReference()
//                            .getRoot().child("game_servers").child(my_server_name).child("players");

                    Map<String, Object> player1 = new HashMap<>();
                    player1.put("player1",my_email);
                    players_reference.updateChildren(player1);

                    Intent intent = new Intent(ServerActivity.this, GameActivity.class);
                    intent.putExtra("serverName", my_server_name);
                    intent.putExtra("my_email", my_email);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    public void showJoinDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Server Name");
        final EditText etServerName = new EditText(this);
        builder.setView(etServerName);
        builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(etServerName.getText())){
                    etServerName.setError("Required");
                    etServerName.requestFocus();
                }else{
                    my_server_name = etServerName.getText().toString();
                    String server_name = "";
                    if(availableServers!=null && availableServers.size()>0){
                        for(int index = 0; index<availableServers.size(); index++){
                            if(my_server_name.equals(availableServers.get(index))) {
                                server_name = my_server_name;
                                break;
                            }
                        }
                        if(server_name!=null && !server_name.equals("")){
                            DatabaseReference found_server = game_servers.child(server_name).child("players");

                            Map<String, Object> map = new HashMap<>();
                            map.put("player2", my_email);
                            found_server.updateChildren(map);

                            Intent intent = new Intent(ServerActivity.this, GameActivity.class);
                            intent.putExtra("serverName", my_server_name);
                            intent.putExtra("my_email", my_email);
                            startActivity(intent);

//                            DatabaseReference player2Ref = found_server.child("players").child("player2");
//                            DatabaseReference player1Ref = found_server.child("players").child("player1");
//                            String player1Key = player1Ref.getKey();
//                            String player2Key = player2Ref.getKey();
//                            if(player2Key!=null){
//                                Toast.makeText(ServerActivity.this, "Server already full.", Toast.LENGTH_SHORT).show();
//                            }else {
//
//                                Map<String, Object> map = new HashMap<>();
//                                map.put("player2", my_email);
//                                found_server.updateChildren(map);
//
//                                Intent intent = new Intent(ServerActivity.this, GameActivity.class);
//                                intent.putExtra("serverName", my_server_name);
//                                intent.putExtra("my_email", my_email);
//                                startActivity(intent);
//                            }
                        }else{
                            Toast.makeText(ServerActivity.this, "Server not found.", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ServerActivity.this, "Server not found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }
}
