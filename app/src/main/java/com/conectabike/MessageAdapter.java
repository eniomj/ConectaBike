package com.conectabike;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    Context context;
    ArrayList<ArrayList<String>> messageList;

    public MessageAdapter(Context context, ArrayList<ArrayList<String>> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView user, date, message;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.dateMessage);
            user = itemView.findViewById(R.id.userMessage);
            message = itemView.findViewById(R.id.message);

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

        holder.message.setText(sublistMessage.get(0));
        holder.date.setText(sublistMessage.get(1));
        holder.user.setText(sublistMessage.get(2));

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
