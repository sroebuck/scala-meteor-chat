package org.atmosphere.samples.chat

import org.atmosphere.util.XSSHtmlFilter
import org.atmosphere.commons.jersey.JsonpFilter
import org.atmosphere.commons.util.EventsLogger
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpServlet}
import org.atmosphere.cpr.{BroadcastFilter, Meteor}
import java.util.LinkedList

/**
 * Simple Servlet that implement the logic to build a Chat application using
 * a {@link Meteor} to suspend and broadcast chat message.
 *
 * @author Jeanfrancois Arcand
 * @author TAKAI Naoto (Orginial author for the Comet based Chat).
 * @author Stuart Roebuck (conversion to Scala).
 */
class ScalaMeteorChat extends HttpServlet {

  /**
   * List of {@link BroadcastFilter}
   */
  private final val list: LinkedList[BroadcastFilter] = new LinkedList[BroadcastFilter]

  list.add(new XSSHtmlFilter)
  list.add(new JsonpFilter)

  /**
   * Create a {@link Meteor} and use it to suspend the response.
   * @param req An {@link HttpServletRequest}
   * @param res An {@link HttpServletResponse}
   */
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
    val m: Meteor = Meteor.build(req, list, null)
    m.addListener(new EventsLogger)
    req.getSession.setAttribute("meteor", m)
    res.setContentType("text/html;charset=ISO-8859-1")
    m.suspend(-1)
    m.broadcast(req.getServerName + "__has suspended a connection from " + req.getRemoteAddr)
  }

  /**
   * Re-use the {@link Meteor} created onthe first GET for broadcasting message.
   *
   * @param req An {@link HttpServletRequest}
   * @param res An {@link HttpServletResponse}
   */
  override def doPost(req: HttpServletRequest, res: HttpServletResponse) {
    val m: Meteor = req.getSession.getAttribute("meteor").asInstanceOf[Meteor]
    res.setCharacterEncoding("UTF-8")
    val action: String = req.getParameterValues("action")(0)
    val name: String = req.getParameterValues("name")(0)

    action match {
      case "login" =>
        req.getSession.setAttribute("name", name)
        m.broadcast("System Message from " + req.getServerName + "__" + name + " has joined.")
        res.getWriter.write("success")
        res.getWriter.flush()

      case "post" =>
        val message: String = req.getParameterValues("message")(0)
        m.broadcast(name + "__" + message)
        res.getWriter.write("success")
        res.getWriter.flush()

      case _ =>
        res.setStatus(422)
        res.getWriter.write("success")
        res.getWriter.flush()
    }
  }

}

object ScalaMeteorChat {
  private final val serialVersionUID: Long = -2919167206889576860L
}

