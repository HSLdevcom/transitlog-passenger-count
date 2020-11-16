package fi.hsl.passengercount

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import fi.hsl.common.pulsar.PulsarApplicationContext
import fi.hsl.passengercount.model.APC
import fi.hsl.passengercount.utils.JsonHelper
import junit.framework.Assert.assertEquals
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.MessageId
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mockito
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.CompletableFuture

class MessageHandlerTest {

    @Test
    fun parseJsonTest(){
        val apc : APC = JsonHelper.parse(File("src/test/resources/sample.json").inputStream())
        assertEquals("555", apc.desi)
        assertEquals(12, apc.oper)
        assertEquals(24.9435, apc.lat)
        assertEquals(60.1967, apc.long)
        assertEquals("GPS", apc.loc)
        assertEquals("regular | defect | other", apc.vehiclecounts.countquality)
        assertEquals(15, apc.vehiclecounts.vehicleload)
        assertEquals(1, apc.vehiclecounts.doorcounts.size)
        assertEquals("door1", apc.vehiclecounts.doorcounts[0].door)
    }

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
        val rawPayload = File("src/test/resources/sample.json").readBytes()
        val apc: APC = JsonHelper.parse(rawPayload)
        messageHandler.writeToFile(apc, rawPayload)
        val file = File("testjson", String.format("day_%s_vehicle_%s.json", apc.oday,apc.veh.toString()))
        assertTrue(file.exists())
        val expectedValue = File("src/test/resources/sample.json").readText(Charset.forName("UTF-8")).replace("\r\n", "")
        val actualValue = file.readText(Charset.forName("UTF-8"))
        assertEquals(expectedValue,actualValue)
        file.delete()
        directory.delete()
    }
}