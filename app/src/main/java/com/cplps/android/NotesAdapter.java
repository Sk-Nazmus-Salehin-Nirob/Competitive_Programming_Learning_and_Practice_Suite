package com.cplps.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cplps.android.models.Note;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private Context context;
    private List<Note> notes;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
    private OnNoteLongClickListener listener;

    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note);
    }

    public NotesAdapter(Context context, OnNoteLongClickListener listener) {
        this.context = context;
        this.notes = new ArrayList<>();
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        TextView tvTime;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_note_content);
            tvTime = itemView.findViewById(R.id.tv_note_time);
        }

        public void bind(Note note) {
            tvContent.setText(note.getContent());
            tvTime.setText(dateFormat.format(new Date(note.getCreatedAt())));

            itemView.setOnLongClickListener(v -> {
                if (listener != null)
                    listener.onNoteLongClick(note);
                return true;
            });
        }
    }
}
