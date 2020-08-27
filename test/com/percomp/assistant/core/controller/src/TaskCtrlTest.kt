package com.percomp.assistant.core.controller.src

import categorymarker.BlackBox
import com.percomp.assistant.core.controller.domain.TaskCtrl
import com.percomp.assistant.core.controller.services.DeviceService
import com.percomp.assistant.core.controller.services.TaskService
import com.percomp.assistant.core.model.*
import com.percomp.assistant.core.util.communication.RaspiAction
import io.ktor.util.KtorExperimentalAPI
import io.mockk.CapturingSlot
import io.mockk.every
import org.junit.experimental.categories.Category
import io.mockk.mockk
import io.mockk.slot
import java.time.Instant
import kotlin.IllegalArgumentException
import kotlin.test.*

/**
 * Test [TaskCtrl] class.
 *
 * @author Álvaro Velasco Gil - alvvela
 */
@KtorExperimentalAPI
class TaskCtrlTest {

    /**
     * Init mocks
     */
    companion object {
        val ctrl : TaskCtrl
        val dS : DeviceService = mockk(relaxed=true)
        val tS : TaskService = mockk(relaxed=true)

        init {
            ctrl = TaskCtrl(deviceService = dS, taskService = tS)
        }
    }

    @BeforeTest
    fun `init dependencies`() {
        println("init test dependencies")
        every { dS.getLastAssignment(any()) } returns null
        every { tS.newEventType(any(), any()) } returns false
    }

    /**
     * addEvent() - Testing method to add new events.
     *
     * Name cannot be empty or null, and should be less than [Constants.NAME]
     * Content cannot be null or empty, and should be less than [Constants.EVENT_CONTENT]
     * Name is unique.
     */

    @Test
    @Category(BlackBox::class)
    fun `addEvent - name is null`(){
        val name : String? = null
        val content : String? = "Content value"
        val slot_name : CapturingSlot<String> = slot()
        val slot_event : CapturingSlot<String> = slot()
        val exception = "Name cannot be empty."
        every {tS.newEventType(capture(slot_name), capture(slot_event))}

        // execute
        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addEvent(name = name, content = content)
        }

