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
    ALIVE,
    CONFIG,
    LOGIN,
    DOING_TASK
}
