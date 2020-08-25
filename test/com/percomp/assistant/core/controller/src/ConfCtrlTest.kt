package com.percomp.assistant.core.controller.src

import categorymarker.BlackBox
import com.percomp.assistant.core.controller.domain.ConfCtrl
import com.percomp.assistant.core.controller.services.ConfService
import com.percomp.assistant.core.controller.services.DeviceService
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.model.ConfDatas
import io.ktor.util.KtorExperimentalAPI
import io.mockk.every
import org.junit.experimental.categories.Category
import kotlin.test.BeforeTest
import kotlin.test.Test
import io.mockk.mockk

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

        init {
            ctrl = ConfCtrl(deviceService = dS, confService = cS, locationService = lS)
        }
    }

    @BeforeTest
    fun `init dependencies`() {
        println("init test dependencies")
        every { ctrl.getConfs(any()) } returns ConfDatas()
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac does not exist, returns null confs`(){
        assert(false)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists but has no relation, return null location`(){
        assert(false)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & has relation but there is no conf for the location`(){
        assert(false)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & has relation & location conf`(){
        assert(false)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists but there is no device conf`(){
        assert(false)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & there is device conf`(){
        assert(false)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists but there is no pending conf`(){
        assert(false)
    }

    @Test
    @Category(BlackBox::class)
    fun `getConfs - mac exists & there is pending conf`(){
        assert(false)
    }



}