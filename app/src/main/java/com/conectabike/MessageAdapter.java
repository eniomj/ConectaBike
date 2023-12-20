package com.conectabike;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> messageList;

    public MessageAdapter(Context context, ArrayList<ArrayList<String>> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage, date;
        String userID;
        ImageView profilePicture;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.dateMessage);
            userMessage = itemView.findViewById(R.id.userMessage);
            profilePicture = itemView.findViewById(R.id.profilepicture);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), UserProfile.class);
                    intent.putExtra("userID", userID);
                    itemView.getContext().startActivity(intent);
                }
            });

        }
    }

    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new MessageAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MyViewHolder holder, int position) {
        ArrayList<String> sublistMessage = messageList.get(position);

        holder.date.setText(sublistMessage.get(1));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
        database.orderByChild("email").equalTo(sublistMessage.get(2)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    holder.userID = dataSnapshot.getKey();

                    String profilePictureUri = dataSnapshot.child("profilePictureUri").getValue(String.class);

                    Glide.with(holder.itemView.getContext())
                            .load(profilePictureUri)
                            .into(holder.profilePicture);

                    String username = dataSnapshot.child("username").getValue(String.class);
                    String message = sublistMessage.get(0);

                    SpannableStringBuilder builder = new SpannableStringBuilder();

                    builder.append(username);
                    builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, username.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.append(" ");
                    builder.append(message);

                    holder.userMessage.setText(builder);
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
