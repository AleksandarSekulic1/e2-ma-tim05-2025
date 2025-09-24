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
 * Adapter za prikaz opreme u Equipment aktivnosti
 */
public class EquipmentAdapter extends BaseAdapter {
    
    private final Context context;
    private final List<EquipmentActivity.EquipmentItem> items;
    private final LayoutInflater inflater;
    
    public EquipmentAdapter(Context context, List<EquipmentActivity.EquipmentItem> items) {
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
            convertView = inflater.inflate(R.layout.item_equipment, parent, false);
            holder = new ViewHolder();
            holder.itemName = convertView.findViewById(R.id.equipmentName);
            holder.itemDescription = convertView.findViewById(R.id.equipmentDescription);
            holder.itemStatus = convertView.findViewById(R.id.equipmentStatus);
            holder.toggleButton = convertView.findViewById(R.id.toggleButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        EquipmentActivity.EquipmentItem item = items.get(position);
        
        holder.itemName.setText(item.getName());
        holder.itemDescription.setText(item.getDescription());
        holder.itemStatus.setText("Status: " + item.getStatus());
        
        // Postavi dugme na osnovu stanja
        if (item.isActive()) {
            holder.toggleButton.setText("DEAKTIVIRAJ");
            holder.toggleButton.setBackgroundResource(R.drawable.button_secondary);
        } else {
            holder.toggleButton.setText("AKTIVIRAJ");
            holder.toggleButton.setBackgroundResource(R.drawable.button_primary);
        }
        
        holder.toggleButton.setOnClickListener(v -> {
            if (item.getToggleAction() != null) {
                item.getToggleAction().run();
            }
        });
        
        return convertView;
    }
    
    private static class ViewHolder {
        TextView itemName;
        TextView itemDescription;
        TextView itemStatus;
        Button toggleButton;
    }
}