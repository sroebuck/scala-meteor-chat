package org.atmosphere.samples.chat

import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpServlet}
import com.weiglewilczek.slf4s.Logging
import org.atmosphere.commons.util.EventsLogger
import org.atmosphere.cpr._
import scala.collection.JavaConversions._
import net.liftweb.json.JsonParser
import io.Source

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
    MyMeteor(request)
  }

  /**
   * I believe that this method is called either directly as an HTTP POST request or as a result of a message being
   * sent through an Atmosphere based connection.  In either case the code handles the situation the same, however
   * at present there is an issue that the jquery.atmosphere.js plugin doesn't encode the messages in a web form
   * format when they are transmitted by websocket so they are not displayed properly.
   */
  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    logger.info("-> doPost")
    val myMeteor = MyMeteor(request)
    response.setCharacterEncoding("UTF-8")
    
    val body = Source.fromInputStream(request.getInputStream).mkString
    logger.info("body: %s".format(body))
    val parameters: Map[String,String] = JsonParser.parse(body).values match {
      case x: Map[String,String] => x
      case _ => Map()
    }
    logger.info("parameters: %s".format(parameters))
    val action = parameters.get("action").getOrElse("")
    val name = parameters.get("name").getOrElse("")
    logger.info("action: %s, name: %s".format(action, name))

    action match {
      case "login" =>
        request.getSession.setAttribute("name", name)
        myMeteor.broadcast("System Message from %s:  %s has joined".format(request.getServerName, name))

      case "post" =>
        val message = parameters.get("message").getOrElse("")
        myMeteor.broadcast("%s:<br>%s<br>".format(name,message.split("\n").mkString("<br>")))

      case _ =>
        logger.warn("Unmatching action!")
        response.setStatus(422)
    }
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
