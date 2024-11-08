package gui;

import core.device.model.Device;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
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
import java.util.stream.Collectors;

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
    Menu fileMenuLabel, projectMenuLabel, helpMenuLabel;

    @FXML
    MenuItem closeMenuLabel, addDeviceMenuLabel, speedMenuLabel, languageMenuLabel;

    @FXML
    Tab sceneTabLabel, snifferTabLabel, messageTabLabel, statisticsTabLabel;

    @FXML
    Button refreshButton;

    //TODO
    @FXML
    ScatterChart<String, Number> rssiScatterChart;
    ObservableList<XYChart.Series<String, Number>> rssiScatterChartData = FXCollections.observableArrayList();

    @FXML
    LineChart<String, Number> rssiLineChart;

    @FXML
    ListView<Device> devicesListView;

    ObservableList<Device> devices = Singleton.getInstance().devices;
    Device master = Singleton.getInstance().master;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        addMaster();
        addDummy();

        loadSpeedMenu();
        loadSlaveComboBox();
        loadMessageSequencePane();
        loadTableView();
        loadDeviceListView();
        loadRssiScatterChart();
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
        ResourceBundle bundle = ResourceBundle.getBundle("view.lang", locale);

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

            DeviceStatisticsUtil.setSentPacketsToggle(selectedRadioButton.getText().equals("Sent Packets"));
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
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setSortType(TableColumn.SortType.DESCENDING);

        TableColumn<Packet, String> typeColumn = new TableColumn<>("Type");
        //typeColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("typeLabel"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("packetType"));

        TableColumn<Packet, String> fromColumn = new TableColumn<>("From");
        //fromColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("fromLabel"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("deviceFrom"));

        TableColumn<Packet, String> toColumn = new TableColumn<>("To");
        //toColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("toLabel"));
        toColumn.setCellValueFactory(new PropertyValueFactory<>("deviceTo"));

        TableColumn<Packet, String> channelColumn = new TableColumn<>("Channel");
        //channelColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("channelLabel"));
        channelColumn.setCellValueFactory(new PropertyValueFactory<>("channel"));

        TableColumn<Packet, String> dataRateColumn = new TableColumn<>("Data Rate");
        //dataRateColumn.setText(ResourceBundle.getBundle("view.lang", Locale.getDefault()).getString("dataRateLabel"));
        dataRateColumn.setCellValueFactory(new PropertyValueFactory<>("dataRate"));

        this.packetTableView.getColumns().setAll(timeColumn, typeColumn, fromColumn, toColumn, channelColumn,
                dataRateColumn);

        this.packetTableView.setItems(master.getAllPackets());
    }

    public void loadDeviceListView(){
        this.devicesListView.setItems(devices);

        this.devicesListView.setOnMouseClicked(click -> {
            //TODO
            if (click.getClickCount() == 2) {
                Device device = this.devicesListView.getSelectionModel().getSelectedItem();

                if(rssiScatterChartData.stream().noneMatch(e -> e.getName().equals(device.toString())))
                    this.rssiScatterChartData.add(generateRSSIData(device));
                else
                    this.rssiScatterChartData.remove(rssiScatterChartData.stream().filter(e -> e.getName().equals(device.toString())).collect(Collectors.toList()).get(0));
            }
        });
    }

    public XYChart.Series<String, Number> generateRSSIData(Device device) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 1; i <= 20; i++)
            series.getData().add(new XYChart.Data<>(String.valueOf(i) , master.getTransceiver().calculateRSSI(device, i)));

        series.setName(device.toString());
        return series;
    }

    //TODO
    public void loadRssiScatterChart() {
        CategoryAxis xAxis = (CategoryAxis) rssiLineChart.getXAxis();
        xAxis.setSide(Side.TOP);
        xAxis.setLabel("Meters");
        xAxis.getCategories().setAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        NumberAxis yAxis = (NumberAxis) rssiLineChart.getYAxis();
        yAxis.setLabel("RSSI");

        this.rssiLineChart.setData(this.rssiScatterChartData);
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
            stage.setScene(new Scene(root, 750, 490));
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

        this.devicesListView.refresh();
    }

    private void addMaster() {
        ScenePaneUtil.makeDraggable(master, scenePane);
        ScenePaneUtil.onClickListener(master, scenePane, detailsTreeView);
        this.scenePane.getChildren().add(master.getDeviceCircle().getCircle());
        master.getDeviceCircle().getCircle().setLayoutX(300);
        master.getDeviceCircle().getCircle().setLayoutY(300);

        MasterPacketFactory.listenForReceivedPacketsOnMaster(master);
    }

    private void addDummy() {
        Device dummy = Singleton.getInstance().dummy;

        Singleton.getInstance().executor.execute(dummy.getPacketFactory());
        System.out.println(Singleton.getInstance().executor.getActiveCount());

        devices.add(dummy);
        ScenePaneUtil.makeDraggable(dummy, scenePane);
        ScenePaneUtil.onClickListener(dummy, scenePane, detailsTreeView);

        this.scenePane.getChildren().add(dummy.getDeviceCircle().getCircle());
        this.scenePane.getChildren().add(dummy.getDeviceCircle().getLine());
        this.scenePane.getChildren().add(dummy.getDeviceCircle().getText());
        dummy.getDeviceCircle().getLine().setVisible(false);

        dummy.getDeviceCircle().getCircle().setLayoutX(100);
        dummy.getDeviceCircle().getCircle().setLayoutY(100);

        //Platform.runLater(() -> this.devicesListView.refresh());
    }

    public static String getRandomColorName() {
        Random random = new Random();
        String[] colors = {"DARKTURQUOISE", "AQUA", "DARKGREEN", "MAROON", "GOLDENROD", "NAVY", "OLIVE", "DARKBLUE",
                "MAGENTA", "PERU"};

        return colors[random.nextInt(9)];
    }

    public void close() {
        Platform.exit();
    }

}
