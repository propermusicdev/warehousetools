package com.proper.warehousetools.goodsin.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 06/10/2014.
 */
public class zzActGoodsInProvideInfo extends Activity {
    private EditText txtGoodsinID;
    private EditText txtProductID;
    private EditText txtSupplier;
    private EditText txtOrderNumber;
    private EditText txtQuantity;
    private Button btnSend;
    private GoodsInProvide provided;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_goodsin_provideinfo);
        provided = new GoodsInProvide();
        txtGoodsinID = (EditText) this.findViewById(R.id.txtGoodsinProvideID);
        txtGoodsinID.addTextChangedListener(new TextChanged(this.txtGoodsinID));
        txtProductID = (EditText) this.findViewById(R.id.txtGoodsinProvideProductID);
        txtProductID.addTextChangedListener(new TextChanged(this.txtProductID));
        txtSupplier = (EditText) this.findViewById(R.id.txtGoodsinProvideSupplier);
        txtSupplier.addTextChangedListener(new TextChanged(this.txtSupplier));
        txtOrderNumber = (EditText) this.findViewById(R.id.txtGoodsinProvideOrderNumber);
        txtOrderNumber.addTextChangedListener(new TextChanged(this.txtOrderNumber));
        txtQuantity = (EditText) this.findViewById(R.id.txtGoodsinProvideQuantity);
        txtQuantity.addTextChangedListener(new TextChanged(this.txtQuantity));
        btnSend = (Button) this.findViewById(R.id.bnGoodsinProvideSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonCliocked(v);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.lyt_goodsin_provideinfo);
        } else {
            setContentView(R.layout.lyt_goodsin_provideinfo);
        }
    }

    private void buttonCliocked(View v) {
        if (v == btnSend) {
            if (provided != null) {
                Intent i = new Intent();
                i.putExtra("ID_EXTRA", provided.getId());
                i.putExtra("PRODUCTID_EXTRA", provided.getProductId());
                i.putExtra("SUPPLIER_EXTRA", provided.getSupplier());
                i.putExtra("ORDERNUMBER_EXTRA", provided.getOrderNumber());
                i.putExtra("QUANTITY_EXTRA", provided.getQuantity());
                setResult(RESULT_OK, i);
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
//        Intent i = new Intent();
//        setResult(RESULT_CANCELED, i);
//        this.finish();
        super.onBackPressed();
    }

    class TextChanged implements TextWatcher {
        private EditText view;

        TextChanged(EditText view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && !s.toString().equalsIgnoreCase("")) {
                String val = s.toString();
                if (view == txtGoodsinID) {
                    provided.setId(Integer.parseInt(val));
                }
                if (view == txtProductID) {
                    provided.setProductId(Integer.parseInt(val));
                }
                if (view == txtSupplier) {
                    provided.setSupplier(val);
                }
                if (view == txtOrderNumber) {
                    provided.setOrderNumber(Integer.parseInt(val));
                }
                if (view == txtQuantity) {
                    provided.setQuantity(Integer.parseInt(val));
                }
            }
        }
    }

    class GoodsInProvide {
        private int Id;
        private int ProductId;
        private String Supplier;
        private int OrderNumber;
        private int Quantity;

        GoodsInProvide() {
        }

        GoodsInProvide(int id, int productId, String supplier, int orderNumber, int quantity) {
            Id = id;
            ProductId = productId;
            Supplier = supplier;
            OrderNumber = orderNumber;
            Quantity = quantity;
        }

        public int getId() {
            return Id;
        }

        public void setId(int id) {
            Id = id;
        }

        public int getProductId() {
            return ProductId;
        }

        public void setProductId(int productId) {
            ProductId = productId;
        }

        public String getSupplier() {
            return Supplier;
        }

        public void setSupplier(String supplier) {
            Supplier = supplier;
        }

        public int getOrderNumber() {
            return OrderNumber;
        }

        public void setOrderNumber(int orderNumber) {
            OrderNumber = orderNumber;
        }

        public int getQuantity() {
            return Quantity;
        }

        public void setQuantity(int quantity) {
            Quantity = quantity;
        }
    }
}