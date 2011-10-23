package plotter.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipal;

import plotter.util.Configuration;

/**
 * Servlet implementation class Home
 */
public class Bill extends HttpServlet {

	private static final long serialVersionUID = -6257366618447650527L;

	Collection<String> allowedUsers;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Bill() {
		super();

		// Load and parse allowed users
		allowedUsers = Arrays.asList(Configuration.getProperty("plotter.bill.users").split(","));
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();

		// Check user
		if (request.getUserPrincipal() == null
				|| !allowedUsers.contains(principal.getAttributes().get("cn"))) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);

			return;
		}

		request.getRequestDispatcher("/WEB-INF/jsp/bill.jsp").forward(request, response);
	}

}