package com.proper.warehousetools.binmove.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.Button;
import android.widget.ListView;
import com.proper.data.binmove.*;
import com.proper.data.binmove.adapters.BinListAdapter;
import com.proper.data.core.IOnDetailSelectionChanged;
import com.proper.data.core.IOnItemSelectionChanged;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.fragments.SingleItemDetailsFragment;
import com.proper.warehousetools.binmove.fragments.SingleItemFragment;

/**
 * Created by Lebel on 27/08/2014.
 */
//public class ActSingleDetails extends BaseFragmentActivity implements IOnItemSelectionChanged, IOnDetailSelectionChanged {
public class ActSingleDetails extends BaseScanActivity implements IOnItemSelectionChanged, IOnDetailSelectionChanged {
    private int transactionNumber = 0;
    private Button btnContinue = null;
    private String ApplicationID = "";
    private BarcodeResponse response;
    private ProductResponse SelectedProductResponse;
    private Bin SelectedBin;
    private BarcodeResponse barcodeResponse;
    private BarcodeBinResponse barcodeBinResponse;
    private BinResponse binResponse;

    public int getInstruction() {
        return NAV_INSTRUCTION;
    }

    public void setInstruction(int instruction) {
        this.NAV_INSTRUCTION = instruction;
    }
    public BarcodeResponse getResponse() {
        return response;
    }

    public void setResponse(BarcodeResponse response) {
        this.response = response;
    }

    public ProductResponse getSelectedProductResponse() {
        return SelectedProductResponse;
    }

    public void setSelectedProductResponse(ProductResponse selectedProductResponse) {
        SelectedProductResponse = selectedProductResponse;
    }

    public Bin getSelectedBin() {
        return SelectedBin;
    }

    public void setSelectedBin(Bin selectedBin) {
        SelectedBin = selectedBin;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Identify where call came from and NAV_instruction
        Bundle extras = getIntent().getExtras();
        NAV_INSTRUCTION = extras.getInt("ACTION_EXTRA"); //  Gives us the inclination of what is going on
        setInstruction(NAV_INSTRUCTION);

        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                //do
                break;
            case R.integer.ACTION_BARCODEQUERY:
                barcodeResponse = (BarcodeResponse) extras.getSerializable("MESSAGE_RESPONSE_EXTRA");
                setResponse(barcodeResponse);
                this.setTitle(barcodeResponse.getProducts().get(0).getBins() != null ?
                        barcodeResponse.getProducts().get(0).getTitle() : barcodeResponse.getProducts().get(1).getTitle());
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                //do
                break;
            case R.integer.ACTION_BINMOVE:
                //do
                break;
            case R.integer.ACTION_SINGLEMOVE:
                barcodeResponse = (BarcodeResponse) extras.getSerializable("MESSAGE_RESPONSE_EXTRA");
                setResponse(barcodeResponse);
                this.setTitle(barcodeResponse.getProducts().get(0).getBins() != null ?
                        barcodeResponse.getProducts().get(0).getFullTitle() : barcodeResponse.getProducts().get(1).getFullTitle());
                break;
        }
        setContentView(R.layout.lyt_binmove_singledetailsmain);
    }

    @Override
    public void onDetailselectionChenged(int detailIndex) {
        FragmentManager fm = getSupportFragmentManager();
        SingleItemDetailsFragment detailsFragment = (SingleItemDetailsFragment) fm.findFragmentById(R.id.ItemDetailsFragment);
        setSelectedBin(detailsFragment.getSelectedBin());   //get currently selected Bin in fragment
    }

    @Override
    public void onItemSelectionChanged(int itemIndex) {
        FragmentManager fm = getSupportFragmentManager();
        SingleItemFragment itemFragment = (SingleItemFragment) fm.findFragmentById(R.id.ItemsFragment);
        SingleItemDetailsFragment detailsFragment = (SingleItemDetailsFragment) fm.findFragmentById(R.id.ItemDetailsFragment);
        setSelectedProductResponse(itemFragment.getSelectedProduct());  //get currently selected product in fragment
        //Load the new Bins & notify the UI that something has changed - notifyDataSetChanged()
        if (detailsFragment.getDetailsInflater() != null) {
            ListView list = (ListView) detailsFragment.getView().findViewById(R.id.lvDetails);
            BinListAdapter detailsAdapter = new BinListAdapter(this, detailsFragment.getDetailsInflater(), getSelectedProductResponse().getBins());
            list.setAdapter(detailsAdapter);
            detailsAdapter.notifyDataSetChanged();
        }
    }
}