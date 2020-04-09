package com.example.foodapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterHolder> {
    public ArrayList<String> items;
    public boolean isOrdered;

    public ItemAdapter(ArrayList<String> items, boolean isOrdered){
        this.items = items;
        this.isOrdered = isOrdered;
    }

    public static class ItemAdapterHolder extends RecyclerView.ViewHolder{
        public TextView item;
        public TextView bulletPoint;

        public ItemAdapterHolder(View v){
            super(v);
            item = v.findViewById(R.id.listItem);
            bulletPoint = v.findViewById(R.id.bulletPoint);

        }
    }

    @Override
    public ItemAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ItemAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemAdapterHolder viewHolder, int position){
        viewHolder.item.setText(items.get(position));
        if(isOrdered){
            viewHolder.bulletPoint.setText(Integer.toString(position + 1) );
        }
    }

    @Override
    public int getItemCount(){return items.size();}


}
