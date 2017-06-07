package com.gmail.guushamm.EuropeanIntegration;

import com.google.gson.Gson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by guushamm on 30-5-17.
 */
public class Example {
	private ArrayList<Car> cars;
	private ArrayList<Invoice> invoices;
	private Gson gson;
	private ArrayList<StolenCar> stolencars;

	public Example() {
		this.cars = new ArrayList<>();
		this.invoices = new ArrayList<>();
		this.stolencars = new ArrayList<>();
		this.gson = new Gson();
	}

	private void example() {
		Connector connector = new Connector();
		connector.prepare();

		/**
		 * Subscribe to the queues used by your country.
		 * In this example we use a separate function for processing the car, this could also be done within the lambda
		 */
		connector.subscribeToQueue(
				Countries.NETHERLANDS,
				Car.class,
				(String message) -> {
					Car car = gson.fromJson(message, Car.class);
					processCar(car);

					// Lambdas in java always have to have a return value
					return null;
				}
		);
		connector.subscribeToQueue(
				Countries.UNITED_KINGDOM,
				Invoice.class,
				(String message) -> {
					Invoice invoice = gson.fromJson(message, Invoice.class);
					processInvoice(invoice);

					// Lambdas in java always have to have a return value
					return null;
				}
		);

		connector.subscribeToQueue(
				Countries.NETHERLANDS,
				StolenCar.class,
				(String message) -> {
					StolenCar stolenCar = gson.fromJson(message, StolenCar.class);
					processStolenCar(stolenCar);

					// Lambdas in java always have to have a return value
					return null;
				}
		);

		/**
		 * Publish some data to test everything is working.
		 * NOTE in an [Invoice] [Invoice.originCountry] is the country where the invoice was generated. [Invoice.destinationCountry] is the cars [Car.destinationCountry].
		 */

		Car car = new Car("testcar", Countries.NETHERLANDS, Countries.UNITED_KINGDOM, false);
		connector.publishCar(car);

		Invoice invoice = new Invoice(500.0, 100.0, "test", Countries.NETHERLANDS, Countries.UNITED_KINGDOM, Date.from(Instant.now()));
		connector.publishInvoice(invoice);

		StolenCar stolenCar = new StolenCar("noot-noot-nooooot", Countries.NETHERLANDS, true);
		connector.publishStolenCar(stolenCar);
	}

	private void processCar(Car car) {
		/**
		 * Do something with your car
		 */

		this.cars.add(car);
		System.out.println(String.format("Received new car: %s", car));
	}

	private void processInvoice(Invoice invoice) {
		/**
		 * Do something with your invoice
		 */

		this.invoices.add(invoice);
		System.out.println(String.format("Received new invoice: %s", invoice));
	}

	private void processStolenCar(StolenCar stolenCar) {
		/**
		 * Do something with your stolen car
		 */

		this.stolencars.add(stolenCar);
		System.out.println(String.format("Stolen car reported: %s", stolenCar));
	}


	public static void main(String[] args) {
		Example example = new Example();
		example.example();
	}
}
