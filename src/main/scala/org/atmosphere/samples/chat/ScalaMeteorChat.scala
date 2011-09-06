package org.atmosphere.samples.chat

import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpServlet}
import com.weiglewilczek.slf4s.Logging
import org.atmosphere.cpr.{AtmosphereResourceEventListener, BroadcastFilter, Meteor}
import org.atmosphere.commons.util.EventsLogger

/**
 * Simple Servlet that implement the logic to build a Chat application using
 * a {@link Meteor} to suspend and broadcast chat message.
 *
 * @author Jeanfrancois Arcand
 * @author TAKAI Naoto (Orginial author for the Comet based Chat).
 * @author Stuart Roebuck (conversion to Scala).
 */
class ScalaMeteorChat extends HttpServlet with Logging {

  private def newMeteorForRequestResponse(request: HttpServletRequest, response: HttpServletResponse): MyMeteor = {
    val mym = MyMeteor(request)
    request.getSession.setAttribute("mymeteor", mym)
    response.setContentType("text/html;charset=UTF-8")
    mym.suspend()
    mym
  }


  /**
   * Create a {@link Meteor} and use it to suspend the response.
   * @param req An {@link HttpServletRequest}
   * @param res An {@link HttpServletResponse}
   */
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    val mym = newMeteorForRequestResponse(request, response)
    mym.broadcast("%s has suspended a connection from %s".format(request.getServerName, request.getRemoteAddr))
  }

  /**
   * Re-use the {@link Meteor} created onthe first GET for broadcasting message.
   *
   * @param req An {@link HttpServletRequest}
   * @param res An {@link HttpServletResponse}
   */
  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    val mym: MyMeteor = Option(request.getSession.getAttribute("mymeteor")) match {
      case None => newMeteorForRequestResponse(request, response)
      case Some(mym: MyMeteor) => mym
      case _ => logger.error("Unexpected situation!"); null
    }
    response.setCharacterEncoding("UTF-8")
    val action: String = request.getParameterValues("action")(0)
    val name: String = request.getParameterValues("name")(0)

    logger.info("action: %s, name: %s".format(action, name))

    action match {
      case "login" =>
        request.getSession.setAttribute("name", name)
        mym.broadcast("System Message from %s:  %s has joined".format(request.getServerName, name))

      case "post" =>
        val message: String = request.getParameterValues("message")(0)
        mym.broadcast("%s\n  %s".format(name,message))

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
 * @param withEventsLogger if true this enables logging of all events occuring to the Meteor object.
 */
case class MyMeteor(request: HttpServletRequest,
                    filters: Set[BroadcastFilter] = Set(),
                    withEventsLogger: Boolean = true) {

  import scala.collection.JavaConversions._

  lazy val meteor = {
    val m = Meteor.build(request, filters.toList, null)
    if (withEventsLogger) m.addListener(new EventsLogger)
    m
  }

  /**
   * Add a listener that will be sent every event that occurs with the Meteor, whether it is the arrival of data or the
   * sending of data.  You can add more than one listener and they will all receive all events.
   */
  def addListener(eventsListener: AtmosphereResourceEventListener) { meteor.addListener(eventsListener)}

  /**
   * Suspend the long-polling connection.  In other words, hold it ready to send a response to the client whenever
   * required.
   */
  def suspend(timeLimitMillis: Int = -1) {
    meteor.suspend(timeLimitMillis)
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
