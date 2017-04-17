package com.istarindia.android.utility;

import com.istarindia.android.pojo.DashboardCard;
import com.istarindia.apps.services.AppAssessmentServices;
import com.istarindia.apps.services.AppJobServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.Job;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.Task;

public class AppDashboardUtility {

	public DashboardCard getDashboardCardForAssessment(Task task) {

		System.out.println("GEting Assessment Card");
		
		int itemId = task.getItemId();
		AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
		Assessment assessment = appAssessmentServices.getAssessment(itemId);

		DashboardCard dashboardCard = null;
		if (assessment != null) {
			System.out.println("Assessment Found");
			dashboardCard = new DashboardCard(task.getId(), assessment.getAssessmenttitle(),
					assessment.getAssessmenttitle(), assessment.getAssessmenttitle(), null,
					assessment.getNumberOfQuestions(), assessment.getAssessmentdurationminutes(), 100, 50,
					task.getItemType(), task.getItemId());
		}else{
			System.out.println("Assessment Not Found");
		}
		return dashboardCard;
	}

	public DashboardCard getDashboardCardForLesson(Task task) {

		int itemId = task.getItemId();
		AppContentServiceUtility contentServiceUtility = new AppContentServiceUtility();
		Lesson lesson = contentServiceUtility.getLesson(itemId);

		DashboardCard dashboardCard = null;
		if (lesson != null) {
			
			if(lesson.getType().equals("VIDEO")){
				System.out.println("LEsson is of type VIDEO");
				String thumbnailURL = lesson.getVideoLesson().getVideo_thumb_url();
				String videoURL = lesson.getVideoLesson().getVideo_url();
				
				dashboardCard = new DashboardCard(task.getId(), lesson.getTitle(), task.getState(), lesson.getDescription(),
						thumbnailURL, videoURL, task.getItemType(), lesson.getId());
			}else{
				dashboardCard = new DashboardCard(task.getId(), lesson.getTitle(), task.getState(), lesson.getDescription(),
						"/root/talentify/presentation.jpeg", null, task.getItemType(), lesson.getId());
			}
		}
		return dashboardCard;
	}

	public DashboardCard getDashboardCardForJob(Task task) {

		int itemId = task.getItemId();
		AppJobServices jobServices = new AppJobServices();
		Job job = jobServices.getJob(itemId);

		DashboardCard dashboardCard = null;
		if (job != null) {
			dashboardCard = new DashboardCard(task.getId(), job.getTitle(), task.getState(), job.getDescription(),
					job.getOrganization().getImage(), task.getItemType(), job.getId());
		}
		return dashboardCard;
	}
}
