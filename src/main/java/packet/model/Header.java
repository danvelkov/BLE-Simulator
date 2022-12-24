package packet.model;

public class Header {
    private LLID LLID;
    private String NESN;
    private String SN;
    private String MD;
    private String CP;
    private String RFU;
    private String length;

    private enum LLID {
        b00, //RFU
        b01, //LL Data PDU
        b10, //LL Data PDU
        b11 //LL Control PDU
    }
}


