package com.proper.warehousetools.binmove.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ActChooser extends PlainActivity {
    private int screenSize;
    private Button btnSingleMove;
    private Button btnBinMove;
    private Button btnExit;
    private Button btnQueries;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_chooser);
//        if (screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL) {
//            getSupportActionBar().setLogo(R.drawable.ic_launcher);
//            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_button_palebrown));
//            getSupportActionBar().setTitle("Welcome " + currentUser.getUserFirstName());
//        }

        //this.setTitle("Welcome " + currentUser.getUserFirstName());
        //setupControls();

        btnSingleMove = (Button) this.findViewById(R.id.bnSingleMove);
        btnBinMove = (Button) this.findViewById(R.id.bnBinMove);
        btnQueries = (Button) this.findViewById(R.id.bnQueries);
        btnExit = (Button) this.findViewById(R.id.bnExitActChooser);
        btnSingleMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClicked(view);
            }
        });
        btnBinMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClicked(view);
            }
        });
        btnQueries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClicked(view);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClicked(view);
            }
        });

        if (screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL) {
            Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);

            btnSingleMove.startAnimation(animFadeIn);
            btnBinMove.startAnimation(animFadeIn);
            btnQueries.startAnimation(animFadeIn);
            btnExit.startAnimation(animFadeIn);
        }
    }

    private void OnButtonClicked(View v) {
        switch(v.getId()) {
            case R.id.bnBinMove:
                if (currentUser != null) {
                    Intent frmMoveChooser = new Intent(ActChooser.this, ActMoveChooser.class);
                    frmMoveChooser.putExtra("INSTRUCTION", 1);
                    startActivityForResult(frmMoveChooser, 1);
                } else {
                    appContext.playSound(2);
                    Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);  // Vibrate for 500 milliseconds
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActChooser.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
                break;
            case R.id.bnSingleMove:
                if (currentUser != null) {
                    if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
                        //Intent frmSingle = new Intent(ActChooser.this, com.proper.warehousetools.binmove.ui.speedata_mt02.ActSingleMain.class);
                        Intent frmSingle = new Intent(ActChooser.this, com.proper.warehousetools.binmove.ui.speedata_mt02.ActSingleMove.class);
                        frmSingle.putExtra("INSTRUCTION", 0);
                        startActivityForResult(frmSingle, 0);
                    }
                    if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
                        Intent frmSingle = new Intent(ActChooser.this, com.proper.warehousetools.binmove.ui.chainway_c4000.ActBinProductMain.class);
                        frmSingle.putExtra("INSTRUCTION", 0);
                        startActivityForResult(frmSingle, 0);
                    }

                } else {
                    appContext.playSound(2);
                    Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);  // Vibrate for 500 milliseconds
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActChooser.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
                break;
            case R.id.bnExitActChooser:
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                finish();
                //overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
                break;
            case R.id.bnQueries:
                if (currentUser != null) {
                    Intent frmQueryChooser = new Intent(ActChooser.this, ActQueryChooser.class);
                    frmQueryChooser.putExtra("INSTRUCTION", 0);
                    startActivityForResult(frmQueryChooser, RESULT_OK);
                } else {
                    appContext.playSound(2);
                    Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    vib.vibrate(2000);
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActChooser.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
                break;
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        onDestroy();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (currentUser == null) {
            currentUser = authenticator.getCurrentUser();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL) {
            Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
            btnSingleMove.startAnimation(animFadeIn);
            btnBinMove.startAnimation(animFadeIn);
            btnQueries.startAnimation(animFadeIn);
            btnExit.startAnimation(animFadeIn);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL) {
            overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
        }
    }

    @Override
    protected void onDestroy() {
//        try {
//            authenticator.logOffUser();
//            //soundPool.release();
//            overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
//        } catch (Exception e) {
//            e.printStackTrace();
//            today = new java.sql.Timestamp(utilDate.getTime());
//            LogEntry log = new LogEntry(1L, ApplicationID, "ActChooser - Attempting Logout - onDestroy", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
//            logger.log(log);
//        }
        super.onDestroy();
        if (screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL) {
            overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
        }
//        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
//        android.os.Process.killProcess(android.os.Process.myPid()); //kill it! never returns to login screen
    }
}