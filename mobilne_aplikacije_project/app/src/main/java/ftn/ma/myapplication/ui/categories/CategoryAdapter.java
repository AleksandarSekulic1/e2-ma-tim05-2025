package ftn.ma.myapplication.ui.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryListener onCategoryListener;

    // --- IZMENA: Interfejs sada podržava i običan i dugi klik ---
    public interface OnCategoryListener {
        void onCategoryClick(Category category);
        void onCategoryLongClick(Category category); // DODATO
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryListener onCategoryListener) {
        this.categoryList = categoryList;
        this.onCategoryListener = onCategoryListener;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public View categoryColorView;
        public TextView categoryNameTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryColorView = itemView.findViewById(R.id.viewCategoryColor);
            categoryNameTextView = itemView.findViewById(R.id.textViewCategoryName);
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category currentCategory = categoryList.get(position);

        holder.categoryNameTextView.setText(currentCategory.getName());
        holder.categoryColorView.setBackgroundColor(currentCategory.getColor());

        // Listener za običan klik (ostaje isti)
        holder.itemView.setOnClickListener(v -> {
            if (onCategoryListener != null) {
                onCategoryListener.onCategoryClick(currentCategory);
            }
        });

        // --- NOVO: Postavljamo listener za dugi klik ---
        holder.itemView.setOnLongClickListener(v -> {
            if (onCategoryListener != null) {
                onCategoryListener.onCategoryLongClick(currentCategory);
            }
            return true; // Vraćamo true da označimo da je događaj obrađen
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
