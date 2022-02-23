package core;

import core.device.model.*;
import gui.AppController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.RadioButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Singleton {
	private static Singleton single_instance = null;

	public Device master, dummy;
	public ObservableList<Device> devices;
	public ThreadPoolExecutor executor;
	public Double speed = 1.0;
	public static Date date = new Date();
	private int currentSecond = (int) (date.getTime() % 10000 / 1000);

	private Singleton() {
		devices = FXCollections.observableArrayList();
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		createMaster();
		createDummy();
		delayTime();
	}

	private void createMaster() {
		Circle circle = new Circle();
		circle.setRadius(15);
		circle.setFill(Color.LIMEGREEN);
		circle.setId("Master");
		DeviceCircle deviceCircle = new DeviceCircle(circle, new Line());

		master = new Device("Master", new Transceiver(), new DeviceAddress("STATIC", null), Device.State.CONNECTION, DataRate.ONEM,
				Device.Appearance.COMPUTER, deviceCircle);
	}

	/////////////
	private void createDummy(){
		Circle circle = new Circle();
		circle.setRadius(10);
		circle.setId("dummy");
		circle.setFill(Color.valueOf(AppController.getRandomColorName()));

		DeviceCircle deviceCircle = new DeviceCircle(circle, new Line());
		DeviceAddress address = new DeviceAddress("STATIC", "");
		dummy = new Device("dummy", new Transceiver(-8, -75, 1, 1), address, Device.State.STANDBY,
				DataRate.ONEM,
				Device.Appearance.UNKNOWN, deviceCircle);

		dummy.getPacketFactory().setAdvertisingInterval("1250.00");
		//dummy.getPacketFactory().setMaxDistance("100");
		dummy.getPacketFactory().setConnectable(true);

	}

	///////////////

	public static Singleton getInstance() {
		if (single_instance == null)
			single_instance = new Singleton();

		return single_instance;
	}

	public static String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd-MM-yyyy");
		return formatter.format(date);
	}
	
	public static String getAddedTime(int addedMilliseconds) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd-MM-yyyy");
		return formatter.format(date.getTime() + addedMilliseconds);
	}

	private void delayTime() {

		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(() -> {
			switch (Singleton.getInstance().speed.toString()) {
				case "1.0" -> date.setTime(date.getTime() + 1000);
				case "0.75" -> date.setTime(date.getTime() + 750);
				case "0.5" -> date.setTime(date.getTime() + 500);
				case "0.25" -> date.setTime(date.getTime() + 250);
				default -> {
				}
			}

			if ((date.getTime() % 10000 / 1000) != currentSecond) {
				currentSecond = (int) (date.getTime() % 10000 / 1000);
				addStateDurations();
			}

		}, 0, 1, TimeUnit.SECONDS);
	}

	private void addStateDurations() {
		devices.forEach(d -> {
			switch (d.getState()) {
				case ADVERTISING -> d.setAdvertisingTime(d.getAdvertisingTime().add(1).longValue());
				case CONNECTION -> d.setConnectedTime(d.getConnectedTime().add(1).longValue());
				case STANDBY -> d.setStandbyTime(d.getStandbyTime().add(1).longValue());
				default -> {
				}
			}
		});

	}
}
