package com.istarindia.android.rest;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.istarindia.android.pojo.DailyTaskPOJO;
import com.istarindia.android.pojo.NotificationPOJO;
import com.istarindia.apps.services.AppCalendarServices;
import com.istarindia.apps.services.AppNotificationServices;
import com.viksitpro.core.dao.entities.IstarNotification;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.notification.IstarNotificationServices;

@Path("notifications/user/{userId}")
public class RESTNotificationService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllNotifications(@PathParam("userId") int userId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			AppNotificationServices appNotificationServices = new AppNotificationServices();
			List<NotificationPOJO> allNotificationPOJOs = appNotificationServices.getNotificationsForUser(userId);

			String result = gson.toJson(allNotificationPOJOs);

			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	@GET
	@Path("{notificationId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNotificationAndEventPOJO(@PathParam("userId") int userId, @PathParam("notificationId") int notificationId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			System.out.println("notification id "+notificationId);
			IstarNotificationServices istarNotificationServices = new IstarNotificationServices();
			IstarNotification istarNotification = istarNotificationServices.getIstarNotification(notificationId);
			
			TaskServices taskServices = new TaskServices();
			if(istarNotification !=null  && istarNotification.getTaskId()!=null  )
			{
				Task task = taskServices.getTask(istarNotification.getTaskId());
				
				if(istarNotification==null || task==null){
					throw new Exception();
				}
				
				AppNotificationServices appNotificationServices = new AppNotificationServices();
				NotificationPOJO notificationPOJO = appNotificationServices.getNotificationPOJO(istarNotification);
	
				AppCalendarServices appCalendarServices = new AppCalendarServices();
				DailyTaskPOJO dailyTaskPOJO = appCalendarServices.getDailyTaskPOJO(task);
				
				HashMap<String, Object> jsonMap = new HashMap<String, Object>();
				
				jsonMap.put("notification", notificationPOJO);
				jsonMap.put("event", dailyTaskPOJO);
	
				String result = gson.toJson(jsonMap);
				return Response.ok(result).build();
			}
			else
			{
				HashMap<String, Object> jsonMap = new HashMap<String, Object>();
				String result = gson.toJson(jsonMap);
				
				return Response.ok(result).build();
			}	
			
			
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response markNotificationAsRead(@PathParam("userId") int userId, List<Integer> notifications){	
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			System.out.println("Mark notifications as read");
			IstarNotificationServices istarNotificationServices = new IstarNotificationServices();
			
			for(Integer notificationId : notifications){
				istarNotificationServices.updateNotificationStatus(notificationId, "READ");
			}
			
			String result = gson.toJson("DONE");
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
}
