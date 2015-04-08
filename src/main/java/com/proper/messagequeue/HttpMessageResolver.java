package com.proper.messagequeue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.util.Log;
import com.proper.data.core.IHttpMessageResolver;
import com.proper.data.diagnostics.WifiLogEntry;
import com.proper.data.goodsin.GoodsInImage;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.utils.StringUtils;
import com.proper.warehousetools.AppContext;
import com.proper.warehousetools.R;
import com.proper.data.diagnostics.Contact;
import com.proper.data.diagnostics.LogEntry;

import com.proper.logger.LogHelper;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Created by Lebel on 16/05/2014.
 */
public class HttpMessageResolver implements IHttpMessageResolver {
    private String deviceIMEI = "";
    private static final String ApplicationID = "WarehouseTools";
    private Date utilDate = Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private LogHelper logger = new LogHelper();
    private String response;
    private int currentConfig;
    private List<Contact> contactList;
    private AppContext appContext;

    public HttpMessageResolver(AppContext appContext) {
        this.appContext = appContext;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getDefaultConfig() {
        currentConfig = R.integer.CONFIG_TESTSERVER; return setConfig(currentConfig);
    }

    public String getDefaultConfigName() {
        getDefaultConfig();
        return setConfigName(currentConfig);
    }

    public String setConfig(int configurqation) {
        String newConfig = "";
        switch(configurqation) {
            case R.integer.CONFIG_TESTSERVER:
                //newConfig = "http://192.168.10.96:8080/warehouse/api/v1/message/barcodequery2";
                //newConfig = "http://192.168.10.248:9090/warehouse.support/api/v1/message/barcodequery2";
                newConfig = "http://192.168.10.248:9090/samplews/api/messages/queue";
                break;
            case R.integer.CONFIG_LIVESERVER:
                //newConfig = "http://192.168.10.246:9090/warehouse.support/api/v1/message/barcodequery2";
                newConfig = "http://192.168.10.246:9090/samplews/api/messages/queue";
                break;
            case R.integer.CONFIG_LIVESERVER_EXTERNAL:
                //newConfig = "http://89.248.28.82:9090/warehouse.support/api/v1/message/barcodequery2";\
                newConfig = "http://89.248.28.82:9090/samplews/api/messages/queue";
                break;
            case R.integer.CONFIG_TESTSERVER_EXTERNAL:
                //newConfig = "http://89.248.28.81:9090/warehouse.support/api/v1/message/barcodequery2";
                newConfig = "http://89.248.28.81:9090/samplews/api/messages/queue";
                break;
        }
        return newConfig;
    }

    public String setConfigName(int configurqation) {
        String newConfig = "";
        switch(configurqation) {
            case R.integer.CONFIG_TESTSERVER:
                newConfig = "CONFIG_TESTSERVER";
                break;
            case R.integer.CONFIG_LIVESERVER:
                newConfig = "CONFIG_LIVESERVER";
                break;
            case R.integer.CONFIG_LIVESERVER_EXTERNAL:
                newConfig = "CONFIG_LIVESERVER_EXTERNAL";
                break;
            case R.integer.CONFIG_TESTSERVER_EXTERNAL:
                newConfig = "CONFIG_TESTSERVER_EXTERNAL";
                break;
        }
        return newConfig;
    }

    @Override
    public String resolveMessageQuery(Message msg) {

        try {
            URL url = new URL(getDefaultConfig());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false); // new line
            conn.setDoInput(true); //new line
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(msg);

            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            osw.write(input);
            osw.flush();
            osw.close();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            //Serialise the returned entity
            //LogEntry newEntry = mapper.readValue(conn.getInputStream(), LogEntry.class);
            //Or Get the string returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response string
                sb.append(line + "");
            }

            reader.close(); // new line added !
            // Append Server Response To Content String but do nothing with it - for now...
            setResponse(sb.toString().trim());

            conn.disconnect();
        } catch(MalformedURLException ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQuery", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        } catch (IOException ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQuery", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQuery", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        }
        return response;
    }

