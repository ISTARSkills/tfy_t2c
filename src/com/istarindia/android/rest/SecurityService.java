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
						Date datefrom = new Date();
						Date dateto = new Date(System.currentTimeMillis()+60*1000);
						Date prevdate = new Date(System.currentTimeMillis()-60*1000);
						
						String timeFrom = dateFormat.format(datefrom).toString();
						String timeTo = dateFormat.format(dateto).toString();
						String timePrev = dateFormat.format(prevdate).toString();
						
						timeFrom = timeFrom.replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "");
						timeTo = timeTo.replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "");
						timePrev = timePrev.replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "");
						value = value.replaceAll("viksit-", "").replaceAll("[^0-9]", "");
						
						System.out.println(timePrev+":"+timeFrom +":"+ value + ":" + timeTo);
						if (timeFrom.equals(timePrev) || timeFrom.equals(value) || timeTo.equals(value)) {
							isTrue = true;
						}
					}
				}
			}
		}

		return isTrue;
	}
}