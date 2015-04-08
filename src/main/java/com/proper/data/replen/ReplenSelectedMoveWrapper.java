package com.proper.data.replen;

import java.io.Serializable;

/**
 * Created by Lebel on 11/12/2014.
 */
public class ReplenSelectedMoveWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    private ReplenMoveListItemResponse Item;
    private boolean workable;

    public ReplenSelectedMoveWrapper() {
    }

    public ReplenSelectedMoveWrapper(ReplenMoveListItemResponse item) {
        Item = item;
        this.workable = (item.getStatus() > 0);
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public ReplenMoveListItemResponse getItem() {
        return Item;
    }

    public void setItem(ReplenMoveListItemResponse item) {
        Item = item;
    }

    public boolean isWorkable() {
        return workable;
    }

    public void setWorkable(boolean workable) {
        this.workable = workable;
    }
}
