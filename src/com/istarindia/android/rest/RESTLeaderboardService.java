package com.istarindia.android.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("leaderboard/user/{userId}")
public class RESTLeaderboardService {

	@GET
	@Path("{courseId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLeaderboardOfUserForCourse(@PathParam("userId") int userId, @PathParam("courseId") int courseId){
		
		
		
		return null;
	}
}
