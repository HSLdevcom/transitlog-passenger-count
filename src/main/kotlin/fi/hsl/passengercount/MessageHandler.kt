package fi.hsl.passengercount

import fi.hsl.common.passengercount.PassengerCountParser
import fi.hsl.common.passengercount.proto.PassengerCount
import fi.hsl.common.pulsar.IMessageHandler
import fi.hsl.common.pulsar.PulsarApplicationContext
import fi.hsl.common.transitdata.TransitdataProperties
import fi.hsl.common.transitdata.TransitdataSchema
import mu.KotlinLogging
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.Message
import org.apache.pulsar.client.api.MessageId
import java.io.File
import java.io.FileOutputStream

class MessageHandler(context: PulsarApplicationContext, private val path : File): IMessageHandler {

    private val log = KotlinLogging.logger {}
    private val consumer: Consumer<ByteArray> = context.consumer!!
    private val FILE_NAME_PATTERN = "day_%s_vehicle_%s.json"
    private val parser = PassengerCountParser.newInstance()

    override fun handleMessage(received: Message<Any>) {
        try {
            if (TransitdataSchema.hasProtobufSchema(received, TransitdataProperties.ProtobufSchema.PassengerCount)) {
                val timestamp: Long = received.eventTime
                val data: ByteArray = received.data

                val passengerCount = PassengerCount.Data.parseFrom(received.data)
                writeToFile(passengerCount.payload)
            }
        } catch (e: Exception) {
            log.error("Exception while handling message", e)
        } finally {
            ack(received.messageId) //Ack all messages
        }
    }

    private fun ack(received: MessageId) {
        consumer.acknowledgeAsync(received)
            .exceptionally { throwable ->
                log.error("Failed to ack Pulsar message", throwable)
                null
            }
            .thenRun {}
    }

    internal fun writeToFile(payload : PassengerCount.Payload){
        val apcJson = parser.toJson(payload)
        val file = File(path, String.format(FILE_NAME_PATTERN, payload.oday,payload.veh.toString()))
        if(!file.exists()) file.createNewFile()
        if(file.length()>0){
            file.appendText("\n")
        }
        val fos = FileOutputStream(file)
        parser.serializeJson(apcJson, fos)
        fos.close()
    }
}