package fi.hsl.passengercount

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import fi.hsl.common.config.ConfigParser
import fi.hsl.common.pulsar.PulsarApplication
import fi.hsl.passengercount.azure.AzureBlobClient
import fi.hsl.passengercount.azure.AzureUploader
import mu.KotlinLogging
import okhttp3.OkHttpClient
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val PATH = "csv"

fun main(vararg args: String) {
    val log = KotlinLogging.logger {}
    val config = ConfigParser.createConfig()
    val path : File = File(PATH)
    if(!path.exists()) path.mkdir()
    val doiTimezone = ZoneId.of(config.getString("doi.timezone"))
    val doiQueryFutureDays = config.getInt("doi.queryFutureDays")

    val client = OkHttpClient()
    try {
        PulsarApplication.newInstance(config).use { app ->
            val context = app.context
            val processor = MessageHandler(context, path)
            val healthServer = context.healthServer
            app.launchWithHandler(processor)
        }
    } catch (e: Exception) {
        log.error("Exception at main", e)
    }
    setupTaskToMoveFiles()
}

/**
 * TODO: add blob configuration
 * Moves the files from the local storage to a shared azure blob
 */
fun setupTaskToMoveFiles(){
    val scheduler = Executors.newScheduledThreadPool(1)
    val tomorrow = LocalDateTime.now().plusDays(1).withHour(3)
    val now = LocalDateTime.now()
    val initialDelay = Duration.between(now, tomorrow)
    scheduler.scheduleWithFixedDelay(Runnable {
        File(PATH).list()!!.forEach { it
            val file = File(PATH, it)
            val azureBlobClient = AzureBlobClient("", "passenger-count-csv")
            val uploader = AzureUploader(azureBlobClient)
            uploader.uploadBlob(file.absolutePath)
            file.delete()
        }
    }, initialDelay.toHours(), 24, TimeUnit.HOURS)

}



