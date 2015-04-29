package com.proper.warehousetools.replen.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.proper.data.core.IReplenSplitLineCommunicator;
import com.proper.data.replen.ReplenLinesItemResponseSelection;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenSplitLine;

/**
 * Created by Knight on 18/12/2014.
 * REF: http://stackoverflow.com/a/23990856, http://examples.javacodegeeks.com/android/core/ui/dialog/android-custom-dialog-example/
 * http://stackoverflow.com/a/20535069 (EditText in listview)
 */
public class SplitLineQuantityFragment extends DialogFragment {
    private ActReplenSplitLine mActivity = null;
    private TextView txtCatalog, txtTotalQty, txtHeaderTitle;
    private EditText txtNewLineQty, txtSrcBin, txtDstBin;
    private Button btnByHand, btnFinish, btnSrcEdit, btnDstEdit;
    private ImageButton btnPlus, btnMinus;
    private com.proper.data.core.IReplenSplitLineCommunicator IReplenSplitLineCommunicator;
    private ReplenLinesItemResponseSelection moveLine = null;
    private int qtyToSplitInput = 0, rowTotalQty = 0, inputByHand = 0, action = 0;
    private String parent = "", srcBinToSplitInput = "", dstBinToSplitInput = "";
    private static final int DIALOG_ACTION_AUTO_INCREMENT = 11;
    private static final int DIALOG_ACTION_MANUAL_INCREMENT = 22;
    
    public SplitLineQuantityFragment() {
        // Empty constructor required for DialogFragment
    }

    public int getQtyToSplitInput() {
        return qtyToSplitInput;
    }

    public void setQtyToSplitInput(int qtyToSplitInput) {
        this.qtyToSplitInput = qtyToSplitInput;
    }

    public String getSrcBinToSplitInput() {
        return srcBinToSplitInput;
    }

    public void setSrcBinToSplitInput(String srcBinToSplitInput) {
        this.srcBinToSplitInput = srcBinToSplitInput;
    }

    public String getDstBinToSplitInput() {
        return dstBinToSplitInput;
    }

