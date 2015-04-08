package com.proper.utils;

import com.proper.data.binmove.Bin;

import java.util.Comparator;

/**
 * Created by Lebel on 11/09/2014.
 * This Comparator class will help sort Bin in a list in a ASCENDING order (smallest to highest or 1,2,3,4 etc...)
 * Since we want the lowest quantity bin on top
 */
public class BinQuantitySorted implements Comparator<Bin> {
    @Override
    public int compare(Bin lhs, Bin rhs) {
        int ret = 0;

        if (lhs.getQty() < rhs.getQty()) {
            ret = 1;
        }else if(lhs.getQty() > rhs.getQty() ) {
            ret = -1;
        }else if (lhs.getQty() == rhs.getQty()) {
            ret = 0;
        }
        return ret;
    }
}
