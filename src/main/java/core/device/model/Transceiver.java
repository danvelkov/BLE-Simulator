package core.device.model;

import packet.model.Packet;

public class Transceiver {
    //private double RxSensitivity; Receiver sensitivity is a measure of the minimum signal strength that a receiver can detect. It tells us the weakest signal that a receiver will be able to identify and process.
    //za kogato shte mahash max distance, tova shte bude noviq parametur ot koito zavisi na kakvo razstoqnie ima vruzka ili ne
    private double powerTransmit;
    private double receiverSensitivity;
    private double gainTransmit;
    private double gainReceive;
    private double distance; //to show distance between this device and master device

    //Similar to CYW43455 module used in Raspberry Pi 4 B
    public Transceiver() {
        //RxSens -96.5 dBm
        this.powerTransmit = 8.5; //dBm
        this.receiverSensitivity = -96.5; //dBm
        this.gainTransmit = 1;
        this.gainReceive = 1;
    }

    public Transceiver(double powerTransmit, double powerReceive, double gainTransmit, double gainReceive) {
        this.powerTransmit = powerTransmit;
        this.receiverSensitivity = powerReceive;
        this.gainTransmit = gainTransmit;
        this.gainReceive = gainReceive;
    }

    //trqbva li ti paket? da za transmitter power
    public Double calculateRSSI(Packet packet) {
        //System.out.println(this.calculatePowerReceived(packet));
        //System.out.println(dBmToWatt(this.calculatePowerReceived(packet)));
        double wavelength = 3 * Math.pow(10, 8) / ((2402 + (Double.parseDouble(packet.getChannel()) * 2)) * 1000);
        return WattTodBm(dBmToWatt(this.calculatePowerReceived(packet)) * packet.getDeviceFrom().getTransceiver().getGainTransmit() * this.gainReceive * Math.pow((wavelength / (4 * Math.PI * this.distance)), 2));
    }

    public Double calculatePowerReceived(Packet packet) {
        //dobavi distance kolko e ot nqkude, moje da se prashta kum antenata suobshtenie sled premestvane na kolko e otdalecheno ot master ustroistvo
        double wavelength = 3 * Math.pow(10, 8) / ((2402 + (Double.parseDouble(packet.getChannel()) * 2)) * 1000);
        //System.out.println(dBmToWatt(packet.getDeviceFrom().getTransceiver().getPowerTransmit()));
        //System.out.println(distance);
        return WattTodBm(dBmToWatt(packet.getDeviceFrom().getTransceiver().getPowerTransmit()) * packet.getDeviceFrom().getTransceiver().getGainTransmit() * this.gainReceive * Math.pow((wavelength / (4 * Math.PI * distance)), 2));
    }

    public double dBmToWatt(Double dBm) {
        return Math.pow(10, ((dBm - 30) / 10));
    }

    public double WattTodBm(Double Watt) {
        return 10 * Math.log10(Watt) + 30;
    }

    public double getPowerTransmit() {
        return powerTransmit;
    }

    public void setPowerTransmit(double powerTransmit) {
        this.powerTransmit = powerTransmit;
    }

    public double getReceiverSensitivity() {
        return receiverSensitivity;
    }

    public void setReceiverSensitivity(double receiverSensitivity) {
        this.receiverSensitivity = receiverSensitivity;
    }

    public double getGainTransmit() {
        return gainTransmit;
    }

    public void setGainTransmit(double gainTransmit) {
        this.gainTransmit = gainTransmit;
    }

    public double getGainReceive() {
        return gainReceive;
    }

    public void setGainReceive(double gainReceive) {
        this.gainReceive = gainReceive;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}

