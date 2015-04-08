package com.proper.data.binmove.enums;

/**
 * Created by Lebel on 25/02/14.
 */
public enum KeyScan {
    DEFAULT_KEY(111),
    F1_KEY(112),
    F2_KEY(113),
    F3_KEY(114),
    YELLOW_KEY(115);

    private int key;

    KeyScan(int keyEntered) {
        this.key = keyEntered;
    }

   // public int getKey() {
        //return this.key;
    //}
}
