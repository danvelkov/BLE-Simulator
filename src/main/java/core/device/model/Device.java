package core.device.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import packet.factory.DevicePacketFactory;
import packet.model.Packet;

public class Device {

	String name;
	Transceiver transceiver;
	DeviceAddress deviceAddress;
	State state;
	DataRate dataRate;
	Appearance appearance;
	Input input;
	Output output;
	DeviceCircle deviceCircle;
	ObservableList<Packet> packetsSent = FXCollections.observableArrayList();
	ObservableList<Packet> packetsReceived = FXCollections.observableArrayList();
	ObservableList<Packet> allPackets = FXCollections.observableArrayList();
	DevicePacketFactory packetFactory = new DevicePacketFactory(this);
	LongProperty standbyTime = new SimpleLongProperty(0), advertisingTime = new SimpleLongProperty(0),
			connectedTime = new SimpleLongProperty(0);

	public Device(String name, Transceiver transceiver, DeviceAddress deviceAddress, State state, DataRate dataRate, Appearance appearance,
				  DeviceCircle deviceCircle) {
		this.name = name;
		this.transceiver = transceiver;
		this.deviceAddress = deviceAddress;
		this.state = state;
		this.dataRate = dataRate;
		this.appearance = appearance;
		setInputOutput(appearance);
		this.deviceCircle = deviceCircle;
	}

	public Device() {
		this.name = "";
		this.transceiver = new Transceiver();
		this.deviceAddress = new DeviceAddress();
		this.state = State.REMOVAL;
		this.dataRate = DataRate.ERROR;
	}

	@Override
	public String toString() {
		return name + " [" + deviceAddress + "]";
	}

	public String getName() {
		return name;
	}

	public Transceiver getTransceiver() {
		return transceiver;
	}

	public void setTransceiver(Transceiver transceiver) {
		this.transceiver = transceiver;
	}

	public DeviceAddress getDeviceAddress() {
		return deviceAddress;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public DataRate getDataRate() {
		return dataRate;
	}

	public Appearance getAppearance() {
		return appearance;
	}

	public Input getInput() { return input; }

	public Output getOutput() { return output; }

	public DeviceCircle getDeviceCircle() {
		return deviceCircle;
	}

	public ObservableList<Packet> getPacketsSent() {
		return packetsSent;
	}

	public ObservableList<Packet> getPacketsReceived() {
		return packetsReceived;
	}

	public void addReceivedPacket(Packet packet) {
		this.packetsReceived.add(packet);
		this.allPackets.add(packet);
	}

	public void addSentPacket(Packet packet) {
		this.packetsSent.add(packet);
		this.allPackets.add(packet);
	}

	private void setInputOutput(Appearance appearance) {
		switch (appearance) {
			case PHONE, COMPUTER -> {
				this.input = Input.KEYBOARD;
				this.output = Output.DISPLAY;
			}
			case CLOCK, HEART_RATE_SENSOR, THERMOMETER, BLOOD_PRESSURE, GLUCOSE_METER, SENSOR -> {
				this.input = Input.NONE;
				this.output = Output.DISPLAY;
			}
			default -> {
				this.input = Input.NONE;
				this.output = Output.NONE;
			}
		}
	}

	public DevicePacketFactory getPacketFactory() {
		return packetFactory;
	}

	public LongProperty getStandbyTime() {
		return standbyTime;
	}

	public void setStandbyTime(long standbyTime) {
		this.standbyTime.set(standbyTime);
	}

	public LongProperty getAdvertisingTime() {
		return advertisingTime;
	}

	public void setAdvertisingTime(long advertisingTime) {
		this.advertisingTime.set(advertisingTime);
	}

	public LongProperty getConnectedTime() {
		return connectedTime;
	}

	public void setConnectedTime(long connectedTime) {
		this.connectedTime.set(connectedTime);
	}

	public ObservableList<Packet> getAllPackets() {
		return allPackets;
	}

	public IOCapability getIOCapabilities(){
		switch(this.getInput()){
			case NONE -> {
				switch (this.getOutput()) {
					case DISPLAY -> {return IOCapability.DISPLAY_ONLY;}
					case NONE -> {return IOCapability.NO_INPUT_NO_OUTPUT;}
				}
			}
			case KEYBOARD -> {
				switch (getOutput()) {
					case DISPLAY -> {return IOCapability.KEYBOARD_DISPLAY;}
					case NONE -> {return IOCapability.KEYBOARD_ONLY;}
				}
			}
		}

		return IOCapability.NO_INPUT_NO_OUTPUT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataRate == null) ? 0 : dataRate.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Device other = (Device) obj;
		if (dataRate != other.dataRate)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (state != other.state)
			return false;
		return true;
	}

	public enum State {
		STANDBY, ADVERTISING, SCANNING, INITIATING, CONNECTION, REMOVAL;
	}

	public enum Appearance {
		UNKNOWN, PHONE, COMPUTER, CLOCK, TAG, THERMOMETER, HEART_RATE_SENSOR, BLOOD_PRESSURE, GLUCOSE_METER, SENSOR, OTHER
	}

	public enum Input {
		NONE, KEYBOARD
	}

	public enum Output {
		NONE, DISPLAY
	}

	public enum IOCapability {
		DISPLAY_ONLY, KEYBOARD_ONLY, NO_INPUT_NO_OUTPUT, KEYBOARD_DISPLAY;
	}

}
