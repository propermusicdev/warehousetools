package com.proper.data.core;

import android.content.Context;
import android.net.Uri;
import com.proper.data.diagnostics.Contact;
import com.proper.data.goodsin.GoodsInImage;
import com.proper.messagequeue.Message;

import java.io.InputStream;
import java.util.AbstractMap;
import java.util.List;

/**
 * Created by Lebel on 16/05/2014.
 */
public interface IHttpMessageResolver {
    String resolveMessageQuery(Message msg);
    String resolveMessageAction(Message msg);
    List<Contact> resolveContacts(Context context);
    Boolean uploadImage(GoodsInImage image);
    AbstractMap.SimpleEntry<Boolean, String> uploadImagesFTP(GoodsInImage images);
}
