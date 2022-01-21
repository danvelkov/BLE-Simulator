package topology.modification;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import core.device.model.Device;
import core.Singleton;
import gui.InputValidator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ConnectionController implements Initializable {

	@FXML
	ComboBox<String> hopIncrementComboBox;
	
	@FXML
	Label errorLabel;

	@FXML
	Spinner<Double> connIntervalSpinner;

	@FXML
	Button connectButton;

	@FXML
	CheckBox channel0, channel1, channel2, channel3, channel4, channel5, channel6, channel7, channel8, channel9,
			channel10, channel11, channel12, channel13, channel14, channel15, channel16, channel17, channel18,
			channel19, channel20, channel21, channel22, channel23, channel24, channel25, channel26, channel27,
			channel28, channel29, channel30, channel31, channel32, channel33, channel34, channel35, channel36,
			selectAllCheckBox;

	Runnable selectAllStateChangeProcessor;
	List<CheckBox> allCheckBoxes = new ArrayList<CheckBox>();
	static Device slave;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		populateComboBox();
		selectAllCheckBoxes();
		loadSpinner();
	}
	
	private void loadSpinner() {
		connIntervalSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
			  if (!newValue) {
				  connIntervalSpinner.increment(0); // won't change value, but will commit editor
			  }
			});
	}

	private void populateComboBox() {
		for (int i = 5; i < 17; i++) {
			this.hopIncrementComboBox.getItems().add(Integer.toString(i));
		}

	}

	private void selectAllCheckBoxes() {
		// https://coderanch.com/t/667118/java/Indeterminate-Select-CheckBox

		allCheckBoxes = Arrays.asList(channel0, channel1, channel2, channel3, channel4, channel5, channel6, channel7,
				channel8, channel9, channel10, channel11, channel12, channel13, channel14, channel15, channel16,
				channel17, channel18, channel19, channel20, channel21, channel22, channel23, channel24, channel25,
				channel26, channel27, channel28, channel29, channel30, channel31, channel32, channel33, channel34,
				channel35, channel36);

		allCheckBoxes.forEach(box -> box.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
			if (selectAllStateChangeProcessor == null) {
				boolean allSelected = allCheckBoxes.stream().map(CheckBox::isSelected).reduce(true, (a, b) -> a && b);

				boolean anySelected = allCheckBoxes.stream().map(CheckBox::isSelected).reduce(false, (a, b) -> a || b);

				if (allSelected) {
					this.selectAllCheckBox.setSelected(true);
					this.selectAllCheckBox.setIndeterminate(false);
				}

				if (!anySelected) {
					this.selectAllCheckBox.setSelected(false);
					this.selectAllCheckBox.setIndeterminate(false);
				}

				if (anySelected && !allSelected) {
					this.selectAllCheckBox.setSelected(false);
					this.selectAllCheckBox.setIndeterminate(true);
				}
			}
		}));

		this.selectAllCheckBox.setAllowIndeterminate(true);

		this.selectAllCheckBox.selectedProperty()
				.addListener((observable, wasSelected, isSelected) -> scheduleSelectAllStateChangeProcessing());
		this.selectAllCheckBox.indeterminateProperty().addListener(
				(observable, wasIndeterminate, isIndeterminate) -> scheduleSelectAllStateChangeProcessing());
	}

	private void scheduleSelectAllStateChangeProcessing() {
		if (selectAllStateChangeProcessor == null) {
			selectAllStateChangeProcessor = this::processSelectAllStateChange;
			Platform.runLater(selectAllStateChangeProcessor);
		}
	}

	private void processSelectAllStateChange() {
		if (!this.selectAllCheckBox.isIndeterminate()) {
			allCheckBoxes.forEach(box -> box.setSelected(this.selectAllCheckBox.isSelected()));
		}
		selectAllStateChangeProcessor = null;
	}

	public void connect() {
		if (validateInput()) {
			errorLabel.setVisible(false);
			slave.getPacketFactory().setConnectionController(this);
			if (ConnectionUtil.startConnectionEvent(Singleton.getInstance().master, slave, getChannelMap()))

				try {

					cancel();

					Singleton.getInstance().executor.submit(() -> {
						try {
							ConnectionUtil.sendEmptyDataPacket(Singleton.getInstance().master, slave,
									getChannelMap(), getHopIncrement(),
									String.valueOf(Math.ceil(
											Double.parseDouble(slave.getPacketFactory().getAdvertisingInterval())
													+ connIntervalSpinner.getValue())));
						} catch (NumberFormatException | InterruptedException e) {
							e.printStackTrace();
						}
					});

				} catch (NumberFormatException | InterruptedException e) {
					e.printStackTrace();
				}
		} else {
			System.out.println("Input fields!");
			errorLabel.setVisible(true);
		}
	}

	private boolean validateInput() {
		return (getChannelMap().size() > 0 && InputValidator.isNotNull(hopIncrementComboBox)
				&& InputValidator.textNumericDouble(connIntervalSpinner));
	}

	public void cancel() throws InterruptedException {
		Stage stage = (Stage) connectButton.getScene().getWindow();
		stage.close();
	}

	public List<String> getChannelMap() {
		return allCheckBoxes.stream().filter(ch -> ch.isSelected()).map(CheckBox::getText).collect(Collectors.toList());
	}

	public String getHopIncrement() {
		return this.hopIncrementComboBox.getValue();
	}

	public String getConnectionInterval() {
		return this.connIntervalSpinner.getValue().toString();
	}

	public static void setSlave(Device s) {
		slave = s;
	}

}
