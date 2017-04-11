package com.istarindia.android.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.utility.AppContentServiceUtility;
import com.istarindia.android.utility.AppPOJOUtility;
import com.viksitpro.core.dao.entities.Assessment;

@Path("user/{userId}/assessments")
public class RESTAssessmentService {

	@GET
	@Path("{assessmentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssessment(@PathParam("assessmentId") int assessmentId){
	
		AppContentServiceUtility appContentServiceUtility= new AppContentServiceUtility();
		Assessment assessment = appContentServiceUtility.getAssessment(assessmentId);
		
		AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
		AssessmentPOJO assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);
		
		Gson gson = new Gson();
		String result = gson.toJson(assessmentPOJO);

		return Response.ok(result).build();
	}
}
