package gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import topology.modification.ConnectionController;
import topology.modification.ConnectionUtil;
import packet.controller.SendPacketController;
import core.Singleton;
import core.device.model.Device;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;
import packet.model.Packet;
import packet.model.PacketType;

import java.io.IOException;
import java.util.*;

public class ScenePaneUtil {

    public static void onClickListener(Device device, Pane scenePane, TreeView<String> detailsTreeView) {
        device.getDeviceCircle().getCircle().setOnMouseClicked(event -> {
            ScenePaneUtil.updateDetailsTreeView(device, detailsTreeView);

            if (event.getButton() == MouseButton.SECONDARY && !device.getName().equals("Master")) {
                openConnectMenu(device, event, scenePane, detailsTreeView);
            }
        });

        device.getDeviceCircle().getCircle().setOnMouseReleased(event -> {
            ScenePaneUtil.updateDetailsTreeView(device, detailsTreeView);
        });
    }

    public static void updateDetailsTreeView(Device device, TreeView<String> detailsTreeView) {
        Circle circle = device.getDeviceCircle().getCircle();

        TreeItem<String> rootItem = new TreeItem<>(circle.getId());
        rootItem.setExpanded(true);
        List<TreeItem<String>> detailsList = new ArrayList<>();
        //detailsList.add(new TreeItem<>(String.valueOf(circle.getLayoutX() - circle.getRadius())));
        //detailsList.add(new TreeItem<>(String.valueOf(circle.getLayoutY() - circle.getRadius())));
        detailsList.add(new TreeItem<>("Name: " + device.getName()));
        detailsList.add(new TreeItem<>("Transmit Power: " + device.getTransceiver().getPowerTransmit()));
        detailsList.add(new TreeItem<>("Receiver Sensitivity: " + device.getTransceiver().getReceiverSensitivity()));
        if (!device.getName().equals("Master"))
           detailsList.add(new TreeItem<String>("RSSI: " + Singleton.getInstance().master.getTransceiver().calculateRSSI(device, Double.parseDouble(device.getDeviceCircle().getText().getText().replaceAll("[^0-9]", ""))).toString()));
////
        detailsList.add(new TreeItem<>("Address: " + device.getDeviceAddress().getAddress()));
        detailsList.add(new TreeItem<>("Address Type: " + device.getDeviceAddress().getType()));
        detailsList.add(new TreeItem<>("Data Rate: " + device.getDataRate().toString()));
        detailsList.add(new TreeItem<>("Appearance: " + device.getAppearance().toString()));
        detailsList.add(new TreeItem<>("State: " + device.getState()));
        detailsList.add(new TreeItem<>("Advertising Interval: " + device.getPacketFactory().getAdvertisingInterval()));
        detailsList.add(new TreeItem<>("Connectable: " + device.getPacketFactory().isConnectable()));

        rootItem.getChildren().addAll(detailsList);

        detailsTreeView.setRoot(rootItem);
        detailsTreeView.showRootProperty().set(false);
    }

