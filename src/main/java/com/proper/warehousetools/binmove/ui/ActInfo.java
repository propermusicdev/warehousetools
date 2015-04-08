package com.proper.warehousetools.binmove.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.binmove.BinMoveObject;
import com.proper.data.binmove.BinMoveResponse;
import com.proper.data.binmove.PartialBinMoveResponse;
import com.proper.data.binmove.adapters.MoveActionAdapter;
import com.proper.data.binmove.adapters.MoveMessageAdapter;
import com.proper.data.diagnostics.LogEntry;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ActInfo extends PlainActivity {
    private TextView txtHeader;
    private ListView lvActions;
    private ListView lvMessages;
    private BinMoveResponse moveResponse = new BinMoveResponse();

    public BinMoveObject getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(BinMoveObject selectedAction) {
        this.selectedAction = selectedAction;
    }

    public BinMoveMessage getSelectedMessage() {
        return selectedMessage;
    }

    public void setSelectedMessage(BinMoveMessage selectedMessage) {
        this.selectedMessage = selectedMessage;
    }

    private BinMoveObject selectedAction;
    private BinMoveMessage selectedMessage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_info);

        Intent bundle = getIntent();
        Bundle extras =  bundle.getExtras();
        int action = extras.getInt("ACTION_EXTRA");
        switch (action) {
            case R.integer.ACTION_PARTIALMOVE:
                PartialBinMoveResponse partial = (PartialBinMoveResponse) extras.getSerializable("RESPONSE_EXTRA");
                moveResponse = new BinMoveResponse(partial);
                break;
            case R.integer.ACTION_BINMOVE:
                moveResponse = (BinMoveResponse) extras.getSerializable("RESPONSE_EXTRA");
                break;
        }

        if (moveResponse != null) {// empty for now
        } else {
            //Explain Message and crash App or Kill
            String msg = "The Response object must not be null.";
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActInfo - onCreate - Line:72", deviceIMEI, RuntimeException.class.getSimpleName(), msg, today);
            logger.log(log);
            throw new RuntimeException(msg + " Please Contact an IT staff");
            //super.onCreate(savedInstanceState);
            //return;
        }
        txtHeader = (TextView) this.findViewById(R.id.txtvInfoHeader);
        lvActions = (ListView) this.findViewById(R.id.lvMessageActions);
        lvMessages = (ListView) this.findViewById(R.id.lvMessages);
        lvActions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                lvAction_Item_Clicked(adapterView, view, pos, id);
            }
        });
        lvMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                lvMessages_Item_clicked(adapterView, view, pos, id);
            }
        });

//        LayoutInflater inflater = (LayoutInflater) this
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MoveActionAdapter actionAdapter = new MoveActionAdapter(this, moveResponse.getMessageObjects());
        MoveMessageAdapter msgAdapter = new MoveMessageAdapter(this, moveResponse.getMessages());
        lvActions.setAdapter(actionAdapter);
        lvMessages.setAdapter(msgAdapter);
    }

    private void lvMessages_Item_clicked(AdapterView<?> adapterView, View view, int pos, long id) {
        if (moveResponse != null) {
            setSelectedMessage(moveResponse.getMessages().get(pos));
        }
    }

    private void lvAction_Item_Clicked(AdapterView<?> adapterView, View view, int pos, long id) {
        if (moveResponse != null) {
            setSelectedAction(moveResponse.getMessageObjects().get(pos));
        }
    }

}