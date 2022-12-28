package packet.model;

import core.device.model.DataRate;
import core.device.model.Device;

import java.util.EnumSet;
import java.util.Set;

public class Packet {
	String time;
	PacketType packetType;
	Device deviceFrom;
	Device deviceTo;
	boolean status;
	String channel;
	DataRate dataRate;
	Packet secondaryPacket;
	Payload payload;

	public Packet(String time, PacketType packetType, Device deviceFrom, Device deviceTo, String channel,
			DataRate dataRate) {
		this.time = time;
		this.packetType = packetType;
		this.deviceFrom = deviceFrom;
		this.deviceTo = deviceTo;
		this.status = false;
		this.channel = channel;
		this.dataRate = dataRate;
	}

	public Packet(String time, PacketType packetType, Device deviceFrom, Device deviceTo, String channel,
				  DataRate dataRate, Payload payload) {
		this.time = time;
		this.packetType = packetType;
		this.deviceFrom = deviceFrom;
		this.deviceTo = deviceTo;
		this.status = false;
		this.channel = channel;
		this.dataRate = dataRate;
		this.payload = payload;
	}

	public Packet(String time, PacketType packetType, Device deviceFrom, Device deviceTo, String channel,
			DataRate dataRate, Packet secondaryPacket) {
		this.time = time;
		this.packetType = packetType;
		this.deviceFrom = deviceFrom;
		this.deviceTo = deviceTo;
		this.channel = channel;
		this.dataRate = dataRate;
		this.secondaryPacket = secondaryPacket;
	}
	

	public Packet(String time, PacketType packetType, Device deviceFrom, Device deviceTo, String channel,
				  DataRate dataRate, Packet secondaryPacket, Payload payload) {
		this.time = time;
		this.packetType = packetType;
		this.deviceFrom = deviceFrom;
		this.deviceTo = deviceTo;
		this.channel = channel;
		this.dataRate = dataRate;
		this.secondaryPacket = secondaryPacket;
		this.payload = payload;
	}

	public Packet() {
		this.time = "00.00.01 01-01-1999";
		this.packetType = PacketType.EMPTY_LL_DATA;
		this.deviceFrom = null;
		this.deviceTo = null;
		this.status = false;
		this.channel = "99";
		this.dataRate = DataRate.ERROR;
		this.secondaryPacket = null;
	}

	public Packet(Packet o) {
		this.time = o.getTime();
		this.packetType = o.getPacketType();
		this.deviceFrom = o.getDeviceFrom();
		this.deviceTo = o.getDeviceTo();
		this.status = o.isStatus();
		this.channel = o.getChannel();
		this.dataRate = o.getDataRate();
		this.secondaryPacket = o.getSecondaryPacket();
		this.payload = o.getPayload();
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public PacketType getPacketType() {
		return packetType;
	}

	public Device getDeviceFrom() {
		return deviceFrom;
	}

	public Device getDeviceTo() {
		return deviceTo;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public DataRate getDataRate() {
		return dataRate;
	}

	public Packet getSecondaryPacket() {
		return secondaryPacket;
	}

	public void setSecondaryPacket(Packet secondaryPacket) {
		this.secondaryPacket = secondaryPacket;
	}

	public Payload getPayload() {
		return payload;
	}

	public void setPayload(Payload payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "Packet [time=" + time + ", packetType=" + packetType + ", deviceFrom=" + deviceFrom + ", deviceTo="
				+ deviceTo + ", status=" + status + ", channel=" + channel + ", dataRate=" + dataRate
				+ ", secondaryPacket=" + secondaryPacket + ", payload=" + payload + "]";
	}

	public static Packet sendPacket(Packet packet) {
		packet.getDeviceFrom().addSentPacket(packet);
		packet.getDeviceTo().addReceivedPacket(packet);

		if (packet.getPacketType().equals(PacketType.ADV_EXT_IND)) {
			try {
				Thread.sleep(500);
				sendPacket(packet.getSecondaryPacket());
			} catch (InterruptedException e) {
				e.printStackTrace();

			}

		}
		return packet;
	}

	public boolean isValid() {
		if (this.dataRate.equals(DataRate.ERROR))
			return false;
		else
			return true;
	}

	public boolean isAdvertisingPacket() {
		Set<PacketType> advertisingPacketTypes = EnumSet.of(PacketType.ADV_EXT_IND, PacketType.ADV_IND,
				PacketType.ADV_NONCONN_IND, PacketType.AUX_ADV_IND, PacketType.AUX_CONNECT_REQ,
				PacketType.AUX_CONNECT_RSP, PacketType.CONNECT_IND);

		return advertisingPacketTypes.contains(getPacketType());
	}

	public boolean isConnectionPacket() {
		return !isAdvertisingPacket();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((dataRate == null) ? 0 : dataRate.hashCode());
		result = prime * result + ((deviceFrom == null) ? 0 : deviceFrom.hashCode());
		result = prime * result + ((deviceTo == null) ? 0 : deviceTo.hashCode());
		result = prime * result + ((packetType == null) ? 0 : packetType.hashCode());
		result = prime * result + ((secondaryPacket == null) ? 0 : secondaryPacket.hashCode());
		result = prime * result + (status ? 1231 : 1237);
		result = prime * result + ((time == null) ? 0 : time.hashCode());
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
		Packet other = (Packet) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (dataRate != other.dataRate)
			return false;
		if (deviceFrom == null) {
			if (other.deviceFrom != null)
				return false;
		} else if (!deviceFrom.equals(other.deviceFrom))
			return false;
		if (deviceTo == null) {
			if (other.deviceTo != null)
				return false;
		} else if (!deviceTo.equals(other.deviceTo))
			return false;
		if (packetType != other.packetType)
			return false;
		if (secondaryPacket == null) {
			if (other.secondaryPacket != null)
				return false;
		} else if (!secondaryPacket.equals(other.secondaryPacket))
			return false;
		if (status != other.status)
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

}
