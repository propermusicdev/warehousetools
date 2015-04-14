package com.proper.warehousetools.goodsin.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 01/10/2014.
 */
public class ActGoodsInManager extends Activity {
    private TextView txtIntro;
    private Button btnNew;
    private Button btnExit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_goodsinmanager);

        txtIntro = (TextView) this.findViewById(R.id.etxtGoodsinManagerIntro);
        btnNew = (Button) this.findViewById(R.id.bnGoodsinManagerIncomingGoods);
        btnExit = (Button) this.findViewById(R.id.bnExitActGoodsinManager);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
    }

    private void buttonClicked(View v) {
        switch (v.getId()) {
            case R.id.bnExitActGoodsinManager:
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                this.finish();
                break;
            case R.id.bnGoodsinManagerIncomingGoods:
                // navigate to new good input
                //Intent intent = new Intent(this, zzActGoodsInInfoDisplay.class);    // test impl
                Intent intent = new Intent(this, ActGoodsInBoardScan.class);
                startActivityForResult(intent, 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 666) {
            Intent i = new Intent();
            setResult(666, i);
            finish();
        }
    }
}