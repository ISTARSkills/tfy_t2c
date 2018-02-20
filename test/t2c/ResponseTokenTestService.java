package t2c;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.junit.Test;

import com.viksitpro.core.logger.ViksitLogger;
public class ResponseTokenTestService {
	private final String USER_AGENT = "Mozilla/5.0";
	String viksit_user_agent = getTockent();
	public static void main(String[] args) throws Exception {

	/*	ResponseTokenTestService http = new ResponseTokenTestService();

		
		
		ViksitLogger.logMSG(this.getClass().getName(),"Testing 1 - Send Http GET request");
		http.sendGet();*/
		
		
		


	}
	@Test  
	// HTTP GET request
	public void sendGet() throws Exception {

		String url = "http://192.168.1.12:8080/t2c/user/456/complex";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");
		con.setRequestProperty ("viksit-user-agent", viksit_user_agent);
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		ViksitLogger.logMSG(this.getClass().getName(),"\nSending 'GET' request to URL : " + url);
		ViksitLogger.logMSG(this.getClass().getName(),"Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		ViksitLogger.logMSG(this.getClass().getName(),">>>>>>> "+response.toString());

	}

	public String getTockent() {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date date = new Date();
		String timeNow = dateFormat.format(date).toString();
		timeNow = timeNow.replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "");
		ViksitLogger.logMSG(this.getClass().getName(),timeNow);

		Random r = new Random();
		int Low = 1;
		int High = 24;
		int Result = r.nextInt(High - Low) + Low;

		for (int j = 0; j < 3; j++) {

			StringBuffer randStr = new StringBuffer();
			char ch;
			Result = r.nextInt(High - Low) + Low;
			for (int i = 0; i < 3; i++) {
				Result = r.nextInt(High - Low) + Low;
				// Result = r.nextInt(High-Low) + Low;
				String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
				ch = CHAR_LIST.charAt(Result);
				randStr.append(ch);
			}

			if (j == 0) {
				timeNow = new StringBuffer(timeNow).insert(timeNow.length() - 10, randStr).toString();
			}
			if (j == 1) {
				timeNow = new StringBuffer(timeNow).insert(timeNow.length() - 3, randStr).toString();
			}
			if (j == 2) {
				timeNow = new StringBuffer(timeNow).insert(timeNow.length() - 7, randStr).toString();
			}

		}
		return "viksit-"+timeNow;
	}

}
