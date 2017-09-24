package com.istarindia.android.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.ComplexObject;
import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.istarindia.android.utility.AppDashboardUtility;
import com.istarindia.apps.factories.TaskFactory;
import com.istarindia.apps.services.AppComplexObjectServices;
import com.istarindia.apps.services.GamificationServices;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.IstarUserDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.entities.TaskDAO;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.utilities.TaskItemCategory;

@Path("tasks/user/{userId}")
public class RESTDashboardService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTaskSummaryForUser(@PathParam("userId") int userId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			TaskServices taskServices = new TaskServices();
			List<Task> allTaskOfUser = taskServices.getAllTaskOfActorForToday(istarUser);
			List<TaskSummaryPOJO> allTaskSummary = new ArrayList<TaskSummaryPOJO>();
			TaskFactory factory = new TaskFactory();
			int completedTasks = 0;
			for (Task task : allTaskOfUser) {
				TaskSummaryPOJO taskSummaryPOJO = null;
				taskSummaryPOJO = factory.getTaskSummary(task);
				if (taskSummaryPOJO != null) {
					if (taskSummaryPOJO.getStatus().equals("COMPLETED")) {
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

			String result = gson.toJson(allTaskSummary);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTaskDetails(@PathParam("userId") int userId, @PathParam("taskId") int taskId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			IstarUser istarUser = new IstarUserDAO().findById(userId);
			Task task = new TaskDAO().findById(taskId);

			Object result = null;
			AppDashboardUtility dashboardUtility = new AppDashboardUtility();

			if (task == null || task.getIstarUserByActor().getId() != istarUser.getId()) {
				System.out.println("invalid task id is "+taskId);
				throw new Exception();
			}
			String itemType = task.getItemType();

			switch (itemType) {
			case TaskItemCategory.ASSESSMENT:
				result = (AssessmentPOJO) dashboardUtility.getAssessmentForTask(task);
				return Response.ok(result).build();
			case TaskItemCategory.LESSON:
				result = (String) dashboardUtility.getLessonForTask(task);
				return Response.ok(gson.toJson(result)).build();
			}
			throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("{taskId}/pojo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTaskSummaryForUser(@PathParam("userId") int userId, @PathParam("taskId") int taskId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {

			Task task = new TaskDAO().findById(taskId);

			if (task == null) {
				throw new Exception("Invlaid Task ID" + taskId);
			}
			TaskFactory factory = new TaskFactory();

			TaskSummaryPOJO taskSummaryPOJO = null;

			taskSummaryPOJO = factory.getTaskSummary(task);

			if (taskSummaryPOJO == null) {
				throw new Exception("Invlaid Task ID" + taskId);
			}

			String result = gson.toJson(taskSummaryPOJO);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@PUT
	@Path("{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response completeTask(@PathParam("userId") int userId, @PathParam("taskId") int taskId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = new IstarUserDAO().findById(userId);

			String role = "";

			try {
				role = istarUser.getUserRoles() != null && istarUser.getUserRoles().iterator().next().getRole() != null
						&& istarUser.getUserRoles().iterator().next().getRole().getRoleName() != null
								? istarUser.getUserRoles().iterator().next().getRole().getRoleName() : "";
			} catch (Exception e) {

			}

			if (!role.equalsIgnoreCase("CONTENT_CREATOR")) {
				TaskServices taskServices = new TaskServices();
				taskServices.completeTask("COMPLETED", false, taskId, istarUser.getAuthToken());

				Task task = taskServices.getTask(taskId);

				if (task.getItemType().equals(TaskItemCategory.LESSON)) {
					AppDashboardUtility appDashboardUtility = new AppDashboardUtility();
					appDashboardUtility.updateStudentPlaylistStatus(task.getItemId(), userId, "COMPLETED");
					GamificationServices gmService = new GamificationServices();
					Lesson lesson = new LessonDAO().findById(task.getItemId());
					gmService.updatePointsAndCoinsOnLessonComplete(istarUser, lesson);					
				}

			}else{
				System.err.println("Task Doesn't marked as Completed due to user("+userId+") role is "+role);
			}
			
			AppComplexObjectServices appComplexObjectServices = new AppComplexObjectServices();
			ComplexObject complexObject = appComplexObjectServices.getComplexObjectForUser(userId);

			if (complexObject == null) {
				throw new Exception();
			}
			String result = gson.toJson(complexObject);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

}
