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
import com.istarindia.android.pojo.DailyTaskPOJO;
import com.istarindia.apps.services.AppCalendarServices;

@Path("calendar/user/{userId}")
public class RESTCalendarService {

	@GET
	@Path("{year}/{month}/{day}")
	@Produces(MediaType.APPLICATION_JSON)	
	public Response getDailyTasksOfUser(@PathParam("userId") int userId, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day){
		
		try{
			AppCalendarServices appCalendarServices = new AppCalendarServices();
			List<DailyTaskPOJO> allTask = appCalendarServices.getDailyTask(userId, day, month, year);
			
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			String result = gson.toJson(allTask);
			
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)	
	public Response getMonthlyTasksOfUser(@PathParam("userId") int userId, @PathParam("year") int year, @PathParam("month") int month){
		
		try{
			AppCalendarServices appCalendarServices = new AppCalendarServices();
			List<DailyTaskPOJO> allTask = appCalendarServices.getMonthlyTask(userId, month, year);
			
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			String result = gson.toJson(allTask);
			
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("{year}")
	@Produces(MediaType.APPLICATION_JSON)	
	public Response getYearlyTasksOfUser(@PathParam("userId") int userId, @PathParam("year") int year){
		
		try{
			AppCalendarServices appCalendarServices = new AppCalendarServices();
			List<DailyTaskPOJO> allTask = appCalendarServices.getYearlyTask(userId, year);
			
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			String result = gson.toJson(allTask);
			
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)	
	public Response getAllTasksOfUser(@PathParam("userId") int userId){
		
		try{
			AppCalendarServices appCalendarServices = new AppCalendarServices();
			List<DailyTaskPOJO> allTask = appCalendarServices.getAllTask(userId);
			
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			String result = gson.toJson(allTask);
			
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
