package com.istarindia.android.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.viksitpro.core.cms.interactive.InteractiveContent;
import com.viksitpro.core.cms.lesson.VideoLesson;
import com.viksitpro.core.cms.oldcontent.CMSLesson;
import com.viksitpro.core.cms.oldcontent.CMSSlide;
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
			// System.out.println("Lesson ID" + task.getItemId());
			Lesson l = new LessonDAO().findById(task.getItemId());
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

		String SOURCE_FOLDER = getMediaPath() + "/lessonXMLs/" + lesson.getId() + "/";
		File sourceFile = new File(SOURCE_FOLDER);

		String zipName = getMediaPath() + "/lessonXMLs/" + lesson.getId() + ".zip";

		File oldZip = new File(zipName);
		if (oldZip.exists()) {
			oldZip.delete();
		}

		ZipFiles zipFiles = new ZipFiles();

		List<String> filesListInDir = new ArrayList<String>();

		String xmlPath = SOURCE_FOLDER + lesson.getId() + "/" + lesson.getId() + ".xml";
		filesListInDir.add(xmlPath);

		try {
			CMSLesson cmsLesson = null;
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(CMSLesson.class);

				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				cmsLesson = (CMSLesson) jaxbUnmarshaller.unmarshal(new File(xmlPath));
			} catch (Exception e) {
			}

			boolean isLessonLevelAudioAvilabel = false;
			if (cmsLesson != null && cmsLesson.getAudio_url() != null && !cmsLesson.getAudio_url().equalsIgnoreCase("")
					&& !cmsLesson.getAudio_url().equalsIgnoreCase("none")
					&& !cmsLesson.getAudio_url().equalsIgnoreCase("null")) {
				File audioFile = new File(SOURCE_FOLDER + lesson.getId() + "/"
						+ cmsLesson.getAudio_url().substring(cmsLesson.getAudio_url().lastIndexOf('/') + 1));
				if (audioFile.exists()) {
					filesListInDir.add(SOURCE_FOLDER + lesson.getId() + "/"
							+ cmsLesson.getAudio_url().substring(cmsLesson.getAudio_url().lastIndexOf('/') + 1));
					isLessonLevelAudioAvilabel = true;
				}
			}

			for (CMSSlide cmsSlide : cmsLesson.getSlides()) {
				// slide bg_image
				if (cmsSlide != null && cmsSlide.getImage_BG() != null
						&& !cmsSlide.getImage_BG().equalsIgnoreCase("none")
						&& !cmsSlide.getImage_BG().equalsIgnoreCase("null")) {
					filesListInDir.add(SOURCE_FOLDER + lesson.getId() + "/"
							+ cmsSlide.getImage_BG().substring(cmsSlide.getImage_BG().lastIndexOf('/') + 1));
				}
				// slide image
				if (cmsSlide != null && cmsSlide.getImage() != null && cmsSlide.getImage().getUrl() != null
						&& !cmsSlide.getImage().getUrl().equalsIgnoreCase("none")
						&& !cmsSlide.getImage().getUrl().equalsIgnoreCase("null")) {
					filesListInDir.add(SOURCE_FOLDER + lesson.getId() + "/" + cmsSlide.getImage().getUrl()
							.substring(cmsSlide.getImage().getUrl().lastIndexOf('/') + 1));
				}
				// slide audio
				if (!isLessonLevelAudioAvilabel && cmsSlide != null && cmsSlide.getAudioUrl() != null
						&& !cmsSlide.getAudioUrl().equalsIgnoreCase("none")
						&& !cmsSlide.getAudioUrl().equalsIgnoreCase("null")) {
					filesListInDir.add(SOURCE_FOLDER + lesson.getId() + "/"
							+ cmsSlide.getAudioUrl().substring(cmsSlide.getAudioUrl().lastIndexOf('/') + 1));
				}
				// slide video url
				if (cmsSlide != null && cmsSlide.getTemplateName().equalsIgnoreCase("ONLY_VIDEO")
						&& cmsSlide.getVideo() != null && cmsSlide.getVideo().getUrl() != null
						&& !cmsSlide.getVideo().getUrl().equalsIgnoreCase("")) {
					filesListInDir.add(SOURCE_FOLDER + lesson.getId() + "/" + cmsSlide.getVideo().getUrl()
							.substring(cmsSlide.getVideo().getUrl().lastIndexOf('/') + 1));
				}

			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Set<String> hs = new HashSet<>();
		hs.addAll(filesListInDir);
		filesListInDir.clear();
		filesListInDir.addAll(hs);

		//System.out.println("Zip path for Lesson:" + lesson.getId());
		/*for (String key : filesListInDir) {
			System.out.println(key);
		}*/

		zipFiles.zipLessonDirectory(filesListInDir, sourceFile, zipName);

		if (getServerType().equalsIgnoreCase("linux")) {
			try {
				Files.setPosixFilePermissions(Paths.get(oldZip.getAbsolutePath()), perms);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String xml_object = getMediaURLPath() + "/lessonXMLs/" + lesson.getId() + ".zip";

		return xml_object;
	}

	public VideoLesson createZIPForVideoLesson(Lesson lesson) throws Exception {
		// System.out.println("Lesson Type is VIDEO");
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

		String SOURCE_FOLDER = getMediaPath() + "/lessonXMLs/" + lesson.getId() + "/";
		File sourceFile = new File(SOURCE_FOLDER);

		String zipName = getMediaPath() + "/lessonXMLs/" + lesson.getId() + ".zip";

		File oldZip = new File(zipName);
		if (oldZip.exists()) {
			oldZip.delete();
		}

		ZipFiles zipFiles = new ZipFiles();
		zipFiles.zipDirectory(sourceFile, zipName);
		if (getServerType().equalsIgnoreCase("linux")) {
			try {
				Files.setPosixFilePermissions(Paths.get(oldZip.getAbsolutePath()), perms);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String xmlPath = SOURCE_FOLDER + lesson.getId() + "/" + lesson.getId() + ".xml";
		String lessonXML = FileUtils.readFileToString(new File(xmlPath));
		Serializer serializer = new Persister();
		videoLesson = serializer.read(VideoLesson.class, lessonXML);
		videoLesson.setZipFileURL(getMediaURLPath() + "/lessonXMLs/" + lesson.getId() + ".zip");
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

		String SOURCE_FOLDER = getMediaPath() + "/lessonXMLs/" + lesson.getId() + "/";
		File sourceFile = new File(SOURCE_FOLDER);

		String zipName = getMediaPath() + "/lessonXMLs/" + lesson.getId() + ".zip";

		File oldZip = new File(zipName);
		if (oldZip.exists()) {
			oldZip.delete();
		}

		ZipFiles zipFiles = new ZipFiles();
		zipFiles.zipDirectory(sourceFile, zipName);
		if (getServerType().equalsIgnoreCase("linux")) {
			try {
				Files.setPosixFilePermissions(Paths.get(oldZip.getAbsolutePath()), perms);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String xmlPath = SOURCE_FOLDER + lesson.getId() + "/" + lesson.getId() + ".xml";
		String lessonXML = FileUtils.readFileToString(new File(xmlPath));
		Serializer serializer = new Persister();
		interactiveContent = serializer.read(InteractiveContent.class, lessonXML);
		interactiveContent.setZipFileURL(getMediaURLPath() + "/lessonXMLs/" + lesson.getId() + ".zip");

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
		// System.out.println("Writing file : '" + pathOffileToInclude + "' to
		// zip file");

		File file = new File(pathOffileToInclude);
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(pathOffileToInclude.replaceAll(mediaPath, "")
				.replaceAll("video/interactive_audios", "").replaceAll("video/interactive_videos", "")
				.replaceAll("video/interactive_images", "").replaceAll(".jpg", ".aaa").replaceAll(".png", ".aaa")
				.replaceAll(".jpeg", ".aaa").replaceAll(".mp4", ".aaa").replaceAll(".json", ".aaa"));
		zipOutputStream.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOutputStream.write(bytes, 0, length);
		}
		zipOutputStream.closeEntry();
		fis.close();
	}

	public static void main(String[] args) {
		CreateZIPForItem kk = new CreateZIPForItem();

		System.out.println("start");
		 for (int i = 163; i < 6953; i++) {
		try {

			Lesson lesson = new LessonDAO().findById(i);
			kk.createZIPForPresentationLesson(lesson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		 }
		System.out.println("end");
		System.exit(0);
	}

}