package com.proper.data.goodsin.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.proper.data.goodsin.GoodsInThumbnail;
import com.proper.warehousetools.R;

import java.io.File;
import java.util.List;

/**
 * Created by Lebel on 31/10/2014.
 */
public class GalleryGridAdapter extends BaseAdapter {
    private Context context;
    private List<GoodsInThumbnail> thumbs;

    public GalleryGridAdapter(Context context, List<GoodsInThumbnail> thumbNails) {
        this.context = context;
        this.thumbs = thumbNails;
    }

    @Override
    public int getCount() {
        return thumbs.size();
    }

    @Override
    public GoodsInThumbnail getItem(int position) {
        return thumbs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ViewHolder holder = new ViewHolder();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.gallery_gridrow, parent, false);

            holder.imageTitle = (TextView) view.findViewById(R.id.galleryRowText);
            holder.image = (ImageView) view.findViewById(R.id.galleryRowImage);
            view.setTag(holder);
        } else  {
            holder = (ViewHolder) view.getTag();
        }

        holder.fileName = this.thumbs.get(position).getFileName();
        holder.imageTitle.setText(String.format("%s", this.thumbs.get(position).getURL()));
        if (thumbs.get(position).getThumbNail() != null) {
            holder.image.setImageBitmap(thumbs.get(position).getThumbNail());
        }else{
            Uri uri = Uri.fromFile(new File(this.thumbs.get(position).getURL()));
            holder.image.setImageBitmap(BitmapFactory.decodeFile(uri.getPath()));
        }
        holder.image.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.position = position;

        return view;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
        String fileName;
        int position;
    }
}
