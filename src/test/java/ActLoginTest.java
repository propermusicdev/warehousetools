import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.proper.security.UserLoginResponse;
import com.proper.warehousetools.ActLogin;
import com.proper.warehousetools.R;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Lebel on 23/09/2014.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ActLoginTest {
    private ActLogin activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(ActLogin.class).create().start().resume().visible().get();
    }

    @Test
    public void litmus() throws ClassNotFoundException {
        View v = mock(View.class);
        when(v.getTag()).thenReturn(1000);
        int tag = (Integer) v.getTag();
        assertEquals(1000, tag);
    }

//    @Test
//    public void clickingLogin_shouldStartSomeMainActivity() {
//        EditText txtInitials = (EditText) activity.findViewById(R.id.etxtLoginInitials);
//        EditText txtPin = (EditText) activity.findViewById(R.id.etxtLoginPin);
//        txtInitials.setText("LF");
//        txtPin.setText("8233");
//        activity.findViewById(R.id.bnLoginProceed).performClick();
//
//        Intent expectedIntent = new Intent(activity, ActMain.class);
//        Intent intent = shadowOf(activity).peekNextStartedActivityForResult().intent;
//        //assertThat("Intents are not equal", shadowOf(activity).getNextStartedActivityForResult().intent, (org.hamcrest.Matcher<Intent>) expectedIntent);
//        //assertThat(activity, new StartedMatcher(ActMain.class)); /Obsolute
//        //assertThat(shadowOf(activity).peekNextStartedActivityForResult().intent.getComponent(), equalTo(new ComponentName(activity, ActMain.class)));
//        assertThat(intent, equalTo(expectedIntent));
//    }

    @Test
    public void loginViews_ShouldValidate() {
        EditText txtInitials = (EditText) activity.findViewById(R.id.etxtLoginInitials);
        EditText txtPin = (EditText) activity.findViewById(R.id.etxtLoginPin);
        txtInitials.setText("LF");
        txtPin.setText("8233");
        Button btnLogin = (Button) activity.findViewById(R.id.bnLoginProceed);
        btnLogin.getText();
        assertEquals("Enter", btnLogin.getText());
        assertEquals("LF", txtInitials.getText().toString());
        assertEquals("8233", txtPin.getText().toString());
    }

    @Test
    public void loginView_ShouldValidateUsers() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String currentUserToken = "{\"RequestedInitials\" : \"LF \",\"UserId\" : \"348\",\"UserFirstName\" : \"Lebel\",\"UserLastName\" : \"Fuayuku\",\"UserCode\" : \"D1CE48\",\"Response\" : \"Success\"}";
        UserLoginResponse currentUser = mapper.readValue(currentUserToken, UserLoginResponse.class);
//        saveAuthentication();
//        Intent i = new Intent(ActLogin.this, ActMain.class);
//        startActivityForResult(i, RESULT_FIRST_USER);
        assertEquals("Are not the same", "Lebel", currentUser.getUserFirstName());
    }

}
