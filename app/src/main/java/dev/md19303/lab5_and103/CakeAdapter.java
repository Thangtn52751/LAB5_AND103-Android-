package dev.md19303.lab5_and103;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CakeAdapter extends RecyclerView.Adapter<CakeAdapter.ViewHolder> {
    private Context context;
    private List<Cake> cakeList;
    private OnCakeInteractionListener listener;

    public CakeAdapter(List<Cake> cakeList, Context context, OnCakeInteractionListener listener) {
        this.cakeList = cakeList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CakeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cake, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CakeAdapter.ViewHolder holder, int position) {
        Cake cake = cakeList.get(position);

        holder.tv_cakeName.setText(cake.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditCake(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDeleteCake(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return cakeList != null ? cakeList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_cakeName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_cakeName = itemView.findViewById(R.id.tv_cakeName);
        }
    }

    public interface OnCakeInteractionListener {
        void onEditCake(int position);

        void onDeleteCake(int position);
    }

    // Update the adapter data dynamically
    public void updateList(List<Cake> newList) {
        cakeList.clear();
        cakeList.addAll(newList);
        notifyDataSetChanged();
    }
}
