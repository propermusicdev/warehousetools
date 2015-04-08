package com.proper.warehousetools.replen.ui.chainway_C4000;

import android.app.Activity;
import android.os.Bundle;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 13/01/2015.
 * NEw Line : Insert a new move line {SrcBin, DstBin, Qty}
 * passed Activity Properties: MovelistId, UserId, USerCode, PRoductId, srcBin, DstBin, Qty
 */
public class ActReplenNewLine extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replen_fgm_newline);
    }
}