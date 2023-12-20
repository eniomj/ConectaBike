package com.conectabike;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<ChatModel> chatList;

    public ChatAdapter(Context context, ArrayList<ChatModel> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage, date;
        String userID;
        ImageView profilePicture;
        RelativeLayout messageItem;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.dateMessage);
            userMessage = itemView.findViewById(R.id.userMessage);
            profilePicture = itemView.findViewById(R.id.profilepicture);
            messageItem = itemView.findViewById(R.id.message_item_id);

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
    public ChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new ChatAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.MyViewHolder holder, int position) {
        ChatModel chatModel = chatList.get(position);

        holder.userMessage.setText(chatModel.getMessage());
        holder.date.setText(chatModel.getDate());

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
        database.orderByChild("email").equalTo(chatModel.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    holder.userID = dataSnapshot.getKey();

                    boolean isCurrentUser = chatModel.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                    String profilePictureUri = dataSnapshot.child("profilePictureUri").getValue(String.class);

                    Glide.with(holder.itemView.getContext())
                            .load(profilePictureUri)
                            .into(holder.profilePicture);

                    String username = dataSnapshot.child("username").getValue(String.class);
                    String message = chatModel.getMessage();

                    SpannableStringBuilder builder = new SpannableStringBuilder();

                    if (isCurrentUser) {
                        // Aplica um estilo diferente se a mensagem for enviada pelo usuário que está logado
                        builder.append(username);
                        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, username.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
                        builder.append(" ");
                        builder.append(message);

                        holder.messageItem.setGravity(Gravity.END);
                        holder.userMessage.setWidth(600);

                    } else {
                        builder.append(username);
                        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, username.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
                        builder.append(" ");
                        builder.append(message);
                    }

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
        return chatList.size();
    }
}
