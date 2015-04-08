package com.proper.data.core;

/**
 * Created by Lebel on 11/12/2014.
 */
public interface IReplenLineCommunicator extends ICommunicator {
    //public void onDialogMessage(int buttonClicked);

    @Override
    void onDialogMessage_ICommunicator(int buttonClicked);
}
