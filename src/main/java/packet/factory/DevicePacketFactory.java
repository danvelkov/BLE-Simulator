package packet.factory;

import packet.model.Payload;
import topology.modification.ConnectionController;
import core.device.model.DataRate;
import core.device.model.Device;
import packet.model.Packet;
import packet.model.PacketType;
import core.Singleton;
import topology.modification.ConnectionUtil;

import java.util.Map;
import java.util.Random;

public class DevicePacketFactory implements Runnable {
	Device device;
	String currentChannel = "37";
	String secondaryChannel = Integer.toString(new Random().nextInt(37));
	private final Object lock = new Object();
	ConnectionController connectionController;
	String advertisingInterval;
	boolean connectable, havingPacketsToSend;
	Packet packetToSend;
	int packetToSendCount;

	public DevicePacketFactory(Device device) {
		super();
		this.device = device;
	}

	@Override
	public void run() {
		synchronized (lock) {
			try {
				switch (device.getState()) {
					case ADVERTISING -> {
						System.out.println("adv");
						advertisementEvent();
						this.run();
					}
					case CONNECTION -> {
						System.out.println("conn");
						connectionEvent();
						this.run();
					}
					case STANDBY -> {
						System.out.println("standby");
						lock.wait();
						this.run();
					}
					case REMOVAL -> System.out.println("removal");
					default -> {
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private void advertisementEvent() throws InterruptedException {
		advertise();
		listenForConnectionRequest();

		Thread.sleep((long) (10 / Singleton.getInstance().speed));

		swapAdvertisingChannel();
	}

	public void advertise() throws InterruptedException {
		Packet extendAdvertismentPacket = null;

		if (this.device.getState().equals(Device.State.ADVERTISING)) {
			Payload advData = new Payload((this.device.getDataRate().equals(DataRate.ONEM) ? PacketType.ADV_IND : PacketType.AUX_ADV_IND).toString(),
					isConnectable() ?
							Map.ofEntries(
									Map.entry("Local Name", device.getName().toString()),
									Map.entry("Flags", "LE Limited Discoverable Mode,LE General Discoverable Mode,BR/EDR Not Supported,Simultaneous LE and BR/EDR (Controller),Simultaneous LE and BR/EDR (Host)"),
									Map.entry("Appearance", device.getAppearance().toString()),
									Map.entry("LE Bluetooth Device Address", device.getDeviceAddress().getAddress()),
									Map.entry("Advertising interval", advertisingInterval)
							)
							:
							Map.ofEntries(
									Map.entry("Local Name", device.getName()),
									Map.entry("Appearance",  device.getAppearance().toString()),
									Map.entry("LE Bluetooth Device Address", device.getDeviceAddress().getAddress()),
									Map.entry("Advertising interval", advertisingInterval)
					));


			Packet advertismentPacket = new Packet(
					this.device.getDataRate().equals(DataRate.ONEM) ? Singleton.getTime() : Singleton.getAddedTime(30),
					this.device.getDataRate().equals(DataRate.ONEM) ? PacketType.ADV_IND : PacketType.AUX_ADV_IND,
					this.device, Singleton.getInstance().master,
					this.device.getDataRate().equals(DataRate.ONEM) ? currentChannel : secondaryChannel,
					this.device.getDataRate(), advData);

			if (!this.device.getDataRate().equals(DataRate.ONEM)) {
				Payload extData = new Payload(PacketType.ADV_EXT_IND.toString(), Map.ofEntries(
						Map.entry("Advertisment secondary channel", secondaryChannel + "\nPHY:" + device.getDataRate()),
						Map.entry("Time for auxilary packet", Singleton.getAddedTime(30))
				));

				extendAdvertismentPacket = new Packet(Singleton.getTime(), PacketType.ADV_EXT_IND, this.device,
						Singleton.getInstance().master, currentChannel, this.device.getDataRate(), advertismentPacket,
						extData);
			}

			if (device.getDeviceCircle().getLine().isVisible()) {
				Packet.sendPacket(extendAdvertismentPacket == null ? advertismentPacket : extendAdvertismentPacket);
			} else
				device.addSentPacket(extendAdvertismentPacket == null ? advertismentPacket : extendAdvertismentPacket);
		}
	}

	private void swapAdvertisingChannel() throws InterruptedException {
		switch (this.currentChannel) {
			case "37" -> this.currentChannel = "38";
			case "38" -> this.currentChannel = "39";
			case "39" -> {
				this.currentChannel = "37";
				Thread.sleep(
						(long) (Long.parseLong(this.advertisingInterval.substring(0, this.advertisingInterval.indexOf(".")))
								/ Singleton.getInstance().speed));
			}
		}
	}

	private void listenForConnectionRequest() {
		if (!this.device.getState().equals(Device.State.CONNECTION)) {
			Packet connectionRequestPacket = device.getPacketsReceived().stream()
					.filter(p -> p.getPacketType()
							.equals(this.device.getDataRate().equals(DataRate.ONEM) ? PacketType.CONNECT_IND
									: PacketType.AUX_CONNECT_REQ)
							&& !p.isStatus())
					.reduce((first, second) -> second).orElse(new Packet());

			if (connectionRequestPacket.getDeviceFrom() != null) {
				if (!connectionRequestPacket.getDataRate().equals(DataRate.ONEM)) {
					Payload connRspData = new Payload(PacketType.AUX_CONNECT_RSP.toString(), Map.ofEntries(
							Map.entry("Advertiser Address", device.getDeviceAddress().getAddress()),
							Map.entry("Target Address", Singleton.getInstance().master.getDeviceAddress().getAddress())));

					Packet connectionResponsePacket = new Packet(Singleton.getTime(), PacketType.AUX_CONNECT_RSP,
							this.device, Singleton.getInstance().master, secondaryChannel, this.device.getDataRate(),
							connRspData);

					Packet.sendPacket(connectionResponsePacket);
				}

				connectionRequestPacket.setStatus(true);
				this.device.setState(Device.State.CONNECTION);

			}
		}
	}

	private void connectionEvent() {
		try {
			Thread.sleep(Double.valueOf(connectionController.getConnectionInterval()).longValue());
			listenForReceivedPackets();
			dataPacketFactory();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void dataPacketFactory() {
		if (this.device.getState().equals(Device.State.CONNECTION)) {
			if (isHavingPacketsToSend()) {
				sendDataPacket();
			} else {
				returnEmptyDataPacket();
			}
		}
	}

	public void returnEmptyDataPacket() {
		if (!isHavingPacketsToSend()) {

			try {
				ConnectionUtil.sendEmptyDataPacket(device, Singleton.getInstance().master,
						this.connectionController.getChannelMap(), this.connectionController.getHopIncrement(),
						this.connectionController.getConnectionInterval());
			} catch (NumberFormatException | InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public void setSendDataPackets(Packet dataPacket, int count) {
		setHavingPacketsToSend(true);
		packetToSend = dataPacket;
		packetToSendCount = count;
	}

	private void sendDataPacket() {
		if (packetToSendCount < 1)
			setHavingPacketsToSend(false);
		else {
			Packet buffPacket = new Packet(packetToSend);
			Packet.sendPacket(buffPacket);
			packetToSendCount--;
			System.out.println(packetToSendCount);

			packetToSend.setChannel(ConnectionUtil.nextChannel(getConnectionController().getChannelMap(),
					getConnectionController().getHopIncrement()));
		}
	}

	private void listenForReceivedPackets() {
		Packet lastReceivedPacket = this.device.getPacketsReceived().get(this.device.getPacketsReceived().size() - 1);
		if (!lastReceivedPacket.isStatus())
			switch (lastReceivedPacket.getPacketType()) {
				case EMPTY_LL_DATA -> lastReceivedPacket.setStatus(true);
				case LL_TERMINATE_IND -> {
					lastReceivedPacket.setStatus(true);
					this.device.setState(Device.State.STANDBY);
				}
				//TODO get payload somehow
//				case LL_CONNECTION_PARAM_REQ -> SecurityManager.sendConfirmPacket(this.device, lastReceivedPacket.getDeviceFrom(), "");
				default -> {
				}
			}

	}


	public void setAdvertisingInterval(String advertisingInterval) {
		this.advertisingInterval = advertisingInterval;
	}

	public void setConnectionController(ConnectionController connectionController) {
		this.connectionController = connectionController;
	}

	public Object getLock() {
		return lock;
	}

	public boolean isConnectable() {
		return connectable;
	}

	public void setConnectable(boolean connectable) {
		this.connectable = connectable;
	}

	public boolean isHavingPacketsToSend() {
		return havingPacketsToSend;
	}

	public void setHavingPacketsToSend(boolean havingPacketsToSend) {
		this.havingPacketsToSend = havingPacketsToSend;
	}

	public String getAdvertisingInterval() {
		return advertisingInterval;
	}

	public ConnectionController getConnectionController() {
		return connectionController;
	}

}
