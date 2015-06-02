package binmove;

import android.view.View;
import com.proper.warehousetools.binmove.ui.chainway_c4000.ActSearchScan;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lebel on 26/05/2015.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ActSearch {
    private ActSearchScan activity;

    @Before
    public void setUp(){
        activity = Robolectric.buildActivity(ActSearchScan.class).create().start().resume().visible().get();
    }

    @Test
    public void litmus() throws ClassNotFoundException {
        View v = mock(View.class);
        when(v.getTag()).thenReturn(1000);
        int tag = (Integer) v.getTag();
        assertEquals(1000, tag);
    }

    @Test
    public void checkActivityNotNull() throws Exception {
        assertNotNull(activity);
    }

//    @Test
//    public void searchView_ShouldScanEANCode(){
//        //do
//    }
}
