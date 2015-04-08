import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.ListView;
import com.proper.warehousetools.ActMain;
import com.proper.warehousetools.R;
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
 * Created by Lebel on 29/09/2014.
 */
//@Config(emulateSdk = 18)
//@RunWith(RobolectricTestRunner.class)
public class ActMainTest extends ActivityInstrumentationTestCase2<ActMain> {
    private ActMain activity;

//    public ActMainTest(String pkg, Class<ActMain> activityClass) {
//        super(pkg, activityClass);
//    }
//
//    public ActMainTest(Class<ActMain> activityClass) {
//        super(activityClass);
//    }
//

    public ActMainTest() {
        super("com.proper.warehousetools", ActMain.class);
    }

    @Before
    @Override
    public void setUp() throws Exception{
        super.setUp();
//        activity = Robolectric.buildActivity(ActMain.class).create().start().resume().visible().get();
//        activity = new ActMain() {
//            @Override
//            public android.support.v7.app.ActionBar getSupportActionBar() {
//                return mock(android.support.v7.app.ActionBar.class);
//            }
//        };
        activity = this.getActivity();
    }

    @SmallTest
    public void precog() {
        ListView lvModule = (ListView) activity.findViewById(R.id.lvModule);
        assertNotNull(lvModule);
    }

    @Test
    public void litmus() throws ClassNotFoundException {
        View v = mock(View.class);
        when(v.getTag()).thenReturn(1000);
        int tag = (Integer) v.getTag();
        assertEquals(1000, tag);
    }

//    @Test
//    public void main_ShouldHaveAListView(){
//        ListView lvModules = (ListView) activity.findViewById(R.id.lvModule);
//        assertNotNull("ListView should not be null", lvModules.getAdapter());
//    }
}
