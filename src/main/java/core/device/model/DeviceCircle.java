package core.device.model;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class DeviceCircle {
	Circle circle;
	Line line;
	Text text;
	boolean showText;
	
	public DeviceCircle(Circle circle, Line line) {
		super();
		this.circle = circle;
		this.line = line;
		this.text = new Text();
		this.showText = true;
	}

	public Circle getCircle() {
		return circle;
	}

	public void setCircle(Circle circle) {
		this.circle = circle;
	}

	public Line getLine() {
		return line;
	}

	public void setLine(Line line) {
		this.line = line;
	}

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	public boolean isShowText() {
		return showText;
	}

	public void setShowText(boolean showText) {
		this.showText = showText;
		text.setDisable(!showText);
		text.setVisible(showText);
	}
	
	
}
