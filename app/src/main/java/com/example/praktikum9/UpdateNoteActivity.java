package com.example.praktikum9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateNoteActivity extends AppCompatActivity {

    private EditText etTitleUpdate, etDescriptionUpdate;
    private Button btnSubmitUpdate;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_note);

        etTitleUpdate = findViewById(R.id.et_titleUpdate);
        etDescriptionUpdate = findViewById(R.id.et_descriptionUpdate);
        btnSubmitUpdate = findViewById(R.id.btn_submitUpdate);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        String noteId = getIntent().getStringExtra("note_id");
        String noteTitle = getIntent().getStringExtra("note_title");
        String noteDesc = getIntent().getStringExtra("note_desc");

        etTitleUpdate.setText(noteTitle);
        etDescriptionUpdate.setText(noteDesc);

        btnSubmitUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(noteId);
            }
        });
    }

    private void updateData(String noteId){
        String titleUpdate = etTitleUpdate.getText().toString();
        String descUpdate = etDescriptionUpdate.getText().toString();
        if (!TextUtils.isEmpty(titleUpdate) && !TextUtils.isEmpty(descUpdate)) {
            Note updatedNote = new Note();
            updatedNote.setId(noteId);
            updatedNote.setTitle(titleUpdate);
            updatedNote.setDescription(descUpdate);

            databaseReference.child("notes").child(FirebaseAuth.getInstance().getUid()).child(noteId)
                    .setValue(updatedNote)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UpdateNoteActivity.this, "Data updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UpdateNoteActivity.this, "Failed to update data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(UpdateNoteActivity.this, "Title and Description cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }
}