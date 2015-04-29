package com.proper.warehousetools.binmove.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.proper.data.binmove.ProductBinSelection;
import com.proper.data.binmove.adapters.BinResponseSelectionAdapter;
import com.proper.data.core.ICommunicator;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.ui.ActBinItemSelection;
import com.proper.warehousetools.binmove.ui.chainway_c4000.ActBinProductMain;
//import com.proper.warehousetools.replen.ui.zzActReplenMiniMove;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenCreateMiniMove;

/**
 * Created by Lebel on 31/07/2014.
 */
public class QuantityDialogFragment extends DialogFragment implements View.OnClickListener {
    private ImageButton btnPlus;
    private ImageButton btnMinus;
    private Button btnByHand;
    private Button btnFinish;
    private TextView binQty;
    private EditText moveQty;
    private ICommunicator ICommunicator;
    private ProductBinSelection moveItem;
    private int rowTotalQty = 0;
    private int inputByHand = 0;
    private int action = 0;
    private static final int DIALOG_ACTION_AUTO_INCREMENT = 11;
    private static final int DIALOG_ACTION_MANUAL_INCREMENT = 22;
    private String parent = "";
    private int selectedIndex = -1;

    public int getMoveInput() {
        return moveInput;
    }

    public void setMoveInput(int moveInput) {
        this.moveInput = moveInput;
    }

    private int moveInput;

