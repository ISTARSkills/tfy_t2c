/**
 * 
 */
package com.istarindia.android.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Sumanth-Istar
 *
 */
public class SecurityService {

	@SuppressWarnings("rawtypes")
	public static boolean checkAuth(HttpServletRequest request) {
		boolean isTrue = false;
		Enumeration names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			Enumeration values = request.getHeaders(name); // support multiple values
			// Auth token Request
			if (values != null) {
				while (values.hasMoreElements()) {
					String value = (String) values.nextElement();
					
					if (name.equalsIgnoreCase("viksit-user-agent")) {
						System.out.println(value);
						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
						Date date = new Date();
						String timeNow = dateFormat.format(date).toString();
						timeNow = timeNow.replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "");
						value = value.replaceAll("viksit-", "").replaceAll("[^0-9]", "");
						System.out.println(timeNow + ":" + value);
						if (timeNow.equals(value)) {
							isTrue = true;
						}
					}
				}
			}
		}

		return isTrue;
	}
}