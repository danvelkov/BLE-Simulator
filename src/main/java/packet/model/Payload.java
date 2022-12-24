package packet.model;

import java.util.Map;

//TODO replace payload fields in Packet with Payload class containing flags and other stuff
public class Payload {
    private String opCode; //operation code
    private Map<String, String> ctrData; //controll data

    public Payload() {
    }

    public Payload(String opCode, Map<String, String> ctrData) {
        this.opCode = opCode;
        this.ctrData = ctrData;
    }

    public String getOpCode() {
        return opCode;
    }

    public void setOpCode(String opCode) {
        this.opCode = opCode;
    }

    public Map<String, String> getCtrData() {
        return ctrData;
    }

    public void setCtrData(Map<String, String> ctrData) {
        this.ctrData = ctrData;
    }

    @Override
    public String toString() {
        return "Payload{" +
                "opCode='" + opCode + '\'' +
                ", ctrData='" + ctrData + '\'' +
                '}';
    }
}
