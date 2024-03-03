package org.remchurch.mealservice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.remchurch.mealservice.util.TomcatAutoLoginEngine;


@WebFilter(
		urlPatterns = { "/*" },
		initParams  = { 
				@WebInitParam(name = "Cache-Control", value = "public, max-age=32400"),   // HTTP/1.1
				@WebInitParam(name = "Expires", value = "Wed, 31 Dec 1969 19:00:00 EST"), // HTTP/1.0
				@WebInitParam(name="dsName", value="jdbc/MCOS_DS")
		}
		)
public class AuthenticationFilter implements Filter {

	private static final Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());
	private static final String[] PUBLIC_PATH_SUFFIXES = {"/js/","/css/","/image/","/m/","/public/"};
	private MainService ms = null;

	/**
	 * Default constructor. 
	 */
	public AuthenticationFilter() {
		logger.fine("AuthenticationFilter:constructor");
	}

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		logger.fine("AuthenticationFilter:destroyed");
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		logger.fine("AuthenticationFilter:init");
		try {
			ms  = MainService.getInstance(config.getInitParameter("dsName"));
		} catch (SQLException e) {
			throw new ServletException("Error creating service.",e);
		}
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		logger.fine("AuthenticationFilter:doFilter");

		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;

		try
		{
			if(httpRequest.getCharacterEncoding() == null)
				httpRequest.setCharacterEncoding("UTF-8");		
			String context = httpRequest.getContextPath();
			String reqPath = httpRequest.getRequestURI();
			String action = httpRequest.getParameter("action");

			//logger.info(DebugFilter.getRequestDebugInfo(httpRequest));

			//Handling the custom login page
			if("login".equals(action)){
				logger.fine("AuthenticationFilter: Form based login");
				if(performFormBasedLogin(httpRequest, httpResponse))
					return; //login success cause redirect
				else {
					httpRequest.setAttribute("target", httpRequest.getParameter("target"));
					httpRequest.setAttribute("errorMsg", "Invalid username/password");
					httpRequest.getRequestDispatcher("/WEB-INF/login.jsp").forward(httpRequest, httpResponse);
					return;
				}
			}

			if(httpRequest.getUserPrincipal()!=null || isPublicPath(reqPath,context)){ //calling business methods
				chain.doFilter(httpRequest, httpResponse);
			}else {
				logger.fine("AuthenticationFilter: Redirect to login");
				httpRequest.setAttribute("target", httpRequest.getRequestURL());
				httpRequest.getRequestDispatcher("/WEB-INF/login.jsp").forward(httpRequest, httpResponse);
			}
		} catch(Exception e) {
			logger.log(Level.WARNING,"Error in authorization: ",e);
			httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filter error!");
		}
	}

	private static boolean isPublicPath(String path, String context) {
		for(String s:PUBLIC_PATH_SUFFIXES) {
			if(path.startsWith(context+s)) 
				return true;
		}
		return false;
	}

	private boolean performFormBasedLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception{
		String username = httpRequest.getParameter("j_username");
		String password = httpRequest.getParameter("j_password");
		String targetURI = httpRequest.getParameter("target");
		try{
			logger.fine("AuthenticationFilter: performFormBasedLogin invoked.");
			List<String> userInfo = new ArrayList<>();
			List<String> roles = ms.getRoles(username,password,userInfo);
			if(roles == null) {
				logger.log(Level.FINE,"[AuthenticationFilter]: invalid password "+username+","+password);
				return false;
			}
			new TomcatAutoLoginEngine().authenticate(httpRequest, httpResponse, userInfo.get(0), "[hidden]", roles);
			HttpSession session = httpRequest.getSession();
			session.setAttribute("username", username);
			session.setAttribute("name", userInfo.get(0));
			session.setAttribute("memberId", userInfo.get(1));

			httpResponse.sendRedirect(targetURI);
			return true;
		}catch(Exception e){
			logger.log(Level.WARNING,"[AuthenticationFilter]: error occurred in Login for "+username, e);
			throw e;
		}
	}

	private static String getRequestURI(HttpServletRequest request) {
		String targetURL = null;

		// this URL is stored in the header by container, this is specific for Tomcat 6.0 only
		String queryURL  = (String) request.getAttribute("javax.servlet.forward.request_uri");

		// Tomcat seems to separate the URI into base and parameters - append the parameters...
		String queryData = (String) request.getAttribute("javax.servlet.forward.query_string");

		if(null != queryURL )
			targetURL  =       queryURL ;
		if(null != queryData)
			targetURL += "?" + queryData;

		if(null == targetURL)
			targetURL = request.getContextPath();
		if(null == targetURL)
			targetURL = "/"; // web server ROOT

		return targetURL;
	}
}
