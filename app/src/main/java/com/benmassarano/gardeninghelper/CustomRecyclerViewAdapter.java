package com.benmassarano.gardeninghelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Plant> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    protected CustomRecyclerViewAdapter(Context context, ArrayList<Plant> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    protected void setClickListener(ItemClickListener listener) {
        mClickListener = listener;
    }

    @NonNull
    @Override
    // inflates the cell layout from xml when needed
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // binds the data to the TextView in each cell
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plant curr = mData.get(position);

        int daysRemaining = curr.getRemainingDays();
        int daysUntilWatering = curr.getDaysUntilWatering();
        Bitmap bitmap = curr.getImage();
        String name = curr.getName();
        holder.daysRemaining.setText(String.valueOf(daysRemaining));
        holder.daysUntilWatering.setText("(" + daysUntilWatering + ")");
        holder.image.setImageBitmap(bitmap);
        holder.name.setText(name);

        if (curr.getRemainingDays() < 3) {
            //red
            holder.daysRemaining.setTextColor(0xFFF13333);
        }
        else {
            //white
            holder.daysRemaining.setTextColor(0xFFFFFFFF);

        }
    }

    @Override
    // total number of cells
    public int getItemCount() {
        return mData.size();
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);

        void onLongItemClick(View view, int position);
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView name;
        ImageView image;
        TextView daysRemaining;
        TextView daysUntilWatering;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            image = itemView.findViewById(R.id.ivFlowerImage);
            daysRemaining = itemView.findViewById(R.id.tvDaysRemaining);
            daysUntilWatering = itemView.findViewById(R.id.tvDaysUntilWatering);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getBindingAdapterPosition());
            }

        }

        @Override
        public boolean onLongClick(View view) {
            if (mClickListener != null) {
                mClickListener.onLongItemClick(view, getBindingAdapterPosition());
            }
            return true;
        }
    }

}
