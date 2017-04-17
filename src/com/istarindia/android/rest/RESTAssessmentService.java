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
import com.istarindia.android.utility.AppUserRankUtility;
import com.istarindia.apps.services.AppAssessmentServices;
import com.istarindia.apps.services.StudentAssessmentServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.BatchGroup;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

@Path("assessments/user/{userId}")
public class RESTAssessmentService {

	@GET
	@Path("{assessmentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssessment(@PathParam("assessmentId") int assessmentId) {

		try {
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			Assessment assessment = appAssessmentServices.getAssessment(assessmentId);

			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			AssessmentPOJO assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);

			Gson gson = new Gson();
			String result = gson.toJson(assessmentPOJO);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("{assessmentId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response submitUserAssessmentResponse(@PathParam("userId") int istarUserId,
			@PathParam("assessmentId") int assessmentId, List<QuestionResponsePOJO> questionResponses) {
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);
			
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			Assessment assessment = appAssessmentServices.getAssessment(assessmentId);

			StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
			AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
			BatchGroup batchGroupOfStudent = appUserRankUtility.getBatchGroupOfUser(istarUser.getId());
			
			Integer batchGroupId = null;
			
			if(batchGroupOfStudent!=null){
				batchGroupId = batchGroupOfStudent.getId();
			}
			
			AppContentServiceUtility appContentServiceUtility = new AppContentServiceUtility();
			
			for (QuestionResponsePOJO questionResponsePOJO : questionResponses) {
				Question question = appContentServiceUtility.getQuestion(questionResponsePOJO.getQuestionId());

				HashMap<String, Boolean> optionsMap = appContentServiceUtility.getAnsweredOptionsMap(question,
						questionResponsePOJO.getOptions());

				//check if the user has already attempted an assessment, if true-->update the fields else create 
				
				studentAssessmentServices.createStudentAssessment(assessment, question, istarUser,
						optionsMap.get("isCorrect"), optionsMap.get("option0"), optionsMap.get("option1"),
						optionsMap.get("option2"), optionsMap.get("option3"), optionsMap.get("option4"), null, null,
						batchGroupId, questionResponsePOJO.getDuration());
			}
			return Response.status(Response.Status.CREATED).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("{assessmentId}/report")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssessmentReportOfUser(@PathParam("userId") int userId, @PathParam("assessmentId") int assessmentId){
		
		AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
		
		Gson gson = new Gson();
		String result = gson.toJson(appAssessmentServices.getAssessmentReport(userId, assessmentId));

		return Response.ok(result).build();
		
		//Calculate Score of the student for assessment--> score/accuracy/batch average
		//get details of skill breakdown--> Points for each skill from AssessmentBechmark and get parent of that skill
		//Get the skill from question_skill_Objective and find its parent
	}
}
