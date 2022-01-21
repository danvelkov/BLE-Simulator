package packet.factory;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import core.device.model.Device;
import packet.model.Packet;
import topology.modification.ConnectionUtil;

public class MasterPacketFactory {
	
	public static void listenForReceivedPacketsOnMaster(Device master) {
		master.getPacketsReceived().addListener((ListChangeListener<Packet>) c -> {
			Packet lastPacket = c.getList().get(c.getList().size() - 1);

			Platform.runLater(() -> {

				switch (lastPacket.getPacketType()) {
					case EMPTY_LL_DATA -> {
						lastPacket.setStatus(true);
						try {
							ConnectionUtil.sendEmptyDataPacket(master, lastPacket.getDeviceFrom(),
									lastPacket.getDeviceFrom().getPacketFactory().getConnectionController()
											.getChannelMap(),
									lastPacket.getDeviceFrom().getPacketFactory().getConnectionController()
											.getHopIncrement(),
									lastPacket.getDeviceFrom().getPacketFactory().getConnectionController()
											.getConnectionInterval());
						} catch (NumberFormatException | InterruptedException e) {
							e.printStackTrace();
						}
					}
					case LL_DATA -> lastPacket.setStatus(true);
					default -> {
					}
				}

			});
		});
	}
}
