package gui;

import core.device.model.Device;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.controlsfx.control.PropertySheet;
import packet.controller.PacketController;
import packet.factory.MasterPacketFactory;
import packet.model.Packet;
import core.*;
import packet.sniffing.MessageSequenceFactory;
import statistics.DeviceStatisticsUtil;
import topology.modification.AddDeviceController;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;

public class AppController implements Initializable {

	@FXML
	Pane scenePane;

	@FXML
	ScatterChart<Integer, Integer> coordinateSystem;

	@FXML
	GridPane messageSequenceGridPane;

	@FXML
	AnchorPane sceneAnchorPane, parentAnchorPane, messageSequencePane;

	@FXML
	PropertySheet propertySheet;

	@FXML
	TreeView<String> detailsTreeView;

	@FXML
	TabPane tabPane;

	@FXML
	ComboBox<String> speedComboBox;

	@FXML
	ComboBox<Device> slaveComboBox, slaveInfoComboBox;

	@FXML
	TableView<Packet> packetTableView;

	@FXML
	TableView<Packet> slavePacketTableView;

	@FXML
	ScrollPane scrollPane;

	@FXML
	StackedBarChart<String, Integer> channelStackedBarChart;

	@FXML
	RadioButton sentPacketsRadioButton, receivedPacketsRadioButton;

	@FXML
	PieChart ratioPieChart;

	@FXML
	RadioMenuItem speed025, speed05, speed075, speed1, englishLanguageRadioButton, bulgarianLanguageRadioButton;
	
	@FXML
	Menu fileMenuLabel, projectMenuLabel,helpMenuLabel;
	
	@FXML
	MenuItem closeMenuLabel, addDeviceMenuLabel, speedMenuLabel, languageMenuLabel;
	
	@FXML
	Tab sceneTabLabel, snifferTabLabel, messageTabLabel, statisticsTabLabel;
	
	@FXML
	Button refreshButton;

	ObservableList<Device> devices = Singleton.getInstance().devices;
	Device master = Singleton.getInstance().master;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		addMaster();

