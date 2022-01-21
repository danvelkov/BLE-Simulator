package statistics;

import packet.controller.PacketController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import core.device.model.Device;
import packet.model.Packet;

import java.io.IOException;
import java.util.Locale;

public class DeviceStatisticsUtil {
	static Device device;
	static TableView<Packet> tableView;
	static PieChart pieChart;
	static boolean sentPacketsToggle = true;
	static Label caption = new Label("");

	public static Device getDevice() {
		return device;
	}

	public static void setDevice(Device device) {
		DeviceStatisticsUtil.device = device;
		load();
	}

	public static TableView<Packet> getTableView() {
		return tableView;
	}

	public static void setTableView(TableView<Packet> tableView) {
		DeviceStatisticsUtil.tableView = tableView;
	}

	public static PieChart getPieChart() {
		return pieChart;
	}

	public static void setPieChart(PieChart pieChart) {
		DeviceStatisticsUtil.pieChart = pieChart;
	}

	public static boolean isSentPacketsToggle() {
		return sentPacketsToggle;
	}

	public static void setSentPacketsToggle(boolean sentPacketsToggle) {
		DeviceStatisticsUtil.sentPacketsToggle = sentPacketsToggle;
		loadTableView();
	}

	public static void load() {
		AnchorPane parent = (AnchorPane) pieChart.getParent().getParent();
		parent.getChildren().remove(caption);

		caption.setVisible(false);

		loadTableView();

		parent.getChildren().add(caption);
		loadPieChart();
	}

	public static void loadTableView() {
		if (sentPacketsToggle)
			loadTable(device.getPacketsSent());
		else
			loadTable(device.getPacketsReceived());
	}

	@SuppressWarnings("unchecked")
	public static void loadTable(ObservableList<Packet> packetList) {
		tableView.setRowFactory(tv -> {
			TableRow<Packet> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Parent root;
					try {
						PacketController.setPacket(row.getItem());
						root = FXMLLoader.load(DeviceStatisticsUtil.class.getResource("/gui/PacketPane.fxml"));
						Stage stage = new Stage();
						stage.setTitle("Packet Info");
						stage.setScene(new Scene(root, 1000, 700));
						//stage.getIcons().add(new Image(DeviceStatisticsUtil.class.getResourceAsStream("/images/logo.png")));
						stage.setResizable(false);
						stage.show();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			return row;
		});

		TableColumn<Packet, String> timeColumn = new TableColumn<>("Time");
		//timeColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("timeLabel"));
		timeColumn.setCellValueFactory(new PropertyValueFactory<Packet, String>("time"));
		timeColumn.setSortType(TableColumn.SortType.DESCENDING);

		TableColumn<Packet, String> typeColumn = new TableColumn<>("Type");
		//typeColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("typeLabel"));
		typeColumn.setCellValueFactory(new PropertyValueFactory<Packet, String>("packetType"));

		TableColumn<Packet, String> fromColumn = new TableColumn<>("From");
		//fromColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("fromLabel"));
		fromColumn.setCellValueFactory(new PropertyValueFactory<Packet, String>("deviceFrom"));

		TableColumn<Packet, String> toColumn = new TableColumn<>("To");
		//toColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("toLabel"));
		toColumn.setCellValueFactory(new PropertyValueFactory<Packet, String>("deviceTo"));

		TableColumn<Packet, String> channelColumn = new TableColumn<>("%Channel");
		//channelColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("channelLabel"));
		channelColumn.setCellValueFactory(new PropertyValueFactory<Packet, String>("channel"));

		TableColumn<Packet, String> dataRateColumn = new TableColumn<>("%Data Rate");
		//dataRateColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("dataRateLabel"));
		dataRateColumn.setCellValueFactory(new PropertyValueFactory<Packet, String>("dataRate"));

		tableView.getColumns().setAll(timeColumn, typeColumn, fromColumn, toColumn, channelColumn, dataRateColumn);

		tableView.setItems(packetList);
	}

	public static void loadPieChart() {
		PieChart.Data slice1 = new PieChart.Data("Standby", 0);
		//slice1.setName(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("standbyLabel"));
		PieChart.Data slice2 = new PieChart.Data("Advertising", 0);
		//slice2.setName(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("advertisingLabel"));
		PieChart.Data slice3 = new PieChart.Data("Connected", 0);
		//slice3.setName(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("connectedLabel"));

		slice1.pieValueProperty().bind(device.getStandbyTime());
		slice2.pieValueProperty().bind(device.getAdvertisingTime());
		slice3.pieValueProperty().bind(device.getConnectedTime());

		ObservableList<PieChart.Data> data = FXCollections.observableArrayList(slice1, slice2, slice3);
		pieChart.setData(data);

		for (final PieChart.Data d : pieChart.getData()) {
			d.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
				@SuppressWarnings("unlikely-arg-type")
				@Override
				public void handle(MouseEvent e) {
					caption.setVisible(false);
					caption.setTranslateX(e.getSceneX());
					caption.setTranslateY(e.getSceneY() - 100);

					if (!Locale.getDefault().equals("bg"))
						caption.setText(
								d.getName() + " state duration: " + String.valueOf(d.getPieValue()) + " seconds");
					else
						caption.setText(
								"Продължителност в " + d.getName() + ": " + String.valueOf(d.getPieValue()) + " секунди");

					caption.setVisible(true);
				}
			});

		}
	}
}
