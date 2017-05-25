package com.istarindia.android.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.utilities.TaskItemCategory;

@Path("get_lesson_details")
public class RESTServletStyle {

	@Context
    UriInfo uriInfo;
	
	@GET	
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPOJOOrXML(@QueryParam("userId") int userId, @QueryParam("taskId") int taskId) throws URISyntaxException{
		
			TaskServices taskServices = new TaskServices();
			Task task = taskServices.getTask(taskId);
			
			System.out.println(uriInfo.getBaseUri());
			System.out.println(uriInfo.getAbsolutePath());
			switch(task.getItemType()){
			case TaskItemCategory.LESSON:
				System.out.println("Task is Video");
				URI uri = new URI(uriInfo.getBaseUri()+"lessons/user/"+userId+"/"+task.getItemId());
				ResponseBuilder builder =  Response.seeOther(uri);
				return builder.build();
			case TaskItemCategory.ASSESSMENT:
				System.out.println("Task is Assessment");
				URI uriAssessment = new URI(uriInfo.getBaseUri()+"assessments/user/"+userId+"/"+task.getItemId());
				ResponseBuilder builderAssessment =  Response.seeOther(uriAssessment);
				return builderAssessment.build();
			case TaskItemCategory.JOB_STUDENT:
				System.out.println("Task is Job");
				URI uriJob = new URI(uriInfo.getBaseUri()+"jobs/user/"+userId+"/"+task.getId());
				ResponseBuilder builderJob =  Response.seeOther(uriJob);
				return builderJob.build();				
			}
			return null;
	}
}
