package com.proper.data.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.proper.data.core.ICommunicator;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 28/07/2014.
 */
public class DialogHelper extends DialogFragment implements View.OnClickListener {
    private Context ctx = null;
    private int imgPositive = 0;
    private int imgNegative = 0;
    private Button btnYes, btnNo, btnOk;
    private ICommunicator ICommunicator;
    private int mSeverity;
    private int mDialogType;
    private String msg = "";
    private String title = "";

//    public DialogHelper(int severity, int dialogType, String message, String title) {
//        this.mDialogType = dialogType;
//        this.mSeverity = severity;
//        this.msg = message;
//        this.title = title;
//    }

    public DialogHelper() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ICommunicator = (ICommunicator) activity;
    }

    //@NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        /** request a window without the title **/
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        //View view = inflater.inflate(R.layout.dialog_base, null);
        View view = inflater.inflate(R.layout.dialog_base, container);
        LinearLayout lytTitle = (LinearLayout) view.findViewById(R.id.lyt_dialogHeader);
        LinearLayout lytBody = (LinearLayout) view.findViewById(R.id.lyt_dialogBody);
        if (getArguments() != null) {
            this.mDialogType  = getArguments().getInt("DialogType_ARG");
            this.mSeverity = getArguments().getInt("Severity_ARG");
            this.msg = getArguments().getString("Message_ARG");
            this.title = getArguments().getString("Title_ARG");
        }else {
            throw new RuntimeException("DialogHelper Arguments should not be null");
        }

        btnYes = (Button) view.findViewById(R.id.bnDialogYes);
        btnNo = (Button) view.findViewById(R.id.bnDialogNo);
        btnOk = (Button) view.findViewById(R.id.bnDialogOk);
        ImageView imgTitle = (ImageView) view.findViewById(R.id.imgDialogTitle);
        TextView txtTitle = (TextView) view.findViewById(R.id.DialogTitle);
        TextView txtMessage = (TextView) view.findViewById(R.id.DialogMessage);
        btnYes.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnNo.setOnClickListener(this);

        if (mDialogType == R.integer.MSG_TYPE_NOTIFICATION) {
            //disable yes, no and only show ok button
            if (btnYes.getVisibility() == View.VISIBLE) this.btnYes.setVisibility(View.GONE);
            if (btnNo.getVisibility() == View.VISIBLE) this.btnNo.setVisibility(View.GONE);
            if (btnOk.getVisibility() != View.VISIBLE) this.btnYes.setVisibility(View.VISIBLE);
        }else if (mDialogType == R.integer.MSG_TYPE_ACTION) {
            //disable ok and only display yes, no
            if (btnOk.getVisibility() == View.VISIBLE) this.btnOk.setVisibility(View.GONE);
            if (btnYes.getVisibility() != View.VISIBLE) this.btnYes.setVisibility(View.VISIBLE);
            if (btnNo.getVisibility() != View.VISIBLE) this.btnNo.setVisibility(View.VISIBLE);
        } else {
            if (btnYes.getVisibility() == View.VISIBLE) this.btnYes.setVisibility(View.GONE);
            if (btnNo.getVisibility() == View.VISIBLE) this.btnNo.setVisibility(View.GONE);
            if (btnOk.getVisibility() != View.VISIBLE) this.btnYes.setVisibility(View.VISIBLE);
        }
        switch (mSeverity) {
            case R.integer.MSG_SEVERITY_POSITIVE:
                //change color of the titleBar
                lytTitle.setBackgroundResource(R.drawable.button_green);
                imgTitle.setImageResource(R.drawable.dialog64success);
                lytBody.setBackgroundResource(R.drawable.border_green);
                break;
            case R.integer.MSG_SEVERITY_FAILURE:
                lytTitle.setBackgroundResource(R.drawable.button_red);
                imgTitle.setImageResource(R.drawable.dialog64error);
                lytBody.setBackgroundResource(R.drawable.border_red);
                break;
            case R.integer.MSG_SEVERITY_WARNING:
                lytTitle.setBackgroundResource(R.drawable.button_blue);
                imgTitle.setImageResource(R.drawable.dialod64info);
                lytBody.setBackgroundResource(R.drawable.border_blue);
                break;
//            default:
//                lytTitle.setBackgroundResource(R.drawable.button_yellow);
//                imgTitle.setImageResource(R.drawable.dialog64warning);
//                lytBody.setBackgroundResource(R.drawable.border_yellow);
//                break;
        }

        imgTitle.setScaleType(ImageView.ScaleType.FIT_XY);
        txtTitle.setText(this.title);
        txtMessage.setText(this.msg);
        this.setCancelable(false);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == btnYes) {
            //do
            ICommunicator.onDialogMessage_ICommunicator(R.integer.MSG_YES);
            dismiss();
        }
        if (v == btnNo) {
            //do
            ICommunicator.onDialogMessage_ICommunicator(R.integer.MSG_NO);
            dismiss();
        }
        if (v == btnOk) {
            //do
            ICommunicator.onDialogMessage_ICommunicator(R.integer.MSG_OK);
            dismiss();
        }
    }
}
