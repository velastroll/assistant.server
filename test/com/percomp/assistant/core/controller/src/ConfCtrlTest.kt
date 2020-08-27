package com.percomp.assistant.core.controller.src

import categorymarker.BlackBox
import com.percomp.assistant.core.controller.domain.ConfCtrl
import com.percomp.assistant.core.controller.domain.Location
import com.percomp.assistant.core.controller.services.ConfService
import com.percomp.assistant.core.controller.services.DeviceService
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.model.*
import io.ktor.util.KtorExperimentalAPI
import io.mockk.CapturingSlot
import io.mockk.every
import org.junit.experimental.categories.Category
import io.mockk.mockk
import io.mockk.slot
import java.lang.IllegalArgumentException
import kotlin.test.*

/**
 * Test [ConfCtrl] class.
 *
 * @author Álvaro Velasco Gil - alvvela
 */
@KtorExperimentalAPI
class ConfCtrlTest {

    /**
     * Init mocks
     */
    companion object {
        val t = 5
        val ctrl : ConfCtrl
        val dS : DeviceService = mockk(relaxed=true)
        val lS : LocationService = mockk(relaxed=true)
        val cS : ConfService = mockk(relaxed=true)
        const val REAL_MAC_1 = "12:23:34:45:56:01"
        const val REAL_MAC_X = "12:23:34:45:56:0X"
        const val REAL_MAC_2 = "12:23:34:45:56:02"
        const val FAKE_MAC = "AB:bc:CD:de:EF:fg"
        const val BEFORE_TIMESTAMP = "2020-01-01T12:00:00Z"
        const val REAL_POSTCODE_2 = 47140
        val REAL_USER_1 = Person(name = "Álvaro", surname = "To Test", nif = "124232XXX", postcode = 47100)
        val REAL_USER_2 = Person(name = "Álvaro", surname = "To Test", nif = "124232XXX", postcode = REAL_POSTCODE_2)
        val REAL_GLOBAL_CONF = ConfData(device = "GLOBAL", timestamp = BEFORE_TIMESTAMP)
        val REAL_RELATION_1 = Relation(device = REAL_MAC_1, user = REAL_USER_1, from = "2019", to = null)
        val REAL_RELATION_2 = Relation(device = REAL_MAC_2, user = REAL_USER_2, from = "2019", to = null)
        val REAL_DATA_PC = ConfData(device = REAL_POSTCODE_2.toString(), pending = false, timestamp = BEFORE_TIMESTAMP)
        init {
            ctrl = ConfCtrl(deviceService = dS, confService = cS, locationService = lS)
        }
    }

