package core.device.model;

import core.Singleton;

public class Transceiver {
    //private double RxSensitivity; Receiver sensitivity is a measure of the minimum signal strength that a receiver can detect. It tells us the weakest signal that a receiver will be able to identify and process.
    private double powerTransmit;
    private double receiverSensitivity;
    private double gainTransmit;
    private double gainReceive;
    //private double distance; //to show distance between this device and master device

    //Similar to CYW43455 module used in Raspberry Pi 4 B //77 page of specification
    public Transceiver() {
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

    public Double calculateRSSI(Device otherDevice, double distance) {
        //TODO v zavisimost ot kolko ustroistva ima v singleton spisuka s ustroistva, tolkova random shuma se uvelichava
        //double wavelength = 3 * Math.pow(10, 8) / ((2402 + (Double.parseDouble(packet.getChannel()) * 2)) * 1_000_000);

        double rand = Math.floor(Math.random() * (5 + Singleton.getInstance().devices.size()) + 5 + Singleton.getInstance().devices.size());
        double RSS = WattTodBm(dBmToWatt(this.calculatePowerReceived(otherDevice, distance)) * otherDevice.getTransceiver().getGainTransmit() * this.gainReceive);
        return RSS - rand;

        //return WattTodBm(dBmToWatt(this.calculatePowerReceived(packet)) * packet.getDeviceFrom().getTransceiver().getGainTransmit() * this.gainReceive * Math.pow((wavelength / (4 * Math.PI * this.distance)), 2));
    }

    public Double calculatePowerReceived(Device otherDevice, double distance) {
        double wavelength = 3 * Math.pow(10, 8) / ((2440 * 1_000_000L));
        // return Math.log10((dBmToWatt(packet.getDeviceFrom().getTransceiver().getPowerTransmit())/0.001) * Math.pow(packet.getDeviceFrom().getTransceiver().getGainTransmit(), 2) * Math.pow(this.gainReceive, 2) * Math.pow(wavelength / 4 * Math.PI * distance, 4));
        return WattTodBm(dBmToWatt(otherDevice.getTransceiver().getPowerTransmit()) * otherDevice.getTransceiver().getGainTransmit() * this.gainReceive * Math.pow((wavelength / (4 * Math.PI * distance)), 2));
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
}

