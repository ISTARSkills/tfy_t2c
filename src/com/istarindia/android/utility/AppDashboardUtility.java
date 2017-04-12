package com.istarindia.android.utility;

import com.istarindia.android.pojo.DashboardCard;
import com.istarindia.apps.services.AppJobServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.Job;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.Task;

public class AppDashboardUtility {
	
	public DashboardCard getDashboardCardForVideo(Task task){
		return null;
	}

	public DashboardCard getDashboardCardForAssessment(Task task) {

		int itemId = task.getItemId();
		AppContentServiceUtility contentServiceUtility = new AppContentServiceUtility();
		Assessment assessment = contentServiceUtility.getAssessment(itemId);

		DashboardCard dashboardCard = null;
		if (assessment != null) {
			dashboardCard = new DashboardCard(task.getId(), assessment.getAssessmenttitle(),
					assessment.getAssessmenttitle(), assessment.getAssessmenttitle(), null,
					assessment.getNumberOfQuestions(), assessment.getAssessmentdurationminutes(), 100, 50,
					task.getItemType(), task.getItemId());
		}
		return dashboardCard;
	}

	public DashboardCard getDashboardCardForLesson(Task task) {

		int itemId = task.getItemId();
		AppContentServiceUtility contentServiceUtility = new AppContentServiceUtility();
		Lesson lesson = contentServiceUtility.getLesson(itemId);

		DashboardCard dashboardCard = null;
		if (lesson != null) {
			dashboardCard = new DashboardCard(task.getId(), lesson.getTitle(), task.getState(), lesson.getDescription(),
					"/root/talentify/presentation.jpeg", null, task.getItemType(), lesson.getId());
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
