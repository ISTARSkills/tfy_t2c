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
import java.util.HashSet;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.istarindia.apps.services.AppCourseServices;
import com.viksitpro.core.cms.interactive.CardContent;
import com.viksitpro.core.cms.interactive.Entity;
import com.viksitpro.core.cms.interactive.EntityOption;
import com.viksitpro.core.cms.interactive.InfoCard;
import com.viksitpro.core.cms.interactive.InteractiveContent;
import com.viksitpro.core.cms.lesson.VideoLesson;
import com.viksitpro.core.dao.entities.Lesson;
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

		Integer itemId = task.getItemId();
		String itemType = task.getItemType();

		switch (itemType) {
		case TaskItemCategory.LESSON:
			System.out.println("Lesson ID" + task.getItemId());
			object = generateXMLForLesson(task.getItemId());
			break;
		}
		return object;
	}

	public Object generateXMLForLesson(Integer lessonId) throws Exception {

		Object object = null;

		AppCourseServices appCourseServices = new AppCourseServices();
		Lesson lesson = appCourseServices.getLesson(lessonId);
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

	private String createZIPForPresentationLesson(Lesson lesson) {
		
		String xml_object = getMediaURLPath()+"/lessons/"+lesson.getId()+".zip";	
		System.out.println("returning "+xml_object);
		return xml_object;
	}

	public VideoLesson createZIPForVideoLesson(Lesson lesson) throws Exception{
		System.out.println("Lesson Type is VIDEO");
		
		String mediaPath = getMediaPath();
		String mediaURLPath = getMediaURLPath();
		
		String lessonXML = lesson.getLessonXml();
		
		VideoLesson videoLesson = null;
		
		Set<String> allUrls = new HashSet<String>();
		
		if (lessonXML != null && !lessonXML.trim().isEmpty()) {
			Serializer serializer = new Persister();
			videoLesson = serializer.read(VideoLesson.class, lessonXML);
			
			if(videoLesson.getVideo_thumb_url()!=null && !videoLesson.getVideo_thumb_url().trim().isEmpty())
				allUrls.add(videoLesson.getVideo_thumb_url().replace(mediaURLPath, mediaPath));
			
			if(videoLesson.getVideo_url()!=null && !videoLesson.getVideo_url().trim().isEmpty())
				allUrls.add(videoLesson.getVideo_url().replace(mediaURLPath, mediaPath));
			
		}	
		System.out.println(allUrls.size());

		for (String url : allUrls) {
			System.out.println("url->" + url);
		}

		File uploadFolder = new File(mediaPath + "lessons/");

		System.out.println(uploadFolder.getAbsolutePath());
		if (!uploadFolder.exists()) {
			System.out.println("Folder does not exists");
			uploadFolder.mkdir();
		}

		String lessonZipFilePath = "/" + lesson.getId() + ".zip";
		createZipFile(uploadFolder.getAbsolutePath() + lessonZipFilePath, allUrls);

		videoLesson.setZipFileURL(mediaURLPath+"lessons" + lessonZipFilePath);
		return videoLesson;
	}
	
	public InteractiveContent createZIPForInteractiveLesson(Lesson lesson) throws Exception {

		String mediaPath = getMediaPath();
		String mediaURLPath = getMediaURLPath();
		
		String lessonXML = lesson.getLessonXml();

		InteractiveContent interactiveContent = null;

		Set<String> allUrls = new HashSet<String>();

		if (lessonXML != null && !lessonXML.trim().isEmpty()) {
			Serializer serializer = new Persister();
			interactiveContent = serializer.read(InteractiveContent.class, lessonXML);

			if (interactiveContent.getBgImage() != null && !interactiveContent.getBgImage().trim().isEmpty()) {
				allUrls.add(interactiveContent.getBgImage().replace(mediaURLPath, mediaPath));
			}

			if (interactiveContent.getAudioUrl() != null && !interactiveContent.getAudioUrl().trim().isEmpty()) {
				allUrls.add(interactiveContent.getAudioUrl().replace(mediaURLPath, mediaPath));
			}

			for (Entity entity : interactiveContent.getQuestions()) {
				if (entity.getBackgroundImage() != null && !entity.getBackgroundImage().trim().isEmpty()) {
					allUrls.add(entity.getBackgroundImage().replace(mediaURLPath, mediaPath));
				}

				if (entity.getTransitionImage() != null && !entity.getTransitionImage().trim().isEmpty()) {
					allUrls.add(entity.getTransitionImage().replace(mediaURLPath, mediaPath));
				}

				if (entity.getOptions() != null && entity.getOptions().size() > 0) {
					for (Entry<Integer, EntityOption> entry : entity.getOptions().entrySet()) {
						if (entry.getValue().getBackgroundImage() != null
								&& !entry.getValue().getBackgroundImage().trim().isEmpty()) {
							allUrls.add(entry.getValue().getBackgroundImage().replace(mediaURLPath, mediaPath));
						}
						if (entry.getValue().getMediaUrl() != null
								&& !entry.getValue().getMediaUrl().trim().isEmpty()) {
							allUrls.add(entry.getValue().getMediaUrl().replace(mediaURLPath, mediaPath));
						}

						if (entry.getValue().getCards() != null && entry.getValue().getCards().size() > 0) {
							for (Entry<Integer, InfoCard> entryInfoCard : entry.getValue().getCards().entrySet()) {

								if (entryInfoCard.getValue().getContent() != null) {
									CardContent content = entryInfoCard.getValue().getContent();

									if (content.getaBackgroundImage() != null
											&& !content.getaBackgroundImage().trim().isEmpty())
										allUrls.add(content.getaBackgroundImage().replace(mediaURLPath, mediaPath));

									if (content.getaForegroundImage() != null
											&& !content.getaForegroundImage().trim().isEmpty())
										allUrls.add(content.getaForegroundImage().replace(mediaURLPath, mediaPath));

									if (content.getbBackgroundImage() != null
											&& !content.getbBackgroundImage().trim().isEmpty())
										allUrls.add(content.getbBackgroundImage().replace(mediaURLPath, mediaPath));

									if (content.getbBottomLeftBGImage() != null
											&& !content.getbBottomLeftBGImage().trim().isEmpty())
										allUrls.add(content.getbBottomLeftBGImage().replace(mediaURLPath, mediaPath));

									if (content.getbBottomLeftFGImage() != null
											&& !content.getbBottomLeftFGImage().trim().isEmpty())
										allUrls.add(content.getbBottomLeftFGImage().replace(mediaURLPath, mediaPath));

									if (content.getbBottomRightBGImage() != null
											&& !content.getbBottomRightBGImage().trim().isEmpty())
										allUrls.add(content.getbBottomRightBGImage().replace(mediaURLPath, mediaPath));

									if (content.getbBottomRightFGImage() != null
											&& !content.getbBottomRightFGImage().trim().isEmpty())
										allUrls.add(content.getbBottomRightFGImage().replace(mediaURLPath, mediaPath));

									if (content.getbCenterBottomBackgroundImage() != null
											&& !content.getbCenterBottomBackgroundImage().trim().isEmpty())
										allUrls.add(content.getbCenterBottomBackgroundImage().replace(mediaURLPath, mediaPath));

									if (content.getbCenterBottomForegroundImage() != null
											&& !content.getbCenterBottomForegroundImage().trim().isEmpty())
										allUrls.add(content.getbCenterBottomForegroundImage().replace(mediaURLPath, mediaPath));

									if (content.getbCenterTopBackgroundImage() != null
											&& !content.getbCenterTopBackgroundImage().trim().isEmpty())
										allUrls.add(content.getbCenterTopBackgroundImage().replace(mediaURLPath, mediaPath));

									if (content.getbCenterTopForegroundImage() != null
											&& !content.getbCenterTopForegroundImage().trim().isEmpty())
										allUrls.add(content.getbCenterTopForegroundImage().replace(mediaURLPath, mediaPath));

									if (content.getbTopleftBGImage() != null
											&& !content.getbTopleftBGImage().trim().isEmpty())
										allUrls.add(content.getbTopleftBGImage().replace(mediaURLPath, mediaPath));

									if (content.getbTopleftFGImage() != null
											&& !content.getbTopleftFGImage().trim().isEmpty())
										allUrls.add(content.getbTopleftFGImage().replace(mediaURLPath, mediaPath));

									if (content.getbTopRightBGImage() != null
											&& !content.getbTopRightBGImage().trim().isEmpty())
										allUrls.add(content.getbTopRightBGImage().replace(mediaURLPath, mediaPath));

									if (content.getbTopRightFGImage() != null
											&& !content.getbTopRightFGImage().trim().isEmpty())
										allUrls.add(content.getbTopRightFGImage().replace(mediaURLPath, mediaPath));

									if (content.getbBottomLeftMediaUrl() != null
											&& !content.getbBottomLeftMediaUrl().trim().isEmpty())
										allUrls.add(content.getbBottomLeftMediaUrl().replace(mediaURLPath, mediaPath));

									if (content.getbBottomRightMediaUrl() != null
											&& !content.getbBottomRightMediaUrl().trim().isEmpty())
										allUrls.add(content.getbBottomRightMediaUrl().replace(mediaURLPath, mediaPath));

									if (content.getbCenterBottomMediaUrl() != null
											&& !content.getbCenterBottomMediaUrl().trim().isEmpty())
										allUrls.add(content.getbCenterBottomMediaUrl().replace(mediaURLPath, mediaPath));

									if (content.getbCenterTopMediaUrl() != null
											&& !content.getbCenterTopMediaUrl().trim().isEmpty())
										allUrls.add(content.getbCenterTopMediaUrl().replace(mediaURLPath, mediaPath));

									if (content.getbTopLeftMediaUrl() != null
											&& !content.getbTopLeftMediaUrl().trim().isEmpty())
										allUrls.add(content.getbTopLeftMediaUrl().replace(mediaURLPath, mediaPath));

									if (content.getbTopRightMediaUrl() != null
											&& !content.getbTopRightMediaUrl().trim().isEmpty())
										allUrls.add(content.getbTopRightMediaUrl().replace(mediaURLPath, mediaPath));

								}

							}
						}

					}
				}
			}
		}

		System.out.println(allUrls.size());

		for (String url : allUrls) {
			System.out.println("url->" + url);
		}

		File uploadFolder = new File(mediaPath + "lessons/");

		System.out.println(uploadFolder.getAbsolutePath());
		if (!uploadFolder.exists()) {
			System.out.println("Folder does not exists");
			uploadFolder.mkdir();
		}

		String lessonZipFilePath = "/" + lesson.getId() + ".zip";
		createZipFile(uploadFolder.getAbsolutePath() + lessonZipFilePath, allUrls);

		interactiveContent.setZipFileURL(mediaURLPath+"lessons" + lessonZipFilePath);

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
		System.out.println("Writing file : '" + pathOffileToInclude + "' to zip file");

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

}