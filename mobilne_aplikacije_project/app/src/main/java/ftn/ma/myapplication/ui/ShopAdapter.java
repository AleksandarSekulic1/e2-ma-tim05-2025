package ftn.ma.myapplication.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import ftn.ma.myapplication.R;

/**
 * Adapter za prikaz stavki u prodavnici
 */
public class ShopAdapter extends BaseAdapter {
    
    private final Context context;
    private final List<ShopActivity.ShopItem> items;
    private final LayoutInflater inflater;
    
    public ShopAdapter(Context context, List<ShopActivity.ShopItem> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        return items.size();
    }
    
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_shop, parent, false);
            holder = new ViewHolder();
            holder.itemName = convertView.findViewById(R.id.itemName);
            holder.itemDescription = convertView.findViewById(R.id.itemDescription);
            holder.itemCost = convertView.findViewById(R.id.itemCost);
            holder.buyButton = convertView.findViewById(R.id.buyButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        ShopActivity.ShopItem item = items.get(position);
        
        holder.itemName.setText(item.getName());
        holder.itemDescription.setText(item.getDescription());
        holder.itemCost.setText("Cena: " + item.getCost() + " novčića");
        
        holder.buyButton.setOnClickListener(v -> {
            if (item.getPurchaseAction() != null) {
                item.getPurchaseAction().run();
            }
        });
        
        return convertView;
    }
    
    private static class ViewHolder {
        TextView itemName;
        TextView itemDescription;
        TextView itemCost;
        Button buyButton;
    }
}