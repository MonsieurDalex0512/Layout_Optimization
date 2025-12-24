package com.example.layout_optimization.adapters;

import android.content.Context;
import android.os.Trace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.layout_optimization.R;
import com.example.layout_optimization.models.Item;

import java.util.List;

public class UnoptimizedAdapter extends RecyclerView.Adapter<UnoptimizedAdapter.ViewHolder> {
    private List<Item> items;
    private Context context;
    
    public UnoptimizedAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Add trace marker
        Trace.beginSection("inflate_unoptimized");
        
        View view = LayoutInflater.from(context)
            .inflate(R.layout.item_unoptimized, parent, false);
        
        Trace.endSection();
        
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trace.beginSection("bind_unoptimized");
        
        Item item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        holder.icon.setImageResource(item.getIconRes());
        holder.timestamp.setText(item.getTimestamp());
        
        Trace.endSection();
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, description, timestamp;
        
        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
