package com.istarindia.apps.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.istarindia.android.pojo.AssessmentReportPOJO;
import com.istarindia.android.pojo.ComplexObject;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.CourseRankPOJO;
import com.istarindia.android.pojo.DailyTaskPOJO;
import com.istarindia.android.pojo.NotificationPOJO;
import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.android.utility.AppUserRankUtility;
import com.istarindia.apps.factories.TaskFactory;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.IstarUserDAO;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;

public class AppComplexObjectServices {

	public ComplexObject getComplexObjectForUser(int userId) {

		ComplexObject complexObject = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		IstarUser istarUser = new IstarUserDAO().findById(userId);
		if (istarUser != null) {
			complexObject = new ComplexObject();
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			AppCourseServices appCourseServices = new AppCourseServices();
			AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
			StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
			TaskServices taskServices = new TaskServices();
			// Id
			complexObject.setId(userId);
			long previousTime = System.currentTimeMillis();
			// Student Profile
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);
			complexObject.setStudentProfile(studentProfile);
			
			// Tasks
			List<Task> allTaskOfUser = taskServices.getAllTaskOfActorForToday(istarUser);
			List<TaskSummaryPOJO> tasks = new ArrayList<TaskSummaryPOJO>();
			int completedTasks = 0;
			int incompletedTasks = 0;
			TaskFactory factory = new TaskFactory();
			for (Task task : allTaskOfUser) {
				TaskSummaryPOJO taskSummaryPOJO = null;
				taskSummaryPOJO = factory.getTaskSummary(task);
				try {
					if (taskSummaryPOJO != null) {
						if (((null != taskSummaryPOJO.getDate() && sdf.parse(sdf.format(taskSummaryPOJO.getDate())).compareTo(sdf.parse(sdf.format(new Date()))) == 0)	|| (null != taskSummaryPOJO.getCompletedDate()
										&& (sdf.parse(sdf.format(taskSummaryPOJO.getCompletedDate()))
												.compareTo(sdf.parse(sdf.format(new Date()))) == 0)))) {
							if (taskSummaryPOJO.getStatus().equals("COMPLETED")) {
								completedTasks++;
							} else {
								incompletedTasks++;
							}
						}
						tasks.add(taskSummaryPOJO);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			complexObject.setTasks(tasks);
			int totalTaskSize = tasks.size();
			if (totalTaskSize > 0) {
				String messageForCompletedTasks = completedTasks + " Tasks Completed";
				String messageForIncompleteTasks = (incompletedTasks) + " tasks remaining for the day";

				for (TaskSummaryPOJO taskSummaryPOJO : tasks) {
					taskSummaryPOJO.setMessageForCompletedTasks(messageForCompletedTasks);
					taskSummaryPOJO.setMessageForIncompleteTasks(messageForIncompleteTasks);
				}
			}
			 System.err.println("complexObject allTaskSummary->" +
			 totalTaskSize + "Time->" +
			 (System.currentTimeMillis()-previousTime));
			// Assessment Reports
			List<AssessmentReportPOJO> allAssessmentReport = appAssessmentServices.getAllAssessmentReportsOfUser(userId);
			complexObject.setAssessmentReports(allAssessmentReport);
			System.err.println("complexObject allAssessmentReport->" +allAssessmentReport.size() + "Time->" +(System.currentTimeMillis()-previousTime));
			// Courses
			List<CoursePOJO> allCoursePOJO = new ArrayList<CoursePOJO>();
			List<Integer> allCourseId = studentPlaylistServices.getCoursesforUser(userId);
			for (Integer courseId : allCourseId) {
				CoursePOJO coursePOJO = appCourseServices.getCoursePojoForUser(userId, courseId);
				if (coursePOJO != null) {				
					allCoursePOJO.add(coursePOJO);
				}
			}
			complexObject.setCourses(allCoursePOJO);
			System.err.println("complexObject allCoursePOJO->" +allCoursePOJO.size() + "Time->" +(System.currentTimeMillis()-previousTime));
			List<CourseRankPOJO> allCourseRanks = appUserRankUtility.getCourseRankPOJOForCoursesOfUsersBatch(userId);
			complexObject.setLeaderboards(allCourseRanks);
			System.err.println("complexObject allCourseRanks->" +allCourseRanks.size() + "Time->" +(System.currentTimeMillis()-previousTime));
			// Events
			AppCalendarServices appCalendarServices = new AppCalendarServices();
			List<DailyTaskPOJO> allTaskEvents = appCalendarServices.getAllTask(userId);
			complexObject.setEvents(allTaskEvents);
			System.err.println("complexObject allTaskEvents->" +allTaskEvents.size() + "Time->" +(System.currentTimeMillis()-previousTime));

			AppNotificationServices appNotificationServices = new AppNotificationServices();
			List<NotificationPOJO> allNotifications = appNotificationServices.getNotificationsForUser(userId);
			complexObject.setNotifications(allNotifications);
			System.err.println("complexObject allNotifications->" +allNotifications.size() + "Time->" +(System.currentTimeMillis()-previousTime));
		}
		return complexObject;
	}

}
