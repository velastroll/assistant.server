package app.di

import com.percomp.assistant.core.app.config.oauth.TokenCtrl
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
    single { TokenCtrl() }
}
