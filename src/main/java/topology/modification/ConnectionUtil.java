package topology.modification;

import core.Singleton;
import core.device.model.DataRate;
import core.device.model.Device;
import packet.model.Packet;
import packet.model.PacketType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ConnectionUtil {
	static String lastUnmappedChannel = "0";

	public static Packet checkForAdvertismentPacket(Device master, Device slave) throws ParseException {
		Packet receivedOneMAdvertisment = master.getPacketsReceived().stream().filter(
				p -> (p.getPacketType().equals(PacketType.ADV_IND) && p.getDeviceFrom().equals(slave) && !p.isStatus()))
				.reduce((first, second) -> second).orElse(new Packet());

		Packet receivedExtendedAdvertisment = master
				.getPacketsReceived().stream().filter(p -> (p.getPacketType().equals(PacketType.ADV_EXT_IND)
						&& p.getDeviceFrom().equals(slave) && !p.isStatus()))
				.reduce((first, second) -> second).orElse(new Packet());

		SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd-MM-yyyy");

		if (receivedOneMAdvertisment != null || receivedExtendedAdvertisment != null)
			return formatter.parse(receivedOneMAdvertisment.getTime())
					.after(formatter.parse(receivedExtendedAdvertisment.getTime())) ? receivedOneMAdvertisment
							: receivedExtendedAdvertisment;

		return null;
	}

	public static Packet checkForConnectionResponse(Device master, Device slave) {
		Packet receivedOneMAdvertisment = slave
				.getPacketsReceived().stream().filter(p -> (p.getPacketType().equals(PacketType.CONNECT_IND)
						&& p.getDeviceFrom().equals(master) && !p.isStatus()))
				.reduce((first, second) -> second).orElse(new Packet());

		Packet receivedTwoMCodedAdvertisment = master
				.getPacketsReceived().stream().filter(p -> (p.getPacketType().equals(PacketType.AUX_CONNECT_RSP)
						&& p.getDeviceFrom().equals(slave) && !p.isStatus()))
				.reduce((first, second) -> second).orElse(new Packet());

		return receivedOneMAdvertisment.isValid() ? receivedOneMAdvertisment : receivedTwoMCodedAdvertisment;
	}


	public static boolean startConnectionEvent(Device master, Device slave, List<String> chMap) {
		if (slave.getState().equals(Device.State.ADVERTISING)) {
			try {
				Packet lastReceivedPacket = checkForAdvertismentPacket(master, slave);
				if (lastReceivedPacket.getDataRate() != DataRate.ERROR) {
					lastReceivedPacket.setStatus(true);

					String llData = "Access Address:" + "\nCRC Init:" + "\nWindow Size:" + "\nWindow Offset:"
							+ "\nInterval:" + slave.getPacketFactory().getConnectionController().getConnectionInterval()
							+ "\nLatency:" + "\nTimeout:" + "\nChannel Map:";

					for (int i = 0; i < chMap.size(); i++) {
						llData = llData.concat(chMap.get(i));
						if (i < chMap.size() - 1) {
							llData = llData.concat(",");
						}
					}

					llData = llData
							.concat("\nHop:" + slave.getPacketFactory().getConnectionController().getHopIncrement()
									+ "\nSleep Clock Accuracy:");

					Packet connPacket = new Packet(Singleton.getTime(),
							lastReceivedPacket.getDataRate().equals(DataRate.ONEM) ? PacketType.CONNECT_IND
									: PacketType.AUX_CONNECT_REQ,
							master, slave,
							lastReceivedPacket.getDataRate().equals(DataRate.ONEM) ? lastReceivedPacket.getChannel()
									: lastReceivedPacket.getSecondaryPacket().getChannel(),
							lastReceivedPacket.getDataRate(), llData);

					Packet.sendPacket(connPacket);

					return true;
				}

			} catch (ParseException e) {
				e.printStackTrace();
			}

			return false;
		}

		return false;

	}

	public static boolean sendEmptyDataPacket(Device from, Device to, List<String> channelMap, String hopIncrement,
			String connectionInterval) throws NumberFormatException, InterruptedException {

		if (Thread.currentThread().getName() != "JavaFX Application Thread")
			Thread.sleep(Double.valueOf(connectionInterval).longValue());

		Device slave = from.getName().equals("Master") ? to : from;
		if (slave.getState().equals(Device.State.CONNECTION)) {

			String currentChannel = nextChannel(channelMap, hopIncrement);

			String emptyData = "Info:Empty PDU";
			Packet emptyDataPacket = new Packet(Singleton.getTime(), PacketType.EMPTY_LL_DATA, from, to, currentChannel,
					slave.getDataRate(), emptyData);

			if (!from.getName().equals("Master")) {
				if (from.getDeviceCircle().getLine().isVisible()) {
					Packet.sendPacket(emptyDataPacket);
				} else
					from.addSentPacket(emptyDataPacket);
			} else {
				if (to.getDeviceCircle().getLine().isVisible()) {
					Packet.sendPacket(emptyDataPacket);
				} else
					from.addSentPacket(emptyDataPacket);
			}

			return true;
		} else
			return false;

	}

	public static String nextChannel(List<String> channelMap, String hopIncrement) {
		int unmappedChannel = (Integer.valueOf(lastUnmappedChannel) + Integer.valueOf(hopIncrement)) % 37;
		lastUnmappedChannel = Integer.toString(unmappedChannel);

		if (channelMap.contains(Integer.toString(unmappedChannel))) {
			return Integer.toString(unmappedChannel);
		} else {
			return Integer.toString(unmappedChannel % channelMap.size());
		}

	}

}
