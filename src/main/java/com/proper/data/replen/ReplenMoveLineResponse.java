package com.proper.data.replen;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Lebel on 04/12/2014.
 */
public class ReplenMoveLineResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private int MovelistId;
    private String Description;
    private String Notes;
    private Timestamp InsertTimeStamp;
    private int AssignedTo;
    private int Status;
    private String StatusName;
    private int ListType;
    private String ListTypeName;

    public ReplenMoveLineResponse() {
    }

    public ReplenMoveLineResponse(int movelistId, String description, String notes, Timestamp insertTimeStamp, int assignedTo, int status, String statusName, int listType, String listTypeName) {
        MovelistId = movelistId;
        Description = description;
        Notes = notes;
        InsertTimeStamp = insertTimeStamp;
        AssignedTo = assignedTo;
        Status = status;
        StatusName = statusName;
        ListType = listType;
        ListTypeName = listTypeName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("MovelistId")
    public int getMovelistId() {
        return MovelistId;
    }

    public void setMovelistId(int movelistId) {
        MovelistId = movelistId;
    }

    @JsonProperty("Description")
    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    @JsonProperty("Notes")
    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    @JsonProperty("InsertTimeStamp")
    public Timestamp getInsertTimeStamp() {
        return InsertTimeStamp;
    }

    public void setInsertTimeStamp(Timestamp insertTimeStamp) {
        InsertTimeStamp = insertTimeStamp;
    }

    @JsonProperty("AssignedTo")
    public int getAssignedTo() {
        return AssignedTo;
    }

    public void setAssignedTo(int assignedTo) {
        AssignedTo = assignedTo;
    }

    @JsonProperty("Status")
    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    @JsonProperty("StatusName")
    public String getStatusName() {
        return StatusName;
    }

    public void setStatusName(String statusName) {
        StatusName = statusName;
    }

    @JsonProperty("ListType")
    public int getListType() {
        return ListType;
    }

    public void setListType(int listType) {
        ListType = listType;
    }

    @JsonProperty("ListTypeName")
    public String getListTypeName() {
        return ListTypeName;
    }

    public void setListTypeName(String listTypeName) {
        ListTypeName = listTypeName;
    }
}
