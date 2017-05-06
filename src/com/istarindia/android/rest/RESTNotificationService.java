package com.istarindia.android.rest;

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
import com.istarindia.android.pojo.NotificationPOJO;
import com.istarindia.apps.services.AppNotificationServices;
import com.viksitpro.core.notification.IstarNotificationServices;

@Path("notifications/user/{userId}")
public class RESTNotificationService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllNotifications(@PathParam("userId") int userId){
		try{
			AppNotificationServices appNotificationServices = new AppNotificationServices();
			List<NotificationPOJO> allNotificationPOJOs = appNotificationServices.getNotificationsForUser(userId);
			
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			String result = gson.toJson(allNotificationPOJOs);

			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response markNotificationAsRead(@PathParam("userId") int userId, List<Integer> notifications){		
		try{
			System.out.println("MArk notifications as read");
			IstarNotificationServices istarNotificationServices = new IstarNotificationServices();
			
			for(Integer notificationId : notifications){
				istarNotificationServices.updateNotificationStatus(notificationId, "READ");
			}
			
			return Response.ok("DONE").build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