        // check
        assertEquals(expected = exception, actual = e.message)
        assertEquals(false, slot_name.isCaptured)
        assertEquals(false, slot_event.isCaptured)
    }

    @Test
    @Category(BlackBox::class)
    fun `addEvent - name is empty`(){
        val name : String? = ""
        val content : String? = "Content value"
        val slot_name : CapturingSlot<String> = slot()
        val slot_event : CapturingSlot<String> = slot()
        val exception = "Name cannot be empty."
        every {tS.newEventType(capture(slot_name), capture(slot_event))}

        // execute
        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addEvent(name = name, content = content)
        }

        // check
        assertEquals(expected = exception, actual = e.message)
        assertEquals(false, slot_name.isCaptured)
        assertEquals(false, slot_event.isCaptured)
    }

    @Test
    @Category(BlackBox::class)
    fun `addEvent - name is too long`(){
        val name : String? = "ThisNameIs16len!"
        val content : String? = "Content value"
        val slot_name : CapturingSlot<String> = slot()
        val slot_event : CapturingSlot<String> = slot()
        val exception = "Wrong name: Length < 16."
        every {tS.newEventType(capture(slot_name), capture(slot_event))}

        // execute
        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addEvent(name = name, content = content)
        }

        // check
        assertEquals(expected = exception, actual = e.message)
        assertEquals(false, slot_name.isCaptured)
        assertEquals(false, slot_event.isCaptured)
    }

    @Test
    @Category(BlackBox::class)
    fun `addEvent - event is null`(){
        val name : String? = "N"
        val content : String? = null
        val slot_name : CapturingSlot<String> = slot()
        val slot_event : CapturingSlot<String> = slot()
        val exception = "Content cannot be empty."
        every {tS.newEventType(capture(slot_name), capture(slot_event))}

        // execute
        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addEvent(name = name, content = content)
        }

        // check
        assertEquals(expected = exception, actual = e.message)
        assertEquals(false, slot_name.isCaptured)
        assertEquals(false, slot_event.isCaptured)
    }

    @Test
    @Category(BlackBox::class)
    fun `addEvent - event is empty`(){
        val name : String? = "ThisNameIsValid"
        val content : String? = ""
        val slot_name : CapturingSlot<String> = slot()
        val slot_event : CapturingSlot<String> = slot()
        val exception = "Content cannot be empty."
        every {tS.newEventType(capture(slot_name), capture(slot_event))}

        // execute
        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addEvent(name = name, content = content)
        }

        // check
        assertEquals(expected = exception, actual = e.message)
        assertEquals(false, slot_name.isCaptured)
        assertEquals(false, slot_event.isCaptured)
    }

    @Test
    @Category(BlackBox::class)
    fun `addEvent - event is too long`(){
        val name : String? = "Name"
        var content = "a"
        for (i in 1..1024) content += "a"
        val slot_name : CapturingSlot<String> = slot()
        val slot_event : CapturingSlot<String> = slot()
        val exception = "Wrong content: Length < 1024."
        every {tS.newEventType(capture(slot_name), capture(slot_event))}

        // execute
        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addEvent(name = name, content = content)
        }

        // check
        assertEquals(expected = exception, actual = e.message)
        assertEquals(false, slot_name.isCaptured)
        assertEquals(false, slot_event.isCaptured)
    }

    @Test
    @Category(BlackBox::class)
    fun `addEvent - already exist an event with this name`(){
        val name : String? = "Name"
        val content = "Event content"
        val exception = "Event cannot be added: The name is already in use."
        every {tS.newEventType(any(), any()) } returns true
        every {tS.newEventType(name!!, any()) } returns false

        // execute
        val e = assertFailsWith<IllegalStateException> {
            ctrl.addEvent(name = name, content = content)
        }

        // check
        assertEquals(expected = exception, actual = e.message)
    }

    /**
     * addTask() - This method add a task to a specific device.
     * Task could be a global task, a task for a specific location, or for a specific device.
     * So, task should be "GLOBAL" or a postcode, or a mac
     */

    @Test
    @Category(BlackBox::class)
    fun `addTask - device is null`(){
        val task = Task()
        val by = "admin"
        val exception = "Device cannot be empty."
        task.device = null
        task.event = "Event"

        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addTask(task, by)
        }

        assertEquals(exception, e.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `addTask - device is empty`(){
        val task = Task()
        val by = "admin"
        val exception = "Device cannot be empty."
        task.device = null
        task.event = "Event"

        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addTask(task, by)
        }

        assertEquals(exception, e.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `addTask - event is null`(){
        val task = Task()
        val by = "admin"
        val exception = "Task must be a type of event."
        task.device = "Device"
        task.event = null

        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addTask(task, by)
        }

        assertEquals(exception, e.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `addTask - event is empty`(){
        val task = Task()
        val by = "admin"
        val exception = "Task must be a type of event."
        task.device = "Device"
        task.event = ""

        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addTask(task, by)
        }

        assertEquals(exception, e.message)
    }


    @Test
    @Category(BlackBox::class)
    fun `addTask - event does not exist`(){
        val task = Task()
        val by = "admin"
        task.device = "Device"
        task.event = "Evento que no existe"
        val exception = "Event '${task.event}' does not exist."

        every { tS.getEventType(any()) } returns Event("Any Event")
        // event does not exist
        every { tS.getEventType(task.event!!) } returns null

        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addTask(task, by)
        }

        assertEquals(exception, e.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `addTask - device does not exist`(){
        val task = Task()
        val by = "admin"
        task.device = "es:te:no:ex:is:te"
        task.event = "Any event"
        val exception = "Device does not exist."

        every { tS.getEventType(any()) } returns Event("Any Event")
        // device does not exist
        every { dS.checkExists(task.device!!) } returns null

        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.addTask(task, by)
        }

        assertEquals(exception, e.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `addTask - global task`(){
        val pc1 = 1000
        val pc2 = 2000
        val d1 = Device(mac = "es:te:ex:is:te:01")
        d1.relation = Relation(user = Person(postcode = pc1, name="", surname = "", nif = ""), from = "2019")
        val d2 = Device(mac = "es:te:ex:is:te:02")
        d2.relation = Relation(user = Person(postcode = pc2, name="", surname = "", nif = ""), from = "2019")
        val d3 = Device(mac = "es:te:ex:is:te:03")
        d3.relation = Relation(user = Person(postcode = pc2, name="", surname = "", nif = ""), from = "2019")

        val task = Task()
        val by = "admin"
        task.device = "GLOBAL"
        task.event = "Any event"

        // counter of devices
        val should_be = 3
        var count = 0
        every { tS.newTask(any()) } answers {
            count++
            true
        }
        every { tS.getEventType(any()) } returns Event("Any Event")
        every { dS.checkExists(task.device!!) } returns Device("Any device")
        every { dS.getAll() } returns listOf(d1, d2, d3)

        // execute it
        ctrl.addTask(task, by)

        assertEquals(should_be, count)
    }

    @Test
    @Category(BlackBox::class)
    fun `addTask - location has no device`(){
        val pc1 = 1000
        val pc2 = 2000
        val d1 = Device(mac = "es:te:ex:is:te:01")
        d1.relation = Relation(user = Person(postcode = pc1, name="", surname = "", nif = ""), from = "2019")
        val d2 = Device(mac = "es:te:ex:is:te:02")
        d2.relation = Relation(user = Person(postcode = pc2, name="", surname = "", nif = ""), from = "2019")
        val d3 = Device(mac = "es:te:ex:is:te:03")
        d3.relation = Relation(user = Person(postcode = pc2, name="", surname = "", nif = ""), from = "2019")

        val task = Task()
        val by = "admin"
        task.device = "3000"
        task.event = "Any event"

        // counter of devices
        val should_be = 0
        var count = 0
        every { tS.newTask(any()) } answers {
            count++
            true
        }
        every { tS.getEventType(any()) } returns Event("Any Event")
        every { dS.checkExists(task.device!!) } returns Device("Any device")
        every { dS.getAll() } returns listOf(d1, d2, d3)

        // execute it
        ctrl.addTask(task, by)

        assertEquals(should_be, count)
    }

    @Test
    @Category(BlackBox::class)
    fun `addTask - location has devices`(){
        val pc1 = 1000
        val pc2 = 2000
        val d1 = Device(mac = "es:te:ex:is:te:01")
        d1.relation = Relation(user = Person(postcode = pc1, name="", surname = "", nif = ""), from = "2019")
        val d2 = Device(mac = "es:te:ex:is:te:02")
        d2.relation = Relation(user = Person(postcode = pc2, name="", surname = "", nif = ""), from = "2019")
        val d3 = Device(mac = "es:te:ex:is:te:03")
        d3.relation = Relation(user = Person(postcode = pc2, name="", surname = "", nif = ""), from = "2019")

        val task = Task()
        val by = "admin"
        task.device = "2000"
        task.event = "Any event"

        // counter of devices
        val should_be = 2
        var count = 0
        every { tS.newTask(any()) } answers {
            count++
            true
        }
        every { tS.getEventType(any()) } returns Event("Any Event")
        every { dS.checkExists(task.device!!) } returns Device("Any device")
        every { dS.getAll() } returns listOf(d1, d2, d3)

        // execute it
        ctrl.addTask(task, by)

        assertEquals(should_be, count)
    }

    @Test
    @Category(BlackBox::class)
    fun `addTask - device exist`(){
        val pc1 = 1000
        val pc2 = 2000
        val d1 = Device(mac = "es:te:ex:is:te:01")
        d1.relation = Relation(user = Person(postcode = pc1, name="", surname = "", nif = ""), from = "2019")
        val d2 = Device(mac = "es:te:ex:is:te:02")
        d2.relation = Relation(user = Person(postcode = pc2, name="", surname = "", nif = ""), from = "2019")
        val d3 = Device(mac = "es:te:ex:is:te:03")
        d3.relation = Relation(user = Person(postcode = pc2, name="", surname = "", nif = ""), from = "2019")

        val task = Task()
        val by = "admin"
        task.device = "es:te:ex:is:te:01"
        task.event = "Any event"
        // counter of devices
        val should_be = 1
        var count = 0
        every { tS.newTask(any()) } answers {
            count++
            true
        }
        every { tS.getEventType(any()) } returns Event("Any Event")
        every { dS.checkExists(task.device!!) } returns Device("Any device")
        every { dS.getAll() } returns listOf(d1, d2, d3)

        // execute it
        ctrl.addTask(task, by)

        assertEquals(should_be, count)
    }

    /**
     * getAll() - retrieve all task assigned to a specific device  in a specific interval on time
     *
     * Device cannot be empty or null
     * 'From' cannot be empty or null
     */

    @Test
    @Category(BlackBox::class)
    fun `getAll - device is empty`(){
        val device = ""
        val from = "2019-12-12"
        val to = ""
        val exc = "Device cannot be empty"

        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.getAll(device, from, to)
        }

        assertEquals(exc, e.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `getAll - device is null`(){
        val device = null
        val from = "2019-12-12"
        val to = ""
        val exc = "Device cannot be empty"

        val e = assertFailsWith<IllegalArgumentException> {
            ctrl.getAll(device, from, to)
        }

        assertEquals(exc, e.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `getAll - from is empty`(){
        val device = "Device"
        val from = ""
        val to = ""

        assertFailsWith<IllegalArgumentException> {
            ctrl.getAll(device, from, to)
        }
    }

    @Test
    @Category(BlackBox::class)
    fun `getAll - from is null`(){
        val device = "Device"
        val from = null
        val to = ""

        assertFailsWith<IllegalArgumentException> {
            ctrl.getAll(device, from, to)
        }
    }

    @Test
    @Category(BlackBox::class)
    fun `getAll - retrieve no tasks`(){
        val device = "Device"
        val from = "2019-12-12"
        val to = ""

        every { tS.getTask(device, from, to) } returns listOf()

        val t = ctrl.getAll(device, from, to)

        assertEquals(0, t.size)
    }

    @Test
    @Category(BlackBox::class)
    fun `getAll - retrieve tasks`(){
        val device = "Device"
        val from = "2019-12-12"
        val to = ""

        val t1 = Task()
        val t2 = Task()
        every { tS.getTask(device, from, to) } returns listOf(t1, t2)

        val t = ctrl.getAll(device, from, to)

        assertEquals(2, t.size)
    }

    /**
     * done() - mark a task as done
     *
     * task cannot be empty or null
     * if task does not exist, return null
     * if task exist, return content
     *
     */

    @Test
    @Category(BlackBox::class)
    fun `done - task is empty`(){
        val task = ""
        val device = "do:es:nt:ma:tt:er"
        val exp = "Task cannot be null."

        val e = assertFailsWith<IllegalArgumentException> { ctrl.done(device, task) }
        assertEquals(exp, e.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `done - task is null`(){
        val task = null
        val device = "do:es:nt:ma:tt:er"
        val exp = "Task cannot be null."

        val e = assertFailsWith<IllegalArgumentException> { ctrl.done(device, task) }
        assertEquals(exp, e.message)
    }

    @Test
    @Category(BlackBox::class)
    fun `done - task does not exist`(){
        val task = "AnyTask"
        val device = "do:es:nt:ma:tt:er"
        val now = Instant.now().toString()

        val slot_device : CapturingSlot<String> = slot()
        val slot_event : CapturingSlot<String> = slot()
        val slot_date : CapturingSlot<String> = slot()

        every { dS.getContent(device, task) } returns null
        every { tS.endTask(capture(slot_device), capture(slot_event), capture(slot_date)) } returns Unit

        val e = ctrl.done(device, task)
        assertEquals(null, e)
        assertTrue(slot_device.isCaptured)
        assertTrue(slot_event.isCaptured)
        assertTrue(slot_date.isCaptured)
        assertTrue(now < slot_date.captured)
    }

    @Test
    @Category(BlackBox::class)
    fun `done - task exists`(){
        val task = "AnyTask"
        val content = "content"
        val device = "do:es:nt:ma:tt:er"
        val now = Instant.now().toString()

        val slot_device : CapturingSlot<String> = slot()
        val slot_event : CapturingSlot<String> = slot()
        val slot_date : CapturingSlot<String> = slot()

        every { dS.getContent(device, task) } returns content
        every { tS.endTask(capture(slot_device), capture(slot_event), capture(slot_date)) } returns Unit

        val e = ctrl.done(device, task)
        assertEquals(content, e)
        assertTrue(slot_device.isCaptured)
        assertTrue(slot_event.isCaptured)
        assertTrue(slot_date.isCaptured)
        assertTrue(now < slot_date.captured)
    }


    /**
     * newStatus() - Set the current status of a specific device, and returns the pending task to do.
     *
     */

    @Test
    fun `newStatus - device does not exist`(){
        val device = "do:es:no:te:xi:st"
        val status = RaspiAction.LOGIN

        every { dS.newStatus(any(), any()) } returns Unit
        every { tS.getPendingTaskForDevice(any()) } returns listOf()

        val e = ctrl.newStatus(device, status)

        // tries to save but returns nothing
        assertEquals(0, e.size)
    }


    @Test
    fun `newStatus - device does exist`(){
        val device = "it:is:ex:is:ti:ng"
        val status = RaspiAction.LOGIN
        val t1 = Task()
        val t2 = Task()

        every { dS.newStatus(any(), any()) } returns Unit
        every { tS.getPendingTaskForDevice(any()) } returns listOf(t1, t2)

        val e = ctrl.newStatus(device, status)
        // saves it and returns pending task
        assertEquals(2, e.size)
    }
}