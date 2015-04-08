package com.proper.warehousetools;

/**
 * Created by Lebel on 28/08/2014.
 */
public class MockClass {

    public String resolveLogIn() {
        return "{\"RequestedInitials\" : \"LF \",\"UserId\" : \"348\",\"UserFirstName\" : \"Lebel\",\"UserLastName\" : \"Fuayuku\",\"UserCode\" : \"D1CE48\",\"Response\" : \"Success\"}";
    }

    public String resolveBoardScan() {
        return "{  \n" +
                "   \"RequestedGoodsInBoardId\":\"35374\",\n" +
                "   \"Delivery\":{  \n" +
                "      \"GoodsInId\":\"35374\",\n" +
                "      \"DateTimeReceived\":\"2014-10-21 09:55:31\",\n" +
                "      \"Supplier\":\"2 ENTERTAIN VIDEO LTD\",\n" +
                "      \"Label\":\"...\",\n" +
                "      \"NumberOfPallets\":\"0\",\n" +
                "      \"NumberOfBoxes\":\"5\",\n" +
                "      \"Location\":\"4 LOWER\",\n" +
                "      \"Courier\":\"TNT\",\n" +
                "      \"Notes\":\"MCDLX192 - VARIOUS TITLES\"\n" +
                "   },\n" +
                "   \"GoodsIn\":{  \n" +
                "      \"StockHeaderId\":\"21463\",\n" +
                "      \"Supplier\":\"SODA\",\n" +
                "      \"SupplierName\":\"2 ENTERTAIN VIDEO LTD\",\n" +
                "      \"Notes\":\"\",\n" +
                "      \"Status\":\"8\",\n" +
                "      \"StatusName\":\"Exported\",\n" +
                "      \"OrderNumber\":\"SODA000311\",\n" +
                "      \"AssignedUserId\":\"289\",\n" +
                "      \"AssignedUserName\":\"Daniel Oros\",\n" +
                "      \"Lines\":\"99\",\n" +
                "      \"UnitsOrdered\":\"486\"\n" +
                "   }\n" +
                "}";
    }

    public String resolveBoardImage() {
        return "{  \n" +
                "   \"GoodsInId\":\"35624\",\n" +
                "   \"Images\":[  \n" +
                "      {  \n" +
                "         \"ImagePath\":\"\\\\cinnamon\\ftpparent\\TelfordHand\\GoodsInImages\\35624(CLOVELLY RECORDINGS LTD)\\IMG_201411309_124650.jpg\"                                                                                                                                                              \",\n" +
                "         \"NewImagePath\":\"\\\\pmdserver\\appfiles\\Images\\Repository\\GoodsIn\\35624_78109_348_5.jpg\"                                                                                                                                                                                                \",\n" +
                "         \"Result\":\"Ok\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"ImagePath\":\"\\\\cinnamon\\ftpparent\\TelfordHand\\GoodsInImages\\35624(CLOVELLY RECORDINGS LTD)\\IMG_201411309_124714.jpg\"                                                                                                                                                             \",\n" +
                "         \"NewImagePath\":\"\\\\pmdserver\\appfiles\\Images\\Repository\\GoodsIn\\35624_78109_348_6.jpg\"                                                                                                                                                                                               \",\n" +
                "         \"Result\":\"Ok\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"Result\":\"No Errors\"\n" +
                "}";
    }

}
