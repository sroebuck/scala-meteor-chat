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
class ScalaMeteorChat extends HttpServlet with Logging {

  /**
   * I believe that this is only called at the beginning when the client tries to establish a connection.  For this
   * reason this code simply creates a Meteor if there isn't one already associated with the Session.
   */
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    logger.info("-> doGet")
    MyMeteor(request, listeners = Seq(MyListener, new EventsLogger))
  }

  /**
   * I believe that this method is called either directly as an HTTP POST request or as a result of a message being
   * sent through an Atmosphere based connection.  In either case the code handles the situation the same, however
   * at present there is an issue that the jquery.atmosphere.js plugin doesn't encode the messages in a web form
   * format when they are transmitted by websocket so they are not displayed properly.
   */
  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    logger.info("-> doPost")
    val myMeteor = MyMeteor(request, listeners = Seq(MyListener, new EventsLogger))
    onReceiptOfBroadcast(myMeteor, request, response)
  }

  /**
   * This completes the handling of the message from the client.  It has been separated into another method as it made
   * it easier to switch things around a bit when trying to debug what is or isn't happening.
   */
  private def onReceiptOfBroadcast(myMeteor: MyMeteor, request: HttpServletRequest, response: HttpServletResponse) {
    logger.info("==> onMeteorBroadcast")
    response.setCharacterEncoding("UTF-8")
    val action: String = request.getParameterValues("action")(0)
    val name: String = request.getParameterValues("name")(0)
    logger.info("action: %s, name: %s".format(action, name))
//    logger.info("request.getParts: %s".format(request.getParts.toList))

    action match {
      case "login" =>
        request.getSession.setAttribute("name", name)
        myMeteor.broadcast("System Message from %s:  %s has joined".format(request.getServerName, name))

      case "post" =>
        val message: String = request.getParameterValues("message")(0)
        myMeteor.broadcast("%s\n  %s".format(name,message))

      case _ =>
        logger.warn("Unmatching action!")
        response.setStatus(422)
    }
  }

}

object MyListener extends AtmosphereResourceEventListener with Logging {

  def onSuspend(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onSuspend")
  }

  def onResume(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onResume")
  }

  def onDisconnect(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onDisconnect")
  }

  def onBroadcast(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onBroadcast - %s".format(event.getMessage))
  }

  def onThrowable(event: AtmosphereResourceEvent[HttpServletRequest, HttpServletResponse]) {
    logger.info("-> onThrowable")
  }

}


/**
 * Simple Scala wrapper for Atmosphere.Meteor class.  This doesn't do a great deal but provides me with a way of
 * locally documenting the Atmosphere package whilst I'm trying to get things to work.
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

object MyMeteor extends Logging {

  /**
   * Create a Meteor object or return one that has already been cached for the particular HttpServletRequest.
   *
   * @param request the incoming HttpServletRequest from which to establish a long-polling transport.
   * @param filters BroadcastFilters that filter any data sent to the client.
   * @param listeners a sequence of `AtmosphereResourceEventListener` to receive events as they happen.  If this
   *    parameter is not supplied then the default `EventsLogger` listener is added which logs events to the logger.
   */
  def apply(request: HttpServletRequest,
            filters: Set[BroadcastFilter] = Set(),
            listeners: Seq[AtmosphereResourceEventListener] = Seq(new EventsLogger)): MyMeteor = {
    val session = request.getSession
    Option(session.getAttribute("meteor")) match {
      case Some(mm: MyMeteor) =>
        // There is an existing MyMeteor object stored in the session, so just return it...
        mm
      case None =>
        // There is no existing MyMeteor object stored in the session, so create one and store it for future use...
        logger.warn("Building a new Meteor!")
        val m = Meteor.build(request, filters.toList, null)
        listeners.foreach(m.addListener(_))
        val mm = MyMeteor(m).suspend()
        session.setAttribute("meteor", mm)
        mm
      case _ =>
        // Another unanticipated event has occurred so throw an exception...
        throw new RuntimeException("This shouldn't happen!")
    }
  }

}
