package stocktake;

import android.view.View;
import com.proper.warehousetools.stocktake.ui.chainway_C4000.ActStockTakeBinCheck;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Knight on 26/01/2015.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ActStockTakeBinCheckTest {
    private ActStockTakeBinCheck activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(ActStockTakeBinCheck.class).create().start().resume().visible().get();
    }

    @Test
    public void litmus() throws ClassNotFoundException {
        View v = mock(View.class);
        when(v.getTag()).thenReturn(1000);
        int tag = (Integer) v.getTag();
        assertEquals(1000, tag);
    }
}
