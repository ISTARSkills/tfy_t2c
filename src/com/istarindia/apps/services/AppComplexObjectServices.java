package com.istarindia.apps.services;

import java.util.ArrayList;
import java.util.List;

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
import com.istarindia.android.utility.AppDashboardUtility;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.android.utility.AppUserRankUtility;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.utilities.TaskCategory;

public class AppComplexObjectServices {

	
	public ComplexObject getComplexObjectForUser(int userId){
		
		ComplexObject complexObject = null;
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(userId);

		if(istarUser!=null){
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			AppDashboardUtility dashboardUtility = new AppDashboardUtility();
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			AppCourseServices appCourseServices = new AppCourseServices();
			AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
			AppServices appServices= new AppServices();
			
			StudentAssessmentServices studentAssessmentServices= new StudentAssessmentServices();
			StudentPlaylistServices studentPlaylistServices= new StudentPlaylistServices();
			
			TaskServices taskServices = new TaskServices();

			complexObject = new ComplexObject();
			//Id
			complexObject.setId(userId);
			long previousTime = System.currentTimeMillis();
			
			//Student Profile
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);
			complexObject.setStudentProfile(studentProfile);
			System.err.println("complexObject StudentProfile " + "Time->"+(System.currentTimeMillis()-previousTime));
			
			//Skills -- to be changed
			List<SkillReportPOJO> allSkills = appServices.getSkillsMapOfUser(userId);
			complexObject.setSkills(allSkills);
			System.err.println("complexObject allSkills->" + allSkills.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//Tasks		
			List<Task> allTaskOfUser = taskServices.getAllTaskOfActorForToday(istarUser);
			List<TaskSummaryPOJO> allTaskSummary = new ArrayList<TaskSummaryPOJO>();
			int completedTasks = 0;
			for (Task task : allTaskOfUser) {
				TaskSummaryPOJO taskSummaryPOJO = null;
				String itemType = task.getItemType();

				switch (itemType) {
				case TaskCategory.ASSESSMENT:
					taskSummaryPOJO = dashboardUtility.getTaskSummaryPOJOForAssessment(task);
					break;
				case TaskCategory.LESSON:
					taskSummaryPOJO = dashboardUtility.getTaskSummaryPOJOForLesson(task);
					break;
				}
				if (taskSummaryPOJO != null) {
					if (taskSummaryPOJO.getStatus().equals("COMPLETE")) {
						completedTasks++;
					}
					allTaskSummary.add(taskSummaryPOJO);
				}
			}

			if (allTaskSummary.size() > 0) {
				String messageForCompletedTasks = completedTasks + " Tasks Completed";
				String messageForIncompleteTasks = (allTaskSummary.size() - completedTasks)
						+ " tasks remaining for the day";

				for (TaskSummaryPOJO taskSummaryPOJO : allTaskSummary) {
					taskSummaryPOJO.setMessageForCompletedTasks(messageForCompletedTasks);
					taskSummaryPOJO.setMessageForIncompleteTasks(messageForIncompleteTasks);
				}
			}
			System.err.println("complexObject allTaskSummary->" + allTaskSummary.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//Assessments			
			List<AssessmentPOJO> allAssessmentsOfUser = new ArrayList<AssessmentPOJO>();	
			for (Task task : allTaskOfUser) {
				if (task.getIsActive() && task.getItemType().equals("ASSESSMENT")) {
					Assessment assessment = appAssessmentServices.getAssessment(task.getItemId());
					if (assessment != null && assessment.getAssessmentQuestions().size() > 0) {
						AssessmentPOJO assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);
						allAssessmentsOfUser.add(assessmentPOJO);
					}
				}
			}
			complexObject.setAssessments(allAssessmentsOfUser);
			System.err.println("complexObject allAssessmentsOfUser->" + allAssessmentsOfUser.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//Assessment Responses
			List<AssessmentResponsePOJO> allResponse = new ArrayList<AssessmentResponsePOJO>();
			List<Integer> allAssessmentIds = studentAssessmentServices.getAllAssessmentsAttemptedByUser(userId);
			for (Integer assessmentId : allAssessmentIds) {
				AssessmentResponsePOJO response = appAssessmentServices.getAssessmentResponseOfUser(assessmentId,
						userId);
				if (response != null) {
					allResponse.add(response);
				}
			}
			complexObject.setAssessmentResponses(allResponse);
			System.err.println("complexObject allResponse->" + allResponse.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//Assessment Reports			
			List<AssessmentReportPOJO> allAssessmentReport = appAssessmentServices.getAllAssessmentReportsOfUser(userId);
			complexObject.setAssessmentReports(allAssessmentReport);
			System.err.println("complexObject allAssessmentReport->" + allAssessmentReport.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
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
			System.err.println("complexObject allCoursePOJO->" + allCoursePOJO.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//CourseRanks - Leaderboard
			/*Set<Integer> allCourseIdSet = new HashSet<Integer>(allCourseId);*/

			List<CourseRankPOJO> allCourseRanks = appUserRankUtility.getCourseRankPOJOForCoursesOfUsersBatch(userId);
			complexObject.setLeaderboards(allCourseRanks);
			System.err.println("complexObject allCourseRanks->" + allCourseRanks.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			//Events
			AppCalendarServices appCalendarServices = new AppCalendarServices();
			List<DailyTaskPOJO> allTaskEvents = appCalendarServices.getAllTask(userId);
			complexObject.setEvents(allTaskEvents);		
			System.err.println("complexObject allTaskEvents->" + allTaskEvents.size() + "Time->" + (System.currentTimeMillis()-previousTime));
			
			AppNotificationServices appNotificationServices = new AppNotificationServices();
			List<NotificationPOJO> allNotifications = appNotificationServices.getNotificationsForUser(userId);
			complexObject.setNotifications(allNotifications);
			System.err.println("complexObject allNotifications->" + allNotifications.size() + "Time->" + (System.currentTimeMillis()-previousTime));
		}
		return complexObject;
	}	
}
