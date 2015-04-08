package com.proper.data.helpers;

import com.proper.data.goodsin.BoardScanResult;
import com.proper.data.goodsin.GoodsIn;
import com.proper.data.goodsin.GoodsInDelivery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

/**
 * Created by Lebel on 30/04/2014.
 * This is a simple helper to correct some of the response columns headers coming from HorseRadish queue
 */
public class ResponseHelper {

    public String refineOutgoingMessage(String input){
        String refined = input;
        if (input.contains("Full_Artist_Value1")) {
            refined = refined.replace("Full_Artist_Value1", "FullArtist");
        }
        if (input.contains("Full_Title_Value1")){
            refined = refined.replace("Full_Title_Value1", "FullTitle");
        }
        if (input.contains("Packshot_URL_Value1")) {
            refined = refined.replace("Packshot_URL_Value1", "PackshotURL");
        }
        if (input.contains("PRO:")) {
            refined = refined.replace("PRO:", "");
        }
        return refined;
    }

    public String refineResponse(String input) {
        String refined = input;

        if (input.contains("Full_Artist_Value1")) {
            refined = refined.replace("Full_Artist_Value1", "FullArtist");
        }
        if (input.contains("Full_Title_Value1")){
            refined = refined.replace("Full_Title_Value1", "FullTitle");
        }
        if (input.contains("Packshot_URL_Value1")) {
            refined = refined.replace("Packshot_URL_Value1", "PackshotURL");
        }
        if (input.contains("PRO:")) {
            refined = refined.replace("PRO:", "");
        }
        try {
            JSONObject jsonResp = new JSONObject(refined);
            JSONArray products = jsonResp.getJSONArray("Products");

            for (int i = 0; i < products.length(); i++) {
                JSONObject prod = products.getJSONObject(i);
                if (prod.has("FullArtist")) {
                    prod.remove("FullArtist");
                }
                if (prod.has("FullTitle")) {
                    prod.remove("FullTitle");
                }
                if (!prod.has("PackshotURL")) {
                    prod.put("PackshotURL", "");
                }
            }
            jsonResp.remove("Products");
            jsonResp.put("Products", products);
            refined = jsonResp.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return refined;
    }

    public String refineProductResponse(String input) {
        String refined = input;

        if (input.contains("Full_Artist_Value1")) {
            refined = refined.replace("Full_Artist_Value1", "FullArtist");
        }
        if (input.contains("Full_Title_Value1")){
            refined = refined.replace("Full_Title_Value1", "FullTitle");
        }
        if (input.contains("Packshot_URL_Value1")) {
            refined = refined.replace("Packshot_URL_Value1", "PackshotURL");
        }
        if (input.contains("PRO:")) {
            refined = refined.replace("PRO:", "");
        }
        try {
            JSONObject jsonResp = new JSONObject(refined);
            JSONArray products = jsonResp.getJSONArray("Products");

            for (int i = 0; i < products.length(); i++) {
                JSONObject prod = products.getJSONObject(i);
                if (!prod.has("PackshotURL")) {
                    prod.put("PackshotURL", "");
                }
            }
            jsonResp.remove("Products");
            jsonResp.put("Products", products);
            refined = jsonResp.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return refined;
    }

    public BoardScanResult refineBoardScanResult(String json) {
        BoardScanResult refined = null;
        GoodsInDelivery delivery = new GoodsInDelivery();
        GoodsIn goodsIn = new GoodsIn();
        try {
            JSONObject resp = new JSONObject(json);
            String requestedGoodsInBoardId = resp.getString("RequestedGoodsInBoardId");
            String del = resp.getString("Delivery");
            String goods = resp.getString("GoodsIn");
            JSONObject deliveryObject = new JSONObject(del);
            JSONObject goodsInObject = new JSONObject(goods);
            delivery = buildDelivery(deliveryObject);
            goodsIn = buildGoodsIn(goodsInObject);
            refined = new BoardScanResult(Integer.parseInt(requestedGoodsInBoardId), delivery, goodsIn);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return  refined;
    }

    private GoodsInDelivery buildDelivery(JSONObject jsonObject) {
        GoodsInDelivery delivery = new GoodsInDelivery();
        int GoodsInId = 0;
        Timestamp DateTimeReceived = null;
        String Supplier = "";
        String Label = "";
        int NumberOfPallets = 0;
        int NumberOfBoxes = 0;
        String Location = "";
        String Courier = "";
        String Notes = "";
        try {
            String goodsIn = jsonObject.getString("GoodsInId");
            String dateTimeReceived = jsonObject.getString("DateTimeReceived");
            String supplier = jsonObject.getString("Supplier");
            String label = jsonObject.getString("Label");
            String numberOfPallets = jsonObject.getString("NumberOfPallets");
            String numberOfBoxes = jsonObject.getString("NumberOfBoxes");
            String location = jsonObject.getString("Location");
            String courier = jsonObject.getString("Courier");
            String notes = jsonObject.getString("Notes");
            if (!goodsIn.isEmpty()) {
                GoodsInId = Integer.parseInt(goodsIn);
            }
            if (!dateTimeReceived.isEmpty()) {
                DateTimeReceived =  Timestamp.valueOf(dateTimeReceived);
            }
            if (!supplier.isEmpty()) {
                Supplier = supplier;
            }
            if (!label.isEmpty()) {
                Label = label;
            }
            if (!numberOfPallets.isEmpty()) {
                NumberOfPallets = Integer.parseInt(numberOfPallets);
            }
            if (!numberOfBoxes.isEmpty()) {
                NumberOfBoxes = Integer.parseInt(numberOfBoxes);
            }
            if (!location.isEmpty()) {
                Location = location;
            }
            if (!courier.isEmpty()) {
                Courier = courier;
            }
            if (!notes.isEmpty()) {
                Notes = notes;
            }
            delivery = new GoodsInDelivery(GoodsInId, DateTimeReceived, Supplier, Label, NumberOfPallets, NumberOfBoxes, Location, Courier, Notes);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return delivery;
    }

    private GoodsIn buildGoodsIn(JSONObject jsonObject) {
        GoodsIn goodsIn = new GoodsIn();
        int StockHeaderId = 0;
        String Supplier = "";
        String SupplierName = "";
        String Notes = "";
        int Status = 0;
        String StatusName = "";
        String OrderNumber = "";
        int AssignedUserId = 0;
        String AssignedUserName = "";
        int Lines = 0;
        int UnitsOrdered = 0;
        try {
            String stockHeaderId = jsonObject.getString("StockHeaderId");
            String supplier = jsonObject.getString("Supplier");
            String supplierName = jsonObject.getString("SupplierName");
            String notes = jsonObject.getString("Notes");
            String status = jsonObject.getString("Status");
            String statusName = jsonObject.getString("StatusName");
            String orderNumber = jsonObject.getString("OrderNumber");
            String assignedUserId = jsonObject.getString("AssignedUserId");
            String assignedUserName = jsonObject.getString("AssignedUserName");
            String lines = jsonObject.getString("Lines");
            String unitsOrdered = jsonObject.getString("UnitsOrdered");
            if (!stockHeaderId.isEmpty()) {
                StockHeaderId = Integer.parseInt(stockHeaderId);
            }
            if (!supplier.isEmpty()) {
                Supplier = supplier;
            }
            if (!supplierName.isEmpty()) {
                SupplierName = supplierName;
            }
            if (!notes.isEmpty()) {
                Notes = notes;
            }
            if (!status.isEmpty()) {
                Status = Integer.parseInt(status);
            }
            if (!statusName.isEmpty()) {
                StatusName = statusName;
            }
            if (!orderNumber.isEmpty()) {
                OrderNumber = orderNumber;
            }
            if (!assignedUserId.isEmpty()) {
                AssignedUserId = Integer.parseInt(assignedUserId);
            }
            if (!assignedUserName.isEmpty()) {
                AssignedUserName = assignedUserName;
            }
            if (!lines.isEmpty()) {
                Lines = Integer.parseInt(lines);
            }
            if (!unitsOrdered.isEmpty()) {
                UnitsOrdered = Integer.parseInt(unitsOrdered);
            }
            goodsIn = new GoodsIn(StockHeaderId, Supplier, SupplierName, Notes, Status, StatusName, OrderNumber, AssignedUserId, AssignedUserName, Lines, UnitsOrdered);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return goodsIn;
    }

    public static int nthOccurrence(String str, String c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos + 1);
        return pos;
    }

}
