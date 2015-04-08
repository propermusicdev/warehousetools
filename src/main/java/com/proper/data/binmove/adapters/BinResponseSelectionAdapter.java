package com.proper.data.binmove.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.proper.data.binmove.ProductBinSelection;
import com.proper.warehousetools.R;
import com.proper.data.core.IOnItemSelectionChanged;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Lebel on 18/06/2014.
 */
public class BinResponseSelectionAdapter extends BaseAdapter implements IOnItemSelectionChanged {
    private Context context;
    private List<ProductBinSelection> thisSelection = new ArrayList<ProductBinSelection>();
    private View thisView;
    private int rowTotalQty = 0;
    private int totalMoveCollection = 0;

    public BinResponseSelectionAdapter(Context context, List<ProductBinSelection> thisResponse) {
        this.context = context;
        this.thisSelection = thisResponse;
    }

    public List<ProductBinSelection> getThisSelection() {
        return thisSelection;
    }

    @Override
    public int getCount() {
        return this.thisSelection.size();
    }

    @Override
    public ProductBinSelection getItem(int i) {
        return this.thisSelection.get(i);
    }

    @Override
    public long getItemId(int i) {
        return this.thisSelection.get(i).getProductId();
    }

    public int getTotalMoveCollection() {
        int value = 0;
        for (ProductBinSelection sel : this.getThisSelection()) {
            value = value + sel.getQtyToMove();
        }
        return value;
    }

