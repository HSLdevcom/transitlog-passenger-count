package fi.hsl.passengercount

import com.fasterxml.jackson.annotation.JsonFormat
import fi.hsl.common.mqtt.proto.Mqtt
import fi.hsl.common.pulsar.IMessageHandler
import fi.hsl.common.pulsar.PulsarApplicationContext
import fi.hsl.common.transitdata.TransitdataProperties
import fi.hsl.common.transitdata.TransitdataSchema
import fi.hsl.passengercount.model.APC
import fi.hsl.passengercount.utils.JsonHelper
import mu.KotlinLogging
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.Message
import org.apache.pulsar.client.api.MessageId
import java.io.File
import java.nio.charset.Charset

class MessageHandler(context: PulsarApplicationContext, private val path : File): IMessageHandler {

    private val log = KotlinLogging.logger {}
    private val consumer: Consumer<ByteArray> = context.consumer!!
    private val FILE_NAME_PATTERN = "day_%s_vehicle_%s.json"

    override fun handleMessage(received: Message<Any>) {
        try {
            if (TransitdataSchema.hasProtobufSchema(received, TransitdataProperties.ProtobufSchema.MqttRawMessage)) {
                val timestamp: Long = received.eventTime
                val data: ByteArray = received.data

                val raw = Mqtt.RawMessage.parseFrom(data)
                val rawPayload = raw.payload.toByteArray()
                val apc: APC = JsonHelper.parse(rawPayload)
                writeToFile(apc, rawPayload)
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

    internal fun writeToFile(apc : APC, bytes : ByteArray){
        val file : File = File(path, String.format(FILE_NAME_PATTERN, apc.oday,apc.veh.toString()))
        val jsonString = String(bytes, Charset.defaultCharset()).replace("\r\n", "")
        if(!file.exists()) file.createNewFile()
        if(file.length()>0){
            file.appendText("\n")
        }
        file.appendText(jsonString)
    }
}