		loadSpeedMenu();
		loadSlaveComboBox();
		loadMessageSequencePane();
		loadTableView();
		loadChannelBarChart();
		loadStatisticsTab();
		languageLoader();
	}

	private void languageLoader() {
		englishLanguageRadioButton.setSelected(true);

		ToggleGroup toggleGroup = new ToggleGroup();
		toggleGroup.getToggles().add(englishLanguageRadioButton);
		toggleGroup.getToggles().add(bulgarianLanguageRadioButton);
	}

	public void changeLanguageEnglish() {
		Locale.setDefault(new Locale("en"));
		loadView(new Locale("en"));
	}

	public void changeLanguageBulgarian() {
		Locale.setDefault(new Locale("bg"));
		loadView(new Locale("bg"));
	}

	private void loadView(Locale locale) {
		ResourceBundle  bundle = ResourceBundle.getBundle("view.lang", locale);
		
		fileMenuLabel.setText(bundle.getString("fileMenuLabel"));
		closeMenuLabel.setText(bundle.getString("closeMenuLabel"));
		projectMenuLabel.setText(bundle.getString("projectMenuLabel"));
		addDeviceMenuLabel.setText(bundle.getString("addDeviceMenuLabel"));
		speedMenuLabel.setText(bundle.getString("speedMenuLabel"));
		helpMenuLabel.setText(bundle.getString("helpMenuLabel"));
		languageMenuLabel.setText(bundle.getString("languageMenuLabel"));
		
		sceneTabLabel.setText(bundle.getString("sceneTabLabel"));
		snifferTabLabel.setText(bundle.getString("snifferTabLabel"));
		messageTabLabel.setText(bundle.getString("messageTabLabel"));
		statisticsTabLabel.setText(bundle.getString("statisticsTabLabel"));
		
		channelStackedBarChart.setTitle(bundle.getString("packetChartLabel"));
		channelStackedBarChart.getYAxis().setLabel(bundle.getString("packetChartCountLabel"));
		channelStackedBarChart.getXAxis().setLabel(bundle.getString("packetChartChannelLabel"));
		
		refreshButton.setText(bundle.getString("refreshButton"));
		
		ratioPieChart.setTitle(bundle.getString("ratioPieChartLabel"));
		
		sentPacketsRadioButton.setText(bundle.getString("sentPacketsRadioButtonLabel"));
		receivedPacketsRadioButton.setText(bundle.getString("receivedPacketsRadioButtonLabel"));
		
		loadTableView();
		DeviceStatisticsUtil.loadTableView();
		DeviceStatisticsUtil.loadPieChart();
	}

	private void toggleListener(ToggleGroup toggle) {
		toggle.selectedToggleProperty().addListener((ov, t, t1) -> {
			RadioButton selectedRadioButton = (RadioButton) toggle.getSelectedToggle();

			if (selectedRadioButton.getText().equals("Sent Packets"))
				DeviceStatisticsUtil.setSentPacketsToggle(true);
			else
				DeviceStatisticsUtil.setSentPacketsToggle(false);
		});
	}

	private void loadMessageSequencePane() {
		MessageSequenceFactory.setMessageSequencePane(messageSequencePane);
		MessageSequenceFactory.setScrollPane(scrollPane);
	}

	public void refreshMessageDiagram(ActionEvent event) {
		if (MessageSequenceFactory.getCurrentSlave() != null)
			MessageSequenceFactory.refresh();
	}

	private void loadSlaveComboBox() {
		this.slaveComboBox.setItems(devices);
		this.slaveComboBox.valueProperty().addListener((ob, ov, nv) -> {
			MessageSequenceFactory.setCurrentSlave(nv);
		});

		this.slaveInfoComboBox.setItems(devices);
		this.slaveInfoComboBox.valueProperty().addListener((ob, ov, nv) -> {
			if (nv != null)
				DeviceStatisticsUtil.setDevice(nv);
			else
				DeviceStatisticsUtil.setDevice(new Device());
		});
	}

	public void loadSpeedMenu() {
		speed1.setSelected(true);

		ToggleGroup toggleGroup = new ToggleGroup();
		toggleGroup.getToggles().add(speed025);
		toggleGroup.getToggles().add(speed05);
		toggleGroup.getToggles().add(speed075);
		toggleGroup.getToggles().add(speed1);
	}

	public void changeSpeed025() {
		changeSpeed("0.25");
	}

	public void changeSpeed05() {
		changeSpeed("0.5");
	}

	public void changeSpeed075() {
		changeSpeed("0.75");
	}

	public void changeSpeed1() {
		changeSpeed("1");
	}

	public void changeSpeed(String speed) {
		Singleton.getInstance().speed = Double.valueOf(speed);
	}

	private void loadStatisticsTab() {
		DeviceStatisticsUtil.setTableView(this.slavePacketTableView);
		DeviceStatisticsUtil.setPieChart(this.ratioPieChart);

		ToggleGroup deviceTableViewToggle = new ToggleGroup();
		this.sentPacketsRadioButton.setToggleGroup(deviceTableViewToggle);
		this.receivedPacketsRadioButton.setToggleGroup(deviceTableViewToggle);

		toggleListener(deviceTableViewToggle);
	}

	public void loadChannelBarChart() {
		XYChart.Series<String, Integer> advInd = new XYChart.Series<>();
		advInd.setName("ADV_IND");

		XYChart.Series<String, Integer> advExtInd = new XYChart.Series<>();
		advExtInd.setName("ADV_EXT_IND");

		XYChart.Series<String, Integer> auxAdvInd = new XYChart.Series<>();
		auxAdvInd.setName("AUX_ADV_IND");

		XYChart.Series<String, Integer> connectInd = new XYChart.Series<>();
		connectInd.setName("CONNECT_IND");

		XYChart.Series<String, Integer> auxConnectReq = new XYChart.Series<>();
		auxConnectReq.setName("AUX_CONNECT_REQ");

		XYChart.Series<String, Integer> auxConnectRsp = new XYChart.Series<>();
		auxConnectRsp.setName("AUX_CONNECT_RSP");

		XYChart.Series<String, Integer> emptyLLdata = new XYChart.Series<>();
		emptyLLdata.setName("EMPTY_LL_DATA");

		XYChart.Series<String, Integer> llData = new XYChart.Series<>();
		llData.setName("LL_DATA");

		XYChart.Series<String, Integer> terminateIND = new XYChart.Series<>();
		terminateIND.setName("LL_TERMINATE_IND");

		master.getAllPackets().addListener(new ListChangeListener<Packet>() {

			@Override
			public void onChanged(Change<? extends Packet> c) {
				Packet lastPacket = c.getList().get(c.getList().size() - 1);

				Platform.runLater(() -> {
					switch (lastPacket.getPacketType()) {
						case ADV_IND -> advInd.getData().add(new XYChart.Data<>(lastPacket.getChannel(), 1));
						case ADV_EXT_IND -> advExtInd.getData().add(new XYChart.Data<>(lastPacket.getChannel(), 1));
						case AUX_ADV_IND -> auxAdvInd.getData().add(new XYChart.Data<>(lastPacket.getChannel(), 1));
						case CONNECT_IND -> connectInd.getData().add(new XYChart.Data<>(lastPacket.getChannel(), 1));
						case AUX_CONNECT_REQ -> auxConnectReq.getData().add(new XYChart.Data<>(lastPacket.getChannel(), 1));
						case AUX_CONNECT_RSP -> auxConnectRsp.getData().add(new XYChart.Data<>(lastPacket.getChannel(), 1));
						case EMPTY_LL_DATA -> emptyLLdata.getData().add(new XYChart.Data<>(lastPacket.getChannel(), 1));
						case LL_DATA -> llData.getData().add(new XYChart.Data<>(lastPacket.getChannel(), 1));
						case LL_TERMINATE_IND -> terminateIND.getData().add(new XYChart.Data<>(lastPacket.getChannel(), 1));
						default -> {
						}
					}
				});
			}
		});

		for (int i = 0; i < 40; i++) {
			advExtInd.getData().add(new XYChart.Data<String, Integer>(Integer.toString(i), 0));
		}

		this.channelStackedBarChart.setCategoryGap(0);
		this.channelStackedBarChart.getData().setAll(advInd, advExtInd, auxAdvInd, connectInd, auxConnectReq,
				auxConnectRsp, emptyLLdata, llData, terminateIND);
	}

	public void loadTableView() {

		this.packetTableView.setRowFactory(tv -> {
			TableRow<Packet> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Parent root;
					try {
						PacketController.setPacket(row.getItem());
						root = FXMLLoader.load(getClass().getResource("/gui/PacketPane.fxml"));
						Stage stage = new Stage();
						stage.setTitle("Packet Info");
						stage.setScene(new Scene(root, 1000, 700));
						stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toString()));
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

		TableColumn<Packet, String> channelColumn = new TableColumn<>("Channel");
		//channelColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("channelLabel"));
		channelColumn.setCellValueFactory(new PropertyValueFactory<Packet, String>("channel"));

		TableColumn<Packet, String> dataRateColumn = new TableColumn<>("Data Rate");
		//dataRateColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("dataRateLabel"));
		dataRateColumn.setCellValueFactory(new PropertyValueFactory<Packet, String>("dataRate"));

		this.packetTableView.getColumns().setAll(timeColumn, typeColumn, fromColumn, toColumn, channelColumn,
				dataRateColumn);

		this.packetTableView.setItems(master.getAllPackets());
	}

	public void addDeviceButton(ActionEvent event) {
		Parent root;
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/AddDeviceWindow.fxml"));
			//fxmlLoader.setResources(ResourceBundle.getBundle("view.lang", Locale.getDefault()));
			root = fxmlLoader.load();
			Stage stage = new Stage();
			stage.setTitle("Add Device");
			stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toString()));
			stage.setScene(new Scene(root, 700, 500));
			stage.setResizable(true);
			stage.show();
			stage.setOnHiding(event1 -> {
				if (AddDeviceController.getDevice() != null)
					addDevice(AddDeviceController.getDevice());
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addDevice(Device device) {
		Singleton.getInstance().executor.execute(device.getPacketFactory());
		System.out.println(Singleton.getInstance().executor.getActiveCount());

		devices.add(device);
		ScenePaneUtil.makeDraggable(device, scenePane);
		ScenePaneUtil.onClickListener(device, scenePane, detailsTreeView);

		this.scenePane.getChildren().add(device.getDeviceCircle().getCircle());
		this.scenePane.getChildren().add(device.getDeviceCircle().getLine());
		this.scenePane.getChildren().add(device.getDeviceCircle().getText());
		device.getDeviceCircle().getLine().setVisible(false);

		device.getDeviceCircle().getCircle().setLayoutX(100);
		device.getDeviceCircle().getCircle().setLayoutY(100);
	}

	private void addMaster() {
		ScenePaneUtil.makeDraggable(master, scenePane);
		ScenePaneUtil.onClickListener(master, scenePane, detailsTreeView);
		this.scenePane.getChildren().add(master.getDeviceCircle().getCircle());
		master.getDeviceCircle().getCircle().setLayoutX(300);
		master.getDeviceCircle().getCircle().setLayoutY(300);
		
		MasterPacketFactory.listenForReceivedPacketsOnMaster(master);
	}
	
	public static String getRandomColorName() {
		Random random = new Random();
		String[] colors = { "DARKTURQUOISE", "AQUA", "DARKGREEN", "MAROON", "GOLDENROD", "NAVY", "OLIVE", "DARKBLUE",
				"MAGENTA", "PERU" };

		return colors[random.nextInt(9)];
	}
	
	public void close() {
		Platform.exit();
	}

}
