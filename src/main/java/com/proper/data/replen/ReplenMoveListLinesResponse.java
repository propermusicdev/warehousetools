package com.proper.data.replen;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 02/12/2014.
 */
public class ReplenMoveListLinesResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private int RequestedUserId;
    private int RequestedMovelistId;
    private ReplenMoveLineResponse MoveList;
    private List<ReplenMoveListLinesItemResponse> MoveListLines;

    public ReplenMoveListLinesResponse() {
    }

    public ReplenMoveListLinesResponse(int requestedUserId, int requestedMovelistId, ReplenMoveLineResponse moveList, List<ReplenMoveListLinesItemResponse> moveListLines) {
        RequestedUserId = requestedUserId;
        RequestedMovelistId = requestedMovelistId;
        MoveList = moveList;
        MoveListLines = moveListLines;
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

    @JsonProperty("RequestedMovelistId")
    public int getRequestedMovelistId() {
        return RequestedMovelistId;
    }

    public void setRequestedMovelistId(int requestedMovelistId) {
        RequestedMovelistId = requestedMovelistId;
    }

    @JsonProperty("MoveList")
    public ReplenMoveLineResponse getMoveList() {
        return MoveList;
    }

    public void setMoveList(ReplenMoveLineResponse moveList) {
        MoveList = moveList;
    }

    @JsonProperty("MoveListLines")
    public List<ReplenMoveListLinesItemResponse> getMoveListLines() {
        return MoveListLines;
    }

    public void setMoveListLines(List<ReplenMoveListLinesItemResponse> moveListLines) {
        MoveListLines = moveListLines;
    }
}