    public HttpResponseHelper resolveHttpMessage(Message msg) {
        setResponse("");
        long startTime = System.currentTimeMillis();
        long endTime;
        long timeElapsed = 0L;
        int httpRespCode = 0;
        Class<?> excepClass = null;
        try {
            URL url = new URL(getDefaultConfig());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false); // new line
            conn.setDoInput(true); // new line
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(msg);

            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            osw.write(input);
            osw.flush();
            osw.close();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                httpRespCode = conn.getResponseCode();
                endTime = System.currentTimeMillis();
                timeElapsed = endTime - startTime;
                throw new RuntimeException("Failed : HTTP error code : " + httpRespCode);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                sb.append(line + "");       // Append server response string
            }

            reader.close(); // new line added !
            setResponse(sb.toString().trim()); // Append Server Response To Content String but do nothing with it - for now...
            httpRespCode = conn.getResponseCode();
            conn.disconnect();
            endTime = System.currentTimeMillis();
            timeElapsed = endTime - startTime;
        } catch (Exception ex) {
            ex.printStackTrace();
            excepClass = ex.getClass();
        }
        return new HttpResponseHelper(!response.isEmpty(), response, httpRespCode, excepClass, "", timeElapsed);
    }

    public String resolveMessageQueue(Message msg) {
        String responseBody = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(msg);
            DefaultHttpClient http = new DefaultHttpClient();
            HttpPost httpMethod = new HttpPost();
            httpMethod.setURI(new URI(getDefaultConfig()));
            httpMethod.setHeader("Accept", "application/json");
            httpMethod.setHeader("Content-type", "application/json");
            httpMethod.setEntity(new StringEntity(input));
            HttpResponse response = http.execute(httpMethod);
            int responseCode = response.getStatusLine().getStatusCode();
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    HttpEntity entity = response.getEntity();
                    if(entity != null)
                    {
                        responseBody = EntityUtils.toString(entity);
                    }
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQueue", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        }
        return responseBody;
    }

    public String resolveMessageQueueAsync(Message msg) {
        String responseBody = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(msg);
            DefaultHttpClient http = new DefaultHttpClient();
            HttpPost httpMethod = new HttpPost();
            httpMethod.setURI(new URI(getDefaultConfig()));
            httpMethod.setHeader("Accept", "application/json");
            httpMethod.setHeader("Content-type", "application/json");
            httpMethod.setEntity(new StringEntity(input));
            HttpResponse response = http.execute(httpMethod);
            int responseCode = response.getStatusLine().getStatusCode();
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    HttpEntity entity = response.getEntity();
                    if(entity != null)
                    {
                        responseBody = EntityUtils.toString(entity);
                    }
                    break;
            }

            Client client = ClientBuilder.newClient();
            final Future<Response> responseFuture = client.target("http://192.168.10.248:9080").path("/api/v1/message/barcodequery2").request().async().get(new InvocationCallback<Response>() {
                //String responseBody1 = "";
                @Override
                public void completed(Response response) {
                    Log.w("resolveMessageQueueAsync - ", "Response status code" + response.getStatus() + "received");
                    System.out.println("Response status code "
                            + response.getStatus() + " received.");
                }

                @Override
                public void failed(Throwable throwable) {
                    Log.w("resolveMessageQueueAsync - ", "Invocation failed !");
                    System.out.println("Invocation failed.");
                    throwable.printStackTrace();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQueue", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        }
        return responseBody;
    }

    @Override
    public String resolveMessageAction(Message msg) {
        return null;
    }

    @Override
    public List<Contact> resolveContacts(Context context) {
        //Code runs
        contactList = new ArrayList<Contact>();
        String dirName = context.getApplicationInfo().dataDir + "/contacts/";
        String fileFullName = context.getApplicationInfo().dataDir + "/contacts/contact.json";
        File fileDir = new File(dirName);
        File jsonContact = new File(fileFullName);
        //Check DIR
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        ObjectMapper mapper = new ObjectMapper();
        if (jsonContact.exists()) {
            try {
                //List<Contact> contactList = mapper.readValue(jsonContact, mapper.getTypeFactory().constructCollectionType(List.class, Contact.class));
                contactList = mapper.readValue(jsonContact, new TypeReference<List<Contact>>(){});
            } catch (IOException e) {
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.log(log);
            }
        }else {
            //Get contacts from a rest web service
            try {
                //URL url = new URL("http://192.168.10.248:9080/warehouse.support/api/v1/contact/getall");
                URL url = new URL("http://192.168.10.246:9080/warehouse.support/api/v1/contact/getall");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                ObjectMapper map = new ObjectMapper();
                contactList = map.readValue(conn.getInputStream(), new TypeReference<List<Contact>>(){});

                conn.disconnect();
                mapper.writeValue(new File(context.getApplicationInfo().dataDir + "/contacts/contact.json"), contactList);
            } catch (IOException e) {
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveContacts", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.log(log);
            } catch(Exception ex) {
                ex.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveContacts", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                logger.log(log);
            }
        }
        return contactList;
    }

    @Override
    public Boolean uploadImage(GoodsInImage image) {

        boolean responseBody = false;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(image);
            DefaultHttpClient http = new DefaultHttpClient();
            HttpPost httpMethod = new HttpPost();
            httpMethod.setURI(new URI(getDefaultConfig()));
            httpMethod.setHeader("Accept", "application/json");
            httpMethod.setHeader("Content-type", "application/json");
            httpMethod.setEntity(new StringEntity(input));
            HttpResponse response = http.execute(httpMethod);
            int responseCode = response.getStatusLine().getStatusCode();
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    HttpEntity entity = response.getEntity();
                    if(entity != null)
                    {
                        String value = EntityUtils.toString(entity);
                        responseBody = StringUtils.toBool(value);
                    }
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQueue", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        }
        return responseBody;
    }

    @Override
    public synchronized AbstractMap.SimpleEntry<Boolean, String> uploadImagesFTP(GoodsInImage images) {
        AbstractMap.SimpleEntry<Boolean, String> ret = null;
        Resources res = appContext.getResources();

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
            String imagesDir = "/GoodsInImages/";
            //change directory
            ftp.changeWorkingDirectory(imagesDir);
            String newFolderDir = String.format("%s(%s)", images.getGoogsinID(), images.getSupplier());
            ftp.makeDirectory(newFolderDir); //create a new directory
            ftp.changeWorkingDirectory(newFolderDir);
            //Upload File

            int successCount = 0;
            for (int i = 0; i < images.getFiles().size(); i++) {
                File file = new File(images.getFiles().get(i).getPath());
                FileInputStream stream = new FileInputStream(file);
                boolean result = ftp.storeFile(file.getName(), stream);
                if (result) {
                    successCount ++;
                }
            }
            if (successCount > 0) {
                //overallResult = true;
                //ret = new AbstractMap.SimpleEntry<Boolean, String>(true, FilenameUtils.concat("ftp://properuk.net/GoodsInImages", newFolderDir));
                ret = new AbstractMap.SimpleEntry<Boolean, String>(true, newFolderDir);
            }
            ftp.logout();
            ftp.disconnect();
        } catch(Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - uploadImageFTP", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        }

        return ret;
    }

    public boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == HttpURLConnection.HTTP_OK);
            } catch (IOException e) {
                Log.e("LOG_TAG", "Error checking internet connection", e);
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - hasActiveInternetConnection", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.log(log);
            }
        } else {
            Log.d("LOG_TAG", "No network available!");
        }
        return false;
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private Boolean LogWifiReceiver(Context context, WifiLogEntry entry) {
        boolean success = false;
        List<WifiLogEntry> entryList = new ArrayList<WifiLogEntry>();
        try {
            //TODO - Use CIFS Client Library -  http://stackoverflow.com/a/10600116 <Already added in Maven>
            String fName = "smb:\\\\cinnamon\\ftpparent\\TelfordHand\\WarehouseWifiLog\\log.json";
            String domain = "PROPER.DOMAIN";
            String username = "administrator";
            String pwd = "proper2580";
            jcifs.Config.setProperty( "jcifs.netbios.wins", "192.168.10.247" );
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, pwd);
            SmbFile file = new SmbFile(fName, auth);
            SmbFileInputStream in = new SmbFileInputStream(file);
            //Retrieve entry list from file
            ObjectMapper mapper = new ObjectMapper();
            entryList = mapper.readValue(in, new TypeReference<List<WifiLogEntry>>(){});
            entryList.add(entry);
            byte[] bytes = mapper.writeValueAsBytes(entryList);
            SmbFileOutputStream os = new SmbFileOutputStream(file);
            os.write(bytes);
            success = true;
        } catch(Exception ex) {
            String msg = "Unable to send";
            Log.d("LOG_TAG", "No network available!");
            AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
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
}
