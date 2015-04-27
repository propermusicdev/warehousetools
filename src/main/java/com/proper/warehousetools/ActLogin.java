package com.proper.warehousetools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.proper.data.diagnostics.LogEntry;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.messagequeue.Message;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ActLogin extends Activity {
    private AppContext appContext;
    private SharedPreferences prefs = null;
    private int screenSize = 0;
    private String deviceID = "";
    private String deviceIMEI = "";
    private static final String ApplicationID = "WarehouseTools";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private Button btnLogin;
    private EditText txtInitials, txtPin;
    private TextView txtConnReport;
    private ImageView logo;
    private LinearLayout lytAdminControl;
    private RadioGroup rgRadioGroup;
    private RadioButton rdLive, rdTest;
    private UserAuthenticator authenticator = null;
    private UserLoginTask loginTask;
    private UserLoginResponse currentUser;
    private String currentUserToken = "", initials = "", pin = "";
    private int loginAttempt = 0, selectedConfig = R.integer.CONFIG_TESTSERVER;
    //private ArrayAdapter<Contact> adapter;
    private LogHelper logger = new LogHelper();
    private DeviceUtils device = null;
    private HttpMessageResolver httpResolver = null;
    protected MockClass testResolver;

    public int getSelectedConfig() {
        return selectedConfig;
    }

    public void setSelectedConfig(int selectedConfig) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.preference_configuration), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("Configuration", selectedConfig);
        editor.commit();
        this.selectedConfig = selectedConfig;
        updateConnectionStringText();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.lyt_login);
        appContext = (AppContext) getApplication();
        authenticator = new UserAuthenticator(this);    //set-up simulated authentication service
        httpResolver = new HttpMessageResolver(appContext);
        device = new DeviceUtils(this);
        testResolver = new MockClass();
        deviceIMEI = device.getIMEI();
        deviceID = device.getDeviceID();

        View screen = null;

        if (deviceID.equalsIgnoreCase(getResources().getString(R.string.SmallDevice))) {
            screen = (ScrollView) this.findViewById(R.id.screenLogin);
        }else if (deviceID.equalsIgnoreCase(getResources().getString(R.string.LargeDevice))) {
            screen = (LinearLayout) this.findViewById(R.id.screenLogin);
        } else {
            //Added for Testing support
            screen = (LinearLayout) this.findViewById(R.id.screenLogin);
        }

        txtInitials = (EditText) this.findViewById(R.id.etxtLoginInitials);
        txtPin = (EditText) this.findViewById(R.id.etxtLoginPin);
        btnLogin = (Button) this.findViewById(R.id.bnLoginProceedOne);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });
        txtInitials.addTextChangedListener(new TextChanged(this.txtInitials));
        txtPin.addTextChangedListener(new TextChanged(this.txtPin));
        TextView lblLoginTitle = (TextView) this.findViewById(R.id.lblLoginTitle);
        logo = (ImageView) this.findViewById(R.id.imgLogo);
        lytAdminControl = (LinearLayout) this.findViewById(R.id.lytLoginAdminControl);
        rgRadioGroup = (RadioGroup) this.findViewById(R.id.rgLoginAdmin);
        rdLive = (RadioButton) this.findViewById(R.id.rdLoginAdminControlLive);
        rdTest = (RadioButton) this.findViewById(R.id.rdLoginAdminControlTest);
        rgRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroupButtonChanged(group, checkedId);
            }
        });
        TextView lblConnReport = (TextView) this.findViewById(R.id.lblLoginConnectionReport);
        txtConnReport = (TextView) this.findViewById(R.id.txtvLoginConnectionReport);
        updateConnectionStringText();
        //opening transition animations (opening, closing)
        //overridePendingTransition(R.anim.activity_open_translate,R.anim.activity_close_scale);

        if (screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL) {
            Animation animRightIn = AnimationUtils.loadAnimation(this, R.anim.right_in);
            Animation animRotateIn_icon = AnimationUtils.loadAnimation(this, R.anim.rotate);
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(600); //You can manage the blinking time with this parameter
            anim.setStartOffset(20);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);

            screen.startAnimation(animRightIn);
            txtInitials.startAnimation(animRightIn);
            txtPin.startAnimation(animRightIn);
            btnLogin.startAnimation(animRightIn);
            lblLoginTitle.startAnimation(animRightIn);
            logo.startAnimation(animRotateIn_icon);
            txtConnReport.startAnimation(anim);
        }
        lytAdminControl.setVisibility(View.GONE);   //For Admin purpose Only - not for production
    }

    private void updateConnectionStringText() {
        String configString = httpResolver.getDefaultConfigName();
        if (configString.equalsIgnoreCase("CONFIG_TESTSERVER")) {
            txtConnReport.setText("TEST SERVER");
            txtConnReport.setTextColor(Color.RED);
        }
        if (configString.equalsIgnoreCase("CONFIG_LIVESERVER")) {
            txtConnReport.setText("LIVE SERVER");
            txtConnReport.setTextColor(Color.GREEN);
        }
        if (configString.equalsIgnoreCase("CONFIG_LIVESERVER_EXTERNAL")) {
            txtConnReport.setText("LIVE SERVER:EXT");
            txtConnReport.setTextColor(Color.GREEN);
        }
        if (configString.equalsIgnoreCase("CONFIG_TESTSERVER_EXTERNAL")) {
            txtConnReport.setText("TEST SERVER:EXT");
            txtConnReport.setTextColor(Color.RED);
        }
    }

    private void effectRadioButton(int config) {
        switch (config) {
            case R.integer.CONFIG_LIVESERVER_EXTERNAL:
                rdLive.performClick();
                break;
            case R.integer.CONFIG_TESTSERVER:
                rdTest.performClick();
                break;
        }
    }

    private void radioGroupButtonChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rdLoginAdminControlLive:
                setSelectedConfig(R.integer.CONFIG_LIVESERVER_EXTERNAL);
                break;
            case R.id.rdLoginAdminControlTest:
                setSelectedConfig(R.integer.CONFIG_TESTSERVER);
                break;
        }
    }

    private void saveAuthentication() {
        // Save to the sharedPreference
        prefs = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ApplicationID", ApplicationID);
        editor.putString("IMEI", deviceIMEI);
        editor.putString("Device", deviceID);
        editor.putString("UserToken", currentUserToken);
        editor.commit();
    }

    private void removeAuthentication() {
        // Save to the sharedPreference
        prefs = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("UserToken");
        editor.remove("IMEI");
        editor.remove("Device");
        editor.commit();
    }

    private void logOn(String initials, String pin) {
        //boolean success = false;
        if (loginTask != null) {
            loginTask.cancel(true);
            loginTask = null;
        }
        if (loginAttempt == 0) {
            currentUserToken = "";
        }
        if (currentUserToken == null) currentUserToken = "";
        if (currentUserToken.isEmpty() && !initials.isEmpty() && !pin.isEmpty()) {
            //if (!initials.isEmpty() && !pin.isEmpty()) {
            hideSoftKeyboard(ActLogin.this);  //Hide the default software Keyboard
            loginTask = new UserLoginTask();
            loginTask.execute(initials, pin);
            //if (currentUser != null) success = true;
        } else {
            ActLogin.this.setTitle(getResources().getString(R.string.currentUser));
            String msgMissingInitials = "Login Error - Missing Initials\nPlease try again";
            String msgMissingPin = "Login Error - Missing Pin\nPlease try again";
            String msgMissingBoth = "Login Error - No Details Entered\nPlease enter initials and pin";

            Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            AlertDialog.Builder builder = new AlertDialog.Builder(ActLogin.this);
            if (!initials.isEmpty() && pin.isEmpty()) {
                //if initials is entered but no pin
                builder.setMessage(msgMissingPin)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
            } else if (initials.isEmpty() && !pin.isEmpty()) {
                //if pin is entered no initials
                builder.setMessage(msgMissingInitials)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
            } else {
                //Both missing
                builder.setMessage(msgMissingBoth)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
            }
            vib.vibrate(2000);  // Vibrate for 500 milliseconds
            builder.show();
        }
        //return success;
    }

    private void logOut() {
        if (currentUser != null) {
            currentUser = null;
            currentUserToken = "";
            txtInitials.setText("");
            txtPin.setText("");
            initials = "";
            pin = "";
            removeAuthentication();
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && activity.getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void ButtonClicked(View v) {
        //do
        if (v == btnLogin) {
            logOn(initials, pin);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 666) {
            logOut();
        } else {
            try {
                if (authenticator.getCurrentUser() != null) {
                    if (authenticator.isAuthenticated()) {
                        logOut();
                    }
                } else {
                    logOut();
                }
                switch (resultCode) {
                    case RESULT_OK:
                        logOut();
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActLogin - Attempting Logout - onActivityResult", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
//            logger.log(log);
            }
            this.finish();
        }
    }

    @Override
    protected void onPause() {
        if (screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL) {
            overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL) {
            Animation animRotateIn_big = AnimationUtils.loadAnimation(this, R.anim.rotate);
            logo.startAnimation(animRotateIn_big);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            logOut();
        } catch (Exception e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActLogin - Attempting Logout - onDestroy", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            //logger.log(log);
        }
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid()); //kill it!
    }

    class TextChanged implements TextWatcher {
        private EditText thisView;

        private TextChanged(EditText view) {
            this.thisView = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (thisView == txtInitials) {
                initials = editable.toString().toUpperCase(Locale.getDefault());
            }
            if (thisView == txtPin) {
                pin = editable.toString();
            }
        }
    }

    private class UserLoginTask extends AsyncTask<String, Void, UserLoginResponse> {
        private ProgressDialog lDialog;

        @Override
        protected void onPreExecute() {
            lDialog = new ProgressDialog(ActLogin.this);
            CharSequence message = "Working hard...checking credentials...";
            CharSequence title = "Please Wait";
            lDialog.setCancelable(true);
            lDialog.setCanceledOnTouchOutside(false);
            lDialog.setMessage(message);
            lDialog.setTitle(title);
            lDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            lDialog.show();
        }

        @Override
        protected UserLoginResponse doInBackground(String... input) {

            try {
                String initials = input[0];
                String pin = input[1];
                String msg = String.format("{\"UserInitials\":\"%s\", \"UserPin\":\"%s\"}", initials, pin);
                ObjectMapper mapper = new ObjectMapper();
                Message thisMessage = new Message();
                today = new java.sql.Timestamp(utilDate.getTime());
                thisMessage.setSource(deviceIMEI);
                thisMessage.setMessageType("UserLogin");
                thisMessage.setIncomingStatus(1); //default value
                thisMessage.setIncomingMessage(msg);
                thisMessage.setOutgoingStatus(0);   //default value
                thisMessage.setOutgoingMessage("");
                thisMessage.setInsertedTimeStamp(today);
                thisMessage.setTTL(100);    //default value

                //currentUserToken = testResolver.resolveLogIn();       // test only
                //currentUserToken = httpResolver.resolveMessageQueue(thisMessage);
                currentUserToken = httpResolver.resolveMessageQuery(thisMessage);
                currentUser = mapper.readValue(currentUserToken, UserLoginResponse.class);
            } catch (Exception e) {
                if (lDialog != null && lDialog.isShowing()) lDialog.dismiss();
                if (!loginTask.isCancelled()) loginTask.cancel(true);
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActLogin - UserLoginTask - doInBackground", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
//                logger.log(log);
                if (!loginTask.isCancelled()) loginTask.cancel(true);
            }
            return currentUser;
        }

        @Override
        protected void onPostExecute(UserLoginResponse userLoginResponse) {
            if (lDialog != null && lDialog.isShowing()) lDialog.dismiss();
            loginAttempt++;
            if (currentUser != null) {
                loginAttempt = 0;
                saveAuthentication();
                ActLogin.this.setTitle(String.format("Hi %s", currentUser.getUserFirstName()));
                Intent i;
                switch (screenSize){
                    case Configuration.SCREENLAYOUT_SIZE_MASK:
                        i = new Intent(ActLogin.this, ActMainLegacy.class);
                        break;
                    case Configuration.SCREENLAYOUT_SIZE_SMALL:
                        i = new Intent(ActLogin.this, ActMainLegacy.class);
                        break;
                    default:
                        if (deviceID.equalsIgnoreCase(getResources().getString(R.string.SmallDevice))) {
                            i = new Intent(ActLogin.this, ActMainLegacy.class);
                        } else {
                            i = new Intent(ActLogin.this, ActMain.class);
                        }
                        break;
                }
                startActivityForResult(i, RESULT_FIRST_USER);
            } else {
                if (loginAttempt <= 2 && !currentUserToken.contains("Failure")) {
                    //boolean success = logOn(initials, pin);    // second automated attempt to counter that weird server anomaly
                    logOn(initials, pin);
                } else {
                    // Refresh Activity to default
                    currentUserToken = "";

                    ActLogin.this.setTitle(getResources().getString(R.string.currentUser));
                    appContext.playSound(2);
                    Vibrator vib = (Vibrator) ActLogin.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);  // Vibrate for 500 milliseconds
                    String mMsg = "Login Error\nYour Initials/Pin combination is incorrect. Please try again";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActLogin.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            loginAttempt = 0;
            loginTask = null;
            if (lDialog != null && lDialog.isShowing()) lDialog.dismiss();
            //refreshActivity();
            //show the activity with user not authenticated - default
        }
    }
}
