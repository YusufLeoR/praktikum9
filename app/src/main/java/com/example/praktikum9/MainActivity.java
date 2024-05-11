package com.example.praktikum9;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etEmail, etPass;
    private Button btnMasuk, btnDaftar, btnGoogle;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    GoogleSignInClient mGoogleSignInClient;

    int RC_SIGN_IN = 20;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etEmail = (EditText)findViewById(R.id.et_email);
        etPass = (EditText)findViewById(R.id.et_pass);
        btnMasuk = (Button) findViewById(R.id.btn_masuk);
        btnDaftar = (Button)findViewById(R.id.btn_daftar);
        btnGoogle = (Button) findViewById(R.id.btn_google);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnMasuk.setOnClickListener(this);
        btnDaftar.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
    }

    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firbaseAuth(account.getIdToken());
            }
            catch (Exception e){
                Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firbaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id",user.getUid());
                    map.put("name", user.getDisplayName());

                    database.getReference().child("users").child(user.getUid()).setValue(map);

                    Intent intent = new Intent(MainActivity.this, InsertNoteActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this, "Ada error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkPreviousGoogleSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    googleSignIn();
                }
            });
        } else {
            googleSignIn();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            Intent intent = new Intent(MainActivity.this, InsertNoteActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_masuk:
                login(etEmail.getText().toString(),
                        etPass.getText().toString());
                break;
            case R.id.btn_daftar:
                signUp(etEmail.getText().toString(),
                        etPass.getText().toString());
                break;
            case R.id.btn_google:
                checkPreviousGoogleSignIn();
                break;
        }
    }
    public void signUp(String email,String password){
        if (!validateForm()){
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override

                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG,
                                    "createUserWithEmail:success");
                            FirebaseUser user =
                                    mAuth.getCurrentUser();
                            updateUI(user);
                            Toast.makeText(MainActivity.this,
                                    user.toString(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG,
                                    "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this,
                                    task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }
    public void login(String email,String password){
        if (!validateForm()){
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, user.toString(), Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }
    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(etEmail.getText().toString())) {
            etEmail.setError("Required");
            result = false;
        } else {
            etEmail.setError(null);
        }
        if (TextUtils.isEmpty(etPass.getText().toString())) {
            etPass.setError("Required");
            result = false;
        } else {
            etPass.setError(null);
        }
        return result;
    }
    public void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, InsertNoteActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this,"Log In First",
                    Toast.LENGTH_SHORT).show();
        }
    }
}