package com.proper.data.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.proper.data.core.IReplenCommunicator;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 11/12/2014.
 */
public class ReplenDialogHelper extends DialogFragment implements View.OnClickListener {
    private Context ctx = null;
    private int imgPositive = 0;
    private int imgNegative = 0;
    private Button btnYes, btnNo, btnOk;
    private IReplenCommunicator IReplenCommunicator;
    private int mSeverity;
    private int mDialogType;
    private String msg = "";
    private String title = "";
    String originatedClass = "";

    public ReplenDialogHelper() {
    }

//Alternative: http://stackoverflow.com/questions/14640397/dialogfragment-throws-classcastexception-if-called-from-fragment
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        IReplenCommunicator = (IReplenCommunicator) activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        Dialog dialog = super.onCreateDialog(savedInstanceState); /** Return custom dialog... **/
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE); /** Request window with no title 21/04/2015**/
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        /** http://stackoverflow.com/a/20466259**/
        if (!getShowsDialog()) {  /**The key check, NOTE: Since we add it to a container then we exppect true**/
            return super.onCreateView(inflater, container, savedInstanceState);
        } else {
            view = inflater.inflate(R.layout.dialog_replen_base, container);
            setCancelable(false);
            return configureDialogView(view);
        }
    }

    private View configureDialogView(View v) {
        LinearLayout lytTitle = (LinearLayout) v.findViewById(R.id.lyt_BaseDialogReplenHeader);
        LinearLayout lytBody = (LinearLayout) v.findViewById(R.id.lyt_BaseDialogReplenBody);
        //Dialog dialog = getDialog(); using my custom
        if (getArguments() != null) {
            Bundle extras = getArguments();
            this.mDialogType  = extras.getInt("DialogType_ARG");
            this.mSeverity = extras.getInt("Severity_ARG");
            this.msg = extras.getString("Message_ARG");
            this.title = extras.getString("Title_ARG");
            if (extras.getString("Originated_ARG") != null && !getArguments().getString("Originated_ARG").isEmpty()) {
                originatedClass = getArguments().getString("Originated_ARG");
            } else {
                originatedClass = "";
            }
            //originatedClass = getArguments().getString("").isEmpty()? "" : getArguments().getString("Originated_ARG");
        }else {
            throw new RuntimeException("ReplenDialogHelper Arguments should not be null");
        }

        btnYes = (Button) v.findViewById(R.id.BaseDialogReplenYes);
        btnNo = (Button) v.findViewById(R.id.BaseDialogReplenNo);
        btnOk = (Button) v.findViewById(R.id.BaseDialogReplenOk);
        ImageView imgTitle = (ImageView) v.findViewById(R.id.BaseDialogReplenTitleImage);
        //TextView txtTitle = (TextView) view.findViewById(R.id.DialogTitle);
        TextView txtMessage = (TextView) v.findViewById(R.id.BaseDialogReplenMessage);
        btnYes.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnNo.setOnClickListener(this);

        if (mDialogType == R.integer.MSG_TYPE_NOTIFICATION) {
            //disable yes, no and only show ok button
            if (btnYes.getVisibility() != View.GONE) this.btnYes.setVisibility(View.GONE);
            if (btnNo.getVisibility() != View.GONE) this.btnNo.setVisibility(View.GONE);
            if (btnOk.getVisibility() != View.VISIBLE) this.btnOk.setVisibility(View.VISIBLE);
//            btnOk.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }else if (mDialogType == R.integer.MSG_TYPE_ACTION) {
            //disable ok and only display yes, no
            if (btnOk.getVisibility() != View.GONE) this.btnOk.setVisibility(View.GONE);
            if (btnYes.getVisibility() != View.VISIBLE) this.btnYes.setVisibility(View.VISIBLE);
            if (btnNo.getVisibility() != View.VISIBLE) this.btnNo.setVisibility(View.VISIBLE);
        } else {
            if (btnYes.getVisibility() != View.GONE) this.btnYes.setVisibility(View.GONE);
            if (btnNo.getVisibility() != View.GONE) this.btnNo.setVisibility(View.GONE);
            if (btnOk.getVisibility() != View.VISIBLE) this.btnOk.setVisibility(View.VISIBLE);
//            btnOk.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        switch (mSeverity) {
            case R.integer.MSG_SEVERITY_FAILURE:
                lytTitle.setBackgroundResource(R.drawable.button_red);
                imgTitle.setImageResource(R.drawable.dialog64error);
                lytBody.setBackgroundResource(R.drawable.border_red);
                break;
            case R.integer.MSG_SEVERITY_WARNING:
                lytTitle.setBackgroundResource(R.drawable.button_yellow);
                imgTitle.setImageResource(R.drawable.dialog64warning);
                lytBody.setBackgroundResource(R.drawable.border_yellow);
                break;
            default: //Assume positive
                if (mDialogType == getResources().getInteger(R.integer.MSG_TYPE_NOTIFICATION)) {
                    //info
                    imgTitle.setImageResource(R.drawable.dialod64info);
                    lytTitle.setBackgroundResource(R.drawable.flat_button_blue);
                    lytBody.setBackgroundResource(R.drawable.border_blue);
                } else {
                    //action
                    imgTitle.setImageResource(R.drawable.dialog64success);
                    lytTitle.setBackgroundResource(R.drawable.button_green);
                    lytBody.setBackgroundResource(R.drawable.border_green);
                }
                break;
        }

        imgTitle.setScaleType(ImageView.ScaleType.FIT_XY);
        //sett
        //txtTitle.setText(this.title);
        txtMessage.setText(this.msg);
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v == btnYes) {
            //do
            IReplenCommunicator.onDialogMessage_IReplenCommunicator(R.integer.MSG_YES, originatedClass);
            dismiss();
        }
        if (v == btnNo) {
            //do
            IReplenCommunicator.onDialogMessage_IReplenCommunicator(R.integer.MSG_NO, originatedClass);
            dismiss();
        }
        if (v == btnOk) {
            //do check for originated class has any value
//            if (originatedClass.isEmpty()) {
//                IReplenCommunicator.onDialogMessage(R.integer.MSG_OK);
//            } else {
//                //ICommunicator.onDialogMessage(R.integer.MSG_OK, originatedClass);
//                if (originatedClass.equalsIgnoreCase(zzUpdateLineFragment.class.getSimpleName())) {
//                    IReplenCommunicator.onDialogMessage(R.integer.MSG_OK);
//                }
//                if (originatedClass.equalsIgnoreCase(ManageMoveLineFragment.class.getSimpleName())) {
//                    IReplenCommunicator.onDialogMessage(R.integer.MSG_OK);
//                }
//                if (originatedClass.equalsIgnoreCase(ManageWorkFragment.class.getSimpleName())) {
//                    IReplenCommunicator.onDialogMessage(R.integer.MSG_OK);
//                }
//                if (originatedClass.equalsIgnoreCase(zzUpdateLineFragment.class.getSimpleName())) {
//                    IReplenCommunicator.onDialogMessage(R.integer.MSG_OK);
//                }
//            }
            IReplenCommunicator.onDialogMessage_IReplenCommunicator(R.integer.MSG_OK, originatedClass);
            dismiss();
        }
    }

    private void clearBackgrounds(View view) {
        while (view != null) {
            view.setBackgroundResource(android.graphics.Color.TRANSPARENT);

            final ViewParent parent = view.getParent();
            if (parent instanceof View) {
                view = (View) parent;
            } else {
                view = null;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //clearBackgrounds(customView);
    }
}

