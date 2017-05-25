package com.istarindia.android.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.istarindia.android.pojo.AppJobPOJO;
import com.istarindia.apps.services.AppJobServices;

@Path("jobs/user/{userId}")
public class RESTJobServices {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJobsOfUser(@PathParam("userId") int userId) {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			AppJobServices appJobServices = new AppJobServices();
			
			List<AppJobPOJO> allAppJobPOJOs = appJobServices.getAllJobsPOJOOfUser(userId);

			String result = gson.toJson(allAppJobPOJOs);
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
	public Response getJobsTaskOfUser(@PathParam("userId") int userId, @PathParam("taskId") int taskId) {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			AppJobServices appJobServices = new AppJobServices();
			
			Object object = appJobServices.getTaskOfJobForUser(taskId);

			String result = gson.toJson(object);
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
}
