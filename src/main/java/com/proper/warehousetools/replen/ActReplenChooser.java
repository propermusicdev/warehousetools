package com.proper.warehousetools.replen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenManageWork;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenSelectBin;

/**
 * Created by Lebel on 01/09/2014.
 */
public class ActReplenChooser extends PlainActivity {
    private TextView txtIntro;
    private Button btnNew;
    private Button btnContinue;
    private Button btnExit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replenchooser);
        txtIntro = (TextView) this.findViewById(R.id.txtvRPChooserIntro);
        btnNew = (Button) this.findViewById(R.id.bnRPChooserNew);
        btnContinue = (Button) this.findViewById(R.id.bnRPChooserContinue);
        btnExit = (Button) this.findViewById(R.id.bnExitActReplenChooser);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });

        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);

        txtIntro.startAnimation(animFadeIn);
        btnNew.startAnimation(animFadeIn);
        btnContinue.startAnimation(animFadeIn);
        btnExit.startAnimation(animFadeIn);
    }

    private void ButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.bnRPChooserNew:
                Intent intent = null;
                if (deviceID.equalsIgnoreCase(getResources().getString(R.string.SmallDevice))) {
                    intent = new Intent(this, com.proper.warehousetools.replen.ui.speedata_mt02.ActReplenScan.class);
                }
                if (deviceID.equalsIgnoreCase(getResources().getString(R.string.LargeDevice))) {
                    intent = new Intent(this, ActReplenSelectBin.class);
                    //intent = new Intent(this, com.proper.warehousetools.replen.ui.chainway_C4000.zzActReplenManageConfig.class);   // Couldn't get it to work
                }
                startActivityForResult(intent, RESULT_OK);
                break;
            case R.id.bnRPChooserContinue:
                // TODO - Find all outstanding (not Processed) move lists for this user on this day
                //Intent resume = new Intent(ActReplenChooser.this, ActReplenResume.class);
                Intent resume = new Intent(ActReplenChooser.this, ActReplenManageWork.class);
                startActivityForResult(resume, RESULT_OK);
                break;
            case R.id.bnExitActReplenChooser:
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                this.finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (currentUser == null) {
            currentUser = authenticator.getCurrentUser();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        txtIntro.startAnimation(animFadeIn);
        btnNew.startAnimation(animFadeIn);
        btnContinue.startAnimation(animFadeIn);
        btnExit.startAnimation(animFadeIn);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_open_scale,R.anim.activity_close_translate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
    }
}