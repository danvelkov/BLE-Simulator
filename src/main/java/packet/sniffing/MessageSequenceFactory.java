package packet.sniffing;

import core.Singleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import core.device.model.Device;
import packet.model.Packet;
import packet.model.PacketType;

import java.util.stream.Collectors;

public class MessageSequenceFactory {
	static AnchorPane messageSequencePane;
	static Device currentSlave;
	static ScrollPane scrollPane;
	static int numberOfMessages = 1;
	static boolean drawedDot = false;

	public AnchorPane getMessageSequencePane() {
		return messageSequencePane;
	}

	public static void setMessageSequencePane(AnchorPane messageSqncPane) {
		messageSequencePane = messageSqncPane;
	}

	public static void setScrollPane(ScrollPane scrlPane) {
		scrollPane = scrlPane;
	}

	public static Device getCurrentSlave() {
		return currentSlave;
	}

	public static void setCurrentSlave(Device slave) {
		currentSlave = slave;
		refresh();
	}

	private static void enableResize() {
		messageSequencePane.prefHeightProperty().bind(scrollPane.heightProperty());
		messageSequencePane.prefWidthProperty().bind(scrollPane.widthProperty());
	}

	public static void refresh() {
		enableResize();
		clear();
		draw();
	}

	private static void draw() {
		if (currentSlave != null)
			makeDeviceRectangle(25, 25, currentSlave.getName());
		else
			makeDeviceRectangle(25, 25, "No device selected");
		makeDeviceRectangle(700, 25, "Master");

		ObservableList<Packet> packets = FXCollections
				.observableArrayList(Singleton.getInstance().master.getAllPackets().stream()
						.filter(p -> p.getDeviceFrom().equals(currentSlave) || p.getDeviceTo().equals(currentSlave))
						.collect(Collectors.toList()));

		for (int i = 0; i < packets.size(); i++) {

			if (packets.get(i).getPacketType().equals(PacketType.ADV_EXT_IND)
					|| packets.get(i).getPacketType().equals(PacketType.AUX_ADV_IND)) {

				if (i > 2 && packets.get(i).getPacketType().equals(packets.get(i - 2).getPacketType())
						&& packets.get(i).getDeviceFrom().equals(packets.get(i - 2).getDeviceFrom())) {
					if (!drawedDot) {
						drawRepeatingDots();
						drawedDot = true;
						generateMessageLines(packets.get(i - 1));
						generateMessageLines(packets.get(i));

					}
				} else {
					generateMessageLines(packets.get(i));
					drawedDot = false;

				}
			} else if (packets.get(i).getPacketType().equals(PacketType.EMPTY_LL_DATA)) {
				if (packets.get(i).getPacketType().equals(packets.get(i - 1).getPacketType())
						&& packets.get(i).getPacketType().equals(packets.get(i - 2).getPacketType())
						&& packets.get(i).getPacketType().equals(packets.get(i - 3).getPacketType())
						&& packets.get(i).getDeviceFrom().equals(packets.get(i - 2).getDeviceFrom())
						&& packets.get(i - 1).getDeviceFrom().equals(packets.get(i - 3).getDeviceFrom())) {
					if (!drawedDot) {
						drawRepeatingDots();
						drawedDot = true;
						generateMessageLines(packets.get(i - 1));
						generateMessageLines(packets.get(i));

					}
				} else {
					generateMessageLines(packets.get(i));
					drawedDot = false;

				}
			} else {

				if (i > 1 && packets.get(i).getPacketType().equals(packets.get(i - 1).getPacketType())
						&& packets.get(i).getDeviceFrom().equals(packets.get(i - 1).getDeviceFrom())) {
					if (!drawedDot) {
						drawRepeatingDots();
						drawedDot = true;
						generateMessageLines(packets.get(i));
					}
				}

				else {
					generateMessageLines(packets.get(i));
					drawedDot = false;

				}

			}
		}
	}

	private static void clear() {
		messageSequencePane.getChildren().clear();
		numberOfMessages = 1;
	}

	private static void makeDeviceRectangle(int startX, int startY, String deviceName) {
		Rectangle device = new Rectangle(startX, startY, 200, 70);
		device.setArcHeight(20);
		device.setArcWidth(20);
		device.setFill(Color.DARKGRAY);
		device.maxHeight(70);
		device.maxWidth(600);
		device.widthProperty().bind(messageSequencePane.widthProperty().multiply(0.2));

		device.setId(deviceName + "Rectangle");

		Line deviceLine = new Line(device.getX() + (device.getWidth() / 2), device.getY() + device.getHeight(),
				device.getX() + (device.getWidth() / 2), messageSequencePane.getHeight());

		deviceLine.startXProperty().bind(device.xProperty().add(device.widthProperty().divide(2))); // works
		deviceLine.startYProperty().bind(device.yProperty().add(device.heightProperty())); // works

		deviceLine.endXProperty().bind(deviceLine.startXProperty()); // works
		deviceLine.endYProperty().bind(messageSequencePane.heightProperty().add(30)); // works

		deviceLine.setId(deviceName + "Line");

		Text text = new Text(deviceName);
		text.setBoundsType(TextBoundsType.VISUAL);
		text.xProperty().bind((device.xProperty()).add(device.widthProperty().divide(2))
				.subtract(text.getLayoutBounds().getWidth() / 2));
		text.yProperty().bind((device.yProperty()).add(device.heightProperty().divide(2))
				.add(text.getLayoutBounds().getHeight() / 2));

		messageSequencePane.getChildren().addAll(device, deviceLine, text);
	}

