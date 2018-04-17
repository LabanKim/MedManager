package com.fusion.kim.medmanager;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference mUsersRef;

    private TextView mNameTv, mEmailTv;
    private EditText mNameInput;
    private Button mUpdateBtn;

    private String mDisplayName = null, mEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mUsersRef = FirebaseDatabase.getInstance().getReference().
                child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mNameTv = findViewById(R.id.tv_display_name);
        mEmailTv = findViewById(R.id.tv_email);
        mNameInput = findViewById(R.id.input_dp_name);
        mUpdateBtn = findViewById(R.id.btn_update);

        mUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mDisplayName = dataSnapshot.child("displayName").getValue(String.class);
                mEmail = dataSnapshot.child("email").getValue(String.class);

                mNameTv.setText(mDisplayName);
                mEmailTv.setText(mEmail);

                mNameInput.setText(mDisplayName);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = mNameInput.getText().toString().trim();

                final ProgressDialog updateProgress = new ProgressDialog(ProfileActivity.this);
                updateProgress.setMessage("Updating Profile...");
                updateProgress.setCancelable(false);
                updateProgress.show();

                mUsersRef.child("displayName").setValue(newName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            updateProgress.dismiss();
                            Toast.makeText(ProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });


    }
}
