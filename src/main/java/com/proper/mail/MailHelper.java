package com.proper.mail;


//import com.proper.data.diagnostics.LogEntry;
import com.proper.logger.LogHelper;
import com.proper.utils.StringUtils;
//import com.proper.warehousetools.AppContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Created by Lebel on 05/11/2014.
 */
public class MailHelper {
//    private String deviceIMEI = "";
//    private static final String ApplicationID = "GoodsIn";
//    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
//    private java.sql.Timestamp today = null;
//    private LogHelper logger = new LogHelper();
    //private String response;
    //private AppContext appContext;

    public boolean sendEmail(MailItem emailItem) {
        boolean responseBody = false;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(emailItem);
            DefaultHttpClient http = new DefaultHttpClient();
            HttpPost httpMethod = new HttpPost();
            String url = "http://192.168.10.80:8080/warehouse/api/v1/log/email";
            //String url = "http://192.168.10.80:8080/warehouse.support/api/v1/log/email";
            //String url = "http://192.168.10.246:9090/warehouse.support/api/v1/log/email";
            httpMethod.setURI(new URI(url));
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
//            today = new java.sql.Timestamp(utilDate.getTime());
//            LogEntry log = new LogEntry(1L, ApplicationID, "MailHelper - sendEmail", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
//            logger.log(log);
        }
        return responseBody;
    }
}
