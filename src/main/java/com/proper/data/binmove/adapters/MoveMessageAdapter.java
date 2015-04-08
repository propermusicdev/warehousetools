package com.proper.data.binmove.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.proper.data.binmove.BinMoveMessage;
import com.proper.warehousetools.R;

import java.util.List;

/**
 * Created by Lebel on 02/06/2014.
 */
public class MoveMessageAdapter extends BaseAdapter {
    private Context context;
    private List<BinMoveMessage> theseMsg;

    public MoveMessageAdapter(Context context, List<BinMoveMessage> messages) {
        this.context = context;
        this.theseMsg = messages;
    }

    @Override
    public int getCount() {
        return theseMsg.size();
    }

    @Override
    public BinMoveMessage getItem(int i) {
        return theseMsg.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        View myView = view;
        ViewHolder holder = new ViewHolder();
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.list_binmove_binmessage_item, viewGroup, false);
            holder.lblMessageName = (TextView) myView.findViewById(R.id.lblMessageName);
            holder.txtMessageName = (TextView) myView.findViewById(R.id.txtvMessageName);
            holder.lblMessageText = (TextView) myView.findViewById(R.id.lblMessageText);
            holder.txtMessageText = (TextView) myView.findViewById(R.id.txtvMessageText);
            holder.lblMessageTimestamp = (TextView) myView.findViewById(R.id.lblMessageTimestamp);
            holder.txtMessageTimestamp = (TextView) myView.findViewById(R.id.txtvMessageTimeStamp);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        BinMoveMessage msg = theseMsg.get(pos);
        //assign control values and display
        holder.lblMessageName.setText("Name:    ");
        holder.lblMessageText.setText("Text:    ");
        holder.lblMessageTimestamp.setText("TimeStamp:     ");
        holder.txtMessageName.setText(msg.getMessageName());
        holder.txtMessageText.setText(msg.getMessageText());
        holder.txtMessageTimestamp.setText(String.format("%s", msg.getMessageTimeStamp()));
        return myView;
    }
    static class ViewHolder {
        TextView lblMessageName;
        TextView txtMessageName;
        TextView lblMessageText;
        TextView txtMessageText;
        TextView lblMessageTimestamp;
        TextView txtMessageTimestamp;
    }
}
/*
"MessageName":"Warning",
"MessageText":"Destination may be over capacity for product: PULSES (KARMIN)",
"MessageTimeStamp":"2014-05-29 16:24:12"*/
