package com.proper.data.binmove;

import android.content.Context;
import android.content.Intent;
import com.proper.bean.Base;

/**
 * Created by Lebel on 21/08/2014.
 */
public class Module extends Base {

    private static final String TAG = "Module";

    private String name;
    private int icon;
    private String className;

    public Module(String name, int icon, String className) {
        this.name = name;
        this.icon = icon;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    /**
     * Navigate to corresponding activity
     */
    public void toActivity(Context context) {
        if (className != null && className.length() > 0) {
            try {
                Intent intent = new Intent(context, Class.forName(context
                        .getPackageName() + ".ui." + className));
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
