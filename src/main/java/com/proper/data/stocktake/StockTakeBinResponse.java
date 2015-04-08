package com.proper.data.stocktake;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 22/01/2015.
 */
public class StockTakeBinResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String BinCode;
    private int StockTakeId;
    private int BinAlreadyChecked;
    private String Result;
    private int OkToContinue;
    private List<StockTakeLine> StockTakeLines;

    public StockTakeBinResponse() {
    }

    public StockTakeBinResponse(String binCode, int stockTakeId, int binAlreadyChecked, String result, int okToContinue, List<StockTakeLine> stockTakeLines) {
        BinCode = binCode;
        StockTakeId = stockTakeId;
        BinAlreadyChecked = binAlreadyChecked;
        Result = result;
        OkToContinue = okToContinue;
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

    @JsonProperty("StockTakeId")
    public int getStockTakeId() {
        return StockTakeId;
    }

    public void setStockTakeId(int stockTakeId) {
        StockTakeId = stockTakeId;
    }

    @JsonProperty("BinAlreadyChecked")
    public int getBinAlreadyChecked() {
        return BinAlreadyChecked;
    }

    public void setBinAlreadyChecked(int binAlreadyChecked) {
        BinAlreadyChecked = binAlreadyChecked;
    }

    @JsonProperty("Result")
    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    @JsonProperty("OkToContinue")
    public int getOkToContinue() {
        return OkToContinue;
    }

    public void setOkToContinue(int okToContinue) {
        OkToContinue = okToContinue;
    }

    @JsonProperty("StockTakeLines")
    public List<StockTakeLine> getStockTakeLines() {
        return StockTakeLines;
    }

    public void setStockTakeLines(List<StockTakeLine> stockTakeLines) {
        StockTakeLines = stockTakeLines;
    }
}
