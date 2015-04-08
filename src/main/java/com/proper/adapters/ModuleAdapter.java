package com.proper.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.proper.data.binmove.Module;
import com.proper.warehousetools.R;

import java.util.List;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ModuleAdapter extends BaseAdapter {
    private List<Module> listItems;
    private Context context;
    private LayoutInflater listContainer;
    private int itemViewResource;

    static class ViewHolder {
        public TextView tvTitle;
        public ImageView ivIcon;
    }

    public ModuleAdapter(Context context, List<Module> modules, int resource) {
        this.context = context;
        this.listContainer = LayoutInflater.from(context);
        listItems = modules;
        this.itemViewResource = resource;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Module getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if (convertView == null) {
            convertView = listContainer.inflate(this.itemViewResource, null);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivModIcon);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvModTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Module module = listItems.get(position);
        holder.ivIcon.setImageResource(module.getIcon());
        holder.ivIcon.setTag("false");
        holder.tvTitle.setText(module.getName());

        return convertView;
    }

}