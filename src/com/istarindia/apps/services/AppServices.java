package com.istarindia.apps.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import com.istarindia.android.utility.AppUtility;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

public class AppServices {
	
	public IstarUser assignToken(IstarUser istarUser) {
		System.out.println("Assigning Token");
		String authenticationToken = AppUtility.getRandomString(20);

		IstarUserServices istarUserServices = new IstarUserServices();
		istarUser = istarUserServices.updateAuthenticationTokenForIstarUser(istarUser, authenticationToken);

		return istarUser;
	}

	public Integer sendOTP(String mobile) throws IOException{
		
		String mobTextingURLBase = "https://mobtexting.com/app/index.php/api?method=sms.normal"
				+ "&api_key=0c9ee1130f2a27302bbef3f39360a9eba5f7e48a&sender=TLNTFY";
		
		int OTP = AppUtility.generateOTP();
		
		String message = "One Time Password to login to Talentify is " + OTP;

		System.out.println(message);
		
		String smsURL = mobTextingURLBase + "&to="+URLEncoder.encode(mobile, "UTF-8")+"&message="+URLEncoder.encode(message, "UTF-8");

		System.out.println(smsURL);
		URL urlObject = new URL(smsURL);
		InputStream inputStream = urlObject.openConnection().getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			System.out.println(line);
		}
		bufferedReader.close();

		return OTP;
	}

}