    private int ConvertPixelsToDp(float pixelValue)
    {
        int dp = (int) ((pixelValue)/ getResources().getDisplayMetrics().density);
        return dp;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ICommunicator = (ICommunicator) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        //Set-up Screen size
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthInDp = ConvertPixelsToDp(metrics.widthPixels);
        int heightInDp = ConvertPixelsToDp(metrics.heightPixels);
        getDialog().getWindow().setLayout(widthInDp, heightInDp);


        //Determine parent activity
        parent = getActivity().getClass().getSimpleName();
        if (parent.equalsIgnoreCase("ActBinProductMain")) {
            moveItem = ((ActBinProductMain)getActivity()).getCurrentBinSelection();     //get data from activity
        }
        if (parent.equalsIgnoreCase("ActBinItemSelection")) {
            moveItem = ((ActBinItemSelection)getActivity()).getCurrentBinSelection();     //get data from activity
            selectedIndex = ((ActBinItemSelection)getActivity()).getCurrentSelectedIndex(); //get index
        }
        if (parent.equalsIgnoreCase("ActReplenCreateMiniMove")) {
            moveItem = ((ActReplenCreateMiniMove)getActivity()).getMoveItem();  //get data from activity
        }
//        if (parent.equalsIgnoreCase("zzActReplenMiniMove")) {
//            moveItem = ((zzActReplenMiniMove)getActivity()).getMoveItem();  //get data from activity
//        }
        //moveItem = ((ActBinProductMain)getActivity()).getCurrentBinSelection();     //get data from activity
        View view = inflater.inflate(R.layout.dialog_quantity, null);
        LinearLayout lytMain = (LinearLayout) view.findViewById(R.id.lyt_dialogQuantity);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lytMain.getLayoutParams();
//        params.width = widthInDp;
//        params.height = heightInDp;
//        lytMain.setLayoutParams(params);
        binQty = (TextView) view.findViewById(R.id.txtDialogBinQty);
        TextView suppCat = (TextView) view.findViewById(R.id.txtDialogSuppCat);
        moveQty = (EditText) view.findViewById(R.id.etxtDialogMoveQty);
        btnPlus = (ImageButton) view.findViewById(R.id.bnDialogPlus);
        btnMinus = (ImageButton) view.findViewById(R.id.bnDialogMinus);
        btnByHand = (Button) view.findViewById(R.id.bnDialogByHand);
        btnFinish = (Button) view.findViewById(R.id.bnDialogFinish);

        binQty.addTextChangedListener(new TextChanged(binQty));
//        moveQty.setOnFocusChangeListener(new View.OnFocusChangeListener()
//                                         {
//                                             @Override
//                                             public void onFocusChange(View v, boolean hasFocus)
//                                             {
//                                                 if(hasFocus)
//                                                 {
//                                                     moveQty.setSelection(moveQty.getText().toString().length());
//                                                 }
//                                             }
//                                         }
//        );
        moveQty.addTextChangedListener(new TextChanged(moveQty));
        //btnPlus.setFocusable(false);
        //btnMinus.setFocusable(false);
        btnPlus.setOnClickListener(this);
        btnPlus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ButtonLongClicked(view);
                return false;
            }
        });
        btnMinus.setOnClickListener(this);
        btnMinus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ButtonLongClicked(view);
                return false;
            }
        });
        btnByHand.setOnClickListener(this);
        btnFinish.setOnClickListener(this);

        rowTotalQty = moveItem.getQtyInBin() + moveItem.getQtyToMove();

        binQty.setText(String.format("%s", moveItem.getQtyInBin()));
        suppCat.setText(moveItem.getSupplierCat());
        moveQty.setText(String.format("%s", moveItem.getQtyToMove()));

        if (binQty.isEnabled()) binQty.setEnabled(false);

        if (moveItem.getQtyInBin() > 0 && moveItem.getQtyToMove() > 0) {
            if (!btnMinus.isEnabled()) btnMinus.setEnabled(true);
            if (!btnPlus.isEnabled()) btnPlus.setEnabled(true);
            binQty.setTextColor(Color.parseColor("#c6c6c6"));
            moveQty.setTextColor(Color.parseColor("#ff9933"));
        }
        if (moveItem.getQtyInBin() == 0 && moveItem.getQtyToMove() > 0) {
            if (moveQty.isEnabled()) moveQty.setEnabled(false);
            if (!btnMinus.isEnabled()) btnMinus.setEnabled(true);
            if (btnPlus.isEnabled()) btnPlus.setEnabled(false);
            binQty.setTextColor(Color.parseColor("#ff1700"));
            moveQty.setTextColor(Color.parseColor("#ff9933"));
        }
        if (moveItem.getQtyInBin() > 0 && moveItem.getQtyToMove() == 0) {
            btnMinus.setEnabled(false);
            if (!btnPlus.isEnabled()) btnPlus.setEnabled(true);
            moveQty.setTextColor(Color.parseColor("#ff1700"));
            binQty.setTextColor(Color.parseColor("#e0e13d"));
        }
        this.setCancelable(false);
        return view;
    }

    private void updateControls() {
        binQty.setText(String.format("%s", moveItem.getQtyInBin()));
        moveQty.setText(String.format("%s", moveItem.getQtyToMove()));

        if (binQty.isEnabled()) binQty.setEnabled(false);

        if (moveItem.getQtyInBin() > 0 && moveItem.getQtyToMove() > 0) {
            if (!btnMinus.isEnabled()) btnMinus.setEnabled(true);
            if (!btnPlus.isEnabled()) btnPlus.setEnabled(true);
            binQty.setTextColor(Color.parseColor("#c6c6c6"));
            moveQty.setTextColor(Color.parseColor("#ff9933"));
        }
        if (moveItem.getQtyInBin() == 0 && moveItem.getQtyToMove() > 0) {
            if (moveQty.isEnabled()) moveQty.setEnabled(false);
            if (!btnMinus.isEnabled()) btnMinus.setEnabled(true);
            if (btnPlus.isEnabled()) btnPlus.setEnabled(false);
            binQty.setTextColor(Color.parseColor("#ff1700"));
            moveQty.setTextColor(Color.parseColor("#ff9933"));
        }
        if (moveItem.getQtyInBin() > 0 && moveItem.getQtyToMove() == 0) {
            btnMinus.setEnabled(false);
            if (!btnPlus.isEnabled()) btnPlus.setEnabled(true);
            moveQty.setTextColor(Color.parseColor("#ff1700"));
            binQty.setTextColor(Color.parseColor("#ff9933"));
        }
    }

    private void showSoftKeyboard() {
        InputMethodManager imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }

    private void manageInputByHand() {
        if (btnByHand.isEnabled()) {
            btnByHand.setEnabled(false);
            btnByHand.setPaintFlags(btnByHand.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (inputByHand == 0) {
            turnOnInputByHand();
            if (!moveQty.isEnabled()) {
                moveQty.setEnabled(true);
            }
            paintByHandButtons();
            if (btnFinish.isEnabled()) btnFinish.setEnabled(false);
            btnFinish.setPaintFlags(btnFinish.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            showSoftKeyboard();
            moveQty.requestFocus();
            selectAll();
        } else {
            turnOffInputByHand();
            paintByHandButtons();
            setMoveInput(Integer.parseInt(moveQty.getText().toString()));
            if (getMoveInput() != 0) {
                moveQty.setText(String.format("%s", getMoveInput()));     // just to trigger text changed
                paintByHandButtons();
                if (!btnFinish.isEnabled()) btnFinish.setEnabled(true);
                btnFinish.setPaintFlags(btnFinish.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                updateControls();
            }
        }
        if (!btnByHand.isEnabled()) {
            btnByHand.setEnabled(true);
            btnByHand.setPaintFlags(btnByHand.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;    //Turn On Input by Hand
    }

    private void paintByHandButtons() {
        final String byHand = "Enter";
        final String finish = "Finish";
        if (inputByHand == 0) {
            btnByHand.setText(byHand);
        } else {
            btnByHand.setText(finish);
        }
    }

    private void selectAll(){
        moveQty.selectAll();
    }

    @Override
    public void onClick(View v) {
        if (v == btnByHand) {
            action = DIALOG_ACTION_MANUAL_INCREMENT;
            if (!moveQty.isEnabled()) {
                moveQty.setEnabled(true);
                moveQty.requestFocus();
            }
            manageInputByHand();
        }
        if (v == btnMinus) {
            action = DIALOG_ACTION_AUTO_INCREMENT;
            moveItem.incrementBin();
            updateControls();
        }
        if (v == btnPlus) {
            action = DIALOG_ACTION_AUTO_INCREMENT;
            moveItem.incrementMove();
            updateControls();
        }
        if (v == btnFinish) {
            if (moveItem.getQtyToMove() <= 0) {
                //Alert that move cannot be zero
                String mMsg = "Bin Move Quantity (MoveQty) MUST not be zero";
                AlertDialog.Builder builder = new AlertDialog.Builder(QuantityDialogFragment.this.getActivity());
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                               //do nothing for now...
                            }
                        });
                builder.show();
            } else {
                if (parent.equalsIgnoreCase("ActBinProductMain")) {
                    ((ActBinProductMain) getActivity()).setCurrentBinSelection(moveItem);    // pass data back to the parent activity
                }
                if (parent.equalsIgnoreCase("ActBinItemSelection")) {
                    ((ActBinItemSelection) getActivity()).setCurrentBinSelection(moveItem);    // pass data back to the parent activity
                    //notify data set has been updated
                    if (selectedIndex != -1) {
                        BinResponseSelectionAdapter adapter = ((ActBinItemSelection) getActivity()).getAdapter();
                        adapter.getItem(selectedIndex).changeMoveTo(moveItem.getQtyToMove());
                        ((ActBinItemSelection) getActivity()).setAdapter(adapter);
                    }
                }
                if (parent.equalsIgnoreCase("ActReplenCreateMiniMove")) {
                    ((ActReplenCreateMiniMove)getActivity()).setMoveItem(moveItem);     // pass data back to the parent activity
                }
                ICommunicator.onDialogMessage_ICommunicator(R.integer.MSG_OK);
                dismiss();
            }
        }
    }

    private void ButtonLongClicked(View view) {
        switch (view.getId()) {
            case R.id.bnDialogMinus:
                if (rowTotalQty != 0) {
                    if (moveItem.getQtyToMove() > 0) {
                        moveItem.purgeMove();
                    }
                }
                break;
            case R.id.bnDialogPlus:
                if (rowTotalQty != 0) {
                    if (moveItem.getQtyInBin() > 0) {
                        moveItem.purgeBin();
                    }
                }
                break;
        }
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

            if (!editable.toString().isEmpty()) {
                int value = Integer.parseInt(editable.toString());
                if (action == DIALOG_ACTION_MANUAL_INCREMENT) {       //check action value
                    if (view == moveQty) {
                        if (getMoveInput() >= 0) {
                            if (value > rowTotalQty) {
                                //Alert that input cannot be larger than rowTotal, default value, updateControls, disable editText, manageInput to default
                                String mMsg = "Move Quantity cannot be larger than the total found in bin";
                                AlertDialog.Builder builder = new AlertDialog.Builder(QuantityDialogFragment.this.getActivity());
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                moveItem.restoreDefaultValue(); //default value
                                                updateControls();
                                                if (moveQty.isEnabled()) {
                                                    moveQty.setEnabled(false);
                                                }
                                                inputByHand = 0;
                                            }
                                        });
                                builder.show();
                            } else {
                                //do increment/decrement
                                if (inputByHand == 0) {
                                    moveItem.changeMoveTo(value);
                                    if (moveQty.isEnabled()) {
                                        moveQty.setEnabled(false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
