package org.atmosphere.samples.chat

import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpServlet}
import com.weiglewilczek.slf4s.Logging
import org.atmosphere.commons.util.EventsLogger
import org.atmosphere.cpr._
import scala.collection.JavaConversions._
import net.liftweb.json.JsonParser

/**
 * Simple Servlet that implement the logic to build a Chat application using
 * a {@link Meteor} to suspend and broadcast chat message.
 *
 * @author Jeanfrancois Arcand
 * @author TAKAI Naoto (Orginial author for the Comet based Chat).
 * @author Stuart Roebuck (conversion to Scala).
 */
class ScalaMeteorChat extends HttpServlet with AtmosphereResourceEventListener with Logging {

  /**
   * Create a {@link Meteor} and use it to suspend the response.
   * @param req An {@link HttpServletRequest}
   * @param res An {@link HttpServletResponse}
   */
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    logger.info("-> doGet")
    val mym = getMeteorForRequestResponse(request, response)
    mym.broadcast("%s has suspended a connection from %s".format(request.getServerName, request.getRemoteAddr))
  }

  /**
   * Receive a posted message.  If there is already a Meteor then use it, otherwise create one.
   *
   * @param req An {@link HttpServletRequest}
   * @param res An {@link HttpServletResponse}
   */
  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    logger.info("-> doPost")
    val myMeteor = getMeteorForRequestResponse(request, response)
    onReceiptOfBroadcast(myMeteor, request, response)
  }

  def onSuspend(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onSuspend")
  }

  def onResume(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onResume")
  }

  def onDisconnect(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onDisconnect")
  }

  /**
   * This appears to be called when Meteor is used to broadcast a message.  It appears to be a message going out, not
   * a message coming in.
   */
  def onBroadcast(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onBroadcast - %s".format(event.getMessage))
  }

  def onThrowable(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onThrowable")
  }

  /**
   * Given a request and response, return the MyMeteor session attribute previously associated with the session or
   * create a new MyMeteor object and attach it to the session for future use.
   */
  private def getMeteorForRequestResponse(request: HttpServletRequest, response: HttpServletResponse): MyMeteor = {
    logger.info("-> newMeteorForRequestResponse")
    response.setContentType("text/html;charset=UTF-8")
    MyMeteor(request, listeners = Seq(this))
  }


  /**
   * Handle a message received that appears to be a "Post" but may come from a Meteor.
   */
  private def onReceiptOfBroadcast(myMeteor: MyMeteor, request: HttpServletRequest, response: HttpServletResponse) {
    logger.info("==> onMeteorBroadcast")
    response.setCharacterEncoding("UTF-8")
    val action: String = request.getParameterValues("action")(0)
    val name: String = request.getParameterValues("name")(0)
//    logger.info("request.getParts: %s".format(request.getParts.toList))

    logger.info("action: %s, name: %s".format(action, name))

    action match {
      case "login" =>
        request.getSession.setAttribute("name", name)
        myMeteor.broadcast("System Message from %s:  %s has joined".format(request.getServerName, name))

      case "post" =>
        val message: String = request.getParameterValues("message")(0)
        myMeteor.broadcast("%s\n  %s".format(name,message))

      case _ =>
        response.setStatus(422)
    }
  }

}



/**
 * Simple Scala wrapper for Atmosphere.Meteor class.
 *
 * @param request the incoming HttpServletRequest from which to establish a long-polling transport.
 * @param filters BroadcastFilters that filter any data sent to the client.
 * @param listeners a sequence of `AtmosphereResourceEventListener` to receive events as they happen.  If this
 *    parameter is not supplied then the default `EventsLogger` listener is added which logs events to the logger.
 */
case class MyMeteor(meteor: Meteor) {

  /**
   * Add a listener that will be sent every event that occurs with the Meteor, whether it is the arrival of data or the
   * sending of data.  You can add more than one listener and they will all receive all events.
   */
  def addListener(eventsListener: AtmosphereResourceEventListener) { meteor.addListener(eventsListener)}

  /**
   * Suspend the long-polling connection.  In other words, hold it ready to send a response to the client whenever
   * required.
   */
  def suspend(timeLimitMillis: Int = -1): MyMeteor = {
    meteor.suspend(timeLimitMillis)
    this
  }

  /**
   * Broadcast a message on the suspended Meteor.
   */
  def broadcast(data: String) {
    // Note that the native broadcast takes an object, so there is probably extra functionality to be unlocked by
    // expanding support beyond a simple String.
    meteor.broadcast(data)
  }

}

object MyMeteor {

  import scala.collection.JavaConversions._

  /**
   * Create a Meteor object or return one that has already been cached for the particular HttpServletRequest.
   */
  def apply(request: HttpServletRequest,
            filters: Set[BroadcastFilter] = Set(),
            listeners: Seq[AtmosphereResourceEventListener] = Seq(new EventsLogger)): MyMeteor = {
    Option(Meteor.lookup(request)).map(MyMeteor(_)).getOrElse {
      val m = Meteor.build(request, filters.toList, null)
      listeners.foreach(m.addListener(_))
      MyMeteor(m).suspend()
    }
  }

}
