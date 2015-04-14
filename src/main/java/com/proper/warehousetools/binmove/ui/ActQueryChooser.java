package com.proper.warehousetools.binmove.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ScrollView;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ActQueryChooser extends PlainActivity {
    private static final String ApplicationID = "BinMove";
    private static final int REQUEST_BARCODE = 1001;
    private static final int REQUEST_BINCODE = 1003;
    private static final int REQUEST_BARCODE_BIN = 1005;
    private int NAV_INSTRUCTION = 0;
//    private Button btnQryBin;
//    private Button btnQryBarcode;
    private Button btnQryBarcodeBin, btnExit, btnSearch;
    private ScrollView screen;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_querychooser);

        screen = (ScrollView) this.findViewById(R.id.screenQueryChooser);

        configureUI(savedInstanceState);

        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        Animation animOpenScale = AnimationUtils.loadAnimation(this, R.anim.activity_open_scale);
        screen.startAnimation(animOpenScale);
//        btnQryBarcode.startAnimation(animFadeIn);
//        btnQryBin.startAnimation(animFadeIn);
        btnSearch.startAnimation(animFadeIn);
        btnQryBarcodeBin.startAnimation(animFadeIn);
        btnExit.startAnimation(animFadeIn);
    }

    private void configureUI(Bundle bundle) {
//        btnQryBin = (Button) this.findViewById(R.id.bnQryChooserBin);
//        btnQryBarcode = (Button) this.findViewById(R.id.bnQryChooserBarcode);
        btnSearch = (Button) this.findViewById(R.id.bnQryChooserSearch);
        btnQryBarcodeBin = (Button) this.findViewById(R.id.bnQryChooserBarcodeBin);
        btnExit = (Button) this.findViewById(R.id.bnExitActQryChooser);
//        btnQryBin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ButtonClicked(view);
//            }
//        });
//        btnQryBarcode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ButtonClicked(view);
//            }
//        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnQryBarcodeBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
    }

    private void ButtonClicked(View view) {
        if (view == btnSearch) {
            //NAV_INSTRUCTION = R.integer.ACTION_BARCODEQUERY;
            if (!deviceID.isEmpty()) {
                if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
                    Intent i = new Intent(ActQueryChooser.this,
                            com.proper.warehousetools.binmove.ui.speedata_mt02.ActQueryScan.class);
                    //i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, RESULT_OK);
                }
                if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
                    Intent i = new Intent(ActQueryChooser.this,
                            com.proper.warehousetools.binmove.ui.chainway_c4000.ActSearchScan.class);
                    //i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, REQUEST_BARCODE);
                }
            } else {
                //prompt deviceID has not been identified
                appContext.playSound(2);
                Vibrator vib = (Vibrator) ActQueryChooser.this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                vib.vibrate(2000);
                String mMsg = "User not Authenticated \nPlease login";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryChooser.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
        } else if (view == btnQryBarcodeBin) {
            NAV_INSTRUCTION = R.integer.ACTION_BARCODE_BINQUERY;
            if (!deviceID.isEmpty()) {
                if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
                    Intent i = new Intent(ActQueryChooser.this,
                            com.proper.warehousetools.binmove.ui.speedata_mt02.ActQueryScan.class);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, RESULT_OK);
                }
                if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
                    Intent i = new Intent(ActQueryChooser.this, com.proper.warehousetools.binmove.ui.chainway_c4000.ActQueryScan.class);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, REQUEST_BARCODE_BIN);
                }
            } else {
                //prompt deviceID has not been identified
                appContext.playSound(2);
                Vibrator vib = (Vibrator) ActQueryChooser.this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                vib.vibrate(2000);
                String mMsg = "User not Authenticated \nPlease login";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryChooser.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
        } else if (view == btnExit) {
            Intent i = new Intent();
            setResult(RESULT_OK, i);
            this.finish();
        } else {
            throw new NullPointerException("Well Done! You have triggered a button that doesn't exist in this reality");
        }
//        else if (view == btnQryBin) {
//            NAV_INSTRUCTION = R.integer.ACTION_BINQUERY;
//            if (!deviceID.isEmpty()) {
//                if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
//                    Intent i = new Intent(ActQueryChooser.this,
//                            com.proper.warehousetools.binmove.ui.speedata_mt02.ActQueryScan.class);
//                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
//                    startActivityForResult(i, RESULT_OK);
//                }
//                if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
////                    Intent i = new Intent(ActQueryChooser.this,
////                            com.proper.warehousetools.binmove.ui.chainway_c4000.ActQueryScan.class);
//                    Intent i = new Intent(ActQueryChooser.this,
//                            com.proper.warehousetools.binmove.ui.chainway_c4000.ActSearchScan.class);
//                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
//                    startActivityForResult(i, REQUEST_BINCODE);
//                }
//            } else {
//                //prompt deviceID has not been identified
//                appContext.playSound(2);
//                Vibrator vib = (Vibrator) ActQueryChooser.this.getSystemService(Context.VIBRATOR_SERVICE);
//                // Vibrate for 500 milliseconds
//                vib.vibrate(2000);
//                String mMsg = "User not Authenticated \nPlease login";
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryChooser.this);
//                builder.setMessage(mMsg)
//                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                //do nothing
//                            }
//                        });
//                builder.show();
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation animSlideInTop = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        screen.startAnimation(animSlideInTop);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_open_scale,R.anim.activity_close_translate);
    }
}