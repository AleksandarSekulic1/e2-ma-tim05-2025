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

    // Konstruktor adaptera, prima listu podataka koju treba prikazati
    public CategoryAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    // ViewHolder klasa - ona čuva reference na UI komponente jednog reda (item_category.xml)
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public View categoryColorView;
        public TextView categoryNameTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryColorView = itemView.findViewById(R.id.viewCategoryColor);
            categoryNameTextView = itemView.findViewById(R.id.textViewCategoryName);
        }
    }

    // Ova metoda se poziva kada RecyclerView treba da kreira novi ViewHolder (novi red)
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Kreiramo novi View tako što "naduvavamo" (inflate) naš layout za jedan red
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    // Ova metoda povezuje podatke iz liste sa UI komponentama u ViewHolder-u
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // Uzimamo kategoriju sa određene pozicije u listi
        Category currentCategory = categoryList.get(position);

        // Postavljamo podatke u UI komponente
        holder.categoryNameTextView.setText(currentCategory.getName());
        holder.categoryColorView.setBackgroundColor(currentCategory.getColor());
    }

    // Ova metoda vraća ukupan broj elemenata u listi
    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
