package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.istarindia.android.pojo.NotificationPOJO;
import com.istarindia.android.pojo.trainerworkflow.CourseContent;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.CourseDAO;
import com.viksitpro.core.dao.entities.IstarNotification;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.IstarUserDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.entities.TaskDAO;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.notification.IstarNotificationServices;
import com.viksitpro.core.utilities.AppProperies;
import com.viksitpro.core.utilities.DBUTILS;
import com.viksitpro.core.utilities.NotificationType;
import com.viksitpro.core.utilities.TaskItemCategory;

public class AppNotificationServices {

	public List<NotificationPOJO> getNotificationsForUser(int istarUserId) {

		List<NotificationPOJO> allNotificationPOJOs = new ArrayList<NotificationPOJO>();

		IstarNotificationServices istarNotificationServices = new IstarNotificationServices();
		List<IstarNotification> allNotifications = istarNotificationServices.getAllNotificationOfUser(istarUserId);
		int totalNotification = 0;	
		for (IstarNotification istarNotification : allNotifications) {
			NotificationPOJO notificationPOJO = getNotificationPOJO(istarNotification);
			if (notificationPOJO != null) {
				allNotificationPOJOs.add(notificationPOJO);
			}
			if(totalNotification==20)
			{
				break;
			}
			totalNotification++;
		}

		try {
			Collections.sort(allNotificationPOJOs, new Comparator<NotificationPOJO>() {
				public int compare(NotificationPOJO o1, NotificationPOJO o2) {
					if (o1.getTime() == null) {
						return (o2.getTime() == null) ? 0 : -1;
					}
					if (o2.getTime() == null) {
						return 1;
					}
					return o2.getTime().compareTo(o1.getTime());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (allNotificationPOJOs.size() <= 20) {
			return allNotificationPOJOs;
		} else {
			return allNotificationPOJOs.subList(0, 20);
		}

	}

	public NotificationPOJO getNotificationPOJO(IstarNotification istarNotification) {
		String mediaUrlPath = AppProperies.getProperty("media_url_path");;
		
		NotificationPOJO notificationPOJO = null;

		if (istarNotification.getTaskId() != null) {
			Task task = new TaskDAO().findById(istarNotification.getTaskId());
			if (task != null) {
				switch (task.getItemType()) {
				case TaskItemCategory.LESSON:
					notificationPOJO = getNotificationPOJOForLesson(istarNotification);
					break;
				case TaskItemCategory.ASSESSMENT:
					notificationPOJO = getNotificationPOJOForAssessment(istarNotification);
					break;
				case TaskItemCategory.CLASSROOM_SESSION:
					notificationPOJO = getNotificationPOJOForClassroomSession(istarNotification);
					break;
				case TaskItemCategory.CLASSROOM_SESSION_STUDENT:
					notificationPOJO = getNotificationPOJOForClassroomSessionStudent(istarNotification);
					break;
				case TaskItemCategory.REMOTE_CLASS_TRAINER:
					notificationPOJO = getNotificationPOJOForRemoteClassroomSession(istarNotification);
					break;
				case TaskItemCategory.REMOTE_CLASS_STUDENT:
					notificationPOJO = getNotificationPOJOForRemoteClassroomSessionStudent(istarNotification);
					break;
				case TaskItemCategory.WEBINAR_TRAINER:
					notificationPOJO = getNotificationPOJOForWebinarTrainer(istarNotification);
					break;
				case TaskItemCategory.WEBINAR_STUDENT:
					notificationPOJO = getNotificationPOJOForWebinarStudent(istarNotification);
					break;

				}
			}

		} else if (istarNotification.getAction() != null && istarNotification.getType() != null) {
			switch (istarNotification.getType()) {
			case "LESSON":
				notificationPOJO = getNotificationPOJOForLesson(istarNotification);
				break;
			case "ASSESSMENT":
				notificationPOJO = getNotificationPOJOForAssessment(istarNotification);
				break;
			}
		} else {
			notificationPOJO = new NotificationPOJO();
			notificationPOJO.setId(istarNotification.getId());
			notificationPOJO.setMessage(istarNotification.getDetails());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			notificationPOJO.setItemType(NotificationType.MESSAGE);
			IstarUser sender = new IstarUserDAO().findById(istarNotification.getSenderId());
			if (sender.getUserProfile() != null) {
				notificationPOJO.setImageURL(mediaUrlPath + sender.getUserProfile().getImage());
			} else {
				notificationPOJO.setImageURL(mediaUrlPath + "users/" + sender.getEmail().charAt(0) + ".png");
			}
		}
		return notificationPOJO;
	}

	private NotificationPOJO getNotificationPOJOForWebinarStudent(IstarNotification istarNotification) {
		NotificationPOJO notificationPOJO = null;
		String mediaUrlPath = "";
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();

		}

		String getCourseIdEventIt = "select batch_schedule_event.id, batch_schedule_event.course_id from batch_schedule_event, task where task.item_id = batch_schedule_event.id and task.item_type in ('"
				+ TaskItemCategory.WEBINAR_STUDENT + "') and task.id = " + istarNotification.getTaskId();
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> ddd = util.executeQuery(getCourseIdEventIt);
		if (ddd.size() > 0) {
			int itemId = (int) ddd.get(0).get("id");
			int course_id = (int) ddd.get(0).get("course_id");
			Course c = new CourseDAO().findById(course_id);
			TaskServices taskServices = new TaskServices();
			Task task = taskServices.getTask(istarNotification.getTaskId());
			notificationPOJO = new NotificationPOJO();
			notificationPOJO.setId(istarNotification.getId());
			notificationPOJO.setMessage(istarNotification.getDetails());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			notificationPOJO.setImageURL(mediaUrlPath + c.getImage_url());
			notificationPOJO.setItemType(NotificationType.WEBINAR_STUDENT);
			notificationPOJO.setItemId(itemId);

			CourseContent content = new TrainerWorkflowServices().getCourseContent(course_id,
					istarNotification.getTaskId());
			if (content != null && content.getItems() != null && content.getItems().size() > 0) {
				notificationPOJO.getItem().put("lessonId", content.getItems().get(content.getCurrentItemOrderId()));

				notificationPOJO.getItem().put("courseId", course_id);

			}
			if (task != null) {
				notificationPOJO.getItem().put("taskId", task.getId());
			}

		}

		return notificationPOJO;
	}

	private NotificationPOJO getNotificationPOJOForWebinarTrainer(IstarNotification istarNotification) {
		NotificationPOJO notificationPOJO = null;
		String mediaUrlPath = "";
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();

		}

		String getCourseIdEventIt = "select batch_schedule_event.id, batch_schedule_event.course_id from batch_schedule_event, task where task.item_id = batch_schedule_event.id and task.item_type in ('"
				+ TaskItemCategory.WEBINAR_TRAINER + "') and task.id = " + istarNotification.getTaskId();
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> ddd = util.executeQuery(getCourseIdEventIt);
		if (ddd.size() > 0) {
			int itemId = (int) ddd.get(0).get("id");
			int course_id = (int) ddd.get(0).get("course_id");
			Course c = new CourseDAO().findById(course_id);
			TaskServices taskServices = new TaskServices();
			Task task = taskServices.getTask(istarNotification.getTaskId());
			notificationPOJO = new NotificationPOJO();
			notificationPOJO.setId(istarNotification.getId());
			notificationPOJO.setMessage(istarNotification.getDetails());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			notificationPOJO.setImageURL(mediaUrlPath + c.getImage_url());
			notificationPOJO.setItemType(NotificationType.WEBINAR_TRAINER);
			notificationPOJO.setItemId(itemId);

			CourseContent content = new TrainerWorkflowServices().getCourseContent(course_id,
					istarNotification.getTaskId());
			if (content != null && content.getItems() != null && content.getItems().size() > 0) {
				notificationPOJO.getItem().put("lessonId", content.getItems().get(content.getCurrentItemOrderId()));

				notificationPOJO.getItem().put("courseId", course_id);

			}
			if (task != null) {
				notificationPOJO.getItem().put("taskId", task.getId());
			}

		}

		return notificationPOJO;
	}

	private NotificationPOJO getNotificationPOJOForRemoteClassroomSessionStudent(IstarNotification istarNotification) {

		NotificationPOJO notificationPOJO = null;
		String mediaUrlPath = "";
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();

		}

		String getCourseIdEventIt = "select batch_schedule_event.id, batch_schedule_event.course_id from batch_schedule_event, task where task.item_id = batch_schedule_event.id and task.item_type in ('"
				+ TaskItemCategory.REMOTE_CLASS_STUDENT + "') and task.id = " + istarNotification.getTaskId();
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> ddd = util.executeQuery(getCourseIdEventIt);
		if (ddd.size() > 0) {
			int itemId = (int) ddd.get(0).get("id");
			int course_id = (int) ddd.get(0).get("course_id");
			Course c = new CourseDAO().findById(course_id);
			TaskServices taskServices = new TaskServices();
			Task task = taskServices.getTask(istarNotification.getTaskId());
			notificationPOJO = new NotificationPOJO();
			notificationPOJO.setId(istarNotification.getId());
			notificationPOJO.setMessage(istarNotification.getDetails());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			notificationPOJO.setImageURL(mediaUrlPath + c.getImage_url());
			notificationPOJO.setItemType(NotificationType.REMOTE_CLASS_STUDENT);
			notificationPOJO.setItemId(itemId);

			CourseContent content = new TrainerWorkflowServices().getCourseContent(course_id,
					istarNotification.getTaskId());
			if (content != null && content.getItems() != null && content.getItems().size() > 0) {
				notificationPOJO.getItem().put("lessonId", content.getItems().get(content.getCurrentItemOrderId()));

				notificationPOJO.getItem().put("courseId", course_id);

			}
			if (task != null) {
				notificationPOJO.getItem().put("taskId", task.getId());
			}

		}

		return notificationPOJO;
	}

	private NotificationPOJO getNotificationPOJOForClassroomSessionStudent(IstarNotification istarNotification) {

		NotificationPOJO notificationPOJO = null;
		String mediaUrlPath = "";
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String getCourseIdEventIt = "select batch_schedule_event.id, batch_schedule_event.course_id from batch_schedule_event, task where task.item_id = batch_schedule_event.id and task.item_type in ('"
				+ TaskItemCategory.CLASSROOM_SESSION_STUDENT + "') and task.id = " + istarNotification.getTaskId();
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> ddd = util.executeQuery(getCourseIdEventIt);
		if (ddd.size() > 0) {
			int itemId = (int) ddd.get(0).get("id");
			int course_id = (int) ddd.get(0).get("course_id");
			Course c = new CourseDAO().findById(course_id);
			TaskServices taskServices = new TaskServices();
			Task task = taskServices.getTask(istarNotification.getTaskId());
			notificationPOJO = new NotificationPOJO();
			notificationPOJO.setId(istarNotification.getId());
			notificationPOJO.setMessage(istarNotification.getDetails());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			notificationPOJO.setImageURL(mediaUrlPath + c.getImage_url());
			notificationPOJO.setItemType(NotificationType.CLASSROOM_SESSION_STUDENT);
			notificationPOJO.setItemId(itemId);

			if (task != null) {
				notificationPOJO.getItem().put("taskId", task.getId());
			}

		}

		return notificationPOJO;
	}

	private NotificationPOJO getNotificationPOJOForClassroomSession(IstarNotification istarNotification) {
		NotificationPOJO notificationPOJO = null;
		String mediaUrlPath = "";
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();

		}

		String getCourseIdEventIt = "select batch_schedule_event.id, batch_schedule_event.course_id from batch_schedule_event, task where task.item_id = batch_schedule_event.id and task.item_type in ('"
				+ TaskItemCategory.CLASSROOM_SESSION + "') and task.id = " + istarNotification.getTaskId();
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> ddd = util.executeQuery(getCourseIdEventIt);
		if (ddd.size() > 0) {
			int itemId = (int) ddd.get(0).get("id");
			int course_id = (int) ddd.get(0).get("course_id");
			Course c = new CourseDAO().findById(course_id);
			TaskServices taskServices = new TaskServices();
			Task task = taskServices.getTask(istarNotification.getTaskId());
			notificationPOJO = new NotificationPOJO();
			notificationPOJO.setId(istarNotification.getId());
			notificationPOJO.setMessage(istarNotification.getDetails());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			notificationPOJO.setImageURL(mediaUrlPath + c.getImage_url());
			notificationPOJO.setItemType(NotificationType.CLASSROOM_SESSION);
			notificationPOJO.setItemId(itemId);

			CourseContent content = new TrainerWorkflowServices().getCourseContent(course_id,
					istarNotification.getTaskId());
			if (content != null && content.getItems() != null && content.getItems().size() > 0) {
				notificationPOJO.getItem().put("lessonId", content.getItems().get(content.getCurrentItemOrderId()));

				notificationPOJO.getItem().put("courseId", course_id);

			}
			if (task != null) {
				notificationPOJO.getItem().put("taskId", task.getId());
			}

		}

		return notificationPOJO;
	}