    public void setTotalMoveCollection(int totalMoveCollection) {
        this.totalMoveCollection = totalMoveCollection;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int currentPos = i;
        thisView = view;
        ViewHolder holder = new ViewHolder();
        if (thisView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            thisView = inflater.inflate(R.layout.list_binmove_binselection_item, viewGroup, false);

            //holder = new ViewHolder();
            holder.binQty = (TextView) thisView.findViewById(R.id.txtSelectionBinQty);
            holder.suppCat = (TextView) thisView.findViewById(R.id.txtSelectionSuppCat);
            holder.moveQty = (TextView) thisView.findViewById(R.id.txtSelectionMoveQty);
            holder.plus = (ImageButton) thisView.findViewById(R.id.bnSelectionPlus);
            holder.minus = (ImageButton) thisView.findViewById(R.id.bnSelectionMinus);
            holder.position = i;
            thisView.setTag(holder);
        } else  {
            holder = (ViewHolder) thisView.getTag();
        }
        holder.binQty.addTextChangedListener(new TextChanged(holder.binQty));
        holder.moveQty.addTextChangedListener(new TextChanged(holder.moveQty));
        holder.plus.setFocusable(false);
        holder.plus.setTag(currentPos);
        holder.minus.setFocusable(false);
        holder.minus.setTag(currentPos);
        holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        holder.plus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //return false;
                ButtonLongClicked(view);
                return  false;
            }
        });
        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        holder.minus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //return false;
                ButtonLongClicked(view);
                return false;
            }
        });

        ProductBinSelection selection = thisSelection.get(i);
        this.setTotalMoveCollection(getThisSelection().iterator().next().getQtyToMove());
        //populate views
        holder.binQty.setText(String.format("%s", selection.getQtyInBin()));
        holder.suppCat.setText(selection.getSupplierCat());
        holder.moveQty.setText(String.format("%s", selection.getQtyToMove()));

        if (holder.binQty.isEnabled()) holder.binQty.setEnabled(false);

        if (selection.getQtyInBin() > 0 && selection.getQtyToMove() > 0) {
            if (!holder.minus.isEnabled()) holder.minus.setEnabled(true);
            if (!holder.plus.isEnabled()) holder.plus.setEnabled(true);
            holder.binQty.setTextColor(Color.parseColor("#c6c6c6"));
            holder.moveQty.setTextColor(Color.parseColor("#000000"));
        }
        if (selection.getQtyInBin() == 0 && selection.getQtyToMove() > 0) {
            if (holder.moveQty.isEnabled()) holder.moveQty.setEnabled(false);
            if (!holder.minus.isEnabled()) holder.minus.setEnabled(true);
            if (holder.plus.isEnabled()) holder.plus.setEnabled(false);
            holder.binQty.setTextColor(Color.parseColor("#ff1700"));
            holder.moveQty.setTextColor(Color.parseColor("#000000"));
        }
        if (selection.getQtyInBin() > 0 && selection.getQtyToMove() == 0) {
            //if (holder.minus.isEnabled()) holder.minus.setEnabled(false);
            holder.minus.setEnabled(false);
            if (!holder.plus.isEnabled()) holder.plus.setEnabled(true);
            holder.moveQty.setTextColor(Color.parseColor("#ff1700"));
            holder.binQty.setTextColor(Color.parseColor("#000000"));
        }
        formatRow(currentPos);
        return thisView;
    }

    private void formatRow(int position) {
        ProductBinSelection iSelection = thisSelection.get(position);
        if (iSelection.getQtyToMove() == 0) {
            thisView.setTag(R.integer.TURN_END, false);
        } else {
            thisView.setTag(R.integer.TURN_END, true);
        }
        //Finally Notify Change
        notifyDataSetChanged();
    }

    private void ButtonClicked(View view) {
        int pos = (Integer) view.getTag();
        ProductBinSelection iSelection = thisSelection.get(pos);
        rowTotalQty = iSelection.getQtyInBin() + iSelection.getQtyToMove();
        switch (view.getId()) {
            case R.id.bnSelectionMinus:
                if (rowTotalQty != 0) {
                    if (iSelection.getQtyInBin() != rowTotalQty && iSelection.getQtyToMove() > 0) {
                        iSelection.incrementBin();
                        thisSelection.remove(pos);
                        thisSelection.add(pos, iSelection);
                    }
                }
                break;
            case R.id.bnSelectionPlus:
                if (rowTotalQty != 0) {
                    if (iSelection.getQtyInBin() > 0) {
                        iSelection.incrementMove();
                        thisSelection.remove(pos);
                        thisSelection.add(pos, iSelection);
                    }
                }
                break;
        }
        formatRow(pos);
    }

    private void ButtonLongClicked(View view) {
        int pos = (Integer) view.getTag();
        ProductBinSelection iSelection = thisSelection.get(pos);
        switch (view.getId()) {
            case R.id.bnSelectionMinus:
//                if (rowTotalQty != 0) {
//                    if (iSelection.getQtyToMove() != rowTotalQty && iSelection.getQtyToMove() >= 10) {
//                        iSelection.take10FromMove();
//                        thisSelection.remove(pos);
//                        thisSelection.add(pos, iSelection);
//                    }
//                }
                if (rowTotalQty != 0) {
                    if (iSelection.getQtyToMove() != rowTotalQty && iSelection.getQtyToMove() >= 0) {
                        iSelection.purgeMove();
                        thisSelection.remove(pos);
                        thisSelection.add(pos, iSelection);
                    }
                }
                break;
            case R.id.bnSelectionPlus:
                if (rowTotalQty != 0) {
                    if (iSelection.getQtyInBin() >= 0) {
                        iSelection.purgeBin();
                        thisSelection.remove(pos);
                        thisSelection.add(pos, iSelection);
                    }
                }
                break;
        }
        formatRow(pos);
    }

    @Override
    public void onItemSelectionChanged(int itemIndex) {
        //currentPos = itemIndex;
    }

    private class TextChanged implements TextWatcher {
        private View view = null;
        private TextChanged(View v) {
            this.view = v;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            int value = Integer.parseInt(editable.toString());
        }
    }

    public void modifyEntry(int index, int newBinQty, int newMoveQty) {
        this.thisSelection.get(index).changeMoveTo(newMoveQty);
    }

    public void remove(int index) {
        this.thisSelection.remove(index);
        this.notifyDataSetChanged();
    }

    public void removeAllExcept(int index) {
        ProductBinSelection excluded = this.getThisSelection().get(index);
        this.thisSelection = new ArrayList<ProductBinSelection>();
        this.thisSelection.add(excluded);
        this.notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView binQty;
        TextView suppCat;
        TextView moveQty;
        ImageButton plus;
        ImageButton minus;
        int position;
    }
}
