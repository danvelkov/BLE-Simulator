package packet.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import core.device.model.Device;
import packet.model.Packet;
import packet.model.PacketType;
import packet.model.Services;
import topology.modification.ConnectionUtil;
import core.Singleton;
import gui.InputValidator;

import java.net.URL;
import java.util.ResourceBundle;

public class SendPacketController implements Initializable {

	@FXML
	Label appearanceLabel, errorLabel;

	@FXML
	Button sendButton;

	@FXML
	ComboBox<Services> serviceComboBox;

	@FXML
	Spinner<Integer> quantitySpinner;

	@FXML
	RadioButton noneRadio, readableRadio, writableRadio, readableAndWritableRadio;

	@FXML
	ToggleGroup permissionsGroup = new ToggleGroup();

	static Device slave;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadView();
	}

	private void loadView() {
		loadAppearance();
		loadServiceCombo();
		loadSpinner();
		loadCheckBox();
	}

	private void loadAppearance() {
		appearanceLabel.setText(slave.getAppearance().toString());
	}

	private void loadServiceCombo() {
		switch (slave.getAppearance()) {
			case BLOOD_PRESSURE -> serviceComboBox.setItems(FXCollections.observableArrayList(
					Services.DEVICE_INFORMATION, Services.BLOOD_PRESSURE, Services.USER_DATA));
			case CLOCK -> serviceComboBox.setItems(FXCollections.observableArrayList(
					Services.CURRENT_TIME, Services.DEVICE_INFORMATION, Services.USER_DATA));
			case COMPUTER -> serviceComboBox.setItems(FXCollections.observableArrayList(
					Services.CURRENT_TIME, Services.DEVICE_INFORMATION, Services.USER_DATA));
			case GLUCOSE_METER -> serviceComboBox.setItems(FXCollections.observableArrayList(
					Services.GLUCOSE, Services.DEVICE_INFORMATION, Services.USER_DATA));
			case HEART_RATE_SENSOR -> serviceComboBox.setItems(FXCollections.observableArrayList(
					Services.DEVICE_INFORMATION, Services.HEARTH_RATE, Services.USER_DATA));
			case OTHER -> serviceComboBox.setItems(FXCollections.observableArrayList(
					Services.CURRENT_TIME, Services.GLUCOSE, Services.DEVICE_INFORMATION,
					Services.HEARTH_RATE, Services.PHONE_ALERT_STATUS, Services.BLOOD_PRESSURE,
					Services.LOCATION_AND_NAVIGATION, Services.PULSE_OXYMETER, Services.USER_DATA));
			case PHONE -> serviceComboBox.setItems(FXCollections
					.observableArrayList(Services.CURRENT_TIME, Services.DEVICE_INFORMATION,
							Services.PHONE_ALERT_STATUS, Services.LOCATION_AND_NAVIGATION, Services.USER_DATA));
			case SENSOR -> serviceComboBox.setItems(FXCollections.observableArrayList(Services.DEVICE_INFORMATION,
					Services.EVNIRONMENTAL_SENSING, Services.USER_DATA));
			case TAG -> serviceComboBox.setItems(FXCollections.observableArrayList(Services.DEVICE_INFORMATION,
					Services.LOCATION_AND_NAVIGATION, Services.USER_DATA));
			case THERMOMETER -> serviceComboBox.setItems(FXCollections.observableArrayList(Services.DEVICE_INFORMATION,
					Services.EVNIRONMENTAL_SENSING, Services.USER_DATA));
			case UNKNOWN -> serviceComboBox.setItems(FXCollections.observableArrayList(
					Services.CURRENT_TIME, Services.GLUCOSE, Services.DEVICE_INFORMATION,
					Services.HEARTH_RATE, Services.PHONE_ALERT_STATUS, Services.BLOOD_PRESSURE,
					Services.LOCATION_AND_NAVIGATION, Services.PULSE_OXYMETER, Services.USER_DATA));
			default -> {
			}
		}
	}

	private void loadSpinner() {
		quantitySpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				quantitySpinner.increment(0); // won't change value, but will commit editor
			}
		});
	}

	private void loadCheckBox() {
		noneRadio.setToggleGroup(permissionsGroup);
		readableRadio.setToggleGroup(permissionsGroup);
		writableRadio.setToggleGroup(permissionsGroup);
		readableAndWritableRadio.setToggleGroup(permissionsGroup);
	}

	private void generateDataPackets() {
		String data = "Attribute Handle:" + "\nAttribute Type:Primary Service" + "\nAttribute Value:"
				+ serviceComboBox.getValue() + "\nAttribute Permissions:"
				+ ((RadioButton) permissionsGroup.getSelectedToggle()).getText();

		Packet dataPacket = new Packet(Singleton.getTime(), PacketType.LL_DATA, slave, Singleton.getInstance().master,
				ConnectionUtil.nextChannel(slave.getPacketFactory().getConnectionController().getChannelMap(),
						slave.getPacketFactory().getConnectionController().getHopIncrement()),
				slave.getDataRate(), data);

		slave.getPacketFactory().setSendDataPackets(dataPacket, quantitySpinner.getValue().intValue());
	}

	public void send() {
		if (validateInput()) {
			errorLabel.setVisible(false);
			if (!slave.getPacketFactory().isHavingPacketsToSend()) {
				generateDataPackets();
				cancel();
			} else {
				System.out.println("Still has packets to send!");
			}
		} else {
			errorLabel.setVisible(true);
		}
	}

	public void cancel() {
		Stage stage = (Stage) appearanceLabel.getScene().getWindow();
		stage.close();
	}

	private boolean validateInput() {
		return (InputValidator.isNotNull(serviceComboBox) && InputValidator.textNumericInteger(quantitySpinner)
				&& InputValidator.isNotNull(permissionsGroup));
	}

	public static void setSlave(Device s) {
		System.out.println(s);
		slave = s;
	}

}
