package com.proper.warehousetools.binmove.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.proper.data.binmove.Bin;
import com.proper.data.binmove.ProductResponse;
import com.proper.data.helpers.ResponseHelper;
import com.proper.messagequeue.Message;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ActProductMoveDialog extends PlainActivity {
    private ResponseHelper responseHelper = new ResponseHelper();
    private ProductResponse thisProduct = new ProductResponse();
    private Bin thisBin = new Bin();
    private Button btnContinue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_productmovedialog);
        btnContinue = (Button) this.findViewById(R.id.bnContinueProductMoveDialog);
        Button btnExit = (Button) this.findViewById(R.id.bnCloseProductMoveDialog);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button_clicked(view);
            }
        });
        Bundle extras = getIntent().getExtras();
        SharedPreferences prefs = getSharedPreferences("devicePreferences", Context.MODE_PRIVATE);
        thisProduct = (ProductResponse) extras.getSerializable("PRODUCT_EXTRA");
        thisBin = (Bin) extras.getSerializable("BIN_EXTRA");
        //
        thisMessage =  new Message();   // instantiate at least once on every load
        if (!btnContinue.isEnabled()) {
            btnContinue.setEnabled(true);
            btnContinue.setPaintFlags(btnContinue.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void Button_clicked(View view) {
        switch (view.getId()) {
            case R.id.bnCloseProductMoveDialog:
                this.finish();
                break;
            case R.id.bnContinueProductMoveDialog:
                //do
                break;
        }
    }
}