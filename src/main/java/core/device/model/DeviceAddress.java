package core.device.model;

import java.util.Random;

public class DeviceAddress {
	private AddressType type;
	private String address;

	public DeviceAddress(String type, String address) {
		this.type = AddressType.valueOf(type);
		switch (type.toString()) {
			case "PUBLIC" -> changeAddress(address);
			case "STATIC", "NONRESOLVABLE" -> this.address = generateRandomAddress();
		}
	}

	public DeviceAddress() {
		this.type = AddressType.STATIC;
		this.address = "";
	}

	public String generateRandomAddress() {
		Random rand = new Random();
		byte[] macAddr = new byte[6];
		rand.nextBytes(macAddr);
		if (this.type.toString().equals("STATIC"))
			macAddr[5] = (byte) (macAddr[5] & 252);
		else if (this.type.toString().equals("NONRESOLVABLE"))
			macAddr[0] = (byte) (macAddr[5] | 3);
		else
			return null;

		StringBuilder sb = new StringBuilder(18);
		for (byte b : macAddr) {

			if (sb.length() > 0)
				sb.append("-");

			sb.append(String.format("%02x", b));
		}

		return sb.toString();
	}

	public boolean changeAddress(String address) {
		if (!type.toString().equals("PUBLIC")) {
			return true;
		} else
			return false;
	}

	public AddressType getType() {
		return type;
	}

	public void setType(AddressType type) {
		this.type = type;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return address;
	}
	
	
	
	
}
