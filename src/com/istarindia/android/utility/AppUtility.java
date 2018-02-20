package com.istarindia.android.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.tomcat.util.codec.binary.Base64;

import com.viksitpro.core.utilities.AppProperies;

public class AppUtility {

	public String imageUpload(String profileImage, String fileFormat, String type, String context) throws IOException {
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		// add owners permission
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		// add group permissions
		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_WRITE);
		perms.add(PosixFilePermission.GROUP_EXECUTE);
		// add others permissions
		perms.add(PosixFilePermission.OTHERS_READ);
		perms.add(PosixFilePermission.OTHERS_WRITE);
		perms.add(PosixFilePermission.OTHERS_EXECUTE);

		String imageUploadPath = AppProperies.getProperty("mediaPath");
		String subDirectory = "users" + "/";

		String serverType = AppProperies.getProperty("server_type");

		String fileExtension = fileFormat;

		File rootUploadFolder = new File(imageUploadPath + subDirectory);
		if (!rootUploadFolder.exists()) {
			// ViksitLogger.logMSG(this.getClass().getName(),"RootUploadFolder does not exists, creating new one");
			rootUploadFolder.mkdir();

			if (serverType.equalsIgnoreCase("linux"))
				Files.setPosixFilePermissions(Paths.get(rootUploadFolder.getAbsolutePath()), perms);

		}
		if (context != null) {
			subDirectory = subDirectory + context + "/";
		}
		// ViksitLogger.logMSG(this.getClass().getName(),"subDirectory"+subDirectory);
		imageUploadPath = imageUploadPath + subDirectory;
		File uploadFolder = new File(imageUploadPath);
		// ViksitLogger.logMSG(this.getClass().getName(),uploadFolder.getAbsolutePath());
		if (!uploadFolder.exists()) {
			// ViksitLogger.logMSG(this.getClass().getName(),"Folder does not exists");
			uploadFolder.mkdir();
			if (serverType.equalsIgnoreCase("linux"))
				Files.setPosixFilePermissions(Paths.get(uploadFolder.getAbsolutePath()), perms);

		}
		String fileName = UUID.randomUUID().toString() + "." + fileExtension;
		String filePath = uploadFolder.getAbsolutePath() + "/" + fileName;
		byte[] imgByteArray = Base64.decodeBase64(profileImage);
		FileOutputStream file = new FileOutputStream(filePath);
		String fileURL = subDirectory + fileName;
		file.write(imgByteArray);
		file.close();
		File f = new File(filePath);
		// ViksitLogger.logMSG(this.getClass().getName(),"absouolte file path ->"+f.getAbsolutePath());
		// ViksitLogger.logMSG(this.getClass().getName(),"fileURL ->"+fileURL);
		if (serverType.equalsIgnoreCase("linux"))
			Files.setPosixFilePermissions(Paths.get(f.getAbsolutePath()), perms);

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
