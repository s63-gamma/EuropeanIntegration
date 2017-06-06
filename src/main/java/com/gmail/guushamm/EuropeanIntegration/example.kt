package com.gmail.guushamm.EuropeanIntegration

import java.util.*

/**
 * Created by guushamm on 23-5-17.
 */
fun main(args: Array<String>) {
    /**
     * Get a [Connector]. This should be used for interaction with the JMS.
     */
    val connector: Connector = Connector()

    /**
     * Should only be run once on startup of the application.
     *
     * Note: This should not be needed but is used to prepare the JMS enviroment.
     */
    connector.prepare()

    /**
     * Subscribe to the queues used by your country.
     */
    connector.subscribeToQueue(
            country = Countries.NETHERLANDS,
            type = Car::class.java,
            handler = { message ->
                println(message)
            }
    )
    connector.subscribeToQueue(
            country = Countries.NETHERLANDS,
            type = Invoice::class.java,
            handler = { message ->
                println(message)
            }
    )
    connector.subscribeToQueue(
            country = Countries.NETHERLANDS,
            type = StolenCar::class.java,
            handler = { message ->
                println(message)
            }
    )

    /**
     * Publish some data to test everything is working.
     * NOTE in an [Invoice] [Invoice.originCountry] is the country where the invoice was generated. [Invoice.destinationCountry] is the cars [Car.countryOfOrigin].
     */
    connector.publishCar(Car(
            licensePlate = "test",
            countryOfOrigin = Countries.NETHERLANDS,
            stolen = false)
    )
    connector.publishInvoice(Invoice(
            licensePlate = "test",
            price = 500.0,
            kilometers = 100.0,
            originCountry = Countries.UNITED_KINGDOM,
            destinationCountry = Countries.NETHERLANDS,
            date = Date())
    )
    connector.publishStolenCar(StolenCar(
            licensePlate = "dank-kush-man",
            countryOfOrigin = Countries.NETHERLANDS)
    )
}
