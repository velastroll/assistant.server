package app.di

import com.percomp.assistant.core.controller.domain.ConfCtrl
import com.percomp.assistant.core.controller.domain.DeviceCtrl
import com.percomp.assistant.core.controller.domain.TaskCtrl
import com.percomp.assistant.core.controller.domain.UserCtrl
import com.percomp.assistant.core.controller.services.LocationCtrl
import com.percomp.assistant.core.controller.services.LocationService
import com.percomp.assistant.core.dao.ConfRepo
import com.percomp.assistant.core.dao.DeviceRepo
import com.percomp.assistant.core.services.LocationRepo
import com.percomp.assistant.core.services.PeopleRepo
import com.percomp.assistant.core.services.TaskRepo
import com.percomp.assistant.core.services.UserRepo
import controller.services.*
import org.koin.dsl.module

/**
 * This module has got the Koin configuration with the declarations of the needed instances.
 * This instances could be injected now, applying the dependency inversion principle.
 */
val myModule = module {
    // Declares a singleton of repository instance
    single { ConfRepo() as ConfService }
    single { DeviceRepo() as DeviceService }
    single { LocationRepo() as LocationService }
    single { PeopleRepo() as PeopleService }
    single { TaskRepo() as TaskService }
    single { UserRepo() as UserService }

    // Creates a singleton instance of domain controllers,  auto-injecting it the service instance
    single { ConfCtrl(get(), get(), get()) }
    single { DeviceCtrl(get(), get(), get(), get()) }
    single { LocationCtrl(get(), get()) }
    single { TaskCtrl(get(), get()) }
    single { UserCtrl(get(), get(), get()) }
}
