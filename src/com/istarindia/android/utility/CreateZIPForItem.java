package com.istarindia.android.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import com.viksitpro.core.utilities.TaskCategory;

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

	public Object generateZIP(Task task) throws Exception {

		Object object = null;

		Integer itemId = task.getItemId();
		String itemType = task.getItemType();

		switch (itemType) {
		case TaskCategory.LESSON:
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
		}
		return object;
	}

	public VideoLesson createZIPForVideoLesson(Lesson lesson) throws Exception{
		System.out.println("Lesson Type is VIDEO");
		String lessonXML = lesson.getLessonXml();
		
		VideoLesson videoLesson = null;
		
		Set<String> allUrls = new HashSet<String>();
		
		if (lessonXML != null && !lessonXML.trim().isEmpty()) {
			Serializer serializer = new Persister();
			videoLesson = serializer.read(VideoLesson.class, lessonXML);
			
			if(videoLesson.getVideo_thumb_url()!=null && !videoLesson.getVideo_thumb_url().trim().isEmpty())
				allUrls.add(videoLesson.getVideo_thumb_url().replace("video/", ""));
			
			if(videoLesson.getVideo_url()!=null && !videoLesson.getVideo_url().trim().isEmpty())
				allUrls.add(videoLesson.getVideo_url().replace("video/", ""));
			
		}	
		System.out.println(allUrls.size());

		for (String url : allUrls) {
			System.out.println("url->" + url);
		}

		String mediaPath = getMediaPath();

		File uploadFolder = new File(mediaPath + "lessons/");

		System.out.println(uploadFolder.getAbsolutePath());
		if (!uploadFolder.exists()) {
			System.out.println("Folder does not exists");
			uploadFolder.mkdir();
		}

		String lessonZipFilePath = "/" + lesson.getId() + ".zip";
		createZipFile(uploadFolder.getAbsolutePath() + lessonZipFilePath, allUrls);

		videoLesson.setZipFileURL("video/lessons" + lessonZipFilePath);
		return videoLesson;
	}
	
	public InteractiveContent createZIPForInteractiveLesson(Lesson lesson) throws Exception {

		String lessonXML = lesson.getLessonXml();

		InteractiveContent interactiveContent = null;

		Set<String> allUrls = new HashSet<String>();

		if (lessonXML != null && !lessonXML.trim().isEmpty()) {
			Serializer serializer = new Persister();
			interactiveContent = serializer.read(InteractiveContent.class, lessonXML);

			if (interactiveContent.getBgImage() != null && !interactiveContent.getBgImage().trim().isEmpty()) {
				allUrls.add(interactiveContent.getBgImage().replace("video/", ""));
			}

			if (interactiveContent.getAudioUrl() != null && !interactiveContent.getAudioUrl().trim().isEmpty()) {
				allUrls.add(interactiveContent.getAudioUrl().replace("video/", ""));
			}

			for (Entity entity : interactiveContent.getQuestions()) {
				if (entity.getBackgroundImage() != null && !entity.getBackgroundImage().trim().isEmpty()) {
					allUrls.add(entity.getBackgroundImage().replace("video/", ""));
				}

				if (entity.getTransitionImage() != null && !entity.getTransitionImage().trim().isEmpty()) {
					allUrls.add(entity.getTransitionImage().replace("video/", ""));
				}

				if (entity.getOptions() != null && entity.getOptions().size() > 0) {
					for (Entry<Integer, EntityOption> entry : entity.getOptions().entrySet()) {
						if (entry.getValue().getBackgroundImage() != null
								&& !entry.getValue().getBackgroundImage().trim().isEmpty()) {
							allUrls.add(entry.getValue().getBackgroundImage().replace("video/", ""));
						}
						if (entry.getValue().getMediaUrl() != null
								&& !entry.getValue().getMediaUrl().trim().isEmpty()) {
							allUrls.add(entry.getValue().getMediaUrl().replace("video/", ""));
						}

						if (entry.getValue().getCards() != null && entry.getValue().getCards().size() > 0) {
							for (Entry<Integer, InfoCard> entryInfoCard : entry.getValue().getCards().entrySet()) {

								if (entryInfoCard.getValue().getContent() != null) {
									CardContent content = entryInfoCard.getValue().getContent();

									if (content.getaBackgroundImage() != null
											&& !content.getaBackgroundImage().trim().isEmpty())
										allUrls.add(content.getaBackgroundImage().replace("video/", ""));

									if (content.getaForegroundImage() != null
											&& !content.getaForegroundImage().trim().isEmpty())
										allUrls.add(content.getaForegroundImage().replace("video/", ""));

									if (content.getbBackgroundImage() != null
											&& !content.getbBackgroundImage().trim().isEmpty())
										allUrls.add(content.getbBackgroundImage().replace("video/", ""));

									if (content.getbBottomLeftBGImage() != null
											&& !content.getbBottomLeftBGImage().trim().isEmpty())
										allUrls.add(content.getbBottomLeftBGImage().replace("video/", ""));

									if (content.getbBottomLeftFGImage() != null
											&& !content.getbBottomLeftFGImage().trim().isEmpty())
										allUrls.add(content.getbBottomLeftFGImage().replace("video/", ""));

									if (content.getbBottomRightBGImage() != null
											&& !content.getbBottomRightBGImage().trim().isEmpty())
										allUrls.add(content.getbBottomRightBGImage().replace("video/", ""));

									if (content.getbBottomRightFGImage() != null
											&& !content.getbBottomRightFGImage().trim().isEmpty())
										allUrls.add(content.getbBottomRightFGImage().replace("video/", ""));

									if (content.getbCenterBottomBackgroundImage() != null
											&& !content.getbCenterBottomBackgroundImage().trim().isEmpty())
										allUrls.add(content.getbCenterBottomBackgroundImage().replace("video/", ""));

									if (content.getbCenterBottomForegroundImage() != null
											&& !content.getbCenterBottomForegroundImage().trim().isEmpty())
										allUrls.add(content.getbCenterBottomForegroundImage().replace("video/", ""));

									if (content.getbCenterTopBackgroundImage() != null
											&& !content.getbCenterTopBackgroundImage().trim().isEmpty())
										allUrls.add(content.getbCenterTopBackgroundImage().replace("video/", ""));

									if (content.getbCenterTopForegroundImage() != null
											&& !content.getbCenterTopForegroundImage().trim().isEmpty())
										allUrls.add(content.getbCenterTopForegroundImage().replace("video/", ""));

									if (content.getbTopleftBGImage() != null
											&& !content.getbTopleftBGImage().trim().isEmpty())
										allUrls.add(content.getbTopleftBGImage().replace("video/", ""));

									if (content.getbTopleftFGImage() != null
											&& !content.getbTopleftFGImage().trim().isEmpty())
										allUrls.add(content.getbTopleftFGImage().replace("video/", ""));

									if (content.getbTopRightBGImage() != null
											&& !content.getbTopRightBGImage().trim().isEmpty())
										allUrls.add(content.getbTopRightBGImage().replace("video/", ""));

									if (content.getbTopRightFGImage() != null
											&& !content.getbTopRightFGImage().trim().isEmpty())
										allUrls.add(content.getbTopRightFGImage().replace("video/", ""));

									if (content.getbBottomLeftMediaUrl() != null
											&& !content.getbBottomLeftMediaUrl().trim().isEmpty())
										allUrls.add(content.getbBottomLeftMediaUrl().replace("video/", ""));

									if (content.getbBottomRightMediaUrl() != null
											&& !content.getbBottomRightMediaUrl().trim().isEmpty())
										allUrls.add(content.getbBottomRightMediaUrl().replace("video/", ""));

									if (content.getbCenterBottomMediaUrl() != null
											&& !content.getbCenterBottomMediaUrl().trim().isEmpty())
										allUrls.add(content.getbCenterBottomMediaUrl().replace("video/", ""));

									if (content.getbCenterTopMediaUrl() != null
											&& !content.getbCenterTopMediaUrl().trim().isEmpty())
										allUrls.add(content.getbCenterTopMediaUrl().replace("video/", ""));

									if (content.getbTopLeftMediaUrl() != null
											&& !content.getbTopLeftMediaUrl().trim().isEmpty())
										allUrls.add(content.getbTopLeftMediaUrl().replace("video/", ""));

									if (content.getbTopRightMediaUrl() != null
											&& !content.getbTopRightMediaUrl().trim().isEmpty())
										allUrls.add(content.getbTopRightMediaUrl().replace("video/", ""));

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

		String mediaPath = getMediaPath();

		File uploadFolder = new File(mediaPath + "lessons/");

		System.out.println(uploadFolder.getAbsolutePath());
		if (!uploadFolder.exists()) {
			System.out.println("Folder does not exists");
			uploadFolder.mkdir();
		}

		String lessonZipFilePath = "/" + lesson.getId() + ".zip";
		createZipFile(uploadFolder.getAbsolutePath() + lessonZipFilePath, allUrls);

		interactiveContent.setZipFileURL("video/lessons" + lessonZipFilePath);

		return interactiveContent;
	}

	public void createZipFile(String zipFileName, Set<String> allFilesToBeZipped) throws IOException {

		FileOutputStream fos = new FileOutputStream(zipFileName);
		ZipOutputStream zipOS = new ZipOutputStream(fos);

		String mediaPath = getMediaPath();

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
		File file = new File(mediaPath + pathOffileToInclude);
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(pathOffileToInclude.replaceAll("interactive/", "").replaceAll(".jpg",".aaa").replaceAll(".png",".aaa").replaceAll(".jpeg",".aaa").replaceAll(".mp4",".aaa").replaceAll(".json",".aaa"));
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
