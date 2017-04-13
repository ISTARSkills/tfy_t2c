package com.istarindia.android.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.istarindia.android.pojo.DashboardCard;
import com.istarindia.android.utility.AppDashboardUtility;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.utilities.TaskCategory;

@Path("dashboard")
public class RESTDashboardService {

	@GET
	@Path("user/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDashboardCard(@PathParam("userId") int userId) {

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);
			
			System.out.println(istarUser.getEmail());
			
			TaskServices taskServices = new TaskServices();			
			List<Task> allTaskOfUser = taskServices.getAllTaskOfActor(istarUser);

			System.out.println("Size of task " + allTaskOfUser.size());
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
						break;
					}

					if (dashboardCard != null) {
						allDashboardCard.add(dashboardCard);
					}
				}
			}
			
			Gson gson = new Gson();
			String result = gson.toJson(allDashboardCard);
			
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
