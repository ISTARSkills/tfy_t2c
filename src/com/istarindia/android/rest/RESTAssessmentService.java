package com.istarindia.android.rest;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.QuestionResponsePOJO;
import com.istarindia.android.utility.AppContentServiceUtility;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.apps.services.StudentAssessmentServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

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
	
	@POST
	@Path("{assessmentId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response submitUserAssessmentResponse(@PathParam("userId") int istarUserId, @PathParam("assessmentId") int assessmentId, 
			List<QuestionResponsePOJO> questionResponses){
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);
				
		AppContentServiceUtility appContentServiceUtility = new AppContentServiceUtility();
		Assessment assessment = appContentServiceUtility.getAssessment(assessmentId);
		
		StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
		
		for(QuestionResponsePOJO questionResponsePOJO : questionResponses){			
			Question question = appContentServiceUtility.getQuestion(questionResponsePOJO.getQuestionId());
			
			HashMap<String, Boolean> optionsMap = appContentServiceUtility.getAnsweredOptionsMap(question, questionResponsePOJO.getOptions());
						
			studentAssessmentServices.createStudentAssessment(assessment, question, istarUser, optionsMap.get("isCorrect"), 
					optionsMap.get("option0"), optionsMap.get("option1"), optionsMap.get("option2"), optionsMap.get("option3"), optionsMap.get("option4"), 
					null, null, null, questionResponsePOJO.getDuration());			
		}		
		return Response.status(Response.Status.CREATED).build();
	}
}
