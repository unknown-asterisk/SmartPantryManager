package com.example.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.model.MatchResult;
import com.example.smartpantry.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/** Custom adapter for the recipe lists ("Cook now" and "Almost there"). */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<MatchResult> items = new ArrayList<>();
    private final Listener listener;
    private boolean almostMode = false;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    /** @param almostMode true = show the "missing ingredient" line instead of the "all in pantry" line */
    public void setItems(List<MatchResult> newItems, boolean almostMode) {
        this.almostMode = almostMode;
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final MatchResult result = items.get(position);
        final Recipe recipe = result.getRecipe();
        int count = recipe.getIngredients().size();

        h.name.setText(recipe.getName());
        h.info.setText(count + (count == 1 ? " ingredient" : " ingredients")
                + (almostMode ? "" : " - all in your pantry"));

        if (almostMode && result.getMissingCount() == 1) {
            MatchResult.IngredientCheck missing = result.getMissing().get(0);
            h.extra.setText("Missing: " + missing.getIngredient().toDisplayString()
                    + " (" + missing.getNote() + ")");
            h.extra.setTextColor(h.itemView.getContext().getColor(R.color.orange_700));
            h.extra.setVisibility(View.VISIBLE);
        } else if (!almostMode && result.getExpiringIngredientCount() > 0) {
            int n = result.getExpiringIngredientCount();
            h.extra.setText("Uses " + n + " ingredient" + (n == 1 ? "" : "s") + " that expire soon");
            h.extra.setTextColor(h.itemView.getContext().getColor(R.color.green_700));
            h.extra.setVisibility(View.VISIBLE);
        } else {
            h.extra.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, info, extra;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_recipe_name);
            info = itemView.findViewById(R.id.tv_recipe_info);
            extra = itemView.findViewById(R.id.tv_recipe_extra);
        }
    }
}
