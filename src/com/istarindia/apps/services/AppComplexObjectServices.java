package com.istarindia.apps.services;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.AssessmentReportPOJO;
import com.istarindia.android.pojo.AssessmentResponsePOJO;
import com.istarindia.android.pojo.ComplexObject;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.CourseRankPOJO;
import com.istarindia.android.pojo.DailyTaskPOJO;
import com.istarindia.android.pojo.NotificationPOJO;
import com.istarindia.android.pojo.SkillReportPOJO;
import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.istarindia.android.pojo.task.AssessmentTask;
import com.istarindia.android.pojo.task.ClassRoomSessionTask;
import com.istarindia.android.utility.AppDashboardUtility;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.android.utility.AppUserRankUtility;
import com.istarindia.apps.factories.TaskFactory;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.utilities.AppProperies;
import com.viksitpro.core.utilities.TaskItemCategory;

public class AppComplexObjectServices {

	
	public ComplexObject getComplexObjectForUser(int userId){
		
		ComplexObject complexObject = null;
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(userId);
		
		
		
		if(istarUser!=null){
			complexObject = new ComplexObject();			
			
			
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			AppDashboardUtility dashboardUtility = new AppDashboardUtility();
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			AppCourseServices appCourseServices = new AppCourseServices();
			AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
			AppServices appServices= new AppServices();			
			StudentPlaylistServices studentPlaylistServices= new StudentPlaylistServices();			
			TaskServices taskServices = new TaskServices();			
			//Id
			complexObject.setId(userId);
			long previousTime = System.currentTimeMillis();			
			//Student Profile
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);
			complexObject.setStudentProfile(studentProfile);
			if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
				//System.err.println(
						//"complexObject StudentProfile " + "Time->" + (System.currentTimeMillis() - previousTime));

			}
			//Skills -- to be changed
			List<SkillReportPOJO> allSkills = appServices.getSkillsMapOfUser(userId);
			complexObject.setSkills(allSkills);
			
			//System.err.println("complexObject allSkills->" + allSkills.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//Tasks		
			List<Task> allTaskOfUser = taskServices.getAllTaskOfActorForToday(istarUser);
			List<TaskSummaryPOJO> tasks = new ArrayList<TaskSummaryPOJO>();
			
			int completedTasks = 0;
			TaskFactory factory = new TaskFactory();
			for (Task task : allTaskOfUser) {
				TaskSummaryPOJO taskSummaryPOJO = null;								
				taskSummaryPOJO = factory.getTaskSummary(task);
					
				if (taskSummaryPOJO != null) {
					if (taskSummaryPOJO.getStatus().equals("COMPLETED")) {
						completedTasks++;
						
					}
					tasks.add(taskSummaryPOJO);
				}
			}
			
			complexObject.setTasks(tasks);
			int totalTaskSize = tasks.size();
			if (totalTaskSize > 0) {
				String messageForCompletedTasks = completedTasks + " Tasks Completed";
				String messageForIncompleteTasks = (totalTaskSize - completedTasks)
						+ " tasks remaining for the day";

				for (TaskSummaryPOJO taskSummaryPOJO : tasks) {
					taskSummaryPOJO.setMessageForCompletedTasks(messageForCompletedTasks);
					taskSummaryPOJO.setMessageForIncompleteTasks(messageForIncompleteTasks);
				}
				
			}
			
			
			//System.err.println("complexObject allTaskSummary->" + totalTaskSize + "Time->" + (System.currentTimeMillis()-previousTime));
			
		
			
			//Assessment Reports			
			List<AssessmentReportPOJO> allAssessmentReport = appAssessmentServices.getAllAssessmentReportsOfUser(userId);
			complexObject.setAssessmentReports(allAssessmentReport);
			//System.err.println("complexObject allAssessmentReport->" + allAssessmentReport.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//Courses
			List<CoursePOJO> allCoursePOJO = new ArrayList<CoursePOJO>();
			List<Integer> allCourseId = studentPlaylistServices.getCoursesforUser(userId);
			for(Integer courseId : allCourseId){
				CoursePOJO coursePOJO = appCourseServices.getCourseOfUser(userId, courseId);
				if(coursePOJO!=null){
				coursePOJO.setSkillObjectives(appCourseServices.getSkillsReportForCourseOfUser(userId, courseId));
				allCoursePOJO.add(coursePOJO);
				}
			}
			
			complexObject.setCourses(allCoursePOJO);
			//System.err.println("complexObject allCoursePOJO->" + allCoursePOJO.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//CourseRanks - Leaderboard
			/*Set<Integer> allCourseIdSet = new HashSet<Integer>(allCourseId);*/

			List<CourseRankPOJO> allCourseRanks = appUserRankUtility.getCourseRankPOJOForCoursesOfUsersBatch(userId);
			complexObject.setLeaderboards(allCourseRanks);
			//System.err.println("complexObject allCourseRanks->" + allCourseRanks.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//Events
			AppCalendarServices appCalendarServices = new AppCalendarServices();
			List<DailyTaskPOJO> allTaskEvents = appCalendarServices.getAllTask(userId);
			complexObject.setEvents(allTaskEvents);		
			//System.err.println("complexObject allTaskEvents->" + allTaskEvents.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			AppNotificationServices appNotificationServices = new AppNotificationServices();
			List<NotificationPOJO> allNotifications = appNotificationServices.getNotificationsForUser(userId);
			complexObject.setNotifications(allNotifications);
			//System.err.println("complexObject allNotifications->" + allNotifications.size() + "Time->" + (System.currentTimeMillis()-previousTime));
		}
		return complexObject;
	}

	
}