    public void setDstBinToSplitInput(String dstBinToSplitInput) {
        this.dstBinToSplitInput = dstBinToSplitInput;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        IReplenSplitLineCommunicator = (IReplenSplitLineCommunicator) activity;
        if (mActivity == null) {
            mActivity = (ActReplenSplitLine) getActivity();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (mActivity == null) {
            mActivity = (ActReplenSplitLine) getActivity();
        }
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mActivity = (ActReplenSplitLine) getActivity();
        //Determine parent activity
        parent = getActivity().getClass().getSimpleName();

        getDialog().setTitle(getString(R.string.app_name));
//        Bundle extras = getArguments();
        View view = inflater.inflate(R.layout.dialog_replen_addline_quantity, container);
        txtHeaderTitle = (TextView) view.findViewById(R.id.DialogTitle);
        txtCatalog = (TextView) view.findViewById(R.id.txtReplenDialogSuppCat);
        txtTotalQty = (TextView) view.findViewById(R.id.txtReplenDialogLineQty);
        txtNewLineQty = (EditText) view.findViewById(R.id.etxtReplenDialogNewLineQty);
        txtSrcBin = (EditText) view.findViewById(R.id.etxtReplenDialogSrcBin);
        txtDstBin = (EditText) view.findViewById(R.id.etxtReplenDialogDstBin);
        btnSrcEdit = (Button) view.findViewById(R.id.bnReplenDialogSrcBinByHand);
        btnDstEdit = (Button) view.findViewById(R.id.bnReplenDialogDstBinByHand);
        btnPlus = (ImageButton) view.findViewById(R.id.bnReplenDialogPlus);
        btnMinus = (ImageButton) view.findViewById(R.id.bnReplenDialogMinus);
        btnByHand = (Button) view.findViewById(R.id.bnReplenDialogByHand);
        btnFinish = (Button) view.findViewById(R.id.bnReplenDialogFinish);
        txtNewLineQty.addTextChangedListener(new TextChanged(txtNewLineQty));
        txtSrcBin.addTextChangedListener(new TextChanged(txtSrcBin));
        txtDstBin.addTextChangedListener(new TextChanged(txtDstBin));
        //btnPlus.setFocusable(false);
        //btnMinus.setFocusable(false);
        btnSrcEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnDstEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnPlus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ButtonLongClicked(view);
                return false;
            }
        });
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnMinus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ButtonLongClicked(view);
                return false;
            }
        });
        btnByHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });

        //moveLine = new ReplenLinesItemResponseSelection((ReplenMoveListLinesItemResponse) extras.getSerializable("LINE_EXTRA")); //TODO - Get Data !!!!
        //moveLine = mActivity.getSplitMoveLineSelection();

        rowTotalQty = mActivity.getSplitMoveLineSelection().getQty() + mActivity.getSplitMoveLineSelection().getQtyToSplit();

        txtTotalQty.setText(String.format("%s", mActivity.getSplitMoveLineSelection().getQty()));
        txtCatalog.setText(mActivity.getSplitMoveLineSelection().getCatNumber());
        txtNewLineQty.setText(String.format("%s", mActivity.getSplitMoveLineSelection().getQtyToSplit()));
        txtSrcBin.setText(mActivity.getSplitMoveLineSelection().getSrcBinCode());
        txtDstBin.setText(mActivity.getSplitMoveLineSelection().getDstBinCode());

        if (txtTotalQty.isEnabled()) txtTotalQty.setEnabled(false);

        if (mActivity.getSplitMoveLineSelection().getQty() > 0 && mActivity.getSplitMoveLineSelection().getQtyToSplit() > 0) {
            if (!btnMinus.isEnabled()) btnMinus.setEnabled(true);
            if (!btnPlus.isEnabled()) btnPlus.setEnabled(true);
            txtTotalQty.setTextColor(Color.parseColor("#c6c6c6"));
            txtNewLineQty.setTextColor(Color.parseColor("#ff9933"));
        }
        if (mActivity.getSplitMoveLineSelection().getQty() == 0 && mActivity.getSplitMoveLineSelection().getQtyToSplit() > 0) {
            if (txtNewLineQty.isEnabled()) txtNewLineQty.setEnabled(false);
            if (!btnMinus.isEnabled()) btnMinus.setEnabled(true);
            if (btnPlus.isEnabled()) btnPlus.setEnabled(false);
            txtTotalQty.setTextColor(Color.parseColor("#ff1700"));
            txtNewLineQty.setTextColor(Color.parseColor("#ff9933"));
        }
        if (mActivity.getSplitMoveLineSelection().getQty() > 0 && mActivity.getSplitMoveLineSelection().getQtyToSplit() == 0) {
            btnMinus.setEnabled(false);
            if (!btnPlus.isEnabled()) btnPlus.setEnabled(true);
            txtNewLineQty.setTextColor(Color.parseColor("#ff1700"));
            txtTotalQty.setTextColor(Color.parseColor("#e0e13d"));
        }
        if (parent.equalsIgnoreCase("ActReplenManageWork")) {
            // moveItem = ((ActReplenManageWork)getActivity()).getCurrentBinSelection();     //get data from activity
        }
        if (parent.equalsIgnoreCase("ActReplenSplitLine")) {
            txtHeaderTitle.setText("Split Line");
        }
        txtNewLineQty.setEnabled(false);
        txtSrcBin.setEnabled(false);
        txtDstBin.setEnabled(false);
        this.setCancelable(false);
        return view;
    }

    private void updateControls() {
        txtTotalQty.setText(String.format("%s", mActivity.getSplitMoveLineSelection().getQty()));
        txtNewLineQty.setText(String.format("%s", mActivity.getSplitMoveLineSelection().getQtyToSplit()));

        if (txtTotalQty.isEnabled()) txtTotalQty.setEnabled(false);

        if (mActivity.getSplitMoveLineSelection().getQty() > 0 && mActivity.getSplitMoveLineSelection().getQtyToSplit() > 0) {
            if (!btnMinus.isEnabled()) btnMinus.setEnabled(true);
            if (!btnPlus.isEnabled()) btnPlus.setEnabled(true);
            txtTotalQty.setTextColor(Color.parseColor("#c6c6c6"));
            txtNewLineQty.setTextColor(Color.parseColor("#ff9933"));
        }
        if (mActivity.getSplitMoveLineSelection().getQty() == 0 && mActivity.getSplitMoveLineSelection().getQtyToSplit() > 0) {
            if (txtNewLineQty.isEnabled()) txtNewLineQty.setEnabled(false);
            if (!btnMinus.isEnabled()) btnMinus.setEnabled(true);
            if (btnPlus.isEnabled()) btnPlus.setEnabled(false);
            txtTotalQty.setTextColor(Color.parseColor("#ff1700"));
            txtNewLineQty.setTextColor(Color.parseColor("#ff9933"));
        }
        if (mActivity.getSplitMoveLineSelection().getQty() > 0 && mActivity.getSplitMoveLineSelection().getQtyToSplit() == 0) {
            btnMinus.setEnabled(false);
            if (!btnPlus.isEnabled()) btnPlus.setEnabled(true);
            txtNewLineQty.setTextColor(Color.parseColor("#ff1700"));
            txtTotalQty.setTextColor(Color.parseColor("#ff9933"));
        }
    }

    private void showSoftKeyboard() {
        InputMethodManager imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }

    private void manageInputByHand(Button button) {
        if (button == btnByHand) {
            if (btnByHand.isEnabled()) {
                btnByHand.setEnabled(false);
                btnByHand.setPaintFlags(btnByHand.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            if (inputByHand == 0) {
                turnOnInputByHand();
                if (!txtNewLineQty.isEnabled()) {
                    txtNewLineQty.setEnabled(true);
                }
                paintByHandButtons(btnByHand);
                if (btnFinish.isEnabled()) btnFinish.setEnabled(false);
                btnFinish.setPaintFlags(btnFinish.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                if (btnPlus.isEnabled()) btnPlus.setEnabled(false);
                if (btnMinus.isEnabled()) btnMinus.setEnabled(false);
                showSoftKeyboard();
                txtNewLineQty.requestFocus();
                selectAll(txtNewLineQty);
            } else {
                turnOffInputByHand();
                paintByHandButtons(btnByHand);
                setQtyToSplitInput(Integer.parseInt(txtNewLineQty.getText().toString()));
                if (getQtyToSplitInput() != 0) {
                    txtNewLineQty.setText(String.format("%s", getQtyToSplitInput()));     // just to trigger text changed
                    paintByHandButtons(btnByHand);
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

        if (button == btnFinish) {
        }

        if (button == btnSrcEdit) {
            if (btnSrcEdit.isEnabled()) {
                btnSrcEdit.setEnabled(false);
                btnSrcEdit.setPaintFlags(btnSrcEdit.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            if (inputByHand == 0) {
                turnOnInputByHand();
                if (!txtSrcBin.isEnabled()) {
                    txtSrcBin.setEnabled(true);
                }
                paintByHandButtons(btnSrcEdit);
                if (btnFinish.isEnabled()) btnFinish.setEnabled(false);
                btnFinish.setPaintFlags(btnFinish.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                if (btnPlus.isEnabled()) btnPlus.setEnabled(false);
//                if (btnMinus.isEnabled()) btnMinus.setEnabled(false);
                showSoftKeyboard();
                txtSrcBin.requestFocus();
                selectAll(txtSrcBin);
            } else {
                turnOffInputByHand();
                paintByHandButtons(btnSrcEdit);
                //setQtyToSplitInput(Integer.parseInt(txtSrcBin.getText().toString()));
                setSrcBinToSplitInput(txtSrcBin.getText().toString());
                if (!getSrcBinToSplitInput().isEmpty()) {
                    txtSrcBin.setText(getSrcBinToSplitInput());     // just to trigger text changed
                    paintByHandButtons(btnSrcEdit);
                    if (!btnFinish.isEnabled()) btnFinish.setEnabled(true);
                    btnFinish.setPaintFlags(btnFinish.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    updateControls();
                }
            }
            if (!btnSrcEdit.isEnabled()) {
                btnSrcEdit.setEnabled(true);
                btnSrcEdit.setPaintFlags(btnSrcEdit.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }

        if (button == btnDstEdit) {
            if (btnDstEdit.isEnabled()) {
                btnDstEdit.setEnabled(false);
                btnDstEdit.setPaintFlags(btnDstEdit.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            if (inputByHand == 0) {
                turnOnInputByHand();
                if (!txtDstBin.isEnabled()) {
                    txtDstBin.setEnabled(true);
                }
                paintByHandButtons(btnDstEdit);
                if (btnFinish.isEnabled()) btnFinish.setEnabled(false);
                btnFinish.setPaintFlags(btnFinish.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                if (btnPlus.isEnabled()) btnPlus.setEnabled(false);
//                if (btnMinus.isEnabled()) btnMinus.setEnabled(false);
                showSoftKeyboard();
                txtDstBin.requestFocus();
                selectAll(txtDstBin);
            } else {
                turnOffInputByHand();
                paintByHandButtons(btnDstEdit);
                //setQtyToSplitInput(Integer.parseInt(txtSrcBin.getText().toString()));
                setDstBinToSplitInput(txtDstBin.getText().toString());
                if (!getDstBinToSplitInput().isEmpty()) {
                    txtDstBin.setText(getDstBinToSplitInput());     // just to trigger text changed
                    paintByHandButtons(btnDstEdit);
                    if (!btnFinish.isEnabled()) btnFinish.setEnabled(true);
                    btnFinish.setPaintFlags(btnFinish.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    updateControls();
                }
            }
            if (!btnDstEdit.isEnabled()) {
                btnDstEdit.setEnabled(true);
                btnDstEdit.setPaintFlags(btnDstEdit.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;    //Turn On Input by Hand
    }

    private void paintByHandButtons(Button button) {
        final String byHand = "Enter";
        final String finish = "Finish";
        final String enter = "EDIT";
        if (button == btnByHand) {
            if (inputByHand == 0) {
                btnByHand.setText(byHand);
            } else {
                btnByHand.setText(finish);
            }
        }

        if (button == btnSrcEdit) {
            if (inputByHand == 0) {
                btnSrcEdit.setText(enter);
            } else {
                btnSrcEdit.setText(finish);
            }
        }

        if (button == btnDstEdit) {
            if (inputByHand == 0) {
                btnDstEdit.setText(enter);
            } else {
                btnDstEdit.setText(finish);
            }
        }
    }

    private void selectAll(EditText editText){
        editText.selectAll();
    }

    private void buttonClicked(View v) {
        if (v == btnSrcEdit) {
//            if (!txtSrcBin.isEnabled()) {
//                txtSrcBin.setEnabled(true);
//                txtSrcBin.requestFocus();
//            }
            manageInputByHand(btnSrcEdit);
        }
        if (v == btnDstEdit) {
//            if (!txtDstBin.isEnabled()) {
//                txtDstBin.setEnabled(true);
//                txtDstBin.requestFocus();
//            }
            manageInputByHand(btnDstEdit);
        }
        if (v == btnByHand) {
            action = DIALOG_ACTION_MANUAL_INCREMENT;
            if (!txtNewLineQty.isEnabled()) {
                txtNewLineQty.setEnabled(true);
                txtNewLineQty.requestFocus();
            }
            manageInputByHand(btnByHand);
        }
        if (v == btnMinus) {
            action = DIALOG_ACTION_AUTO_INCREMENT;
            mActivity.getSplitMoveLineSelection().incrementBin();
            updateControls();
        }
        if (v == btnPlus) {
            action = DIALOG_ACTION_AUTO_INCREMENT;
            mActivity.getSplitMoveLineSelection().incrementMove();
            updateControls();
        }
        if (v == btnFinish) {
            if (mActivity.getSplitMoveLineSelection() == null || mActivity.getSplitMoveLineSelection().getQtyToSplit() <= 0) {
                //Alert that move cannot be zero
                String mMsg = "Bin Move Quantity (txtNewLineQty) MUST not be zero";
                AlertDialog.Builder builder = new AlertDialog.Builder(SplitLineQuantityFragment.this.getActivity());
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing for now...
                            }
                        });
                builder.show();
            } else {
//                if (parent.equalsIgnoreCase("ActReplenManageWork")) {
//                    ((ActReplenManageWork) getActivity()).setSplitMoveLineSelection(moveLine);    //TODO - pass data back to the parent activity !!!
//                }
//                if (parent.equalsIgnoreCase("ActReplenSplitLine")) {
//                    ((ActReplenSplitLine) getActivity()).setSplitMoveLineSelection(moveLine);    //TODO - pass data back to the parent activity !!!
//                }
                IReplenSplitLineCommunicator.onDialogMessage_IReplenSplitLineCommunicator(R.integer.MSG_OK);
                dismiss();
            }
        }
    }

    private void ButtonLongClicked(View view) {
        switch (view.getId()) {
            case R.id.bnDialogMinus:
                if (rowTotalQty != 0) {
                    if (mActivity.getSplitMoveLineSelection().getQtyToSplit() > 0) {
                        mActivity.getSplitMoveLineSelection().purgeMove();
                    }
                }
                break;
            case R.id.bnDialogPlus:
                if (rowTotalQty != 0) {
                    if (mActivity.getSplitMoveLineSelection().getQty() > 0) {
                        mActivity.getSplitMoveLineSelection().purgeBin();
                    }
                }
                break;
        }
    }

    /** REF:
     * http://stackoverflow.com/questions/19197085/why-is-onresume-method-of-a-fragment-never-fired-after-dismissing-a-dialogfragme
     * **/
    private void dismissDialog() {
        getActivity().startActivityForResult(getActivity().getIntent(), 10);
        dismiss();
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
                if (view == txtNewLineQty) {
                    int value = Integer.parseInt(editable.toString());
                    if (action == DIALOG_ACTION_MANUAL_INCREMENT) {       //check action value
                        if (view == txtNewLineQty) {
                            if (getQtyToSplitInput() >= 0) {
                                if (value > rowTotalQty) {
                                    //Alert that input cannot be larger than rowTotal, default value, updateControls, disable editText, manageInput to default
                                    String mMsg = "Move Quantity cannot be larger than the total found in bin";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(SplitLineQuantityFragment.this.getActivity());
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    mActivity.getSplitMoveLineSelection().restoreDefaultValue(); //default value
                                                    updateControls();
                                                    if (txtNewLineQty.isEnabled()) {
                                                        txtNewLineQty.setEnabled(false);
                                                    }
                                                    inputByHand = 0;
                                                }
                                            });
                                    builder.show();
                                } else {
                                    //do increment/decrement
                                    if (inputByHand == 0) {
                                        mActivity.getSplitMoveLineSelection().changeMoveTo(value);
                                        if (txtNewLineQty.isEnabled()) {
                                            txtNewLineQty.setEnabled(false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Source or Destination
                    String value = editable.toString().trim();
                    if (value.length() == 5) {
                        //we're in business
                        if (view == txtDstBin) {
                            mActivity.getSplitMoveLineSelection().setDstBinCode(value);
                            setDstBinToSplitInput(value);
                            if (txtDstBin.isEnabled()) {
                                txtDstBin.setEnabled(false);
                            }
                        }
                        if (view == txtSrcBin) {
                            mActivity.getSplitMoveLineSelection().setSrcBinCode(value);
                            setSrcBinToSplitInput(value);
                            if (txtSrcBin.isEnabled()) {
                                txtSrcBin.setEnabled(false);
                            }
                        }
                    }
                }
            }
        }
    }
}