package com.istarindia.apps.services;

import java.util.ArrayList;
import java.util.List;

import com.istarindia.android.pojo.NotificationPOJO;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.IstarNotification;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.notification.IstarNotificationServices;
import com.viksitpro.core.utilities.NotificationType;
import com.viksitpro.core.utilities.TaskCategory;

public class AppNotificationServices {

	public List<NotificationPOJO> getNotificationsForUser(int istarUserId) {

		List<NotificationPOJO> allNotificationPOJOs = new ArrayList<NotificationPOJO>();

		IstarNotificationServices istarNotificationServices = new IstarNotificationServices();
		List<IstarNotification> allNotifications = istarNotificationServices.getAllNotificationOfUser(istarUserId);

		for (IstarNotification istarNotification : allNotifications) {
			NotificationPOJO notificationPOJO = getNotificationPOJO(istarNotification);
			if (notificationPOJO != null) {
				allNotificationPOJOs.add(notificationPOJO);
			}
		}
		return allNotificationPOJOs;
	}

	public NotificationPOJO getNotificationPOJO(IstarNotification istarNotification) {

		NotificationPOJO notificationPOJO = null;

		TaskServices taskServices = new TaskServices();
		Task task = taskServices.getTask(istarNotification.getTaskId());

		if (task != null) {
			switch (task.getItemType()) {
			case TaskCategory.LESSON:
				notificationPOJO = getNotificationPOJOForLesson(istarNotification);
				break;
			case TaskCategory.ASSESSMENT:
				notificationPOJO = getNotificationPOJOForAssessment(istarNotification);
				break;

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
		}
		return notificationPOJO;
	}

	public NotificationPOJO getNotificationPOJOForLesson(IstarNotification istarNotification) {
		NotificationPOJO notificationPOJO = null;

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
			StudentPlaylistServices studentPlaylistServices=new StudentPlaylistServices();
			StudentPlaylist studentPlaylist = studentPlaylistServices.getStudentPlaylistOfUserForLesson(istarNotification.getReceiverId(), lesson.getId());

			if(studentPlaylist!=null){
			notificationPOJO = new NotificationPOJO();
			notificationPOJO.setId(istarNotification.getId());
			notificationPOJO.setMessage(istarNotification.getDetails());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			notificationPOJO.setImageURL(lesson.getImage_url());
			notificationPOJO.setItemType(NotificationType.LESSON+"_" + lesson.getType());
			notificationPOJO.setItemId(lesson.getId());
			
			notificationPOJO.getItem().put("id", lesson.getId());
			notificationPOJO.getItem().put("moduleId", studentPlaylist.getModule().getId());
			notificationPOJO.getItem().put("courseId", studentPlaylist.getCourse().getId());
			notificationPOJO.getItem().put("cmsessionId", studentPlaylist.getCmsession().getId());
			if(task!=null){
				notificationPOJO.getItem().put("taskId", task.getId());
			}
			}
		}
		return notificationPOJO;
	}

	public NotificationPOJO getNotificationPOJOForAssessment(IstarNotification istarNotification) {
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
			notificationPOJO.setMessage(istarNotification.getDetails());
			notificationPOJO.setStatus(istarNotification.getStatus());
			notificationPOJO.setTime(istarNotification.getCreatedAt());
			Course course = appCourseServices.getCourse(assessment.getCourse());
			notificationPOJO.setImageURL(course.getImage_url());
			notificationPOJO.setItemType(NotificationType.ASSESSMENT);
			notificationPOJO.setItemId(assessment.getId());
			
			notificationPOJO.getItem().put("id", assessment.getId());
			notificationPOJO.getItem().put("courseId", course.getId());
			if(task!=null){
				notificationPOJO.getItem().put("taskId", task.getId());
			}
		}
		return notificationPOJO;
	}
}
