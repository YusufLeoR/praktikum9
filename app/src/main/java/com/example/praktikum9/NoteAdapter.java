package com.example.praktikum9;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private OnNoteDeleteListener deleteListener;
    private OnNoteUpdateListener updateListener;


    public NoteAdapter(List<Note> noteList, OnNoteDeleteListener deleteListener, OnNoteUpdateListener updateListener) {
        this.noteList = noteList;
        this.deleteListener = deleteListener;
        this.updateListener = updateListener;
    }
    public interface OnNoteDeleteListener {
        void onDelete(Note note);
    }

    public interface OnNoteUpdateListener {
        void onUpdate(Note note);
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDesc;
        ImageView ivDelete;
        ImageView ivUpdate;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_titlerow);
            tvDesc = itemView.findViewById(R.id.tv_descrow);
            ivDelete = itemView.findViewById(R.id.iv_deletedata);
            ivUpdate = itemView.findViewById(R.id.iv_updatedata);
        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvDesc.setText(note.getDescription());
        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteListener.onDelete(note);
            }
        });
        holder.ivUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateListener.onUpdate(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }
}