package com.istarindia.android.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.logger.UserJourney;
import com.viksitpro.core.logger.ViksitLogger;
import com.viksitpro.core.utilities.DBUTILS;

/**
 * Servlet Filter implementation class SecurityFilter
 */

@WebFilter(filterName = "LogB", urlPatterns = { "/*" })
public class SecurityFilter implements Filter {

	String security_token_check = "";

	public SecurityFilter() {
		super();
		initSecurityDelegator();
	}

	private void initSecurityDelegator() {
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propertyFileName + "' not found in the classpath");
			}
			security_token_check = properties.getProperty("security_token_check");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse res = (HttpServletResponse) response;
		if (security_token_check.equalsIgnoreCase("true")) {
			// ViksitLogger.logMSG(this.getClass().getName(),"security_token_check true");
			if (checkAuth((HttpServletRequest) request)) {

				ViksitLogger.logMSG(this.getClass().getName(), "Access Approved");
				chain.doFilter(request, res);
			} else {
				ViksitLogger.logMSG(this.getClass().getName(), "Access Denied");
				res.setStatus(500);
				res.getWriter().print("\"istarViksitProComplexKeySecurity Access Denied.\"");
			}

		} else {
			long start = System.currentTimeMillis();
			HttpSession session = ((HttpServletRequest) request).getSession(true);
			chain.doFilter(request, res);
			long end = System.currentTimeMillis();
			// ViksitLogger.logMSG(this.getClass().getName(),"security_token_check false");
			new UserJourney().createUserJourneyEntry(request, end - start, session);
		}

	}

	private boolean checkAuth(HttpServletRequest request) {
		ViksitLogger.logMSG(this.getClass().getName(), "Accessed via filter ");
		return SecurityService.checkAuth(request);

	}

	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
