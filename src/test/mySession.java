package test;

import java.io.*;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import java.sql.Timestamp;
import java.util.*;

@WebServlet("/mySession")
public class mySession extends HttpServlet {

	HashMap globalTable = new HashMap();
	HashMap sessionInfo;

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		HttpSession session = request.getSession();

		synchronized (session) {
			String message;
			Integer sessionTracker = (Integer) session
					.getAttribute("sessionTracker");

			// Check if it is a new session
			if (sessionTracker == null) {

				sessionTracker = 0;

				// Create a new cookie
				String sessionID = session.getId();
				String version = Integer.toString(sessionTracker);
				String metadata = request.getLocalAddr();
				String cookieVal = sessionID + ", " + version + ", " + metadata;

				Cookie myCookie = new Cookie("CS5300PROJ1SESSION", cookieVal);
				myCookie.setPath("/");
				response.addCookie(myCookie);

				// Set session message
				message = "Hello User!";
				session.setAttribute("message", message);

				// Set session time out
				session.setMaxInactiveInterval(30);
				myCookie.setMaxAge(30);

				// Create a time stamp
				long retryDate = System.currentTimeMillis();
				Timestamp original = new Timestamp(retryDate);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(original.getTime());
				cal.add(Calendar.SECOND, session.getMaxInactiveInterval());
				Timestamp timestamp = new Timestamp(cal.getTime().getTime());

				// Set and print out the session data table
				// <sessionID, version, message, expiration-timestamp>
				sessionInfo = new HashMap();
				sessionInfo.put("version", sessionTracker);
				sessionInfo.put("message", session.getAttribute("message"));
				sessionInfo.put("timestamp", timestamp);
				globalTable.put(sessionID, sessionInfo);

				System.out
						.println("\nSession Data Table: \n<sessionID, version, message, expiration-timestamp>");
				for (Object key : globalTable.keySet()) {
					System.out.println("<"
							+ key
							+ ", "
							+ ((HashMap) globalTable.get(key)).get("version")
							+ ", "
							+ ((HashMap) globalTable.get(key)).get("message")
							+ ", "
							+ ((Timestamp) ((HashMap) globalTable.get(key))
									.get("timestamp")) + ">");
				}

			} else {
				message = "Welcome Back";
				session.setAttribute("message", message);
				sessionTracker = sessionTracker + 1;
			}
			session.setAttribute("sessionTracker", sessionTracker);

			PrintWriter out = response.getWriter();
			String title = "My First Servlet";
			String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
					+ "Transitional//EN\">\n";
			out.println(docType
					+ "<HTML>\n"
					+ "<HEAD><TITLE>"
					+ title
					+ "</TITLE></HEAD>\n"
					+ "<CENTER>\n"
					+ "<H1>"
					+ (String) session.getAttribute("message")
					+ "</H1>\n"
					+ "<H2>Information on Your Session:</H2>\n"
					+ "<TABLE BORDER=1>\n"
					+ "  <TH>Info Type<TH>Value\n"
					+ "<TR>\n"
					+ "  <TD>ID\n"
					+ "  <TD>"
					+ session.getId()
					+ "\n"
					+ "<TR>\n"
					+ "  <TD>Session Expiration Time\n"
					+ "  <TD>"
					+ ((HashMap) globalTable.get(session.getId()))
							.get("timestamp")
					+ "\n"
					+ "<TR>\n"
					+ "  <TD>Server Identity\n"
					+ "  <TD>"
					+ request.getLocalAddr()
					+ "\n"
					+ "</TABLE>\n"
					+ "<br>"
					+ "<FORM ACTION=\"mySession\" METHOD=\"POST\">"
					+ "<INPUT TYPE=\"TEXT\" NAME=\"newSession\" maxlength=\"512\" pattern=\"[.a-zA-Z0-9_-]*\">"
					+ "<P><INPUT TYPE=\"SUBMIT\" NAME=\"Action\" VALUE=\"Replace\">"
					+ "<P><INPUT TYPE=\"SUBMIT\" NAME=\"Action\" VALUE=\"Refresh\">"
					+ "<P><INPUT TYPE=\"SUBMIT\" NAME=\"Action\" VALUE=\"Logout\">"
					+ "</FORM>" + "The SESSION TIMEOUT period is "
					+ session.getMaxInactiveInterval() + " seconds.<br><br>"
					+ "</CENTER></BODY></HTML>");
		}
	}

	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		HttpSession session = request.getSession();
		Integer sessionTracker = (Integer) session
				.getAttribute("sessionTracker");

		PrintWriter out = response.getWriter();
		String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
				+ "Transitional//EN\">\n";

		synchronized (session) {

			String title = "My First Servlet";
			String action = request.getParameter("Action");

			if (sessionTracker == null) {
				out.println(docType + "<HTML>\n"
						+ "<HEAD><TITLE>Session Info</TITLE></HEAD>"
						+ "Your session is time out! <br><br>"
						+ "<a href=\"index.jsp\">Back to the index page</a>"
						+ "</CENTER></BODY></HTML>");

				// Remove timed-out sessions
				Timestamp currTime = new Timestamp(System.currentTimeMillis());
				for (Object key : globalTable.keySet()) {
					Timestamp timestamp = (Timestamp) ((HashMap) globalTable
							.get(key)).get("timestamp");
					if (timestamp.before(currTime)) {
						globalTable.remove(key);
					}
				}
				return;
			}

			if ("Replace".equals(action)) {
				String newMessage = request.getParameter("newSession");
				session.setAttribute("message", newMessage);
				sessionTracker += 1;

				// Create a time stamp
				long retryDate = System.currentTimeMillis();
				Timestamp original = new Timestamp(retryDate);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(original.getTime());
				cal.add(Calendar.SECOND, session.getMaxInactiveInterval());
				Timestamp timestamp = new Timestamp(cal.getTime().getTime());

				sessionInfo = new HashMap();
				sessionInfo.put("version", sessionTracker);
				sessionInfo.put("message", session.getAttribute("message"));
				sessionInfo.put("timestamp", timestamp);
				globalTable.put(session.getId(), sessionInfo);

				// Update cookie
				String sessionID = session.getId();
				String version = Integer.toString(sessionTracker);
				String metadata = request.getLocalAddr();
				String cookieVal = sessionID + ", " + version + ", " + metadata;

				Cookie[] cookies = request.getCookies();
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getValue().split(",")[0].equals(sessionID)) {
						cookies[i].setValue(cookieVal);
						cookies[i].setMaxAge(30);
					}
				}

				System.out.println("\nAction: Replace");
				System.out
						.println("Session Data Table: \n<sessionID, version, message, expiration-timestamp>");
				for (Object key : globalTable.keySet()) {
					System.out.println("<"
							+ key
							+ ", "
							+ ((HashMap) globalTable.get(key)).get("version")
							+ ", "
							+ ((HashMap) globalTable.get(key)).get("message")
							+ ", "
							+ ((Timestamp) ((HashMap) globalTable.get(key))
									.get("timestamp")) + ">");
				}

				out.println(docType
						+ "<HTML>\n"
						+ "<HEAD><TITLE>"
						+ title
						+ "</TITLE></HEAD>\n"
						+ "<CENTER>\n"
						+ "<H1>"
						+ (String) session.getAttribute("message")
						+ "</H1>\n"
						+ "<H2>Information on Your Session:</H2>\n"
						+ "<TABLE BORDER=1>\n"
						+ "  <TH>Info Type<TH>Value\n"
						+ "<TR>\n"
						+ "  <TD>ID\n"
						+ "  <TD>"
						+ session.getId()
						+ "\n"
						+ "<TR>\n"
						+ "  <TD>Session Expiration Time\n"
						+ "  <TD>"
						+ ((HashMap) globalTable.get(session.getId()))
								.get("timestamp")
						+ "\n"
						+ "<TR>\n"
						+ "  <TD>Server Identity\n"
						+ "  <TD>"
						+ request.getLocalAddr()
						+ "\n"
						+ "</TABLE>\n"
						+ "<br>"
						+ "<FORM ACTION=\"mySession\" METHOD=\"POST\">"
						+ "<INPUT TYPE=\"TEXT\" NAME=\"newSession\" maxlength=\"512\" pattern=\"[.a-zA-Z0-9_-]*\">"
						+ "<P><INPUT TYPE=\"SUBMIT\" NAME=\"Action\" VALUE=\"Replace\">"
						+ "<P><INPUT TYPE=\"SUBMIT\" NAME=\"Action\" VALUE=\"Refresh\">"
						+ "<P><INPUT TYPE=\"SUBMIT\" NAME=\"Action\" VALUE=\"Logout\">"
						+ "</FORM>" + "The NEW SESSION TIMEOUT period is "
						+ session.getMaxInactiveInterval()
						+ " seconds.<br><br>" + "</CENTER></BODY></HTML>");
				session.setAttribute("sessionTracker", sessionTracker);

			} else if ("Refresh".equals(action)) {
				session.setMaxInactiveInterval(30);
				sessionTracker += 1;

				if (((String) session.getAttribute("message"))
						.equals("Hello, User!")) {
					session.setAttribute("message", "Welcome Back");
				}

				int timeout = session.getMaxInactiveInterval();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis()
						+ timeout * 1000);

				String sessionID = session.getId();
				sessionInfo = new HashMap();
				sessionInfo.put("version", sessionTracker);
				sessionInfo.put("message", session.getAttribute("message"));
				sessionInfo.put("timestamp", timestamp);
				globalTable.put(sessionID, sessionInfo);

				// Update cookie
				String version = Integer.toString(sessionTracker);
				String metadata = request.getLocalAddr();
				String cookieVal = sessionID + ", " + version + ", " + metadata;

				Cookie[] cookies = request.getCookies();
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getValue().split(",")[0].equals(sessionID)) {
						cookies[i].setValue(cookieVal);
						cookies[i].setMaxAge(30);
					}
				}

				System.out.println("\nAction: Refresh");
				System.out
						.println("Session Data Table: \n<sessionID, version, message, expiration-timestamp>");
				for (Object key : globalTable.keySet()) {
					System.out.println("<"
							+ key
							+ ", "
							+ ((HashMap) globalTable.get(key)).get("version")
							+ ", "
							+ ((HashMap) globalTable.get(key)).get("message")
							+ ", "
							+ ((Timestamp) ((HashMap) globalTable.get(key))
									.get("timestamp")) + ">");
				}

				out.println(docType
						+ "<HTML>\n"
						+ "<HEAD><TITLE>"
						+ title
						+ "</TITLE></HEAD>\n"
						+ "<CENTER>\n"
						+ "<H1>"
						+ (String) session.getAttribute("message")
						+ "</H1>\n"
						+ "<H2>Information on Your Session:</H2>\n"
						+ "<TABLE BORDER=1>\n"
						+ "  <TH>Info Type<TH>Value\n"
						+ "<TR>\n"
						+ "  <TD>ID\n"
						+ "  <TD>"
						+ session.getId()
						+ "\n"
						+ "<TR>\n"
						+ "  <TD>Session Expiration Time\n"
						+ "  <TD>"
						+ ((HashMap) globalTable.get(session.getId()))
								.get("timestamp")
						+ "\n"
						+ "<TR>\n"
						+ "  <TD>Server Identity\n"
						+ "  <TD>"
						+ request.getLocalAddr()
						+ "\n"
						+ "</TABLE>\n"
						+ "<br>"
						+ "<FORM ACTION=\"mySession\" METHOD=\"POST\">"
						+ "<INPUT TYPE=\"TEXT\" NAME=\"newSession\" maxlength=\"512\" pattern=\"[.a-zA-Z0-9_-]*\">"
						+ "<P><INPUT TYPE=\"SUBMIT\" NAME=\"Action\" VALUE=\"Replace\">"
						+ "<P><INPUT TYPE=\"SUBMIT\" NAME=\"Action\" VALUE=\"Refresh\">"
						+ "<P><INPUT TYPE=\"SUBMIT\" NAME=\"Action\" VALUE=\"Logout\">"
						+ "</FORM>" + "The NEW SESSION TIMEOUT period is "
						+ session.getMaxInactiveInterval()
						+ " seconds.<br><br>" + "</CENTER></BODY></HTML>");
				session.setAttribute("sessionTracker", sessionTracker);

			} else if ("Logout".equals(action)) {
				String sessionID = session.getId();
				globalTable.remove(sessionID);
				session.invalidate();

				// Update cookie
				Cookie[] cookies = request.getCookies();
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getValue().split(",")[0].equals(sessionID)) {
						cookies[i].setMaxAge(0);
					}
				}

				System.out.println("\nAction: Logout");
				out.println(docType
						+ "<HTML>\n"
						+ "<HEAD><TITLE>"
						+ title
						+ "</TITLE></HEAD>\n"
						+ "<CENTER>\n You have been successfully loged out!<br>"
						+ "<a href=\"index.jsp\">Back to the index page</a>"
						+ "</CENTER></BODY></HTML>");
			}
			out.println("</BODY></HTML>");
		}
	}

}
