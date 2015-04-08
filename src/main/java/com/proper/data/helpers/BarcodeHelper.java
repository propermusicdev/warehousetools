package com.proper.data.helpers;

/**
 * Created by Lebel on 19/05/2014.
 */
public class BarcodeHelper {
    public static String formatBarcode(String barcode) {
        int length = barcode.length();
        String refined = "";
        switch (length) {
            case 13:
                //do nothing
                refined = barcode;
                break;
            case 12:
                // stuff zero at the beginning
                refined = "0" + barcode;
                break;
            case 14:
                // trim the leading zero
                refined = barcode.substring(1);
                break;
        }
        return refined;
    }
}
