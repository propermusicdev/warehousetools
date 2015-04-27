package com.proper.warehousetools.goodsin.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.goodsin.*;
import com.proper.data.helpers.MediaScannerHelper;
import com.proper.mail.GMailSender;
import com.proper.mail.MailHelper;
import com.proper.mail.MailItem;
import com.proper.utils.PhotoHelper;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by Lebel on 10/10/2014.
 */
public class ActGoodsInReceive extends PlainActivity {
    private static final int TAKE_PICTURE_REQUEST_CODE = 1010;
    private static final int SHOW_GALLERY_REQUEST_CODE = 1011;
    private Button btnPhoto, btnSend, btnExit;
    private ImageView imgPhoto;
    private TextView txtGoodsInID, txtNumOfPallet, txtNumOfBoxes, txtStockHeader, txtOrderNumber,
            txtStatus, txtUnitsOrder, txtUser, txtLines, txtCourier, txtSupplier;
    private Uri mPhotoPathUri;

    private BoardScanResult boardScanResult;
    private GoodsInImage img = null;
    private UploadPhotoAsync uploadAsyncTask;
    private int pictureCount = 0;
    private List<Uri> currentGallery = null;
    private List<GoodsInThumbnail> thumbnails = null;       //From the gallery view in ActGoodsInPhotoDisplay

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_goodsin_receive);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            throw new RuntimeException("ActGoodsInReceive - Extras cannot be null");
        }else {
            boardScanResult = (BoardScanResult) extras.getSerializable("BOARDSCAN_EXTRA");
            if (boardScanResult == null) {
                throw new NullPointerException("ActGoodsInReceive - ScanResult Extras cannot be null");
            }
        }
        imgPhoto = (ImageView) this.findViewById(R.id.imgGoodsinReceived);
        txtGoodsInID = (TextView) this.findViewById(R.id.txtGoodsinReceiveGoodsInID);
        txtNumOfPallet = (TextView) this.findViewById(R.id.txtGoodsinReceiveNumOfPallet);
        txtNumOfBoxes = (TextView) this.findViewById(R.id.txtGoodsinReceiveNumOfBoxes);
        txtStockHeader = (TextView) this.findViewById(R.id.txtGoodsinReceiveStockHeaderID);
        txtOrderNumber = (TextView) this.findViewById(R.id.txtGoodsinReceiveOrderNumber);
        txtStatus = (TextView) this.findViewById(R.id.txtGoodsinReceiveStatus);
        txtUnitsOrder = (TextView) this.findViewById(R.id.txtGoodsinReceiveUnitsOrdered);
        txtUser = (TextView) this.findViewById(R.id.txtGoodsinReceiveAssignedUser);
        txtLines = (TextView) this.findViewById(R.id.txtGoodsinReceiveNumOfLines);
        txtCourier = (TextView) this.findViewById(R.id.txtGoodsinReceiveCourier);
        txtSupplier = (TextView) this.findViewById(R.id.txtGoodsinReceiveSupplier);
        btnPhoto = (Button) this.findViewById(R.id.bnGoodsinReceiveTakePhoto);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnSend = (Button) this.findViewById(R.id.bnGoodsinReceiveSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnExit = (Button) this.findViewById(R.id.bnExitActGoodsinReceive);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });

        //Display Values
        txtGoodsInID.setText(String.format("%s", boardScanResult.getDelivery().getGoodsInId()));
        txtNumOfPallet.setText(String.format("%s", boardScanResult.getDelivery().getNumberOfPallets()));
        txtNumOfBoxes.setText(String.format("%s", boardScanResult.getDelivery().getNumberOfBoxes()));
        txtStockHeader.setText(String.format("%s", boardScanResult.getGoodsIn().getStockHeaderId()));
        txtOrderNumber.setText(String.format("%s", boardScanResult.getGoodsIn().getOrderNumber()));
        txtStatus.setText(String.format("%s", boardScanResult.getGoodsIn().getStatus()));
        txtUnitsOrder.setText(String.format("%s", boardScanResult.getGoodsIn().getUnitsOrdered()));
        txtUser.setText(boardScanResult.getGoodsIn().getAssignedUserName());
        txtLines.setText(String.format("%s", boardScanResult.getGoodsIn().getLines()));
        txtCourier.setText(boardScanResult.getDelivery().getCourier());
        txtSupplier.setText(boardScanResult.getDelivery().getSupplier());       //Changed this bcoz it caused folder name with null
        currentGallery = new ArrayList<Uri>();
    }

    private void buttonClicked(View v) {
        if (v == btnExit) {
            if (mPhotoPathUri != null) {
                //check if the files are sent
                File dir = new File(mPhotoPathUri.getPath()).getParentFile();
                if (dir.listFiles().length < 1) {
                    //then proceed with the exit
                    Intent i = new Intent();
                    setResult(RESULT_OK, i);
                    this.finish();
                } else {
                    //Prompt that there are outstanding files, if they will be delete prior to exit
                    String mMsg = "You have taken some pictures that are not sent if you exit now then they will be deleted\n" +
                            "Do you really want to exit";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInReceive.this);
                    builder.setMessage(mMsg)
                            .setTitle("Are you sure?")
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do delete those files before exiting
                                    File thisDir = new File(mPhotoPathUri.getPath()).getParentFile();
                                    try {
                                        FileUtils.cleanDirectory(thisDir);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Intent i = new Intent();
                                    setResult(RESULT_OK, i);
                                    ActGoodsInReceive.this.finish();
                                }
                            });
                    builder.show();
                }
            } else {
                // Just exit, since no picture is taken yet
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                ActGoodsInReceive.this.finish();
            }
        }
        if (v == btnPhoto) {
            //Take picture if (pictureCount < 4) {
            if (pictureCount < 8) {
                mPhotoPathUri = PhotoHelper.generateTimeStampPhotoFileUri(boardScanResult.getDelivery().getGoodsInId());

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoPathUri);
                startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE);
            }else {
                //  TODO - Alert that no more pictures allowed
            }
        }
        if (v == btnSend) {
            //Save the photo into our chosen network location
            if (mPhotoPathUri != null && !mPhotoPathUri.toString().isEmpty() && pictureCount > 0) {
                //Already taken Photo
                //File file = new File(mPhotoPathUri.getPath());
                String userDetail = currentUser.getUserFirstName() + currentUser.getUserLastName().substring(0, 1).toUpperCase(Locale.getDefault()) + ".";
                img = new GoodsInImage(boardScanResult.getDelivery().getSupplier(), boardScanResult.getDelivery().getGoodsInId(), 0, boardScanResult.getGoodsIn().getOrderNumber(), boardScanResult.getGoodsIn().getUnitsOrdered(), userDetail, currentGallery);

                if (img != null) {
                    //Send the image through FTP & store location in db
                    uploadAsyncTask = new UploadPhotoAsync();
                    uploadAsyncTask.execute(img);
                }
            } else {
                // Notify unable to send picture
                String mMsg = "Please take a picture as proof of this delivery";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInReceive.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
        }
        if (v == imgPhoto) {
            if (mPhotoPathUri != null) {
                Intent i = new Intent(ActGoodsInReceive.this, ActGoodsInPhotoDisplay.class);
                i.putExtra("DIRECTORY_EXTRA", mPhotoPathUri.getPath());
                i.putExtra("GALLERY_EXTRA", (Serializable) currentGallery);
                startActivityForResult(i, SHOW_GALLERY_REQUEST_CODE);
            }
        }
    }

    protected void handleSendAutomatedEmailAsyncService(String pictureLinks) {
        boolean result = false;
        String msg = String.format("<html><h2>New Goods!</h2>\n" +
                        "<p>----------  This is an automated email notification --------------</p>\n" +
                        "<h4>New incoming goods:</h4><table border=1><tr><td><strong>GoodsIn ID:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Supplier:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Courier:</strong>\n" +
                        "</td><td style=\"text-align:center;\">%s</td></tr><tr><td><strong>HandledBy:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Notes:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Images:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr></table></html>", boardScanResult.getDelivery().getGoodsInId(),
                boardScanResult.getDelivery().getSupplier(),boardScanResult.getDelivery().getCourier(),
                boardScanResult.getGoodsIn().getAssignedUserName(), boardScanResult.getDelivery().getNotes(), pictureLinks);
        String[] TO = {"stockcontrol@propermusicgroup.com"};
        String[] CC = {"erica.day@propermusicgroup.com", "lebel.fuayuku@propermusicgroup.com"};
        String sender = "propermusicdev@propermusicgroup.com";
        String subject = "New Goods-In !!!";
        try {
            MailItem mailItem = new MailItem(TO, sender, subject, msg, true);
            MailHelper mailHelper = new MailHelper();
            mailHelper.sendEmail(mailItem);
        } catch (Exception e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActGoodsInReceive - UploadPhotoAsyncTask - handleSendAutomatedEmailAsync", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
        }
    }

    //REF:
    //http://stackoverflow.com/questions/2020088/sending-email-in-android-using-javamail-api-without-using-the-default-built-in-a/2033124#2033124
    protected void handleSendAutomatedEmailAsync(String pictureLinks) {
        String msg = String.format("<h2>New Goods!</h2>\n" +
                        "<p>----------  This is an automated email notification --------------</p>\n" +
                        "<h4>New incoming goods:</h4><table border=1><tr><td><strong>GoodsIn ID:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Supplier:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Courier:</strong>\n" +
                        "</td><td style=\"text-align:center;\">%s</td></tr><tr><td><strong>HandledBy:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Notes:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Images:</strong></td>\n" +
                        "<td style=\"text-align:center;\">%s</td></tr></table>", boardScanResult.getDelivery().getGoodsInId(),
                boardScanResult.getDelivery().getSupplier(),boardScanResult.getDelivery().getCourier(),
                boardScanResult.getGoodsIn().getAssignedUserName(), boardScanResult.getDelivery().getNotes(), pictureLinks);
        GMailSender sender = new GMailSender("propermusicdev@gmail.com", "propermus1c");
        try {
            sender.sendMail("New Goods-In !!!", msg, "propermusicdev@gmail.com",
                    "lebel.fuayuku@propermusicgroup.com"); //, stockcontrol@propermusicgroup.com, erica.day@propermusicgroup.com");
        } catch (Exception e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActGoodsInReceive - UploadPhotoAsyncTask - handleSendAutomatedEmailAsync", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
        }
    }

    protected void handleSendAutomatedEmail(String pictureLinks) {

        String msg = String.format("<h2>New Goods!</h2>\n" +
                "<p>----------  This is an automated email notification --------------</p>\n" +
                "<h4>New incoming goods:</h4><table border=1><tr><td><strong>GoodsIn ID:</strong></td>\n" +
                "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Supplier:</strong></td>\n" +
                "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Courier:</strong>\n" +
                "</td><td style=\"text-align:center;\">%s</td></tr><tr><td><strong>HandledBy:</strong></td>\n" +
                "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Notes:</strong></td>\n" +
                "<td style=\"text-align:center;\">%s</td></tr><tr><td><strong>Images:</strong></td>\n" +
                "<td style=\"text-align:center;\">%s</td></tr></table>", boardScanResult.getDelivery().getGoodsInId(),
                boardScanResult.getDelivery().getSupplier(),boardScanResult.getDelivery().getCourier(),
                boardScanResult.getGoodsIn().getAssignedUserName(), boardScanResult.getDelivery().getNotes(), pictureLinks);
        String[] TO = {"stockcontrol@propermusicgroup.com"};
        String[] CC = {"erica.day@propermusicgroup.com", "lebel.fuayuku@propermusicgroup.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New Goods-In !!!");
        emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(msg));

        try {
            //startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            startActivityForResult(Intent.createChooser(emailIntent, "Send  mail..."), RESULT_OK);
            //finish();
            Log.i("Success: Email sent...", "ActGoodsInReceive");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ActGoodsInReceive.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleTakePictureResult(int resultCode, Intent resultIntent) {
        if (resultCode == RESULT_OK) {
            String photoPathName = mPhotoPathUri.getPath();
            currentGallery.add(mPhotoPathUri);  //add uri to list
            PhotoHelper.addPhotoToMediaStoreAndDisplayThumbnail(
                    photoPathName, this, imgPhoto);
            pictureCount ++;
        }
        else {
            mPhotoPathUri = null;
            Toast.makeText(this, "User Canceled", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        switch (requestCode) {
            case TAKE_PICTURE_REQUEST_CODE:
                handleTakePictureResult(resultCode, resultIntent);
                break;
            case SHOW_GALLERY_REQUEST_CODE:
                Bundle bundle = resultIntent.getExtras();
                thumbnails = (List<GoodsInThumbnail>) bundle.getSerializable("THUMBNAILS_EXTRA");
                //thumbnails = Arrays.asList((GoodsInThumbnail[]) bundle.getSerializable("THUMBNAILS_EXTRA"));
                break;
        }

    }

    private class UploadPhotoAsync extends AsyncTask<GoodsInImage, Void, AbstractMap.SimpleEntry<Boolean, AbstractMap.SimpleEntry<String, String>>> {
        protected ProgressDialog xDialog;

        @Override
        protected void onPreExecute() {
            //startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActGoodsInReceive.this);
            CharSequence message = "Working hard...contacting webservice...";

            CharSequence title = "Please Wait";
            xDialog.setCancelable(true);
            xDialog.setCanceledOnTouchOutside(false);
            xDialog.setMessage(message);
            xDialog.setTitle(title);
            xDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            xDialog.show();
        }

        @Override
        protected AbstractMap.SimpleEntry<Boolean, AbstractMap.SimpleEntry<String, String>> doInBackground(GoodsInImage... images) {
            AbstractMap.SimpleEntry<Boolean, AbstractMap.SimpleEntry<String, String>> response = null;
            String dir = images[0].getFiles().get(0).getPath();
            File directory = new File(dir);
            //---@@@@@@@@@@@@@@@@@@@  1.  Upload images using FTP       @@@@@@@@@@@@@@@@@@@---//
            AbstractMap.SimpleEntry<Boolean, String> resolved = resolver.uploadImagesFTP(images[0]);
            //AbstractMap.SimpleEntry<Boolean, String> resolved = new AbstractMap.SimpleEntry<Boolean, String>(true, "35624(CLOVELLY RECORDINGS LTD)");  //test
            if (resolved != null) {
                if (resolved.getKey() == true) {
                    try {
                        //---@@@@@@@@@@@@@@@@@@@  2. Update the webservice with the file paths     @@@@@@@@@@@@@@@@@@@---//
                        List<GoodsInImagePathIn> paths = new ArrayList<GoodsInImagePathIn>();
                        for (int i = 0; i < currentGallery.size(); i++) {
                            File file  = new File(currentGallery.get(i).getPath());
                            paths.add(new GoodsInImagePathIn(FilenameUtils.concat(resolved.getValue(), file.getName())));
                        }
                        GoodsInImagesRequest req = new GoodsInImagesRequest(boardScanResult.getDelivery().getGoodsInId(),
                                currentUser.getUserId(), currentUser.getUserCode(), paths);
                        ObjectMapper mapper = new ObjectMapper();
                        String msg = mapper.writeValueAsString(req);
                        today = new java.sql.Timestamp(utilDate.getTime());
                        thisMessage.setSource(deviceIMEI);
                        thisMessage.setMessageType("GoodsInBoardImages");
                        thisMessage.setIncomingStatus(1); //default value
                        thisMessage.setIncomingMessage(msg);
                        thisMessage.setOutgoingStatus(0);   //default value
                        thisMessage.setOutgoingMessage("");
                        thisMessage.setInsertedTimeStamp(today);
                        thisMessage.setTTL(100);    //default value
                        String resp = resolver.resolveMessageQuery(thisMessage);
                        //String resp = testResolver.resolveBoardImage();       //  test
                        //---@@@@@@@@@@@@@@@@@@@  3.  Then send an automated email     @@@@@@@@@@@@@@@@@@@---//
//                    String picLinkString = "";
//                    for (int i = 0; i < currentGallery.size(); i++) {
//                        File file = new File(currentGallery.get(i).getPath());
//                        picLinkString = picLinkString + String.format("<li><a href=\"ftp://properuk.net/GoodsInImages/%s/%s\">%s</a></li>", resolved.getValue(), file.getName(), file.getName());
//                    }
//                    handleSendAutomatedEmailAsyncService(picLinkString);
                        //---@@@@@@@@@@@@@@@@@@@  4.  Finally, Delete files form device        @@@@@@@@@@@@@@@@@@@---//
                        if (directory.getParentFile().isDirectory()) {
                            FileUtils.cleanDirectory(directory.getParentFile());
                        } else {
                            //build an absolute path manually
                            String direct = directory.getAbsolutePath();
                            File loc = new File(direct);
                            FileUtils.cleanDirectory(loc);
                        }
                        response = new AbstractMap.SimpleEntry<Boolean, AbstractMap.SimpleEntry<String, String>>(resolved.getKey(), new AbstractMap.SimpleEntry<String, String>(resolved.getValue(), resp));
                    } catch (IOException e) {
                        e.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActGoodsInReceive - UploadPhotoAsyncTask", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                }
            } else  {
                //
                try {
                    if (directory.getParentFile().isDirectory()) {
                        FileUtils.cleanDirectory(directory.getParentFile());
                    } else {
                        //build an absolute path manually
                        String direct = directory.getAbsolutePath();
                        File loc = new File(direct);
                        FileUtils.cleanDirectory(loc);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActGoodsInReceive - UploadPhotoAsyncTask", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                    logger.log(log);
                }
            }
            //Send broadcast to scan media again because we just empty directory
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));  //not advised causes permission exception
            //MediaScannerHelper scannerHelper = new MediaScannerHelper(ActGoodsInReceive.this, mPhotoPathUri.getPath()); // provided that mPhotoPathUri is not null
            new MediaScannerHelper(ActGoodsInReceive.this, Environment.getExternalStorageDirectory().getAbsolutePath());        //new line
            return response;
        }

        @Override
        protected void onPostExecute(AbstractMap.SimpleEntry<Boolean, AbstractMap.SimpleEntry<String, String>> response) {
            if (xDialog != null && xDialog.isShowing()) {
                xDialog.dismiss();
            }
            //AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInReceive.this);
            if (response != null) {
                if (response.getKey() == true) {
//                    if (response.getValue().getKey().isEmpty()) {
//                        //Notify files updated but unable to build directory // Since we're storing images in root dir then new dir isn't needed, so this will always return empty string
//                        String mMsg = "Files uploaded successfully but was unable to build directory initially.\nPlease contact IT if error continues";
//                        AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInReceive.this);
//                        builder.setMessage(mMsg)
//                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        //do nothing
//                                    }
//                                });
//                        builder.show();
//                    }else
                    if (response.getValue().getValue().isEmpty()) {
                        //Notify files uploaded but unable to message service failed
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInReceive.this);String mMsg = "Files uploaded successfully but message service failed.\nPlease contact IT if error continues";
                        builder.setMessage(mMsg)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do nothing
                                    }
                                });
                        builder.show();
                    } else {
                        //Success, Assumes everything went OK ! Send Email and navigate to next screen
//                        String picLinkString = "";
//                        for (int i = 0; i < currentGallery.size(); i++) {
//                            File file = new File(currentGallery.get(i).getPath());
//                            picLinkString = picLinkString + String.format("<li><a href=\"ftp://properuk.net/GoodsInImages/%s/%s\">%s</a></li>", response.getValue(), file.getName(), file.getName());
//                        }
//                        handleSendAutomatedEmail(picLinkString);    //send email
//                        Intent i = new Intent(ActGoodsInReceive.this, ActGoodsInStockHeaderProduct.class);
//                        i.putExtra("BOARDSCAN_EXTRA", boardScanResult);
//                        i.putExtra("PHOTOLOCATION_EXTRA", response.getValue().getKey());
//                        startActivityForResult(i, RESULT_OK);
                        String mMsg = "Success ! - Pictures sent successfully";
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInReceive.this);
                        builder.setMessage(mMsg)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //end this
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActGoodsInReceive.this.finish();
                                    }
                                });
                        builder.show();

                    }
                } else {
                    String mMsg = "Unable to upload a Goods-In Image over the network.\nPlease contact IT if error continues";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInReceive.this);
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
    }
}