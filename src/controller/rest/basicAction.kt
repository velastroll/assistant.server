package com.percomp.assistant.core.rest


import com.percomp.assistant.core.app.config.oauth.TokenCtrl
import com.percomp.assistant.core.controller.domain.DeviceCtrl
import com.percomp.assistant.core.controller.domain.TaskCtrl
import com.percomp.assistant.core.model.Event
import com.percomp.assistant.core.model.Task
import com.percomp.assistant.core.model.UserType
import com.percomp.assistant.core.util.communication.RaspiAction
import com.percomp.assistant.core.util.communication.Response
import io.ktor.application.call
import io.ktor.auth.OAuth2Exception
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.server.engine.BaseApplicationResponse

fun Route.basicAction(){

    val deviceCtrl = DeviceCtrl()
    val taskCtrl = TaskCtrl()
    val auth = TokenCtrl()

    route("device"){

        /**
         * This call try both login a device as a sign up if is not registered yet.
         */
        post("login"){
            try {
                log.warn("[/device/login] ------------------------- ")
                // retrieve data
                val postParameters: Parameters = call.receiveParameters()
                log.warn("/device/login : request = $postParameters")
                val request = CredentialRequest(user = postParameters["user"]!!, password = postParameters["password"]!!)
                // check account
                val auth = deviceCtrl.check(request)
                log.warn("/device/login : auth=$auth")
                // return credentials
                call.respond(HttpStatusCode.OK, auth)
            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException){
            }
            catch(e : OAuth2Exception.InvalidGrant){
                try {
                    log.warn("[login] Unauthorized: $e")
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized: $e")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.warn("[login] Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError, "Internal error: $e")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }


        /**
         * Device says that it's alive, so reply it their available task.
         */
        get("alive"){
            try {
                log.warn("[alive] -------------------------- ")
                // check authorization
                var accesstoken =
                    call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("Missing token.")
                accesstoken = auth.cleanTokenTag(accesstoken)
                val device = auth.checkAccessToken(UserType.DEVICE, accesstoken)
                    ?: throw OAuth2Exception.InvalidGrant("Expired token.")

                log.info("[alive] Retrieved device: $device")
                // save state on DB
                val tasks = taskCtrl.newStatus(device, RaspiAction.ALIVE)

                // reply
                if (tasks.isEmpty()) {
                    log.info("[alive] Respond OK")
                    call.respond(HttpStatusCode.OK, Response(status = 200, action = RaspiAction.ALIVE))
                } else {
                    log.info("[alive] Respond tasks [${tasks.size}]")
                    call.respond(HttpStatusCode.MultipleChoices, tasks)
                }
            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException){}
            catch(e : OAuth2Exception.InvalidGrant){
                try {
                    log.warn("[alive] Unauthorized: $e")
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized: $e")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.warn("[alive] Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError, "Internal error: $e")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

        /**
         * Device says that is doing one task, so the core marks it as done.
         */
        get("/task/{task}/doing"){
            try {
                log.warn("[doing task] ----------------------- ")
                // check authorization
                var accesstoken =
                    call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("Missing token.")
                accesstoken = auth.cleanTokenTag(accesstoken)
                val device = auth.checkAccessToken(UserType.DEVICE, accesstoken)
                    ?: throw OAuth2Exception.InvalidGrant("Expired token.")
                log.info("[doing task] Retrieved device: $device")

                // retrieve task and update device status
                val task = call.parameters["task"]
                log.info("[doing task] Doing the task: $task")
                taskCtrl.newStatus(device, RaspiAction.DOING_TASK, task)

                // mark task as done
                taskCtrl.done(device, task)
                log.info("[doing task] Updated task as done")

                // response
                call.respond(HttpStatusCode.OK, Response(status = 200, action = RaspiAction.ALIVE))
                log.info("[alive] Respond OK")
            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException){}
            catch(e : OAuth2Exception.InvalidGrant){
                try {
                    log.warn("[doing task] Unauthorized: $e")
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized: $e")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.warn("[Doing task] Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError, "Internal error: $e")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }


    }

    /* WORKERS */

    route("worker"){

        /**
         * Worker creates a new type of event
         */
        post("event"){
            try {
                // check authorization
                log.debug("[worker/event] --")
                var accesstoken = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("Missing token")
                    accesstoken = auth.cleanTokenTag(accesstoken)
                val worker_username = auth.checkAccessToken(UserType.USER, accesstoken)
                val request = call.receive<Event>()
                log.debug("[worker/event] Access for $worker_username")
                // add relation
                taskCtrl.addEvent(name= request.name, content = request.content)
                // respond it
                log.debug("[worker/event] Ok")
                call.respond(HttpStatusCode.OK, "Added.")
            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException){
            }
            catch(e : OAuth2Exception.InvalidGrant){
                try {
                    log.error("[worker/event] Unauthorized: ${e.message}")
                    call.respond(HttpStatusCode.Unauthorized)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.error("[worker/event] Internal error: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

        /**
         * Retrieve all the possible types of events.
         */
        get("event"){
            try {
                // check authorization
                log.debug("[worker/event] ------------------------")
                var accesstoken = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("Missing token")
                accesstoken = auth.cleanTokenTag(accesstoken)
                val worker_username = auth.checkAccessToken(UserType.USER, accesstoken)
                log.debug("[worker/event] Access for $worker_username")

                // retrieve event
                val events = taskCtrl.getEvents()
                log.debug("[worker/event] Retrieved type of events.")

                call.respond(HttpStatusCode.OK, events)
                log.debug("[worker/event] Ok.")

            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException){
            }
            catch(e : OAuth2Exception.InvalidGrant){
                try {
                    log.error("[worker/event] Unauthorized: ${e.message}")
                    call.respond(HttpStatusCode.Unauthorized)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.error("[worker/event] Internal error: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

        /**
         * Worker creates a new task for a specific device or group.
         */
        post("task"){
            try {
                // check authorization
                log.info("[worker/task] --")
                var accesstoken = call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("Missing token")
                    accesstoken = auth.cleanTokenTag(accesstoken)
                val worker_username = auth.checkAccessToken(UserType.USER, accesstoken) ?: throw OAuth2Exception.InvalidGrant("Expired token")
                val request = call.receive<Task>()
                log.info("[worker/task] Access for $worker_username")
                // add relation
                taskCtrl.addTask(task = request, by=worker_username)
                // respond it
                log.info("[worker/task] Ok")
                call.respond(HttpStatusCode.OK, "Added.")
            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException){
            }
            catch(e : OAuth2Exception.InvalidGrant){
                try {
                    log.warn("[worker/task] Unauthorized: ${e.message}")
                    call.respond(HttpStatusCode.Unauthorized)
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.error("[worker/task] Internal Error: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, "${e.message}")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }

        /**
         * Retrieve all the task of a specific device.
         */
        post("tasks"){
            try {
                log.warn("[worker/tasks] --")
                // check authorization
                var accesstoken =
                    call.request.headers["Authorization"] ?: throw OAuth2Exception.InvalidGrant("Missing token.")
                accesstoken = auth.cleanTokenTag(accesstoken)
                val worker = auth.checkAccessToken(UserType.DEVICE, accesstoken)
                    ?: throw OAuth2Exception.InvalidGrant("Expired token.")

                log.info("[worker/tasks] Worker: $worker is authorized.")
                val req = call.receive<TaskRequest>()

                // Retrieve task
                log.info("[worker/tasks] Retrieving all task of ${req.device}.")
                val tasks = taskCtrl.getAll(req.device, req.from, req.to)

                // reply
                log.info("[worker/tasks] Ok")
                call.respond(HttpStatusCode.OK, tasks)
            }
            catch (e: BaseApplicationResponse.ResponseAlreadySentException){}
            catch(e : OAuth2Exception.InvalidGrant){
                try {
                    log.warn("[alive] Unauthorized: $e")
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized: $e")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
            catch(e : Exception){
                try {
                    log.warn("[alive] Internal error: $e")
                    call.respond(HttpStatusCode.InternalServerError, "Internal error: $e")
                } catch (e: BaseApplicationResponse.ResponseAlreadySentException){
                }
            }
        }
    }
}

data class Request (
    var status : Int = 404,
    var action : RaspiAction? = null,
    var data : String? = null
)

data class TaskRequest (
    var device : String? = null,
    var from : String = "2018-12-12T123:59:59.999Z",
    var to : String = "2999-12-12T123:59:59.999Z"
)