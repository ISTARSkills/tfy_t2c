package com.istarindia.android.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.istarindia.android.pojo.DashboardCard;
import com.istarindia.apps.services.AssessmentServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.IstarUser;
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
			}

			allDashboardCard.add(dashboardCard);
		}

		//GenericEntity<List<DashboardCard>> entity = new GenericEntity<List<DashboardCard>>(allDashboardCard) {};
		return Response.ok(allDashboardCard).build();
	}

	private DashboardCard getDashboardCardForAssessment(Task task) {

		int itemId = task.getItemId();
		System.out.println("task id is "+task.getId());
		AssessmentServices assessmentServices = new AssessmentServices();
		Assessment assessment = assessmentServices.getAssessment(itemId);

		DashboardCard dashboardCard = null;
		if(assessment!=null){
			System.out.println("Assessment is Not null");
		dashboardCard =  new DashboardCard(task.getId(), assessment.getAssessmenttitle(), assessment.getAssessmenttitle(), assessment.getAssessmenttitle(), null, assessment.getNumberOfQuestions(), assessment.getAssessmentdurationminutes() , 100, 50, task.getItemType(), task.getItemId());
		}
		return dashboardCard;
	}
}
