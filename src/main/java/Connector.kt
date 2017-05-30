import com.google.gson.Gson
import com.rabbitmq.client.*
import java.time.Instant


/**
 * Created by guushamm on 23-5-17.
 */
class Connector(usernameToUse: String = defaultUsername, passwordToUse: String = defaultPassword, hostToUse: String = defaultHost, virtualHostToUse: String = defaultVirtualHost) {
    val factory: ConnectionFactory by lazy {
        ConnectionFactory().apply {
            username = usernameToUse
            password = passwordToUse
            host = hostToUse
            virtualHost = virtualHostToUse
            port = 5672
            isAutomaticRecoveryEnabled = false
        }
    }
    val channel: Channel by lazy {
        factory.newConnection().createChannel()
    }
    val gson: Gson by lazy {
        Gson()
    }

    /**
     * Sends a [Car] to the JMS.
     *
     * The [Car] gets serialized using GSON this should be compatible with other JSON implementations though errors could occur.
     * Uses the [Car.countryOfOrigin] property as the routing key. Thus delivering the car to the proper queue and country.
     *
     * @param car The car that has to be send.
     */
    fun publishCar(car: Car) {
        try {
            val serializedCar: String = gson.toJson(car)
            channel.basicPublish(carExchangeName, car.countryOfOrigin.toString(), null, serializedCar.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sends a [Invoice] to the JMS.
     *
     * The invoice gets serialized using GSON this should be compatible with other JSON implementations though errors could occur.
     * Uses the [Invoice.destinationCountry] property as the routing key. Thus delivering the invoice to the proper queue country.
     *
     * @param invoice The car that has to be send.
     */
    fun publishInvoice(invoice: Invoice) {
        try {
            val serializedInvoice: String = gson.toJson(invoice)
            channel.basicPublish(invoiceExchangeName, invoice.destinationCountry.toString(), null, serializedInvoice.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Handles all the configuration of the JMS.
     *
     * In theory this should only need to be executed once. But should be called on application startup in case of a crash of the JMS.
     * It creates channels and queues for all the countries.
     *
     * @property Countries Enum containing all the countries in the system.
     * @property carExchangeName The default car exchange.
     * @property invoiceSuffix The default invoice exchange.
     * @property carSuffix The default suffix for a car queue.
     * @property invoiceSuffix The default suffix for a invoice queue.
     */
    fun prepare() {
        channel.exchangeDeclare(carExchangeName, "direct", true)
        Countries.values().forEach { country ->
            val queueName = "$country$carSuffix"
            channel.queueDeclare(queueName, true, false, false, null)
            channel.queueBind(queueName, carExchangeName, country.toString())
        }

        channel.exchangeDeclare(invoiceExchangeName, "direct", true)
        Countries.values().forEach { country ->
            val queueName = "$country$invoiceSuffix"
            channel.queueDeclare(queueName, true, false, false, null)
            channel.queueBind(queueName, invoiceExchangeName, country.toString())
        }
    }

    /**
     * Consumes messages on a specified queue and executes a handler.
     *
     * @param queueName Name of the queue to subscribe to. By default the queues are named. Country + suffix as defined by [carSuffix] and [invoiceSuffix].
     * @param handler Lambda to execute on receiving of message. Should take a message parameter.
     *
     * @property channel The channel to use.
     */
    private fun subscribe(queueName: String, handler: (message: String) -> Unit) {
        channel.basicConsume(queueName, false, "$queueName${Instant.now()}",
                object : DefaultConsumer(channel) {
                    override fun handleDelivery(consumerTag: String,
                                                envelope: Envelope,
                                                properties: AMQP.BasicProperties,
                                                body: ByteArray) {
                        val deliveryTag = envelope.deliveryTag
                        val message = String(body)
                        handler(message)
                        channel.basicAck(deliveryTag, false)
                    }
                })
    }

    /**
     * Subscribes to the queue of the specified [Countries] and type of the desired data. See [subscribe] for more details.
     *
     * @param country The [Countries] queue to subscribe to.
     * @param type The type of the desired object.
     * @param handler The lambda to execute on message.
     *
     * @property carSuffix The default suffix for a car queue.
     * @property invoiceSuffix The default suffix for a invoice queue.
     */
    fun subscribeToQueue(country: Countries, type: Any, handler: (message: String) -> Unit) {
        var queueName = "$country"
        when (type) {
            Car::class.java -> queueName += carSuffix
            Invoice::class.java -> queueName += invoiceSuffix
        }
        subscribe(
                queueName = queueName,
                handler = handler
        )
    }

}