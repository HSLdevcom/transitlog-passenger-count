package fi.hsl.passengercount

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

val PATH = "json"

fun main(vararg args: String) {
    val log = KotlinLogging.logger {}
    val config = ConfigParser.createConfig()
    val path : File = File(PATH)
    if(!path.exists()) path.mkdir()

    try {
        PulsarApplication.newInstance(config).use { app ->
            val context = app.context
            val messageHandler = MessageHandler(context, path)
            val healthServer = context.healthServer
            app.launchWithHandler(messageHandler)
            setupTaskToMoveFiles(context.config!!.getString("application.blobPath"),
                context.config!!.getString("application.blobContainer"), messageHandler)
        }
    } catch (e: Exception) {
        log.error("Exception at main", e)
    }

}

/**
 * TODO: add blob configuration
 * Moves the files from the local storage to a shared azure blob
 */
fun setupTaskToMoveFiles(blobConnectionString : String, blobContainer : String, messageHandler: MessageHandler){
    val scheduler = Executors.newScheduledThreadPool(1)
    val tomorrow = LocalDateTime.now().plusDays(1).withHour(3)
    val now = LocalDateTime.now()
    val initialDelay = Duration.between(now, tomorrow)
    scheduler.scheduleWithFixedDelay(Runnable {
        File(PATH).list()!!.forEach {
            val file = File(PATH, it)
            val azureBlobClient = AzureBlobClient(blobConnectionString, blobContainer)
            val uploader = AzureUploader(azureBlobClient)
            uploader.uploadBlob(file.absolutePath)
            file.delete()
        }
        messageHandler.ackMessages()
    }, initialDelay.toHours(), 24, TimeUnit.HOURS)

}



