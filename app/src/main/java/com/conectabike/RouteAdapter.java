package com.conectabike;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.MyViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> messageList;

    public RouteAdapter(Context context, ArrayList<ArrayList<String>> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage, date, username;
        String userID;
        ImageView profilePicture;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.dateMessage);
            userMessage = itemView.findViewById(R.id.userMessage);
            username = itemView.findViewById(R.id.username);
            profilePicture = itemView.findViewById(R.id.profilepicture);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), UserProfile.class);
                intent.putExtra("userID", userID);
                itemView.getContext().startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public RouteAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new RouteAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteAdapter.MyViewHolder holder, int position) {
        ArrayList<String> sublistMessage = messageList.get(position);

        holder.date.setText(sublistMessage.get(1));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
        database.orderByChild("email").equalTo(sublistMessage.get(2)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    holder.userID = dataSnapshot.getKey();
                    // foto de perfil
                    String profilePictureUri = dataSnapshot.child("profilePictureUri").getValue(String.class);
                    Glide.with(holder.itemView.getContext())
                            .load(profilePictureUri)
                            .into(holder.profilePicture);
                    // nome de usuário
                    String username = dataSnapshot.child("username").getValue(String.class);
                    holder.username.setText(username);
                    // mensagem
                    String message = sublistMessage.get(0);
                    holder.userMessage.setText(message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("logdebug", "db error: " + error);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (messageList != null) {
            return messageList.size();
        } else {
            return 0;
        }
    }
}
