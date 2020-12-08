package com.example.comp39x;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class menu extends AppCompatActivity {
    private Button creategroup;
    public static String sports = "";
    private static final String TAG = "menu";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore fstore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fstore = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_menu);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(menu.this);
        final String personName = acct.getGivenName();
        String displayName = acct.getDisplayName();
        final String email = acct.getEmail();
        final String last = acct.getFamilyName();
        final String id1 = acct.getId();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference docIdRef = rootRef.collection("users").document(id1);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                    } else {
                        DocumentReference documentReference = fstore.collection("users").document(id1);
                        Map<String, Object> user = new HashMap<>();
                        user.put("first", personName);
                        user.put("email", email);
                        user.put("last", last);
                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "info updated");
                            }
                        });
                    }
                } else {
                    Log.d("TAG", "Failed with: ", task.getException());
                }
            }
        });

        creategroup = (Button) findViewById(R.id.button);


        creategroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(menu.this, creategruop.class));

            }
        });

    }
}