    @BeforeTest
    fun `init dependencies`() {
        println("init test dependencies")
        every { dS.getLastAssignment(any()) } returns null
        every { dS.getLastAssignment(REAL_MAC_1) } returns REAL_RELATION_1
        every { dS.getLastAssignment(REAL_MAC_2) } returns REAL_RELATION_2
        every { cS.get(any(), any()) } returns null
        every { cS.get(REAL_POSTCODE_2.toString(), any()) } returns REAL_DATA_PC
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac does not exist`(){
        val c = ctrl.getConfs(FAKE_MAC)
        assertNull(c.global)
        assertNull(c.location)
        assertNull(c.deviceConf)
        assertNull(c.pendingConf)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & no relation`(){
        every { cS.get("GLOBAL", any()) } returns REAL_GLOBAL_CONF
        val c = ctrl.getConfs(REAL_MAC_X)
        assertNotNull(c.global)
        assertNull(c.location)
        assertNull(c.deviceConf)
        assertNull(c.pendingConf)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & relation & no location conf`(){
        every { cS.get("GLOBAL", any()) } returns REAL_GLOBAL_CONF
        val c = ctrl.getConfs(REAL_MAC_X)
        assertNotNull(c.global)
        assertNull(c.location)
        assertNull(c.deviceConf)
        assertNull(c.pendingConf)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & has relation & location conf`(){
        every { cS.get("GLOBAL", any()) } returns REAL_GLOBAL_CONF
        val c = ctrl.getConfs(REAL_MAC_2)
        assertNotNull(c.global)
        assertNotNull(c.location)
        assertNull(c.deviceConf)
        assertNull(c.pendingConf)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & no device conf`(){
        every { cS.get("GLOBAL", any()) } returns REAL_GLOBAL_CONF
        every { cS.get(REAL_MAC_2, pending = any()) } returns null
        val c = ctrl.getConfs(REAL_MAC_2)
        assertNotNull(c.global)
        assertNotNull(c.location)
        assertNull(c.deviceConf)
        assertNull(c.pendingConf)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & device conf`(){
        every { cS.get("GLOBAL", any()) } returns REAL_GLOBAL_CONF
        every { cS.get(REAL_MAC_2, pending = false) } returns REAL_GLOBAL_CONF
        every { cS.get(REAL_MAC_2, pending = true) } returns null
        val c = ctrl.getConfs(REAL_MAC_2)
        assertNotNull(c.global)
        assertNotNull(c.location)
        assertNotNull(c.deviceConf)
        assertNull(c.pendingConf)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & no pending conf`(){
        every { cS.get("GLOBAL", any()) } returns REAL_GLOBAL_CONF
        every { cS.get(REAL_MAC_2, pending = false) } returns REAL_GLOBAL_CONF
        every { cS.get(REAL_MAC_2, pending = true) } returns null
        val c = ctrl.getConfs(REAL_MAC_2)
        assertNotNull(c.global)
        assertNotNull(c.location)
        assertNotNull(c.deviceConf)
        assertNull(c.pendingConf)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & no conf & pending conf`(){
        every { cS.get("GLOBAL", any()) } returns REAL_GLOBAL_CONF
        every { cS.get(REAL_MAC_2, pending = false) } returns null
        every { cS.get(REAL_MAC_2, pending = true) } returns REAL_GLOBAL_CONF
        val c = ctrl.getConfs(REAL_MAC_2)
        assertNotNull(c.global)
        assertNotNull(c.location)
        assertNull(c.deviceConf)
        assertNotNull(c.pendingConf)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & pending conf`(){
        every { cS.get("GLOBAL", any()) } returns REAL_GLOBAL_CONF
        every { cS.get(REAL_MAC_2, pending = false) } returns REAL_GLOBAL_CONF
        every { cS.get(REAL_MAC_2, pending = true) } returns REAL_GLOBAL_CONF
        val c = ctrl.getConfs(REAL_MAC_2)
        assertNotNull(c.global)
        assertNotNull(c.location)
        assertNotNull(c.deviceConf)
        assertNotNull(c.pendingConf)
    }

    /**
     * getActual - Testing method to retrieve the current configuration.
     *
     * This method retrieves the most updated conf between: Global, locale, device, and pending.
     * If mac does not exist, returns the global configuration.
     */

    @Test
    @Category(BlackBox::class)
    fun `getActual - mac no exist`(){
        val ts_g = "2019-12-31T12:00:00.000Z"

        // only global conf is set
        every { cS.get(any(), any()) } returns null
        every { cS.get("GLOBAL", any()) } returns ConfData(device = "GLOBAL", timestamp = ts_g)

        // test
        val c = ctrl.getActual(REAL_MAC_X)
        assertNotNull(c)
        assert(c.device == "GLOBAL")
    }

    @Test(expected = IllegalArgumentException::class)
    @Category(BlackBox::class)
    fun `getActual - mac exist & no conf`(){

        // no conf is set
        every { cS.get(any(), any()) } returns null

        // throw exception due to no existence of conf
        val c = ctrl.getActual(REAL_MAC_X)
        assertNotNull(c)
    }

    @Test
    @Category(BlackBox::class)
    fun `getActual - mac exist & conf is global conf`(){

        val ts_g = "2019-12-31T12:00:00.000Z"
        val ts_l = "2019-12-30T12:00:00.000Z"
        val ts_d = "2019-12-29T12:00:00.000Z"
        val ts_p = "2019-12-28T12:00:00.000Z"
        val postcode = "1000"
        val mac = "te:st:in:gm:ac:00"

        every { cS.get("GLOBAL", any()) } returns ConfData(device = "GLOBAL", timestamp = ts_g)
        every { cS.get(postcode, any()) } returns ConfData(device = postcode, timestamp = ts_l)
        every { cS.get(mac, pending = false) } returns ConfData(device = mac, timestamp = ts_d)
        every { cS.get(mac, pending = true) } returns ConfData(device = mac, timestamp = ts_p)
        every { dS.getLastAssignment(any()) } returns null
        every { dS.getLastAssignment(mac) } returns Relation(device=mac, from = BEFORE_TIMESTAMP ,
            user = Person(name = "t1", surname = "t2", postcode = postcode.toInt(), nif = "124232XXX"))

        // test
        val c = ctrl.getActual(mac)
        assertNotNull(c)
        assertEquals(ts_g, c.timestamp)
        assert(c.device == "GLOBAL")
    }

    @Test
    @Category(BlackBox::class)
    fun `getActual - mac exist & conf is locale conf`(){
        val ts_g = "2019-12-30T12:00:00.000Z"
        val ts_l = "2019-12-31T12:00:00.000Z"
        val ts_d = "2019-12-29T12:00:00.000Z"
        val ts_p = "2019-12-28T12:00:00.000Z"
        val postcode = "1000"
        val mac = "te:st:in:gm:ac:00"

        every { cS.get("GLOBAL", any()) } returns ConfData(device = "GLOBAL", timestamp = ts_g)
        every { cS.get(postcode, any()) } returns ConfData(device = postcode, timestamp = ts_l)
        every { cS.get(mac, pending = false) } returns ConfData(device = mac, timestamp = ts_d)
        every { cS.get(mac, pending = true) } returns ConfData(device = mac, timestamp = ts_p)
        every { dS.getLastAssignment(any()) } returns null
        every { dS.getLastAssignment(mac) } returns Relation(device=mac, from = BEFORE_TIMESTAMP ,
            user = Person(name = "t1", surname = "t2", postcode = postcode.toInt(), nif = "124232XXX"))

        // test
        val c = ctrl.getActual(mac)
        assertNotNull(c)
        assertEquals(ts_l, c.timestamp)
        assertEquals(postcode, c.device)
    }

    @Test
    @Category(BlackBox::class)
    fun `getActual - mac exist & conf is device conf`(){
        val ts_g = "2019-12-30T12:00:00.000Z"
        val ts_l = "2019-12-29T12:00:00.000Z"
        val ts_d = "2019-12-31T12:00:00.000Z"
        val ts_p = "2019-12-28T12:00:00.000Z"
        val postcode = "1000"
        val mac = "te:st:in:gm:ac:00"

        every { cS.get("GLOBAL", any()) } returns ConfData(device = "GLOBAL", timestamp = ts_g)
        every { cS.get(postcode, any()) } returns ConfData(device = postcode, timestamp = ts_l)
        every { cS.get(mac, pending = false) } returns ConfData(device = mac, timestamp = ts_d)
        every { cS.get(mac, pending = true) } returns ConfData(device = mac, timestamp = ts_p)
        every { dS.getLastAssignment(any()) } returns null
        every { dS.getLastAssignment(mac) } returns Relation(device=mac, from = BEFORE_TIMESTAMP ,
            user = Person(name = "t1", surname = "t2", postcode = postcode.toInt(), nif = "124232XXX"))

        // test
        val c = ctrl.getActual(mac)
        assertNotNull(c)
        assertEquals(ts_d, c.timestamp)
        assertEquals(mac, c.device)
    }

    @Test
    @Category(BlackBox::class)
    fun `getActual - mac exist & conf is pending conf`(){
        val ts_g = "2019-12-30T12:00:00.000Z"
        val ts_l = "2019-12-28T12:00:00.000Z"
        val ts_d = "2019-12-29T12:00:00.000Z"
        val ts_p = "2019-12-31T12:00:00.000Z"
        val postcode = "1000"
        val mac = "te:st:in:gm:ac:00"

        every { cS.get("GLOBAL", any()) } returns ConfData(device = "GLOBAL", timestamp = ts_g)
        every { cS.get(postcode, any()) } returns ConfData(device = postcode, timestamp = ts_l)
        every { cS.get(mac, pending = false) } returns ConfData(device = mac, timestamp = ts_d)
        every { cS.get(mac, pending = true) } returns ConfData(device = mac, timestamp = ts_p)
        every { dS.getLastAssignment(any()) } returns null
        every { dS.getLastAssignment(mac) } returns Relation(device=mac, from = BEFORE_TIMESTAMP ,
            user = Person(name = "t1", surname = "t2", postcode = postcode.toInt(), nif = "124232XXX"))

        // test
        val c = ctrl.getActual(mac)
        assertNotNull(c)
        assertEquals(ts_p, c.timestamp)
        assertEquals(mac, c.device)
    }

    /**
     * new() - Testing method to create new configurations.
     *
     * device should be "GLOBAL", or an existing postcode, or an existing mac.
     * Also, must have a body, and their sleep_time period should be higher than 10 seconds.
     */

    @Test
    @Category(BlackBox::class)
    fun `new - device is null`(){
        val c = ConfData(device = null, body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)
        val actual = assertFailsWith<IllegalArgumentException> {
            ctrl.new(data = c)
        }
        val expected = "Device is not specified."
        assertEquals(expected, actual.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `new - device is empty`(){
        val mac = "te:st:in:gm:ac:00"
        every { dS.checkExists(any()) } returns null
        every { dS.checkExists(mac) } returns Device(mac=mac)

        val c = ConfData(device = "", body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)
        val actual = assertFailsWith<IllegalArgumentException> {
            ctrl.new(data = c)
        }
        val expected = "Device is not specified."
        assertEquals(expected, actual.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `new - device does not exist`(){
        val mac = "te:st:in:gm:ac:00"
        every { dS.checkExists(any()) } returns null
        //every { dS.checkExists(mac) } returns Device(mac=mac)
        val c = ConfData(device = mac, body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)
        val actual = assertFailsWith<IllegalArgumentException> {
            ctrl.new(data = c)
        }
        val expected = "Device $mac does not exist."
        assertEquals(expected, actual.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `new - invalid postcode & include chars`(){
        val postcode = "12F26"
        val mac = "te:st:in:gm:ac:00"
        every { dS.checkExists(any()) } returns null
        every { dS.checkExists(mac) } returns Device(mac=mac)
        every { lS.getLocationByPostalCode(any()) } returns Location(name = "", postcode = 1234, latitude = 0.0, longitude = 0.0, people = arrayListOf())
        val c = ConfData(device = postcode, body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)
        val actual = assertFailsWith<IllegalArgumentException> {
            ctrl.new(data = c)
        }
        val expected = "Not valid postcode $postcode"
        assertEquals(expected, actual.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `new - device length = 6`(){
        val postcode = "123456"
        val mac = "te:st:in:gm:ac:00"


        val slot : CapturingSlot<ConfData> = slot()
        every { cS.post(capture(slot)) } returns Unit
        every { dS.checkExists(any()) } returns null
        every { dS.checkExists(mac) } returns Device(mac=mac)
        every { lS.getLocationByPostalCode(any()) } returns Location(name = "", postcode = postcode.toInt(), latitude = 0.0, longitude = 0.0, people = arrayListOf())
        val c = ConfData(device = postcode, body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)
        ctrl.new(data = c)
        // avoid save conf if devices length is between 6 and 16
        assertFalse(slot.isCaptured)
    }

    @Test
    @Category(BlackBox::class)
    fun `new - device length = 16`(){
        val postcode = "1234567890123456"
        val mac = "te:st:in:gm:ac:00"
        val slot : CapturingSlot<ConfData> = slot()
        every { cS.post(capture(slot)) } returns Unit
        every { dS.checkExists(any()) } returns null
        every { dS.checkExists(mac) } returns Device(mac=mac)
        every { lS.getLocationByPostalCode(any()) } returns Location(name = "", postcode = 1234, latitude = 0.0, longitude = 0.0, people = arrayListOf())
        val c = ConfData(device = postcode, body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)
        ctrl.new(data = c)
        // avoid save conf if devices length is between 6 and 16
        assertFalse(slot.isCaptured)

    }

    @Test
    @Category(BlackBox::class)
    fun `new - no registered postcode`(){
        every { lS.getLocationByPostalCode(any()) } returns null
        val postcode = "1234"
        val mac = "te:st:in:gm:ac:00"
        every { dS.checkExists(any()) } returns null
        every { dS.checkExists(mac) } returns Device(mac=mac)
        val c = ConfData(device = postcode, body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)
        val actual = assertFailsWith<IllegalArgumentException> {
            ctrl.new(data = c)
        }
        val expected = "Does not exist location with postal code 1234"
        assertEquals(expected, actual.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `new - body is null`(){
        val mac = "te:st:in:gm:ac:00"
        every { dS.checkExists(any()) } returns null
        every { dS.checkExists(mac) } returns Device(mac=mac)
        val c = ConfData(device = mac, body = null, timestamp = BEFORE_TIMESTAMP)
        val actual = assertFailsWith<IllegalArgumentException> {
            ctrl.new(data = c)
        }
        val expected = "Configuration has not body to configure."
        assertEquals(expected, actual.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `new - sleep time is less than 10`(){
        val mac = "te:st:in:gm:ac:00"
        every { dS.checkExists(any()) } returns null
        every { dS.checkExists(mac) } returns Device(mac=mac)
        val c = ConfData(device = mac, body = ConfBody(sleep_sec = 9), timestamp = BEFORE_TIMESTAMP)
        val actual = assertFailsWith<IllegalArgumentException> {
            ctrl.new(data = c)
        }
        val expected = "Sleep time 9 should be greater or equal than 10 seconds."
        assertEquals(expected, actual.message)
    }


    @Test
    @Category(BlackBox::class)
    fun `new - global conf`(){
        val device = "GLOBAL"
        val slot_post : CapturingSlot<ConfData> = slot()
        val slot_device : CapturingSlot<String> = slot()
        val slot_pending : CapturingSlot<Boolean> = slot()
        every { cS.post(capture(slot_post)) } returns Unit
        every { cS.delete(capture(slot_device), capture(slot_pending)) } returns Unit
        every { dS.checkExists(any()) } returns null
        val c = ConfData(device = device, body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)

        ctrl.new(data = c)

        assert(slot_post.isCaptured)
        assertEquals(expected = device, actual = slot_post.captured.device)
        assert(slot_pending.isCaptured)
        assertEquals(expected = false, actual = slot_pending.captured)
        assert(slot_device.isCaptured)
        assertEquals(expected = device, actual = slot_device.captured)
    }

    @Test
    @Category(BlackBox::class)
    fun `new - location conf`(){
        val device = "1000"
        val slot_post : CapturingSlot<ConfData> = slot()
        val slot_device : CapturingSlot<String> = slot()
        val slot_pending : CapturingSlot<Boolean> = slot()
        val c = ConfData(device = device, body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)
        val location = Location(name = "", postcode = 1234, latitude = 0.0, longitude = 0.0, people = arrayListOf())
        every { cS.post(capture(slot_post)) } returns Unit
        every { cS.delete(capture(slot_device), capture(slot_pending)) } returns Unit
        every { dS.checkExists(any()) } returns null
        every { lS.getLocationByPostalCode(any()) } returns location

        ctrl.new(data = c)

        assert(slot_post.isCaptured)
        assertEquals(expected = device, actual = slot_post.captured.device)
    }

    @Test
    @Category(BlackBox::class)
    fun `new - device conf`(){
        val device = "01:12:23:34:45:56"
        val slot_post : CapturingSlot<ConfData> = slot()
        val slot_device : CapturingSlot<String> = slot()
        val slot_pending : CapturingSlot<Boolean> = slot()
        val c = ConfData(device = device, body = ConfBody(sleep_sec = 10), timestamp = BEFORE_TIMESTAMP)
        every { cS.post(capture(slot_post)) } returns Unit
        every { cS.delete(capture(slot_device), capture(slot_pending)) } returns Unit
        every { dS.checkExists(any()) } returns null
        every { dS.checkExists(device) } returns Device(mac=device)

        ctrl.new(data = c)

        assert(slot_post.isCaptured)
        assertEquals(expected = device, actual = slot_post.captured.device)
        assert(slot_pending.isCaptured)
        assertEquals(expected = true, actual = slot_pending.captured)
        assert(slot_device.isCaptured)
        assertEquals(expected = device, actual = slot_device.captured)
    }

    /**
     * updated() - this method
     */

}