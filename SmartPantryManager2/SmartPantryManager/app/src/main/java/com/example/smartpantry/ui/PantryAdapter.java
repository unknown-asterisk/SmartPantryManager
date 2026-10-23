package com.example.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.QuantityFormat;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/** Custom RecyclerView adapter that shows the pantry rows loaded from SQLite. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** Callbacks so the Activity (not the adapter) decides what edit/delete do. */
    public interface Listener {
        void onEdit(Ingredient ingredient);
        void onDelete(Ingredient ingredient);
    }

    private final List<Ingredient> items = new ArrayList<>();
    private final Listener listener;
    private int alertWindowDays = -1; // -1 = expiring-soon highlighting disabled

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Ingredient> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /** Pass the alert window in days, or -1 to switch the highlighting off. */
    public void setAlertWindowDays(int days) {
        this.alertWindowDays = days;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final Ingredient item = items.get(position);
        h.name.setText(item.getName());
        h.quantity.setText(QuantityFormat.format(item.getQuantity()) + " " + item.getUnit());
        bindExpiry(h.expiry, item);

        h.edit.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
        h.itemView.setOnClickListener(v -> listener.onEdit(item));
    }

    private void bindExpiry(TextView tv, Ingredient item) {
        Long days = item.daysUntilExpiry();
        int grey = tv.getContext().getColor(R.color.grey_600);
        int orange = tv.getContext().getColor(R.color.orange_700);
        int red = tv.getContext().getColor(R.color.red_700);

        if (days == null) {
            tv.setText("No expiry date");
            tv.setTextColor(grey);
        } else if (days < 0) {
            tv.setText("Expired " + (-days) + (days == -1 ? " day ago" : " days ago"));
            tv.setTextColor(red);
        } else if (alertWindowDays >= 0 && days <= alertWindowDays) {
            tv.setText(days == 0 ? "Expires today" : "Expires in " + days + (days == 1 ? " day" : " days"));
            tv.setTextColor(orange);
        } else {
            tv.setText("Expires " + DateUtils.toDisplay(item.getExpiryDate()));
            tv.setTextColor(grey);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, quantity, expiry;
        final ImageButton edit, delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            quantity = itemView.findViewById(R.id.tv_quantity);
            expiry = itemView.findViewById(R.id.tv_expiry);
            edit = itemView.findViewById(R.id.btn_edit);
            delete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
