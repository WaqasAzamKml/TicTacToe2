package com.approsoft.tictactoe;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private List<AuthUI.IdpConfig> providers;
    private static final int RC_SIGN_IN = 1122;

//    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        btnLogout = (Button) findViewById(R.id.btnLogout);
//        btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        MainActivity.this.finish();
//                    }
//                });
//            }
//        });

        providers = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            Toast.makeText(this, "User: "+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, ServerActivity.class));
            MainActivity.this.finish();
        }else{
            providers.add(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());
            providers.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
            providers.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setProviders(
                            providers
                    ).build(), RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "User: "+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, ServerActivity.class));
                MainActivity.this.finish();
            }else{
                Toast.makeText(this, "Unable to Log in.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
