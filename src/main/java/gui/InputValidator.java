package gui;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class InputValidator {
	
	public static boolean isNotNull(TextField input) {
		if (input.getText() == null)
			System.out.println("textnull");
		return input != null;
	}

	public static boolean isNotNull(@SuppressWarnings("rawtypes") ComboBox input) {
		if (input.getValue() == null)
			System.out.println("combo");
		return input.getValue() != null;
	}

	public static boolean isNotNull(ToggleGroup input) {
		if (input.getSelectedToggle() == null)
			System.out.println("radio");

		return input.getSelectedToggle() != null;
	}
	
	public static boolean textAlphaNumeric(TextField inputTextField) {
		if (!inputTextField.getText().matches("^[a-zA-Z0-9_]{1,25}$"))
			System.out.println("alpanumeric");
		return inputTextField.getText().matches("^[a-zA-Z0-9_]{1,25}$");
	}

	public static boolean textNumeric(TextField inputTextField) {
		if (!inputTextField.getText().matches("^[0-9]{1,25}$"))
			System.out.println("numeric" + inputTextField.getId());
		return inputTextField.getText().matches("^[0-9]{1,25}$");
	}
	
	public static boolean textNumericDouble(Spinner<Double> inputTextField) {
		if (!inputTextField.getValue().toString().matches("^[0-9]+.[0-9]+$"))
			System.out.println("numeric");
		return inputTextField.getValue().toString().matches("^[0-9]+.[0-9]+$");
	}
	

	public static boolean textNumericInteger(Spinner<Integer> inputTextField) {
		if (!inputTextField.getValue().toString().matches("^[0-9]{1,25}$"))
			System.out.println("numeric");
		return inputTextField.getValue().toString().matches("^[0-9]{1,25}$");
	}

	public static boolean textAlphabetFirstCapital(TextField inputTextField) {
		return inputTextField.getText().matches("[A-Z][a-z]+[ ]+[A-Z][a-z]{1,25}");
	}

	public static boolean textPin(TextField inputTextField) {
		return inputTextField.getText().matches("[0-9]{10}");
	}

	public static boolean textCommission(TextField inputTextField) {
		return inputTextField.getText().matches("^[0-9]*\\.?[0-9]+$");
	}

	public static boolean textLetters(TextField inputTextField) {
		return inputTextField.getText().matches("[A-Za-z ]{1,30}");
	}

	public static boolean textAddress(TextField inputTextField) {
		return inputTextField.getText().matches("[A-Za-z0-9'\"\\.\\-\\s\\,]{2,25}");
	}

	public static <T> boolean textCombo(ComboBox<T> comboField) {
		return !comboField.getSelectionModel().isEmpty();
	}
	
}