	private static void generateMessageLines(Packet packet) {
		Rectangle deviceFromRectangle = packet.getDeviceFrom().equals(currentSlave)
				? (Rectangle) messageSequencePane.lookup("#" + packet.getDeviceFrom().getName() + "Rectangle")
				: (Rectangle) messageSequencePane.lookup("#MasterRectangle");

		Rectangle deviceToRectangle = packet.getDeviceTo().equals(currentSlave)
				? (Rectangle) messageSequencePane.lookup("#" + packet.getDeviceTo().getName() + "Rectangle")
				: (Rectangle) messageSequencePane.lookup("#MasterRectangle");

		Line deviceLine = packet.getDeviceFrom().equals(currentSlave)
				? (Line) messageSequencePane.lookup("#" + packet.getDeviceFrom().getName() + "Line")
				: (Line) messageSequencePane.lookup("#MasterLine");

		Line deviceLineТо = packet.getDeviceTo().equals(currentSlave)
				? (Line) messageSequencePane.lookup("#" + packet.getDeviceTo().getName() + "Line")
				: (Line) messageSequencePane.lookup("#MasterLine");

		Arrow messageArrow = new Arrow(deviceLine.getStartX(),
				deviceFromRectangle.getLayoutY() + deviceFromRectangle.getHeight() + 15, deviceLineТо.getEndX(),
				deviceToRectangle.getLayoutY() + deviceToRectangle.getHeight() + 15,
				packet.getDeviceFrom().equals(currentSlave) ? Arrow.Direction.RIGHT : Arrow.Direction.LEFT);

		messageSequencePane.getChildren().addAll(messageArrow.getLine1(), messageArrow.getLine2(),
				messageArrow.getLine());

		messageArrow.getLine().startXProperty().bind(deviceLine.startXProperty());
		messageArrow.getLine().endXProperty().bind(deviceLineТо.startXProperty());

		messageArrow.getLine().startYProperty()
				.bind(deviceFromRectangle.heightProperty().add(deviceFromRectangle.getY()).add(30 * numberOfMessages));
		messageArrow.getLine().endYProperty()
				.bind(deviceFromRectangle.heightProperty().add(deviceFromRectangle.getY()).add(30 * numberOfMessages));

		Text text = new Text(packet.getPacketType().toString());
		text.setBoundsType(TextBoundsType.VISUAL);
		text.xProperty().bind(messageArrow.getLine().startXProperty().add(messageArrow.getLine().endXProperty())
				.divide(2).subtract(text.getLayoutBounds().getWidth() / 2));
		text.yProperty().bind(messageArrow.getLine().startYProperty().subtract(5));

		messageSequencePane.getChildren().add(text);

		numberOfMessages++;
	}

	private static void drawRepeatingDots() {
		Line masterLine = (Line) messageSequencePane.lookup("#MasterLine");
		Line slaveLine = (Line) messageSequencePane.lookup("#" + currentSlave.getName() + "Line");
		Circle c1 = new Circle(2);

		c1.centerXProperty().bind(slaveLine.startXProperty().add(masterLine.startXProperty()).divide(2));
		c1.centerYProperty().bind(masterLine.startYProperty().add(30 * numberOfMessages));

		Circle c2 = new Circle(2);
		c2.centerXProperty().bind(c1.centerXProperty());
		c2.centerYProperty().bind(c1.centerYProperty().add(10));

		Circle c3 = new Circle(2);
		c3.centerXProperty().bind(c2.centerXProperty());
		c3.centerYProperty().bind(c2.centerYProperty().add(10));

		numberOfMessages += 2;

		messageSequencePane.getChildren().addAll(c1, c2, c3);
	}

}

class Arrow {

	enum Direction {
		LEFT, RIGHT;
	}

	Line line;
	Line arrowLine1 = new Line();
	Line arrowLine2 = new Line();

	public Arrow(double startX, double startY, double endX, double endY, Direction direction) {
		line = new Line(startX, startY, endX, endY);

		if (direction.equals(Direction.RIGHT)) {
			arrowLine1.startXProperty().bind(line.endXProperty().subtract(5));
			arrowLine1.startYProperty().bind(line.endYProperty().subtract(5));

			arrowLine1.endXProperty().bind(line.endXProperty());
			arrowLine1.endYProperty().bind(line.endYProperty());

			arrowLine2.startXProperty().bind(line.endXProperty().subtract(5));
			arrowLine2.startYProperty().bind(line.endYProperty().add(5));

			arrowLine2.endXProperty().bind(line.endXProperty());
			arrowLine2.endYProperty().bind(line.endYProperty());
		} else {
			arrowLine1.startXProperty().bind(line.endXProperty());
			arrowLine1.startYProperty().bind(line.endYProperty());

			arrowLine1.endXProperty().bind(line.endXProperty().add(5));
			arrowLine1.endYProperty().bind(line.endYProperty().subtract(5));

			arrowLine2.startXProperty().bind(line.endXProperty());
			arrowLine2.startYProperty().bind(line.endYProperty());

			arrowLine2.endXProperty().bind(line.endXProperty().add(5));
			arrowLine2.endYProperty().bind(line.endYProperty().add(5));
		}

	}

	public Line getLine() {
		return this.line;
	}

	public Line getLine1() {
		return arrowLine1;
	}

	public Line getLine2() {
		return arrowLine2;
	}

}
