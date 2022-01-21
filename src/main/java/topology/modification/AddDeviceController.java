package topology.modification;

import core.device.model.*;
import gui.AppController;
import gui.InputValidator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ResourceBundle;

public class AddDeviceController implements Initializable {

	static Device newDevice;
	@FXML
	TextField deviceNameTextField, publicAddressTextField, maxDistanceTextField, transmitPower, receivePower, transmitterGain, receiverGain;
	@FXML
	Label staticAddressLabel, resolvableAddressLabel, nonResolvableAddressLabel, errorLabel;
	@FXML
	ComboBox<DataRate> dataRateComboBox;
	@FXML
	ComboBox<Device.Appearance> appearanceComboBox;
	@FXML
	RadioButton publicRadioButton, staticRadioButton, resolvableRadioButton, nonResolvableRadioButton,
			connectableRadioButton, nonConnectableRadioButton;
	@FXML
	ToggleGroup addressGroup, connectableGroup;
	@FXML
	Spinner<Double> advertisingIntervalSpinner;
	AddressType selectedAddressType;
	DeviceAddress deviceAddress;

	public static Device getDevice() {
		return newDevice;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadDataRateChoiceBox();
		loadAppearanceComboBox();
		createTogleGroup();
		setUpIntervalSpinner();
	}

	private void setUpIntervalSpinner() {
		StringConverter<Double> doubleConverter = new StringConverter<Double>() {
			private final DecimalFormat df = new DecimalFormat("###.###");

			@Override
			public String toString(Double object) {
				if (object == null) {
					return "";
				}
				return df.format(object);
			}

			@Override
			public Double fromString(String string) {
				try {
					if (string == null) {
						return null;
					}
					string = string.trim();
					if (string.length() < 1) {
						return null;
					}
					return df.parse(string).doubleValue();
				} catch (ParseException ex) {
					throw new RuntimeException(ex);
				}
			}
		};

		//this.advertisingIntervalSpinner.getValueFactory().setConverter(doubleConverter);

		advertisingIntervalSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				advertisingIntervalSpinner.increment(0); // won't change value, but will commit editor
			}
		});

	}

	private void loadDataRateChoiceBox() {
		this.dataRateComboBox.setItems(
				FXCollections.observableArrayList(new DataRate[] { DataRate.ONEM, DataRate.TWOM, DataRate.CODED }));
	}

	private void loadAppearanceComboBox() {
		this.appearanceComboBox.setItems(FXCollections.observableArrayList(Device.Appearance.values()));
	}

	private void createTogleGroup() {
		addressGroup = new ToggleGroup();
		publicRadioButton.setToggleGroup(addressGroup);
		staticRadioButton.setToggleGroup(addressGroup);
		resolvableRadioButton.setToggleGroup(addressGroup);
		nonResolvableRadioButton.setToggleGroup(addressGroup);

		connectableGroup = new ToggleGroup();
		connectableRadioButton.setToggleGroup(connectableGroup);
		nonConnectableRadioButton.setToggleGroup(connectableGroup);

		this.toggleListener();
	}

	private void toggleListener() {
		addressGroup.selectedToggleProperty().addListener((ov, t, t1) -> {
			RadioButton selectedRadioButton = (RadioButton) addressGroup.getSelectedToggle();
			String selectedValue = selectedRadioButton.getText();

			switch (selectedValue) {
				case "Public" -> {
					publicAddressTextField.setVisible(true);
					staticAddressLabel.setVisible(false);
					resolvableAddressLabel.setVisible(false);
					nonResolvableAddressLabel.setVisible(false);
					selectedAddressType = AddressType.PUBLIC;
					deviceAddress = new DeviceAddress("PUBLIC", null);
				}
				case "Static" -> {
					deviceAddress = new DeviceAddress("STATIC", null);
					staticAddressLabel.setText(deviceAddress.getAddress());
					publicAddressTextField.setVisible(false);
					staticAddressLabel.setVisible(true);
					resolvableAddressLabel.setVisible(false);
					nonResolvableAddressLabel.setVisible(false);
					selectedAddressType = AddressType.STATIC;
				}
				case "Resolvable" -> {
					publicAddressTextField.setVisible(false);
					staticAddressLabel.setVisible(false);
					resolvableAddressLabel.setVisible(true);
					nonResolvableAddressLabel.setVisible(false);
					selectedAddressType = AddressType.RESOLVABLE;
				}
				case "Non Resolvable" -> {
					deviceAddress = new DeviceAddress("NONRESOLVABLE", null);
					nonResolvableAddressLabel.setText(deviceAddress.getAddress());
					publicAddressTextField.setVisible(false);
					staticAddressLabel.setVisible(false);
					resolvableAddressLabel.setVisible(false);
					nonResolvableAddressLabel.setVisible(true);
					selectedAddressType = AddressType.NONRESOLVABLE;
				}
			}
		});
	}

	public void addDevice() {
		if (validateInput()) {
			errorLabel.setVisible(false);
			if (selectedAddressType != null) {
				if (selectedAddressType.equals(AddressType.PUBLIC))
					deviceAddress.setAddress(publicAddressTextField.getText());

				Circle circle = new Circle();
				circle.setRadius(10);
				circle.setId(deviceNameTextField.getText());
				circle.setFill(Color.valueOf(AppController.getRandomColorName()));

				DeviceCircle deviceCircle = new DeviceCircle(circle, new Line());

				newDevice = new Device(deviceNameTextField.getText(), new Transceiver(Double.parseDouble(transmitPower.getText()), Double.parseDouble(receivePower.getText()), Double.parseDouble(transmitterGain.getText()), Double.parseDouble(receiverGain.getText())), deviceAddress, "STANDBY",
						dataRateComboBox.getSelectionModel().getSelectedItem(),
						appearanceComboBox.getSelectionModel().getSelectedItem(), deviceCircle);

				newDevice.getPacketFactory().setAdvertisingInterval(advertisingIntervalSpinner.getValue().toString());
				newDevice.getPacketFactory().setMaxDistance(maxDistanceTextField.getText());
				newDevice.getPacketFactory().setConnectable(
						((RadioButton) connectableGroup.getSelectedToggle()).getText().equals("Connectable"));

				cancel();
			}
		} else {
			System.out.println("Input fields!");
			errorLabel.setVisible(true);
		}

	}

	private boolean validateInput() {
		return (InputValidator.isNotNull(deviceNameTextField) && InputValidator.isNotNull(dataRateComboBox)
				&& InputValidator.isNotNull(appearanceComboBox) && InputValidator.textNumeric(maxDistanceTextField)
				&& InputValidator.textNumericDouble(advertisingIntervalSpinner) && InputValidator.isNotNull(addressGroup)
				&& InputValidator.isNotNull(connectableGroup));
	}

	public void cancel() {
		Stage stage = (Stage) staticAddressLabel.getScene().getWindow();
		stage.close();
		newDevice = null;
	}
}
