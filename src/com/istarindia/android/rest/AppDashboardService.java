package com.istarindia.android.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.istarindia.android.pojo.DashboardCard;
import com.istarindia.apps.services.ContentServiceUtility;
import com.istarindia.apps.services.JobServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Job;
import com.viksitpro.core.dao.entities.Presentation;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.utilities.TaskCategory;

@Path("dashboard")
public class AppDashboardService {

	@GET
	@Path("{userId}/cards")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDashboardCard(@PathParam("userId") int userId) {

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(userId);
		Set<Task> allTaskOfUser = istarUser.getTasksForActor();

		List<DashboardCard> allDashboardCard = new ArrayList<DashboardCard>();

		for (Task task : allTaskOfUser) {

				DashboardCard dashboardCard = null;
				String itemType = task.getItemType();

				switch (itemType) {
				case TaskCategory.ASSESSMENT:
					dashboardCard = getDashboardCardForAssessment(task);
					break;
				case TaskCategory.LEARN:
					dashboardCard = getDashboardCardForPresentation(task);
					break;
				case TaskCategory.JOB:
					dashboardCard = getDashboardCardForJob(task);
				}

				if (dashboardCard != null) {
					allDashboardCard.add(dashboardCard);
				}
		}
		System.out.println("Dashboard cards returned");
		return Response.ok(allDashboardCard).build();
	}

	private DashboardCard getDashboardCardForAssessment(Task task) {

		int itemId = task.getItemId();
		ContentServiceUtility contentServiceUtility = new ContentServiceUtility();
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

	private DashboardCard getDashboardCardForPresentation(Task task) {

		int itemId = task.getItemId();
		ContentServiceUtility contentServiceUtility = new ContentServiceUtility();
		Presentation presentation = contentServiceUtility.getPresentation(itemId);

		DashboardCard dashboardCard = null;
		if (presentation != null) {
			// constructor for presentation
		}
		return dashboardCard;
	}
	
	private DashboardCard getDashboardCardForJob(Task task){
		
		int itemId = task.getItemId();
		JobServices jobServices = new JobServices();
		Job job = jobServices.getJob(itemId);
		
		DashboardCard dashboardCard = null;
		if(job!=null){
			dashboardCard = new DashboardCard(task.getId(), job.getTitle(), task.getState(), job.getDescription(), job.getOrganization().getImage(), task.getItemType(), job.getId());
		}
		return dashboardCard;
	}
}
