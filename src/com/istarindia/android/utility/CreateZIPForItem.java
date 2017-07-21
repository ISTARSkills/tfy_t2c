package com.istarindia.android.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.istarindia.apps.services.AppCourseServices;
import com.viksitpro.core.cms.interactive.CardContent;
import com.viksitpro.core.cms.interactive.Entity;
import com.viksitpro.core.cms.interactive.EntityOption;
import com.viksitpro.core.cms.interactive.InfoCard;
import com.viksitpro.core.cms.interactive.InteractiveContent;
import com.viksitpro.core.cms.lesson.VideoLesson;
import com.viksitpro.core.cms.oldcontent.services.ZipFiles;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.dao.entities.Task;

import com.viksitpro.core.utilities.TaskItemCategory;

public class CreateZIPForItem {

	public String getMediaPath() {
		String mediaPath = null;
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaPath = properties.getProperty("mediaPath");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mediaPath;
	}

	public String getMediaURLPath() {
		String mediaPath = null;
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mediaPath;
	}
	
	public Object generateZIP(Task task) throws Exception {

		Object object = null;

		
		String itemType = task.getItemType();

		switch (itemType) {
		case TaskItemCategory.LESSON:
			//System.out.println("Lesson ID" + task.getItemId());
			Lesson l = new  LessonDAO().findById(task.getItemId());
			object = generateXMLForLesson(l);
			break;
		}
		return object;
	}

	public Object generateXMLForLesson(Lesson lesson) throws Exception {

		Object object = null;
		String lessonType = lesson.getType();

		switch (lessonType) {
		case "INTERACTIVE":
			object = createZIPForInteractiveLesson(lesson);
			break;
		case "VIDEO":
			object = createZIPForVideoLesson(lesson);
			break;
		case "PRESENTATION":
			object = createZIPForPresentationLesson(lesson);			
			break;
		}
		return object;
	}

	private String getServerType() {
		String mediaPath = null;
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaPath = properties.getProperty("server_type");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mediaPath;
	}
	
	private String createZIPForPresentationLesson(Lesson lesson) {

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
		
		String SOURCE_FOLDER = getMediaPath() + "/lessonXMLs/" + lesson.getId()+"/";
		File sourceFile = new File(SOURCE_FOLDER);

		String zipName = getMediaPath() + "/lessonXMLs/" + lesson.getId() + ".zip";
		
		File oldZip = new File(zipName);
		if(oldZip.exists())
		{
				oldZip.delete();
		}
		
		ZipFiles zipFiles = new ZipFiles();
		zipFiles.zipDirectory(sourceFile, zipName);
		if(getServerType().equalsIgnoreCase("linux"))
		{
		try {
			Files.setPosixFilePermissions(Paths.get(oldZip.getAbsolutePath()), perms);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		String xml_object = getMediaURLPath()+"/lessonXMLs/"+lesson.getId()+".zip";	
		//System.out.println("returning "+xml_object);
		return xml_object;
	}

	public VideoLesson createZIPForVideoLesson(Lesson lesson) throws Exception{
		//System.out.println("Lesson Type is VIDEO");
		VideoLesson videoLesson = null;
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
		
		String SOURCE_FOLDER = getMediaPath() + "/lessonXMLs/" + lesson.getId()+"/";
		File sourceFile = new File(SOURCE_FOLDER);

		String zipName = getMediaPath() + "/lessonXMLs/" + lesson.getId() + ".zip";
		
		File oldZip = new File(zipName);
		if(oldZip.exists())
		{
				oldZip.delete();
		}
		
		ZipFiles zipFiles = new ZipFiles();
		zipFiles.zipDirectory(sourceFile, zipName);
		if(getServerType().equalsIgnoreCase("linux"))
		{
		try {
			Files.setPosixFilePermissions(Paths.get(oldZip.getAbsolutePath()), perms);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		String xmlPath = SOURCE_FOLDER+lesson.getId()+"/"+lesson.getId()+".xml";	
		String lessonXML = FileUtils.readFileToString(new File(xmlPath));
		Serializer serializer = new Persister();
		videoLesson = serializer.read(VideoLesson.class, lessonXML);
		videoLesson.setZipFileURL(getMediaURLPath()+"/lessonXMLs/"+lesson.getId()+".zip");			
		return videoLesson;		
	}
	
	public InteractiveContent createZIPForInteractiveLesson(Lesson lesson) throws Exception {
		InteractiveContent interactiveContent = null;
		
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
		
		String SOURCE_FOLDER = getMediaPath() + "/lessonXMLs/" + lesson.getId()+"/";
		File sourceFile = new File(SOURCE_FOLDER);

		String zipName = getMediaPath() + "/lessonXMLs/" + lesson.getId() + ".zip";
		
		File oldZip = new File(zipName);
		if(oldZip.exists())
		{
				oldZip.delete();
		}
		
		ZipFiles zipFiles = new ZipFiles();
		zipFiles.zipDirectory(sourceFile, zipName);
		if(getServerType().equalsIgnoreCase("linux"))
		{
		try {
			Files.setPosixFilePermissions(Paths.get(oldZip.getAbsolutePath()), perms);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		String xmlPath = SOURCE_FOLDER+lesson.getId()+"/"+lesson.getId()+".xml";	
		String lessonXML = FileUtils.readFileToString(new File(xmlPath));
		Serializer serializer = new Persister();
		interactiveContent = serializer.read(InteractiveContent.class, lessonXML);
		interactiveContent.setZipFileURL(getMediaURLPath()+"/lessonXMLs/"+lesson.getId()+".zip");	
		
		return interactiveContent;
	}

	public void createZipFile(String zipFileName, Set<String> allFilesToBeZipped) throws IOException {

		String mediaPath = getMediaPath();

		FileOutputStream fos = new FileOutputStream(zipFileName);
		ZipOutputStream zipOS = new ZipOutputStream(fos);


		if (mediaPath != null) {
			for (String filePathToBeZipped : allFilesToBeZipped) {
				writeToZipFile(mediaPath, filePathToBeZipped, zipOS);
			}

			zipOS.close();
			fos.close();
		}
	}

	public static void writeToZipFile(String mediaPath, String pathOffileToInclude, ZipOutputStream zipOutputStream)
			throws FileNotFoundException, IOException {
		//System.out.println("Writing file : '" + pathOffileToInclude + "' to zip file");

		File file = new File(pathOffileToInclude);
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(pathOffileToInclude.replaceAll(mediaPath, "").replaceAll("video/interactive_audios", "").replaceAll("video/interactive_videos", "").replaceAll("video/interactive_images", "").replaceAll(".jpg",".aaa").replaceAll(".png",".aaa").replaceAll(".jpeg",".aaa").replaceAll(".mp4",".aaa").replaceAll(".json",".aaa"));
		zipOutputStream.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOutputStream.write(bytes, 0, length);
		}
		zipOutputStream.closeEntry();
		fis.close();
	}
	
	
	public static void main(String[] args)
	{
		CreateZIPForItem kk = new CreateZIPForItem();
		for(int i=1 ; i<6913; i++)
		{
			try{
				kk.createZIPForPresentationLesson(new LessonDAO().findById(i));
			}
			catch(Exception e)
			{
				
			}
		}
	}

}