package com.conectabike;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EducationAdapter extends RecyclerView.Adapter<EducationAdapter.ViewHolder> {
    private String[] reasonTitles;
    private String[] reasonExplanations;
    private String[] reasonSources;

    public EducationAdapter(String[] reasonTitles, String[] reasonExplanations, String[] reasonSources) {
        this.reasonTitles = reasonTitles;
        this.reasonExplanations = reasonExplanations;
        this.reasonSources = reasonSources;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.education_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.titleTextView.setText(reasonTitles[position]);
        holder.explanationTextView.setText(reasonExplanations[position]);
        holder.sourceTextView.setText(reasonSources[position]);
    }

    @Override
    public int getItemCount() {
        return reasonTitles.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView explanationTextView;
        public TextView sourceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            explanationTextView = itemView.findViewById(R.id.explanationTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
        }
    }
}
