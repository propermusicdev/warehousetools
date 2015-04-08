package com.proper.warehousetools.binmove.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.proper.data.binmove.BarcodeResponse;
import com.proper.data.binmove.Bin;
import com.proper.data.binmove.ProductResponse;
import com.proper.data.binmove.adapters.BinListAdapter;
import com.proper.data.core.IOnDetailSelectionChanged;
import com.proper.data.diagnostics.LogEntry;
import com.proper.logger.LogHelper;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.ui.ActSingleDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 14/04/2014.
 */
public class SingleItemDetailsFragment extends Fragment {
    private int NAV_INSTRUCTION = 0;
    private TextView txtArtist;
    private Button btnExit;
    private BinListAdapter detailsAdapter;
    private LayoutInflater detailsInflater;
    private List<Bin> binList;
    private BarcodeResponse responses = new BarcodeResponse();
    private List<Bin> currentBins = null;
    private Bin selectedBin;
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private String deviceIMEI = "";
    private String ApplicationID = "BinMove"; //etString(R.string.app_name);
    private LogHelper logger = new LogHelper();

    public Bin getSelectedBin() {
        return selectedBin;
    }

    public void setSelectedBin(Bin selectedBin) {
        this.selectedBin = selectedBin;
    }

    public BinListAdapter getDetailsAdapter() {
        return detailsAdapter;
    }

    public void setDetailsAdapter(BinListAdapter detailsAdapter) {
        this.detailsAdapter = detailsAdapter;
        detailsAdapter.notifyDataSetChanged();
    }

    public LayoutInflater getDetailsInflater() {
        return detailsInflater;
    }

    public void setDetailsInflater(LayoutInflater detailsInflater) {
        this.detailsInflater = detailsInflater;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        detailsInflater = inflater;
        View myView = inflater.inflate(R.layout.lyt_binmove_fgmsingleitemdetails, container, false);
        txtArtist = (TextView) myView.findViewById(R.id.txtvArtist);
        btnExit = (Button) myView.findViewById(R.id.bnExitActSingleDetails);
        ListView lv = (ListView) myView.findViewById(R.id.lvDetails);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                OnListItemClick(adapterView, view, i, l);
            }
        });
        ActSingleDetails activity = (ActSingleDetails) getActivity();
        responses = activity.getResponse();
        NAV_INSTRUCTION = activity.getInstruction();
        currentBins = new ArrayList<Bin>();
        currentBins = getBins(responses.getProducts(), 0);
        detailsAdapter = new BinListAdapter(myView.getContext(), detailsInflater, currentBins);
        lv.setAdapter(detailsAdapter);
        registerForContextMenu(lv);
        txtArtist.setText(responses.getProducts().get(0).getArtist());
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });

        return myView;
    }

    private void ButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.bnExitActSingleDetails:
                Intent resultIntent = new Intent();
                this.getActivity().setResult(getActivity().RESULT_OK, resultIntent);
                this.getActivity().finish();
                break;
        }
    }

    private void OnListItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
        ActSingleDetails activity = (ActSingleDetails) getActivity();
        if (currentBins.isEmpty()) {
            currentBins = new ArrayList<Bin>();
            //currentBins = getBins(responses.getProducts(), 0);
            currentBins = activity.getSelectedProductResponse().getBins();  //set currently selected bins
        }
        view.setSelected(true); //Helps towards highlighting row    **********************
        setSelectedBin(currentBins.get(pos));
        IOnDetailSelectionChanged listner = (IOnDetailSelectionChanged) getActivity();
        listner.onDetailselectionChenged(pos);      //pass position index to the main activity through interface
        //ListView thisList = (ListView) view;
        //thisList.showContextMenu();
    }

    public List<Bin> getBins(List<ProductResponse> responseList, int productIndex) {
        binList = responseList.get(productIndex).getBins();
        return binList;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_SINGLEMOVE:
                menu.add("Move Product Here");
                break;
            case R.integer.ACTION_BINMOVE:
                menu.add("Move All To This Bin");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //return super.onContextItemSelected(item);
        try {
            //AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int productId = 0;
            String bin = "";
            int binLength = 0;
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int index = info.position;  //get position of item clicked
            setSelectedBin(currentBins.get(index));
            IOnDetailSelectionChanged listner = (IOnDetailSelectionChanged) getActivity();
            listner.onDetailselectionChenged(index);
            String selectedbin = selectedBin.getBinCode();
            //getView().setSelected(true);    //Helps towards highlighting row    ***************
            if (item.getTitle().toString().equalsIgnoreCase("Move Product Here")) {
                //do move-single-product action here
                ActSingleDetails activity = (ActSingleDetails) getActivity();
                productId = activity.getSelectedProductResponse().getProductId();   //retrieve the exact product to move
                //bin = activity.getSelectedBin().getBinCode();    //retrieve the exact bin code to fill
               // bin = currentBins.get(menuInfo.position).getBinCode();
                bin = selectedbin;
                //TODO - Implement a way to complete a product move !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                productId ++;
                productId --;
                selectedbin = "+" + selectedbin + "+";
                binLength = bin.length() - selectedbin.length();
                binLength ++;
                binLength --;
            }
            new AlertDialog.Builder(getActivity()).setTitle(R.string.DIA_ALERT).setMessage(String.
                    format("You've chosen item index: %s", index)).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    //finish();
                }
            }).show();
            if (item.getTitle().toString().equalsIgnoreCase("Move All To This Bin")) {
                //do move-entire-bin action here
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "SingleItemDetailsFragment - onContextItemSelected", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        }
        return true;
    }
}
