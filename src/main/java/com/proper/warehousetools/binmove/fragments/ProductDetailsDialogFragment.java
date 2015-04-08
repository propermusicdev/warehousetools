package com.proper.warehousetools.binmove.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.proper.data.binmove.ProductBinResponse;
import com.proper.data.core.ICommunicator;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.ui.ActBinItemSelection;

/**
 * Created by Lebel on 04/08/2014.
 */
public class ProductDetailsDialogFragment extends DialogFragment implements View.OnClickListener {
    private ICommunicator ICommunicator;
    private ProductBinResponse moveItem = new ProductBinResponse();
    private Button btnOk;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ICommunicator = (ICommunicator) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        this.moveItem = ((ActBinItemSelection)getActivity()).getSelectedProduct();     //get data from activity
        View view = inflater.inflate(R.layout.dialog_productdetails, null);

        btnOk = (Button) view.findViewById(R.id.bnDialogFinish);
        btnOk.setOnClickListener(this);
        TextView txtSuppCat = (TextView) view.findViewById(R.id.txtDialogSuppCat);
        TextView txtArtist = (TextView) view.findViewById(R.id.txtDialogArtist);
        TextView txtTitle = (TextView) view.findViewById(R.id.txtDialogATitle);
        TextView txtBarcode = (TextView) view.findViewById(R.id.txtDialogBarcode);
        TextView txtFormat = (TextView) view.findViewById(R.id.txtDialogFormat);
        TextView txtEAN = (TextView) view.findViewById(R.id.txtDialogEAN);
        TextView txtStockAmount = (TextView) view.findViewById(R.id.txtDialogStockAmount);
        TextView txtQtyInBin = (TextView) view.findViewById(R.id.txtDialogQtyInBin);

        //Load data
        txtSuppCat.setText(moveItem.getSupplierCat());
        txtArtist.setText(moveItem.getArtist());
        txtTitle.setText(moveItem.getTitle());
        txtBarcode.setText(moveItem.getBarcode());
        txtFormat.setText(moveItem.getFormat());
        txtEAN.setText(moveItem.getEAN());
        txtStockAmount.setText(String.format("%s", moveItem.getFormat()));
        txtQtyInBin.setText(String.format("%s", moveItem.getQtyInBin()));

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == btnOk) {
            ICommunicator.onDialogMessage_ICommunicator(R.integer.MSG_OK);
            dismiss();
        }
    }
}
