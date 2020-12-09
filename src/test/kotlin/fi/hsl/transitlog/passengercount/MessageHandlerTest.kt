package fi.hsl.transitlog.passengercount

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import fi.hsl.common.passengercount.PassengerCountParser
import fi.hsl.common.passengercount.json.APCJson
import fi.hsl.common.pulsar.PulsarApplicationContext
import junit.framework.Assert.assertEquals
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.MessageId
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.CompletableFuture

class MessageHandlerTest {

    private val parser = PassengerCountParser.newInstance()

    @Test
    fun writeToFileTest(){
        var mockConsumer = mock<Consumer<ByteArray>>{
            on{acknowledgeAsync(any<MessageId>())} doReturn (CompletableFuture<Void>())
        }
        var mockContext = mock<PulsarApplicationContext>{
            on{consumer} doReturn (mockConsumer)
        }

        val directory = File("testjson")
        directory.mkdir()
        val messageHandler = MessageHandler(mockContext, directory)
        //Read test file, deserialize it from json and create a protobuf payload
        val sourceApc: APCJson = parser.parseJson((File("src/test/resources/passenger-count-sample.json").readBytes()))!!
        val passengerCount = parser.parsePayload(sourceApc)
        //Write protobuf payload to file as json
        messageHandler.writeToFile(passengerCount)
        //Read new file; deserialize it from json
        val file = File("testjson", String.format("day_%s_vehicle_%s.json", sourceApc.apc.oday,sourceApc.apc.veh.toString()))
        assertTrue(file.exists())
        val actualApc: APCJson = parser.parseJson((File("testjson", String.format("day_%s_vehicle_%s.json", sourceApc.apc.oday,sourceApc.apc.veh.toString())).readBytes()))!!
        //Compare original and actual values
        assertEquals(sourceApc.apc.desi,actualApc.apc.desi)
        assertEquals(sourceApc.apc.dir,actualApc.apc.dir)
        assertEquals(sourceApc.apc.lat,actualApc.apc.lat)
        assertEquals(sourceApc.apc.lon,actualApc.apc.lon)
        assertEquals(sourceApc.apc.route,actualApc.apc.route)
        assertEquals(sourceApc.apc.tsi,actualApc.apc.tsi)
        assertEquals(sourceApc.apc.vehiclecounts.countquality, actualApc.apc.vehiclecounts.countquality)
        assertEquals(sourceApc.apc.vehiclecounts.extensions, actualApc.apc.vehiclecounts.extensions)
        assertEquals(sourceApc.apc.vehiclecounts.vehicleloadratio, actualApc.apc.vehiclecounts.vehicleloadratio)
        assertEquals(sourceApc.apc.vehiclecounts.doorcounts[0].door, actualApc.apc.vehiclecounts.doorcounts[0].door)
        assertEquals(sourceApc.apc.vehiclecounts.doorcounts[0].count[0].clazz, actualApc.apc.vehiclecounts.doorcounts[0].count[0].clazz)
        file.delete()
        directory.delete()
    }
}