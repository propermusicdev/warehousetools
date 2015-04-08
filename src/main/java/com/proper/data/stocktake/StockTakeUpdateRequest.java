package com.proper.data.stocktake;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 27/01/2015.
 */
public class StockTakeUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String BinCode;
    private int UserId;
    private String UserCode;
    private int StockTakeId;
    private List<StockTakeUpdateLine> StockTakeLines;

    public StockTakeUpdateRequest() {
    }

    public StockTakeUpdateRequest(String binCode, int userId, String userCode, int stockTakeId, List<StockTakeUpdateLine> stockTakeLines) {
        BinCode = binCode;
        UserId = userId;
        UserCode = userCode;
        StockTakeId = stockTakeId;
        StockTakeLines = stockTakeLines;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("BinCode")
    public String getBinCode() {
        return BinCode;
    }

    public void setBinCode(String binCode) {
        BinCode = binCode;
    }

    @JsonProperty("UserId")
    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    @JsonProperty("UserCode")
    public String getUserCode() {
        return UserCode;
    }

    public void setUserCode(String userCode) {
        UserCode = userCode;
    }

    @JsonProperty("StockTakeId")
    public int getStockTakeId() {
        return StockTakeId;
    }

    public void setStockTakeId(int stockTakeId) {
        StockTakeId = stockTakeId;
    }

    @JsonProperty("StockTakeLines")
    public List<StockTakeUpdateLine> getStockTakeLines() {
        return StockTakeLines;
    }

    public void setStockTakeLines(List<StockTakeUpdateLine> stockTakeLines) {
        StockTakeLines = stockTakeLines;
    }
}
