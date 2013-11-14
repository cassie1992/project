package edu.uwm.cs361.fantastic_five.training_tracker;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class UserServlet extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		String username = null;
		Cookie[] cookies = req.getCookies();

		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals("username")) {
					username = c.getValue();
				}
			}
		}

		if (username != null) {
			resp.sendRedirect("/homepage");
		}
		else {
			resp.setContentType("text/html");
			resp.getWriter().println("<h1>Not logged in</h1>");
			resp.getWriter().println("<a href='/login' method='POST'> Back to Log In</a>");
		}
	}
}

