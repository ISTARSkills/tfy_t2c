package com.istarindia.android.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;

import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.tomcat.util.codec.binary.Base64;

public class AppUtility {

	public String imageUpload(String profileImage, String fileFormat, String type, String context) throws IOException{
		
		String imageUploadPath ="";
		String subDirectory = "users"+"/";
		String fileURLPrefix="";
		String fileExtension = fileFormat;

			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					String benchmark = properties.getProperty("pointsBenchmark");
					imageUploadPath = properties.getProperty("mediaPath");
					fileURLPrefix = properties.getProperty("fileURLPrefix");
					System.out.println("imageUploadPath"+imageUploadPath);
					System.out.println("fileURLPrefix"+fileURLPrefix);
					System.out.println("benchmark"+benchmark);
				}

				File rootUploadFolder = new File(imageUploadPath+subDirectory);
				if(!rootUploadFolder.exists()){
					System.out.println("RootUploadFolder does not exists, creating new one");
					rootUploadFolder.mkdir();
				}
				
				if(context!=null){
					subDirectory = subDirectory + context + "/";
				}						
				
				System.out.println("subDirectory"+subDirectory);
		imageUploadPath = imageUploadPath+subDirectory;
		File uploadFolder = new File(imageUploadPath);
		System.out.println(uploadFolder.getAbsolutePath());
		if(!uploadFolder.exists()){
			System.out.println("Folder does not exists");
			uploadFolder.mkdir();
		}
								
		String fileName = UUID.randomUUID().toString()+"."+fileExtension;
		System.out.println("fileName"+fileName);
		String filePath = uploadFolder.getAbsolutePath()+"/"+fileName;
		System.out.println("filePath"+filePath);
		byte[] imgByteArray = Base64.decodeBase64(profileImage);
        FileOutputStream file = new FileOutputStream(filePath);
        String fileURL = fileURLPrefix + subDirectory +fileName;
        System.out.println("fileURL"+fileURL);
        file.write(imgByteArray);
        file.close();
				
		return fileURL;
	}
	
	
	public static String getRandomString(int length) {

		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom random = new SecureRandom();

		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
			sb.append(AB.charAt(random.nextInt(AB.length())));
		return sb.toString();
	}
	
	public static Integer generateOTP(){
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		return otp;
	}
	
	public static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
