package packet.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import packet.model.Packet;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class PacketController implements Initializable {

	@FXML
	AnchorPane anchorPane;

	@FXML
	TreeView<String> payloadTreeView;

	static Packet packet;
	ObservableList<Rectangle> rectangles = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		draw();

		if (packet.getPayload() != null)
			loadTreeView();
	}

	public static void setPacket(Packet pckt) {
		packet = pckt;
	}

	private void loadTreeView() {
		TreeItem<String> packetInfo = new TreeItem<String>("Packet Info");
		packetInfo.setExpanded(true);
		TreeItem<String> header = new TreeItem<String>("Header");
		header.setExpanded(true);
		TreeItem<String> data = new TreeItem<String>("Payload Data");
		data.setExpanded(true);
		addTreeViewData(packet.getPayload(), data);
		addTreeViewData("RSSI:" + packet.getDeviceFrom().getTransceiver().calculateRSSI(packet).toString(), header);

		this.payloadTreeView.setRoot(packetInfo);
		packetInfo.getChildren().addAll(header, data);
		AnchorPane.setTopAnchor(this.payloadTreeView, 400.0);

	}

	//adds payload data
	private void addTreeViewData(String payload, TreeItem<String> rootItem) {

		List<String> items = Arrays.asList(payload.split("\\r?\\n"));
		items.forEach(i -> {
			List<String> currentItem = Arrays.asList(i.split(":"));
			System.out.println(currentItem.get(0));
			TreeItem<String> label = new TreeItem<String>(currentItem.get(0));
			label.setExpanded(true);

			if (currentItem.size() > 1) {
				List<String> subItems = Arrays.asList(currentItem.get(1).split(", "));
				subItems.forEach(sI -> {

					if (currentItem.get(0).equals("Local name")) {
						System.out.println(sI.getBytes().length); 
					} else if (currentItem.get(0).equals("Advertising interval")) {
						System.out.println(Long
								.toBinaryString(Double.doubleToLongBits((Double.parseDouble(sI) / 0.625))).length());
					}

					label.getChildren().add(new TreeItem<String>(sI));
				});

				rootItem.getChildren().add(label);
			}
		});

	}

	private void draw() {
		int initialX = 35, initialY = 100;
		addRectangle(initialX, initialY, "Preamble", 65, 65, 1);
		addRectangle(getNextInitialX(initialX), initialY, "Access Address", 160, 65, 4);
		Rectangle payload = addRectangle(getNextInitialX(initialX), initialY, "Protocol Data Unit (PDU)", 500, 65, 0);

		addRectangleSize(generatePDU(initialX, initialY), payload);

		addRectangle(getNextInitialX(initialX), initialY, "CRC", 130, 65, 3);
	}

	private int generatePDU(int initialX, int initialY) {
		int sizeValue = 0;
		Rectangle previousRectangle = drawNextLines();
		initialX = previousRectangle.xProperty().intValue();
		initialY = previousRectangle.yProperty().add(previousRectangle.heightProperty()).intValue() + 30;

		switch (packet.getPacketType()) {
			case ADV_EXT_IND -> {
				addRectangle((initialX), initialY, "Header", 125, 65, 2);
				Rectangle AdvExtPayload = addRectangle(getNextInitialX(initialX), initialY, "Advertising Payload", 375, 65,
						0);
				initialX = previousRectangle.xProperty().intValue();
				initialY = (int) initialY + 30 + 65;
				sizeValue = generatePayload(initialX, initialY);
				addRectangleSize(sizeValue, AdvExtPayload);
				sizeValue += 2;
			}
			case ADV_IND, ADV_NONCONN_IND -> {
				addRectangle((initialX), initialY, "Header", 125, 65, 2);
				Rectangle AdvOneMpayload = addRectangle(getNextInitialX(initialX), initialY, "Advertising Payload", 375, 65,
						0);
				initialX = previousRectangle.xProperty().intValue();
				initialY = (int) initialY + 30 + 65;
				sizeValue = generatePayload(initialX, initialY);
				addRectangleSize(sizeValue, AdvOneMpayload);
				sizeValue += 2;
			}
			case AUX_ADV_IND -> {
				addRectangle((initialX), initialY, "Header", 125, 65, 2);
				Rectangle AdvTwoMpayload = addRectangle(getNextInitialX(initialX), initialY, "Advertising Payload", 375, 65,
						0);
				initialX = previousRectangle.xProperty().intValue();
				initialY = (int) initialY + 30 + 65;
				sizeValue = generatePayload(initialX, initialY);
				addRectangleSize(sizeValue, AdvTwoMpayload);
				sizeValue += 2;
			}
			case AUX_CONNECT_REQ, CONNECT_IND -> {
				addRectangle((initialX), initialY, "Header", 125, 65, 2);
				Rectangle connectReqPayload = addRectangle(getNextInitialX(initialX), initialY,
						"Connection Request Payload", 375, 65, 0);
				initialX = previousRectangle.xProperty().intValue();
				initialY = (int) initialY + 30 + 65;
				sizeValue = generatePayload(initialX, initialY);
				addRectangleSize(sizeValue, connectReqPayload);
				sizeValue += 2;
			}
			case AUX_CONNECT_RSP -> {
				addRectangle((initialX), initialY, "Header", 125, 65, 2);
				Rectangle connectRspPayload = addRectangle(getNextInitialX(initialX), initialY,
						"Connection Response Payload", 375, 65, 0);
				initialX = previousRectangle.xProperty().intValue();
				initialY = (int) initialY + 30 + 65;
				sizeValue = generatePayload(initialX, initialY);
				addRectangleSize(sizeValue, connectRspPayload);
				sizeValue += 2;
			}
			case EMPTY_LL_DATA -> {
				addRectangle((initialX), initialY, "Header", 125, 65, 2);
				Rectangle emptyPayload = addRectangle(getNextInitialX(initialX), initialY, "Data Payload", 375, 65, 0);
				initialX = previousRectangle.xProperty().intValue();
				initialY = (int) initialY + 30 + 65;
				sizeValue = generatePayload(initialX, initialY);
				addRectangleSize(sizeValue, emptyPayload);
				sizeValue += 2;
			}
			case LL_TERMINATE_IND -> {
				addRectangle((initialX), initialY, "Header", 125, 65, 2);
				Rectangle terminatePayload = addRectangle(getNextInitialX(initialX), initialY, "Data Payload", 375, 65, 0);
				initialX = previousRectangle.xProperty().intValue();
				initialY = (int) initialY + 30 + 65;
				sizeValue = generatePayload(initialX, initialY);
				addRectangleSize(sizeValue, terminatePayload);
				sizeValue += 2;
			}
			case LL_DATA -> {
				addRectangle((initialX), initialY, "Header", 125, 65, 2);
				Rectangle dataPayload = addRectangle(getNextInitialX(initialX), initialY, "Data Payload", 250, 65, 0);
				initialX = previousRectangle.xProperty().intValue();
				initialY = (int) initialY + 30 + 65;
				sizeValue = generatePayload(initialX, initialY);
				addRectangleSize(sizeValue, dataPayload);
				initialY = previousRectangle.yProperty().add(previousRectangle.heightProperty()).intValue() + 30;
				addRectangle(getNextInitialX(initialX), initialY, "MIC", 125, 65, 4);
				sizeValue += 6;
			}
			default -> {
			}
		}

		return sizeValue;
	}

	private int generatePayload(int initialX, int initialY) {
		int sizeValue = 0;

		Rectangle previousRectangle = drawNextLines();
		initialX = previousRectangle.xProperty().intValue();
		initialY = previousRectangle.yProperty().add(previousRectangle.heightProperty()).intValue() + 30;

		switch (packet.getPacketType()) {
			case ADV_EXT_IND -> {
				addRectangle((initialX), initialY, "Adv Mode", 75, 65, 1);
				if (packet.getDeviceFrom().getPacketFactory().isConnectable()) {
					addRectangle(getNextInitialX(initialX), initialY, "AdvDataInfo", 150, 65, 2);
					addRectangle(getNextInitialX(initialX), initialY, "Aux Ptr", 150, 65, 3);
					sizeValue = 1 + 2 + 3;
				} else {
					addRectangle(getNextInitialX(initialX), initialY, "AdvA", 300, 65, 6);
					sizeValue = 1 + 6;
				}
			}
			case ADV_IND, ADV_NONCONN_IND -> {
				int payloadSizeOneM = packet.getDeviceFrom().getName().getBytes().length
						+ (packet.getDeviceFrom().getPacketFactory().isConnectable() ? 2 : 0) + 5 + Long
						.toBinaryString(Double.doubleToLongBits((Double.parseDouble(
								packet.getDeviceFrom().getPacketFactory().getAdvertisingInterval()) / 0.625)))
						.length() / 8;
				addRectangle((initialX), initialY, "AdvA", 150, 65, 6);
				addRectangle(getNextInitialX(initialX), initialY, "AdvData", 225, 65, payloadSizeOneM);
				sizeValue = payloadSizeOneM + 6;
			}
			case AUX_ADV_IND -> {
				int payloadSizeTwoM = packet.getDeviceFrom().getName().getBytes().length
						+ (packet.getDeviceFrom().getPacketFactory().isConnectable() ? 2 : 0) + 5 + Long
						.toBinaryString(Double.doubleToLongBits((Double.parseDouble(
								packet.getDeviceFrom().getPacketFactory().getAdvertisingInterval()) / 0.625)))
						.length() / 8;
				addRectangle((initialX), initialY, "AdvA", 75, 65, 6);
				addRectangle(getNextInitialX(initialX), initialY, "AdvDataInfo", 75, 65, 2);
				addRectangle(getNextInitialX(initialX), initialY, "AdvData", 225, 65, payloadSizeTwoM);
				sizeValue = payloadSizeTwoM + 6 + 2;
			}
			case AUX_CONNECT_REQ, CONNECT_IND -> {
				addRectangle((initialX), initialY, "InitA", 75, 65, 6);
				addRectangle(getNextInitialX(initialX), initialY, "AdvA", 75, 65, 6);
				addRectangle(getNextInitialX(initialX), initialY, "LLData", 225, 65, 22);
				sizeValue = 6 + 6 + 22;
			}
			case AUX_CONNECT_RSP -> {
				addRectangle((initialX), initialY, "Adv Mode", 75, 65, 2);
				addRectangle(getNextInitialX(initialX), initialY, "AdvA", 150, 65, 6);
				addRectangle(getNextInitialX(initialX), initialY, "TargetA", 150, 65, 6);
				sizeValue = 2 + 6 + 6;
			}
			case EMPTY_LL_DATA -> {
				addRectangle((initialX), initialY, "OpCode", 75, 65, 1);
				Rectangle emptyPayload = addRectangle(getNextInitialX(initialX), initialY, "CtrData", 300, 65, 0);
				addRectangleSize(0, emptyPayload);
				sizeValue = 1;
			}
			case LL_TERMINATE_IND -> {
				addRectangle((initialX), initialY, "OpCode", 75, 65, 1);
				addRectangle(getNextInitialX(initialX), initialY, "CtrData", 300, 65, 1);
				sizeValue = 1 + 1;
			}
			case LL_DATA -> {
				int payloadSizeData = 2 + 2 + 2 + 1 + 12;
				addRectangle((initialX), initialY, "L2CAP Hdr", 100, 65, 4);
				addRectangle(getNextInitialX(initialX), initialY, "OpCode", 50, 65, 1);
				addRectangle(getNextInitialX(initialX), initialY, "Data", 100, 65, payloadSizeData);
				sizeValue = payloadSizeData + 4 + 1;
			}
			default -> {
			}
		}

		return sizeValue;
	}

	private Rectangle drawNextLines() {
		Rectangle payloadRectangle = rectangles.get(rectangles.size() - 1);
		int startX = payloadRectangle.xProperty().intValue();
		int endX = payloadRectangle.xProperty().add(payloadRectangle.widthProperty()).intValue();
		int y = payloadRectangle.yProperty().add(payloadRectangle.heightProperty()).intValue();

		Line line1 = new Line(startX, y, startX, y + 30);
		Line line2 = new Line(endX, y, endX, y + 30);

		anchorPane.getChildren().addAll(line1, line2);

		return payloadRectangle;
	}

	private Rectangle addRectangle(int startX, int startY, String label, int width, int height, int biteSize) {
		Rectangle rectangle = new Rectangle(startX, startY, width, height);
		rectangle.setFill(Color.DARKGRAY);

		rectangle.setStyle(
				"-fx-background-color: black, red; -fx-background-insets: 0, 5; -fx-min-width: 20; -fx-min-height:20; -fx-max-width:20; -fx-max-height: 20;");

		addBorder(rectangle);

		anchorPane.getChildren().add(rectangle);
		rectangles.add(rectangle);

		addRectangleTitle(label, rectangle);

		if (biteSize != 0)
			addRectangleSize(biteSize, rectangle);

		return rectangle;
	}

	private void addBorder(Rectangle rectangle) {
		Line leftBorder = new Line(rectangle.xProperty().doubleValue(), rectangle.yProperty().add(1).doubleValue(),
				rectangle.xProperty().doubleValue(),
				rectangle.yProperty().add(rectangle.heightProperty()).subtract(1).doubleValue());

		Line rightBorder = new Line(rectangle.xProperty().add(rectangle.widthProperty()).doubleValue(),
				rectangle.yProperty().doubleValue(), rectangle.xProperty().add(rectangle.widthProperty()).doubleValue(),
				rectangle.yProperty().add(rectangle.heightProperty()).doubleValue());

		Line topBorder = new Line(rectangle.xProperty().doubleValue(), rectangle.yProperty().doubleValue(),
				rectangle.xProperty().add(rectangle.widthProperty()).doubleValue(),
				rectangle.yProperty().doubleValue());

		Line bottomBorder = new Line(rectangle.xProperty().doubleValue(),
				rectangle.yProperty().add(rectangle.heightProperty()).doubleValue(),
				rectangle.xProperty().add(rectangle.widthProperty()).doubleValue(),
				rectangle.yProperty().add(rectangle.heightProperty()).doubleValue());

		anchorPane.getChildren().addAll(leftBorder, rightBorder, topBorder, bottomBorder);
	}

	private void addRectangleTitle(String label, Rectangle rectangle) {
		Text text = new Text(label);
		text.setBoundsType(TextBoundsType.VISUAL);
		text.xProperty().bind((rectangle.xProperty()).add(rectangle.widthProperty().divide(2))
				.subtract(text.getLayoutBounds().getWidth() / 2));
		text.yProperty().bind((rectangle.yProperty()).subtract(10));

		anchorPane.getChildren().addAll(text);
	}

	private void addRectangleSize(int biteSize, Rectangle rect) {
		Text text = new Text(String.valueOf(biteSize));
		text.setBoundsType(TextBoundsType.VISUAL);
		text.xProperty().bind(
				(rect.xProperty()).add(rect.widthProperty().divide(2)).subtract(text.getLayoutBounds().getWidth() / 2));
		text.yProperty().bind((rect.yProperty()).add(rect.heightProperty().divide(2)));

		anchorPane.getChildren().addAll(text);
	}

	private int getNextInitialX(int initialX) {
		return rectangles.get(rectangles.size() - 1).xProperty()
				.add(rectangles.get(rectangles.size() - 1).widthProperty()).intValue();
	}

}
