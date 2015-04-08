package com.proper.data.helpers;

import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.replen.ReplenLineFeedBack;
import com.proper.data.replen.ReplenLineFeedBackResponse;
import com.proper.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Knight on 13/01/2015.
 */
public class ReplenResponseHelper {

    public ReplenLineFeedBackResponse refineSplitResponse(String input) {
        ReplenLineFeedBackResponse ret = new ReplenLineFeedBackResponse();
        try {
            JSONObject resp = new JSONObject(input);
            String nline = resp.getString("NewLine");
            String oline = resp.getString("OldLine");
            JSONObject newLine = new JSONObject(nline);
            JSONObject oldLine = new JSONObject(oline);
            JSONArray msgList = resp.getJSONArray("Messages");
            ReplenLineFeedBack NewLine = buildLine(newLine);
            ReplenLineFeedBack OldLine = buildLine(oldLine);
            List<BinMoveMessage> lineMessages = buildMessageList(msgList);
            /** List of BinMoveObject = null - Always**/
            String result = resp.getString("Result");
            ret = new ReplenLineFeedBackResponse(NewLine, OldLine, lineMessages, null, result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    private ReplenLineFeedBack buildLine(JSONObject jsonObject) {
        ReplenLineFeedBack line = new ReplenLineFeedBack();
        int MovelistId = 0;
        int MovelistLineId = 0;
        Timestamp InsertTimeStamp = null;
        int ProductId = 0;
        String SrcBinCode = "";
        String DstBinCode = "";
        int Qty = 0;
        int RemoveLink = 0;
        int MovementId = 0;
        String SortOrder = "";
        boolean QtyConfirmed = false;
        boolean Completed = false;
        try {
            String movelistId = jsonObject.getString("MovelistId");
            String movelistLineId = jsonObject.getString("MovelistLineId");
            String insertTimeStamp = jsonObject.getString("InsertTimeStamp");
            String productId = jsonObject.getString("ProductId");
            String srcBinCode = jsonObject.getString("SrcBinCode");
            String dstBinCode = jsonObject.getString("DstBinCode");
            String qty = jsonObject.getString("Qty");
            String removeLink = jsonObject.getString("RemoveLink");
            String movementId = jsonObject.getString("MovementId");
            String sortOrder = jsonObject.getString("SortOrder");
            String qtyConfirmed = jsonObject.getString("QtyConfirmed");
            String completed = jsonObject.getString("Completed");
            if (!movelistId.isEmpty()) {
                MovelistId = Integer.parseInt(movelistId);
            }
            if (!movelistLineId.isEmpty()) {
                MovelistLineId = Integer.parseInt(movelistLineId);
            }
            if (!insertTimeStamp.isEmpty()) {
                InsertTimeStamp = Timestamp.valueOf(insertTimeStamp);
            }
            if (!productId.isEmpty()) {
                ProductId = Integer.parseInt(productId);
            }
            if (!srcBinCode.isEmpty()) {
                SrcBinCode = srcBinCode;
            }
            if (!dstBinCode.isEmpty()) {
                DstBinCode = dstBinCode;
            }
            if (!qty.isEmpty()) {
                Qty = Integer.parseInt(qty);
            }
            if (!removeLink.isEmpty()) {
                RemoveLink = Integer.parseInt(removeLink);
            }
            if (!movementId.isEmpty()) {
                MovementId = Integer.parseInt(movementId);
            }
            if (!sortOrder.isEmpty()) {
                SortOrder = sortOrder;
            }
            if (!qtyConfirmed.isEmpty()) {
                QtyConfirmed = StringUtils.toBool(qtyConfirmed);
            }
            if (!completed.isEmpty()) {
                Completed = StringUtils.toBool(completed);
            }
            line = new ReplenLineFeedBack(MovelistId, MovelistLineId, InsertTimeStamp, ProductId, SrcBinCode, DstBinCode,
                    Qty, RemoveLink, MovementId, SortOrder, QtyConfirmed, Completed);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return  line;
    }
    
    public List<BinMoveMessage> buildMessageList(JSONArray jsonArray) {
        List<BinMoveMessage> list = new ArrayList<BinMoveMessage>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject msg = jsonArray.getJSONObject(i);
                String msgName = msg.getString("MessageName");
                String msgText = msg.getString("MessageText");
                String msgTime = msg.getString("MessageTimeStamp");
                BinMoveMessage binMoveMessage = new BinMoveMessage(msgName, msgText, Timestamp.valueOf(msgTime));
                list.add(binMoveMessage);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
