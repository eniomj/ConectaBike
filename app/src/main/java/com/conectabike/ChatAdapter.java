package com.conectabike;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private final ArrayList<ChatModel> chatList;

    public ChatAdapter(ArrayList<ChatModel> chatList) {
        this.chatList = chatList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView message, date, username;
        String userID;
        ImageView profilePicture;
        RelativeLayout messageItem;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.dateMessage);
            message = itemView.findViewById(R.id.userMessage);
            username = itemView.findViewById(R.id.username);
            profilePicture = itemView.findViewById(R.id.profilepicture);
            messageItem = itemView.findViewById(R.id.message_item_id);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), UserProfile.class);
                intent.putExtra("userID", userID);
                itemView.getContext().startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new ChatAdapter.MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.MyViewHolder holder, int position) {
        ChatModel chatModel = chatList.get(position);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
        database.orderByChild("email").equalTo(chatModel.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // foto de usuário
                    String profilePictureUri = dataSnapshot.child("profilePictureUri").getValue(String.class);
                    Glide.with(holder.itemView.getContext())
                            .load(profilePictureUri)
                            .into(holder.profilePicture);
                    // nome de usuário
                    String username = dataSnapshot.child("username").getValue(String.class);
                    holder.username.setText(username);
                    // data
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
                    try {
                        Date date = dateFormat.parse(chatModel.getDate());
                        assert date != null;
                        long timeDifference = System.currentTimeMillis() - date.getTime();
                        String relativeTime = getRelativeTime(timeDifference);
                        holder.date.setText(relativeTime);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // mensagem
                    holder.message.setText(chatModel.getMessage());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("logdebug", "db error: " + error);
            }
        });
    }

    private String getRelativeTime(long timeDifference) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifference);
        long days = TimeUnit.MILLISECONDS.toDays(timeDifference);

        if (seconds < 60) {
            return (seconds == 1) ? "1 segundo atrás" : seconds + " segundos atrás";
        } else if (minutes < 60) {
            return (minutes == 1) ? "1 minuto atrás" : minutes + " minutos atrás";
        } else if (hours < 24) {
            return (hours == 1) ? "1 hora atrás" : hours + " horas atrás";
        } else {
            return (days == 1) ? "1 dia atrás" : days + " dias atrás";
        }
    }
    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
