package plotter.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipal;

/**
 * Servlet implementation class Home
 */
public class Bill extends HttpServlet {

	private static final long serialVersionUID = -6257366618447650527L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Bill() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();

		Properties properties = new Properties();
		properties.load(getServletContext().getResourceAsStream("/WEB-INF/configuration.properties"));

		// Parse allowed users
		Collection<String> allowedUsers = Arrays.asList(properties.getProperty("plotter.bill.users").split(","));

		// Check user
		if (request.getUserPrincipal() == null
				|| !allowedUsers.contains(principal.getAttributes().get("cn"))) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);

			return;
		}

		request.getRequestDispatcher("/WEB-INF/jsp/bill.jsp").forward(request, response);
	}

}