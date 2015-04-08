package com.proper.messagequeue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;
import com.proper.data.diagnostics.WifiLogEntry;
import com.proper.warehousetools.R;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 16/02/2015.
 */
public class LogResolver {
    private String TSAG = LogResolver.class.getSimpleName();
    private String response;
    private Context context = null;

    public LogResolver(Context context) {
        this.context = context;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getDefaultConfig() {
        return setConfig(R.integer.CONFIG_TESTSERVER);
    }

    public String setConfig(int configurqation) {
        String newConfig = "";
        switch(configurqation) {
            case R.integer.CONFIG_TESTSERVER:
                newConfig = "http://192.168.10.248:9090/samplews/api/messages/queue";
                break;
            case R.integer.CONFIG_LIVESERVER:
                newConfig = "http://192.168.10.246:9090/samplews/api/messages/queue";
                break;
            case R.integer.CONFIG_LIVESERVER_EXTERNAL:
                newConfig = "http://89.248.28.82:9090/samplews/api/messages/queue";
                break;
            case R.integer.CONFIG_TESTSERVER_EXTERNAL:
                newConfig = "http://89.248.28.81:9090/samplews/api/messages/queue";
                break;
        }
        return newConfig;
    }

    public Boolean LogWifiReceiver(Context context, WifiLogEntry entry) {
        boolean success = false;
        List<WifiLogEntry> entryList = new ArrayList<WifiLogEntry>();
        try {
            //TODO - Use CIFS Client Library -  http://stackoverflow.com/a/10600116 <Already added in Maven>
            String fName = "\\\\cinnamon\\ftpparent\\TelfordHand\\WarehouseWifiLog\\log.json";
            String domain = "PROPER.DOMAIN";
            String username = "administrator";
            String pwd = "Proper2580";
            jcifs.Config.setProperty( "jcifs.netbios.wins", "192.168.10.247" );
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, pwd);
            SmbFile file = new SmbFile(fName, auth);
            if (file != null && file.exists()) {
                InputStream is = file.getInputStream();
                //Retrieve entry list from file
                ObjectMapper mapper = new ObjectMapper();
                entryList = mapper.readValue(is, new TypeReference<List<WifiLogEntry>>(){});
                entryList.add(entry);
                byte[] bytes = mapper.writeValueAsBytes(entryList);
                SmbFileOutputStream os = new SmbFileOutputStream(file);
                os.write(bytes);
                success = true;
            }
        } catch(Exception ex) {
            String msg = "Unable to send";
            Log.d("LOG_TAG", "No network available!");
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(msg)
                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do nothing
                        }
                    });
            builder.show();
        }
        return success;
    }

    public Boolean LogWifiReceiverByFTP(Context context, WifiLogEntry entry) {
        boolean success = false;
        List<WifiLogEntry> entryList = new ArrayList<WifiLogEntry>();
        Resources res = context.getResources();
        try {
            org.apache.commons.net.ftp.FTPClient ftp = new org.apache.commons.net.ftp.FTPClient();
            String host = res.getString(R.string.FTP_HOST_EXTERNAL);
            String user = res.getString(R.string.FTP_DEFAULTUSER);
            String pass = res.getString(R.string.FTP_PASSWORD);
            ftp.connect(host);
            ftp.login(user, pass);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE); //new
            ftp.setBufferSize(3774873); //3.6MB - ftp.setBufferSize(0)// new line improve speed
            ftp.enterLocalPassiveMode();
            //ftp.connect(host);
            String imagesDir = "/WarehouseWifiLog/";
            //change directory
            ftp.changeWorkingDirectory(imagesDir);
            InputStream is = ftp.retrieveFileStream("log.json");
            int nBytes = is.available();
            if (is != null && nBytes > 0) {
                ObjectMapper mapper = new ObjectMapper();
                entryList = mapper.readValue(is, new TypeReference<List<WifiLogEntry>>(){});
                entryList.add(entry);
                byte[] bytes = mapper.writeValueAsBytes(entryList);
                ftp.appendFile("log.json", new ByteArrayInputStream(bytes));
                success = true;
            }
        } catch(Exception ex) {
            //String msg = "Unable to send";
            Log.d("LOG_TAG", "No network available!");
            if (ex.getMessage().contains("EOFException")) {
                return false;
            }
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setMessage(msg)
//                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            //do nothing
//                        }
//                    });
//            builder.show();
        }
        return success;
    }
}
