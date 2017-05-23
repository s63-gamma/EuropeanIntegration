import java.util.*

/**
 * Created by guushamm on 23-5-17.
 */
data class Invoice(val kilometers: Double, val price: Double, val licensePlate: String, val destinationCountry: Countries, val originCountry: Countries, val date: Date)