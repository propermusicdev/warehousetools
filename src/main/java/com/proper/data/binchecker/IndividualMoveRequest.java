package com.proper.data.binchecker;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 18/03/2015.
 */
public class IndividualMoveRequest implements Serializable{
    private static final long serialVersionUID = 1L;
    private String MoveListType;
    private List<IndividualMoveLine> Products;
    private String UserCode;
    private int UserId;

    public IndividualMoveRequest() {
    }

    public IndividualMoveRequest(String moveListType, List<IndividualMoveLine> products, String userCode, int userId) {
        MoveListType = moveListType;
        Products = products;
        UserCode = userCode;
        UserId = userId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("MoveListType")
    public String getMoveListType() {
        return MoveListType;
    }

    public void setMoveListType(String moveListType) {
        MoveListType = moveListType;
    }

    @JsonProperty("Products")
    public List<IndividualMoveLine> getProducts() {
        return Products;
    }

    public void setProducts(List<IndividualMoveLine> products) {
        Products = products;
    }

    @JsonProperty("UserCode")
    public String getUserCode() {
        return UserCode;
    }

    public void setUserCode(String userCode) {
        UserCode = userCode;
    }

    @JsonProperty("UserId")
    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }
}
