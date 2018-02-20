package com.istarindia.android.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viksitpro.core.logger.ViksitLogger;

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
		HttpServletResponse res=(HttpServletResponse)response;
		if (security_token_check.equalsIgnoreCase("true")) {
			//ViksitLogger.logMSG(this.getClass().getName(),"security_token_check true");
			if (checkAuth((HttpServletRequest) request)) {
				
				ViksitLogger.logMSG(this.getClass().getName(), "Access Approved");
				chain.doFilter(request, res);
			} else {
				ViksitLogger.logMSG(this.getClass().getName(),"Access Denied");
				res.setStatus(500);
				res.getWriter().print("\"istarViksitProComplexKeySecurity Access Denied.\"");
			}

		} else {

			chain.doFilter(request, res);
			//ViksitLogger.logMSG(this.getClass().getName(),"security_token_check false");
		}

	}

	

	private boolean checkAuth(HttpServletRequest request) {
		ViksitLogger.logMSG(this.getClass().getName(),"Accessed via filter ");
		return SecurityService.checkAuth(request);

	}

	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
