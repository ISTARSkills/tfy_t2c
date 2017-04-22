package com.istarindia.apps.services;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.AssessmentReportPOJO;
import com.istarindia.android.pojo.AssessmentResponsePOJO;
import com.istarindia.android.pojo.ComplexObject;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.CourseRankPOJO;
import com.istarindia.android.pojo.DailyTaskPOJO;
import com.istarindia.android.pojo.SkillReportPOJO;
import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.istarindia.android.utility.AppDashboardUtility;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.android.utility.AppUserRankUtility;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.StudentPlaylist;
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
			System.out.println("complexObject StudentProfile " + "Time->"+(previousTime-System.currentTimeMillis())/1000);
			//Skills
			List<SkillReportPOJO> allSkills = appServices.getSkillsMapOfUser(userId);
			complexObject.setSkills(allSkills);
			System.out.println("complexObject allSkills->" + allSkills.size() + "Time->" + (previousTime-System.currentTimeMillis())/1000);
			//Tasks		
			List<Task> allTaskOfUser = taskServices.getAllTaskOfActorForToday(istarUser);
			List<TaskSummaryPOJO> allTaskSummary = new ArrayList<TaskSummaryPOJO>();
			
			for(Task task : allTaskOfUser){
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
				if(taskSummaryPOJO!=null){
					allTaskSummary.add(taskSummaryPOJO);
				}
			}
			complexObject.setTasks(allTaskSummary);
			System.out.println("complexObject allTaskSummary->" + allTaskSummary.size() + "Time->" + (previousTime-System.currentTimeMillis())/1000);
			//Assessments			
			List<AssessmentPOJO> allAssessmentsOfUser = new ArrayList<AssessmentPOJO>();	
			for (Task task : allTaskOfUser) {				
				if (task.getIsActive() && task.getItemType().equals("ASSESSMENT")) {
					Assessment assessment = appAssessmentServices.getAssessment(task.getItemId());	
					if(assessment!=null && assessment.getAssessmentQuestions().size() > 0){
					AssessmentPOJO assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);
						allAssessmentsOfUser.add(assessmentPOJO);
					}
				}
			}	
			complexObject.setAssessments(allAssessmentsOfUser);
			System.out.println("complexObject allAssessmentsOfUser->" + allAssessmentsOfUser.size() + "Time->" + (previousTime-System.currentTimeMillis())/1000);
			//Assessment Responses
			List<AssessmentResponsePOJO> allResponse = new ArrayList<AssessmentResponsePOJO>();
			List<Integer> allAssessmentIds = studentAssessmentServices.getAllAssessmentsAttemptedByUser(userId);
			for(Integer assessmentId : allAssessmentIds){
				AssessmentResponsePOJO response = appAssessmentServices.getAssessmentResponseOfUser(assessmentId, userId);
				if(response!=null){
					allResponse.add(response);
				}
			}
			complexObject.setAssessmentResponses(allResponse);
			System.out.println("complexObject allResponse->" + allResponse.size() + "Time->" + (previousTime-System.currentTimeMillis())/1000);
			//Assessment Reports
			List<AssessmentReportPOJO> allAssessmentReport = new ArrayList<AssessmentReportPOJO>();
			for(Integer assessmentId : allAssessmentIds){
				AssessmentReportPOJO assessmentReportPOJO = appAssessmentServices.getAssessmentReport(userId, assessmentId);
				if(assessmentReportPOJO!=null){
					allAssessmentReport.add(assessmentReportPOJO);	
				}
			}
			complexObject.setAssessmentReports(allAssessmentReport);
			System.out.println("complexObject allAssessmentReport->" + allAssessmentReport.size() + "Time->" + (previousTime-System.currentTimeMillis())/1000);
			//Courses
			List<CoursePOJO> allCoursePOJO = new ArrayList<CoursePOJO>();
			List<Integer> allCourseId = studentPlaylistServices.getCoursesforUser(userId);
			for(Integer courseId : allCourseId){
				CoursePOJO coursePOJO = appCourseServices.getCourseOfUser(userId, courseId);
				coursePOJO.setSkillObjectives(appCourseServices.getSkillsReportForCourseOfUser(userId, courseId));
				allCoursePOJO.add(coursePOJO);
			}
			complexObject.setCourses(allCoursePOJO);
			System.out.println("complexObject allCoursePOJO->" + allCoursePOJO.size() + "Time->" + (previousTime-System.currentTimeMillis())/1000);
			//CourseRanks - Leaderboard
			List<StudentPlaylist> allStudentPlaylist = studentPlaylistServices.getStudentPlaylistOfUser(userId);
			Set<Integer> allCourseIdSet = new HashSet<Integer>(allCourseId);

			List<CourseRankPOJO> allCourseRanks = appUserRankUtility.getCourseRankPOJOForCoursesOfUsersBatch(userId, allCourseIdSet);
			complexObject.setLeaderboards(allCourseRanks);
			System.out.println("complexObject allCourseRanks->" + allCourseRanks.size() + "Time->" + (previousTime-System.currentTimeMillis())/1000);
			//Events
			AppCalendarServices appCalendarServices = new AppCalendarServices();
			List<DailyTaskPOJO> allTaskEvents = appCalendarServices.getAllTask(userId);
			complexObject.setEvents(allTaskEvents);		
			System.out.println("complexObject allTaskEvents->" + allTaskEvents.size() + "Time->" + (previousTime-System.currentTimeMillis())/1000);
		}
		return complexObject;
	}	
}
