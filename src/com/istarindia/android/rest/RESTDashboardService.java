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
import com.istarindia.android.pojo.DashboardCard;
import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.istarindia.android.utility.AppDashboardUtility;
import com.istarindia.apps.services.AppAssessmentServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.utilities.TaskCategory;

@Path("tasks/user/{userId}")
public class RESTDashboardService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTaskIds(@PathParam("userId") int userId) {

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);
			
			TaskServices taskServices = new TaskServices();			
			List<Task> allTaskOfUser = taskServices.getAllTaskOfActor(istarUser);
	
			List<Integer> allTasks = new ArrayList<Integer>();
			
			for(Task task : allTaskOfUser){				
				if(task.getIsActive()){
					String itemType = task.getItemType();
					Integer itemId = task.getItemId();
					if(itemType.equals(TaskCategory.ASSESSMENT)){
						AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
						Assessment assessment = appAssessmentServices.getAssessment(itemId);						
							if (assessment != null && assessment.getAssessmentQuestions().size() > 0) {
								allTasks.add(task.getId());
							}
						}else{
							allTasks.add(task.getId());
						}
					}
				}
			Gson gson = new Gson();
			String result = gson.toJson(allTasks);
			
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("summary")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSummaryOfTasks(@PathParam("userId") int userId, @PathParam("taskId") int taskId){
				
		try{
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);
			
			TaskServices taskServices = new TaskServices();			
			List<Task> allTaskOfUser = taskServices.getAllTaskOfActorForToday(istarUser);
			List<TaskSummaryPOJO> allTaskSummary = new ArrayList<TaskSummaryPOJO>();
			
			for(Task task : allTaskOfUser){
				TaskSummaryPOJO taskSummaryPOJO = new TaskSummaryPOJO();
				
				taskSummaryPOJO.setId(task.getId());
				taskSummaryPOJO.setItemId(task.getItemId());
				taskSummaryPOJO.setItemType(task.getItemType());
				if(task.getIsActive()){
					taskSummaryPOJO.setStatus("INCOMPLETE");
					taskSummaryPOJO.setDate(task.getEndDate());
				}else{
					taskSummaryPOJO.setStatus("COMPLETE");
					taskSummaryPOJO.setDate(task.getUpdatedAt());
				}
				taskSummaryPOJO.setName(task.getName());					
				allTaskSummary.add(taskSummaryPOJO);
			}
			Gson gson = new Gson();
			String result = gson.toJson(allTaskSummary);
			
			return Response.ok(result).build();			
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}		
	}
	
	@GET
	@Path("{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDashboardCardOfTask(@PathParam("userId") int userId, @PathParam("taskId") int taskId) {

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);
			
			TaskServices taskServices = new TaskServices();			
			Task task = taskServices.getTask(taskId);
			
			AppDashboardUtility dashboardUtility = new AppDashboardUtility();
			DashboardCard dashboardCard = null;

				if (task!=null && task.getIsActive() && task.getIstarUserByActor().getId()==istarUser.getId()) {
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
				}else{
					return Response.status(Response.Status.BAD_REQUEST).build();
				}

			Gson gson = new Gson();
			String result = gson.toJson(dashboardCard);
			
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@PUT
	@Path("{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response completeTask(@PathParam("userId") int userId, @PathParam("taskId") int taskId){
		try{
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);
			
		TaskServices taskServices = new TaskServices();
		taskServices.completeTask("COMPLETED", false, taskId, istarUser.getAuthToken());
		
			return Response.status(Response.Status.CREATED).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