	private NotificationPOJO getNotificationPOJOForRemoteClassroomSession(IstarNotification istarNotification) {
		NotificationPOJO notificationPOJO = null;
		String mediaUrlPath = "";
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();

		}

		String getCourseIdEventIt = "select batch_schedule_event.id, batch_schedule_event.course_id from batch_schedule_event, task where task.item_id = batch_schedule_event.id and task.item_type in ('"
				+ TaskItemCategory.REMOTE_CLASS_TRAINER + "') and task.id = " + istarNotification.getTaskId();
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> ddd = util.executeQuery(getCourseIdEventIt);
		if (ddd.size() > 0) {
			int itemId = (int) ddd.get(0).get("id");
			int course_id = (int) ddd.get(0).get("course_id");
			Course c = new CourseDAO().findById(course_id);
			TaskServices taskServices = new TaskServices();
			Task task = taskServices.getTask(istarNotification.getTaskId());
			notificationPOJO = new NotificationPOJO();
			notificationPOJO.setId(istarNotification.getId());
			notificationPOJO.setMessage(istarNotification.getDetails());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			notificationPOJO.setImageURL(mediaUrlPath + c.getImage_url());
			notificationPOJO.setItemType(NotificationType.REMOTE_CLASS_TRAINER);
			notificationPOJO.setItemId(itemId);

			CourseContent content = new TrainerWorkflowServices().getCourseContent(course_id,
					istarNotification.getTaskId());
			if (content != null && content.getItems() != null && content.getItems().size() > 0) {
				notificationPOJO.getItem().put("lessonId", content.getItems().get(content.getCurrentItemOrderId()));

				notificationPOJO.getItem().put("courseId", course_id);

			}
			if (task != null) {
				notificationPOJO.getItem().put("taskId", task.getId());
			}

		}

