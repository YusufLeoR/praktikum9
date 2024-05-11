package com.example.praktikum9;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InsertNoteActivity extends AppCompatActivity implements View.OnClickListener, NoteAdapter.OnNoteDeleteListener, NoteAdapter.OnNoteUpdateListener {

    private TextView tvEmail, tvUid;
    private Button btnKeluar, btnSubmit;
    private FirebaseAuth mAuth;
    private EditText etTitle, etDesc;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private Note note;

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_note);

        tvEmail = findViewById(R.id.tv_email);
        tvUid = findViewById(R.id.tv_uid);
        btnKeluar = findViewById(R.id.btn_keluar);

        mAuth = FirebaseAuth.getInstance();
        btnKeluar.setOnClickListener(this);

        etTitle = findViewById(R.id.et_title);
        etDesc = findViewById(R.id.et_description);
        btnSubmit = findViewById(R.id.btn_submit);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        note = new Note();
        btnSubmit.setOnClickListener(this);

        recyclerView = findViewById(R.id.rv_note);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteList = new ArrayList<>();

        noteAdapter = new NoteAdapter(noteList, this, this);
        recyclerView.setAdapter(noteAdapter);

        retrieveNotesFromFirebase();

    }
    private void deleteData(Note note) {
        String userId = mAuth.getCurrentUser().getUid();
        String noteId = note.getId();
        System.out.println(noteId);
        System.out.println(userId);
        databaseReference.child("notes").child(userId).child(noteId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        noteList.remove(note);
                        noteAdapter.notifyDataSetChanged(); // Notify adapter about the change
                        Toast.makeText(InsertNoteActivity.this, "Data dihapus", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InsertNoteActivity.this, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void retrieveNotesFromFirebase() {
        DatabaseReference notesRef = databaseReference.child("notes").child(mAuth.getUid());

        notesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Note note = snapshot.getValue(Note.class);
                noteList.add(note);
                noteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Note updatedNote = snapshot.getValue(Note.class);
                for (int i = 0; i < noteList.size(); i++) {
                    if (Objects.equals(noteList.get(i).getId(), updatedNote.getId())) {
                        noteList.set(i, updatedNote);
                        break;
                    }
                }
                noteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String removedNoteId = snapshot.getKey();
                for (int i = 0; i < noteList.size(); i++) {
                    if (noteList.get(i).getId().equals(removedNoteId)) {
                        noteList.remove(i);
                        noteAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void logOut(){
        mAuth.signOut();
        Intent intent = new Intent(InsertNoteActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//makesure user cant go back
        startActivity(intent);
        finish();
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            tvEmail.setText(currentUser.getEmail());
            tvUid.setText(currentUser.getUid());
        }
    }
    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(etTitle.getText().toString())) {
            etTitle.setError("Required");
            result = false;
        } else {
            etTitle.setError(null);
        }
        if (TextUtils.isEmpty(etDesc.getText().toString())) {
            etDesc.setError("Required");
            result = false;
        } else {
            etDesc.setError(null);
        }
        return result;
    }
    public void submitData() {
        if (!validateForm()) {
            return;
        }

        String title = etTitle.getText().toString();
        String desc = etDesc.getText().toString();

        String id = databaseReference.child("notes").child(mAuth.getUid()).push().getKey();
        Note baru = new Note(id, title, desc);

        databaseReference.child("notes").child(mAuth.getUid()).child(id).setValue(baru)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(InsertNoteActivity.this, "Add data", Toast.LENGTH_SHORT).show();
                        etTitle.setText("");
                        etDesc.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InsertNoteActivity.this, "Failed to Add data", Toast.LENGTH_SHORT).show();
                        e.printStackTrace(); // Print the error details
                    }
                });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_keluar:
                logOut();
                break;
            case R.id.btn_submit:
                submitData();
                break;
        }
    }
    @Override
    public void onDelete(Note note) {
        deleteData(note);
    }

    @Override
    public void onUpdate(Note note){
        Intent intent = new Intent(InsertNoteActivity.this, UpdateNoteActivity.class);
        intent.putExtra("note_id", note.getId());
        intent.putExtra("note_title", note.getTitle());
        intent.putExtra("note_desc", note.getDescription());
        startActivity(intent);
    }
}