package com.proper.data.replen;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 27/11/2014.
 */
public class ReplenMoveListResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private int RequestedUserId;
    private List<ReplenMoveListItemResponse> Movelists;
    private int MovelistsReturned;

    public ReplenMoveListResponse() {
    }

    public ReplenMoveListResponse(int requestedUserId, List<ReplenMoveListItemResponse> movelists, int movelistsReturned) {
        RequestedUserId = requestedUserId;
        Movelists = movelists;
        MovelistsReturned = movelistsReturned;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("RequestedUserId")
    public int getRequestedUserId() {
        return RequestedUserId;
    }

    public void setRequestedUserId(int requestedUserId) {
        RequestedUserId = requestedUserId;
    }

    @JsonProperty("Movelists")
    public List<ReplenMoveListItemResponse> getMovelists() {
        return Movelists;
    }

    public void setMovelists(List<ReplenMoveListItemResponse> movelists) {
        Movelists = movelists;
    }

    @JsonProperty("MovelistsReturned")
    public int getMovelistsReturned() {
        return MovelistsReturned;
    }

    public void setMovelistsReturned(int movelistsReturned) {
        MovelistsReturned = movelistsReturned;
    }
}