		return notificationPOJO;
	}

	public NotificationPOJO getNotificationPOJOForLesson(IstarNotification istarNotification) {
		// System.err.println("istarNotification
		// id>>>>>>"+istarNotification.getId());
		NotificationPOJO notificationPOJO = null;
		String mediaUrlPath = "";
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
				// System.out.println("media_url_path"+mediaUrlPath);
			}
		} catch (IOException e) {
			e.printStackTrace();

		}
		TaskServices taskServices = new TaskServices();
		Task task = taskServices.getTask(istarNotification.getTaskId());
		AppCourseServices appCourseServices = new AppCourseServices();
		Lesson lesson = null;
		if (task != null) {
			lesson = appCourseServices.getLesson(task.getItemId());
		} else if (istarNotification.getAction() != null && istarNotification.getType() != null) {
			try {
				Integer itemId = Integer.parseInt(istarNotification.getAction());
				lesson = appCourseServices.getLesson(itemId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (lesson != null) {
			StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
			StudentPlaylist studentPlaylist = studentPlaylistServices
					.getStudentPlaylistOfUserForLesson(istarNotification.getReceiverId(), lesson.getId());

			if (studentPlaylist != null) {
				notificationPOJO = new NotificationPOJO();
				notificationPOJO.setId(istarNotification.getId());
				notificationPOJO.setMessage(istarNotification.getDetails());
				notificationPOJO.setStatus(istarNotification.getStatus());
				notificationPOJO.setTime(istarNotification.getCreatedAt());
				notificationPOJO.setImageURL(mediaUrlPath + lesson.getImage_url());
				notificationPOJO.setItemType(NotificationType.LESSON + "_" + lesson.getType());
				notificationPOJO.setItemId(lesson.getId());

				notificationPOJO.getItem().put("id", lesson.getId());
				notificationPOJO.getItem().put("moduleId", studentPlaylist.getModule().getId());
				notificationPOJO.getItem().put("courseId", studentPlaylist.getCourse().getId());
				notificationPOJO.getItem().put("cmsessionId", studentPlaylist.getCmsession().getId());
				if (task != null) {
					notificationPOJO.getItem().put("taskId", task.getId());
				}
			}
		}
		return notificationPOJO;
	}

	public NotificationPOJO getNotificationPOJOForAssessment(IstarNotification istarNotification) {

		String mediaUrlPath = "";
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();

		}

		NotificationPOJO notificationPOJO = null;

		TaskServices taskServices = new TaskServices();
		Task task = taskServices.getTask(istarNotification.getTaskId());
		AppCourseServices appCourseServices = new AppCourseServices();
		AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
		Assessment assessment = null;
		if (task != null) {
			assessment = appAssessmentServices.getAssessment(task.getItemId());
		} else if (istarNotification.getAction() != null && istarNotification.getType() != null) {
			try {
				Integer itemId = Integer.parseInt(istarNotification.getAction());
				assessment = appAssessmentServices.getAssessment(itemId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (assessment != null) {
			notificationPOJO = new NotificationPOJO();
			notificationPOJO.setId(istarNotification.getId());
			notificationPOJO.setMessage(istarNotification.getTitle());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			Course course = assessment.getLesson().getCmsessions().iterator().next().getModules().iterator().next().getCourses().iterator().next();
			notificationPOJO.setImageURL(course.getImage_url());
			notificationPOJO.setItemType(NotificationType.ASSESSMENT);
			notificationPOJO.setItemId(assessment.getId());
			notificationPOJO.setImageURL(mediaUrlPath + "course_images/assessment.png");
			notificationPOJO.getItem().put("id", assessment.getId());
			notificationPOJO.getItem().put("courseId", course.getId());
			if (task != null) {
				notificationPOJO.getItem().put("taskId", task.getId());
			}
		}
		return notificationPOJO;
	}
}
