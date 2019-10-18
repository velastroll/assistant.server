package com.percomp.assistant.core.util.communication


/**
 * List of actions available on each device.
 * The device sends a POST request with the status [ALIVE].
 * Then, the server can respond, or send a new action with the response.
 * After this requested action by de side of the server, the device should send a new request in function
 * of the action that the server send. It's detailed on each enum.
 *
 * @date 2019-10-18
 * @author Álvaro Velasco Gil
 */
public enum class RaspiAction{

    /**
     * R: I have credentials, and I'm still alive
     * S: {Action: "ALIVE", status: "OK"} or Http.Unauthorized
     */ ALIVE,

    // if R receive an Unauthorized response, it try to login with a request at the specific service.
    // so S should reply the Tokens, or Unauthorized.

    /**
     * R: {action: "ALIVE"}
     * S: {action: "CONFIG", status: "OK", data: Configuration}
     * R: {action: "CONFIG", status "OK"} ||  {action: "CONFIG", status "ERROR", data: ErrorMsg}
     * S: {Action: "ALIVE", status: "OK"}
     */ CONFIG

,

}
