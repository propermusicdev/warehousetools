package com.proper.warehousetools;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import com.proper.warehousetools.goodsin.ui.ActGoodsInManager;

/**
 * Created by Lebel on 30/10/2014.
 */
public class ActMainLegacy extends ListActivity {
    protected AppContext appContext;
    protected int screenSize;
    protected String deviceID = "";
    protected String deviceIMEI = "";
    protected java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    protected java.sql.Timestamp today = null;
    protected DeviceUtils device = null;
    protected HttpMessageResolver resolver = null;
    protected UserAuthenticator authenticator = null;
    protected UserLoginResponse currentUser = null;
    protected LogHelper logger = new LogHelper();
    protected com.proper.messagequeue.Message thisMessage = null;
    protected MockClass testResolver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appContext = (AppContext) getApplication();
        screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        authenticator = new UserAuthenticator(this);
        device = new DeviceUtils(this);
        logger = new LogHelper();
        thisMessage = new com.proper.messagequeue.Message();
        deviceID = device.getDeviceID();
        deviceIMEI = device.getIMEI();
        currentUser = authenticator.getCurrentUser();
        resolver = new HttpMessageResolver(appContext);
        testResolver = new MockClass();
        String[] modules = getResources().getStringArray(R.array.defaultname);
        LayoutInflater inflater = (LayoutInflater) ActMainLegacy.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1,modules);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        Log.i("MY", "arg2=" + pos);

        //lstModule.get(pos).toActivity(ActMainLegacy.this);

        //Navigate to chosen activity
        Intent i = null;
        switch (pos) {
            case 0:
                //Navigate to BinMove entry point
                if (screenSize != Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
                    String mMsg = "Coming Soon.\nPlease try again later";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActMainLegacy.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                } else {
                    i =  new Intent(ActMainLegacy.this, com.proper.warehousetools.binmove.ui.ActChooser.class);
                    startActivityForResult(i, RESULT_FIRST_USER);
                }
                break;
            case 3:
                // REPLEN
                if (screenSize != Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                    i = new Intent(ActMainLegacy.this, com.proper.warehousetools.replen.ActReplenChooser.class);
                    startActivityForResult(i, RESULT_FIRST_USER);
                } else {
                    String mMsg = "Coming Soon.\nPlease try again later";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActMainLegacy.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
                break;
            case 1:
                //GOODS_IN
                String toastMsg;
                switch(screenSize) {
                    case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                        toastMsg = "Extra Large screen";
                        i = new Intent(ActMainLegacy.this, ActGoodsInManager.class);
                        startActivityForResult(i, RESULT_FIRST_USER);
                        break;
                    case Configuration.SCREENLAYOUT_SIZE_LARGE:
                        toastMsg = "Large screen";
                        String mMsgx = "Device Incompatible\nPlease try the tablet instead";
                        AlertDialog.Builder builderx = new AlertDialog.Builder(ActMainLegacy.this);
                        builderx.setMessage(mMsgx)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do nothing
                                    }
                                });
                        builderx.show();
                        break;
                    case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                        toastMsg = "Normal screen";
                        String mMsg = "Device Incompatible\nPlease try the tablet instead";
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActMainLegacy.this);
                        builder.setMessage(mMsg)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do nothing
                                    }
                                });
                        builder.show();
                        break;
                    case Configuration.SCREENLAYOUT_SIZE_SMALL:
                        toastMsg = "Small screen";
                        String mMsg1 = "Device Incompatible\nPlease try the tablet instead";
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActMainLegacy.this);
                        builder1.setMessage(mMsg1)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do nothing
                                    }
                                });
                        builder1.show();
                        break;
                    default:
                        toastMsg = "Screen size is neither large, normal or small";
                }
                Toast.makeText(ActMainLegacy.this, toastMsg, Toast.LENGTH_LONG).show();
//                        if (deviceID.equalsIgnoreCase(getResources().getString(R.string.LargeDevice))) {
//                            int currentVersion = Build.VERSION.SDK_INT;
//                            if (currentVersion > 15) {
//                                i = new Intent(ActMainLegacy.this, com.proper.warehousetools.goodsin.ui.ActGoodsInManager.class);
//                                startActivityForResult(i, RESULT_FIRST_USER);
//                            } else {
//                                String mMsg = "Device Incompatible\nPlease try the tablet instead";
//                                AlertDialog.Builder builder = new AlertDialog.Builder(ActMainLegacy.this);
//                                builder.setMessage(mMsg)
//                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int id) {
//                                                //do nothing
//                                            }
//                                        });
//                                builder.show();
//                            }
//                            break;
//                        }
                break;
            default:
                //Navigate to Goods-In,... entry point
                String mMsg = "Coming Soon.\nPlease try again later";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActMainLegacy.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
                break;
        }

    }
}