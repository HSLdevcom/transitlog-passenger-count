package fi.hsl.transitlog.passengercount

import fi.hsl.common.passengercount.PassengerCountParser
import fi.hsl.common.passengercount.json.APCJson
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
    private var lastHandledMessage : MessageId? = null
    private var handledMessages = 0

    override fun handleMessage(received: Message<Any>) {
        try {
            if (TransitdataSchema.hasProtobufSchema(received, TransitdataProperties.ProtobufSchema.PassengerCount)) {
                val passengerCount = PassengerCount.Data.parseFrom(received.data)
                writeToFile(passengerCount.payload)
                handledMessages++
                if(handledMessages == 100000){
                    log.info("Handled 100000 messages, everything seems fine")
                }

            }
            lastHandledMessage = received.messageId
        } catch (e: Exception) {
            log.error("Exception while handling message", e)
        }
    }

    public fun ackMessages() {
        if(lastHandledMessage != null){
            consumer.acknowledgeAsync(lastHandledMessage)
                .exceptionally { throwable ->
                    log.error("Failed to ack Pulsar messages", throwable)
                    null
                }
                .thenRun {}
        }
    }

    internal fun writeToFile(payload : PassengerCount.Payload){
        val apcJson = parser.toJson(payload)
        val file = File(path, String.format(FILE_NAME_PATTERN, payload.oday,payload.veh.toString()))
        if(!file.exists()) file.createNewFile()
        if(file.length()>0){
            file.appendText("\n")
        }
        val fos = FileOutputStream(file, true)
        parser.serializeJson(apcJson, fos)
        fos.close()
    }
}