    private static void openConnectMenu(Device device, MouseEvent event, Pane scenePane,
                                        TreeView<String> detailsTreeView) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = new MenuItem(device.getState().equals(Device.State.CONNECTION) ? "Disconnect" : "Connect");
        item1.setOnAction(new EventHandler<>() {

            @Override
            public void handle(ActionEvent event) {
                if (item1.getText().equals("Connect")) {
                    Parent root;
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/ConnectionPane.fxml"));
                        //fxmlLoader.setResources(ResourceBundle.getBundle("view.lang", Locale.getDefault()));
                        root = fxmlLoader.load();
                        Stage stage = new Stage();
                        stage.setTitle("Connect");
                        stage.setScene(new Scene(root, 675, 380));
                        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toString()));
                        ConnectionController.setSlave(device);
                        //stage.setResizable(false);
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Disconnect Event");
                    String llData = "Error Code:0x00" + "\nName:Success";
                    Packet disconnectPacket = new Packet(Singleton.getTime(), PacketType.LL_TERMINATE_IND,
                            Singleton.getInstance().master, device,
                            ConnectionUtil.nextChannel(
                                    device.getPacketFactory().getConnectionController().getChannelMap(),
                                    device.getPacketFactory().getConnectionController().getHopIncrement()),
                            device.getDataRate(), llData);

                    Packet.sendPacket(disconnectPacket);
                }
            }
        });

        MenuItem item2 = new MenuItem(device.getDeviceCircle().isShowText() ? "Hide distance" : "Show distance");
        item2.setOnAction(event1 -> device.getDeviceCircle().setShowText(!device.getDeviceCircle().isShowText()));

        MenuItem item3 = new MenuItem("Remove");
        item3.setOnAction(event2 -> {
            System.out.println("Remove Event");

            if (device.getState().equals(Device.State.STANDBY)) {
                synchronized (device.getPacketFactory().getLock()) {
                    device.setState(Device.State.REMOVAL);
                    device.getPacketFactory().getLock().notify();
                }
            } else {
                if (device.getState().equals(Device.State.CONNECTION)) {
                    Packet disconnectPacket = new Packet(Singleton.getTime(), PacketType.LL_TERMINATE_IND,
                            Singleton.getInstance().master, device,
                            ConnectionUtil.nextChannel(
                                    device.getPacketFactory().getConnectionController().getChannelMap(),
                                    device.getPacketFactory().getConnectionController().getHopIncrement()),
                            device.getDataRate());

                    Packet.sendPacket(disconnectPacket);
                }

                device.setState(Device.State.REMOVAL);
            }

            scenePane.getChildren().remove(device.getDeviceCircle().getLine());
            scenePane.getChildren().remove(device.getDeviceCircle().getCircle());
            scenePane.getChildren().remove(device.getDeviceCircle().getText());

            Singleton.getInstance().devices.remove(device);

            Singleton.getInstance().executor.remove(device.getPacketFactory());
        });

        Menu item4 = new Menu("Change State");
        MenuItem standbyState = new MenuItem("Standby");
        MenuItem advertisingState = new MenuItem("Advertising");

        standbyState.setDisable(device.getState().equals(Device.State.STANDBY));

        standbyState.setOnAction(event12 -> {
            device.setState(Device.State.STANDBY);

            ScenePaneUtil.updateDetailsTreeView(device, detailsTreeView);
        });

        advertisingState.setDisable(device.getState().equals(Device.State.ADVERTISING));

        advertisingState.setOnAction(event13 -> {
            device.setState(Device.State.ADVERTISING);
            ScenePaneUtil.updateDetailsTreeView(device, detailsTreeView);
            synchronized (device.getPacketFactory().getLock()) {
                device.getPacketFactory().getLock().notify();
            }

        });

        item4.getItems().addAll(advertisingState, standbyState);

        MenuItem item6 = new MenuItem("Send Data Packets");
        item6.setOnAction(new EventHandler<>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Send Packets Event");
                Parent root;
                try {
                    SendPacketController.setSlave(device);
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/SendPacketPane.fxml"));
                    //fxmlLoader.setResources(ResourceBundle.getBundle("view.lang", Locale.getDefault()));
                    root = fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setTitle("Send Packets");
                    stage.setScene(new Scene(root, 320, 370));
                    stage.setResizable(false);
                    stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        SeparatorMenuItem sep = new SeparatorMenuItem();
        sep.setVisible(device.getState().equals(Device.State.CONNECTION));
        item6.setVisible(device.getState().equals(Device.State.CONNECTION));

        contextMenu.getItems().addAll(item1, item2, item3, item4, sep, item6);

        item1.setDisable(!device.getDeviceCircle().getLine().isVisible() || !device.getPacketFactory().isConnectable()
                || device.getState().equals(Device.State.STANDBY));

        contextMenu.show(device.getDeviceCircle().getCircle(), event.getScreenX(), event.getScreenY());
    }

    public static void makeDraggable(Device device, Pane scenePane) {

        class Delta {
            double x, y;
        }

        Circle circle = device.getDeviceCircle().getCircle();

        final Delta dragDelta = new Delta();

        circle.setOnMousePressed(mouseEvent -> {
            dragDelta.x = circle.getLayoutX() - mouseEvent.getSceneX();
            dragDelta.y = circle.getLayoutY() - mouseEvent.getSceneY();
            circle.setCursor(Cursor.MOVE);
        });
        circle.setOnMouseReleased(mouseEvent -> circle.setCursor(Cursor.HAND));
        circle.setOnMouseDragged(mouseEvent -> {
            double newX = mouseEvent.getSceneX() + dragDelta.x;
            double newY = mouseEvent.getSceneY() + dragDelta.y;

            if (newX > scenePane.getLayoutBounds().getWidth())
                newX = scenePane.getLayoutBounds().getWidth() - circle.getRadius() * 2;

            if (newX < scenePane.getLayoutBounds().getMinX() + circle.getRadius() * 2)
                newX = scenePane.getLayoutBounds().getMinX() + circle.getRadius() * 2;

            if (newY > scenePane.getLayoutBounds().getHeight() - circle.getRadius() * 2)
                newY = scenePane.getLayoutBounds().getHeight() - circle.getRadius() * 2;

            if (newY < scenePane.getLayoutBounds().getMinY())
                newY = scenePane.getLayoutBounds().getMinY() + circle.getRadius() * 2;

            circle.setLayoutX(newX);
            circle.setLayoutY(newY);
            if (!circle.getId().equals("Master"))
                drawConnection(device, scenePane);
            else {
                Singleton.getInstance().devices.forEach(c -> drawConnection(c, scenePane));
            }

        });
        circle.setOnMouseEntered(mouseEvent -> circle.setCursor(Cursor.HAND));

    }

    public static void drawConnection(Device device, Pane scenePane) {
        int distanceMultiplier = 8;
        Circle master = (Circle) scenePane.lookup("#Master");
        Circle c = device.getDeviceCircle().getCircle();
        Line line = device.getDeviceCircle().getLine();
        Text text = device.getDeviceCircle().getText();
        line.setVisible(false);
        text.setVisible(false);

        DoubleBinding distance = Bindings.createDoubleBinding(() -> {
            Point2D start = new Point2D(c.getLayoutX(), c.getLayoutY());
            Point2D end = new Point2D(master.getLayoutX(), master.getLayoutY());
            return start.distance(end);
        }, c.layoutXProperty(), c.layoutYProperty(), master.layoutXProperty(), master.layoutYProperty());

        //tova izchislenie trqbva da e sushtoto kato v treeview-a
        DoubleProperty rssiProperty = new SimpleDoubleProperty(Singleton.getInstance().master.getTransceiver().calculateRSSI(device,distance.divide(distanceMultiplier).doubleValue()));
        //System.out.println(distance.divide(distanceMultiplier).doubleValue());
        //dSystem.out.println(rssiProperty);

        //TODO
        if (!c.equals(master) && rssiProperty.greaterThanOrEqualTo(Singleton.getInstance().master.getTransceiver().getReceiverSensitivity()).get()) {
            line.setStartX(c.getLayoutX());
            line.setStartY(c.getLayoutY());
            line.setEndX(master.getLayoutX());
            line.setEndY(master.getLayoutY());

            line.getStrokeDashArray().addAll(2d, 21d);
            line.setVisible(true);

            text.textProperty().bind(Bindings.format("Distance: %.0f m", distance.divide(distanceMultiplier)));
            text.setBoundsType(TextBoundsType.VISUAL);
            text.xProperty().bind(line.startXProperty().add(line.endXProperty()).divide(2));
            text.yProperty().bind(line.startYProperty().add(line.getEndY()).divide(2));
            text.setVisible(!text.isDisabled());
        }
    }
}
