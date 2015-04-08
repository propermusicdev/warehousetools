import android.content.Context;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.messagequeue.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by Lebel on 30/09/2014.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class HttpMessageResolverTest {
    private Context appContext;

    @Before
    public void setUp() {
        appContext = Robolectric.application.getApplicationContext();
    }

    //@UsesMocks(HttpMessageResolver.class)
    @Test
    public void resolver_shouldResolveMessage() {
        HttpMessageResolver mockResolver = mock(HttpMessageResolver.class);
        Message mockMessage = mock(Message.class);
        String result = mockResolver.resolveMessageQuery(mockMessage);
        assertEquals("This method should return an empty string", null, result);
    }

    @Test
    public void resolver_shouldResolveMessageQueue() {
        HttpMessageResolver mockResolver = mock(HttpMessageResolver.class);
        Message mockMessage = mock(Message.class);
        String result = mockResolver.resolveMessageQueue(mockMessage);
        assertEquals("This method should return an empty string", null, result);
    }

    @Test
    public void resolver_shouldResolveMessageAction() {
        HttpMessageResolver mockResolver = mock(HttpMessageResolver.class);
        Message mockMessage = mock(Message.class);
        String result = mockResolver.resolveMessageAction(mockMessage);
        assertEquals("This method should return an empty string", null, result);
    }
}
