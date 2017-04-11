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
import com.istarindia.android.utility.AppDashboardUtility;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.utilities.TaskCategory;

@Path("user/{userId}")
public class RESTDashboardService {

	@GET
	@Path("dashboard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDashboardCard(@PathParam("userId") int userId) {

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(userId);
		Set<Task> allTaskOfUser = istarUser.getTasksForActor();

		AppDashboardUtility dashboardUtility = new AppDashboardUtility();
		List<DashboardCard> allDashboardCard = new ArrayList<DashboardCard>();

		for (Task task : allTaskOfUser) {
			if (task.getIsActive()) {
				DashboardCard dashboardCard = null;
				String itemType = task.getItemType();

				switch (itemType) {
				case TaskCategory.ASSESSMENT:
					dashboardCard = dashboardUtility.getDashboardCardForAssessment(task);
					break;
				case TaskCategory.LESSON:
					dashboardCard = dashboardUtility.getDashboardCardForLesson(task);
					break;
				case TaskCategory.JOB:
					dashboardCard = dashboardUtility.getDashboardCardForJob(task);
				case TaskCategory.VIDEO:
					dashboardCard = dashboardUtility.getDashboardCardForVideo(task);
					break;
				}

				if (dashboardCard != null) {
					allDashboardCard.add(dashboardCard);
				}
			}
		}
		return Response.ok(allDashboardCard).build();
	}
}
