package com.istarindia.android.rest;

import java.util.ArrayList;
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
import com.istarindia.android.pojo.AssessmentReportPOJO;
import com.istarindia.android.pojo.QuestionResponsePOJO;
import com.istarindia.android.utility.AppContentServiceUtility;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.apps.services.AppAssessmentServices;
import com.istarindia.apps.services.AppBatchStudentsServices;
import com.istarindia.apps.services.StudentAssessmentServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentOption;
import com.viksitpro.core.dao.entities.BatchGroup;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.StudentAssessment;
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

			AppBatchStudentsServices appBatchStudentsServices = new AppBatchStudentsServices();
			BatchGroup batchGroupOfStudent = appBatchStudentsServices.getBatchGroupOfStudent(istarUser.getId());
			
			Integer batchGroupId = null;
			
			if(batchGroupOfStudent!=null){
				batchGroupId = batchGroupOfStudent.getId();
			}
			
			AppContentServiceUtility appContentServiceUtility = new AppContentServiceUtility();
			
			for (QuestionResponsePOJO questionResponsePOJO : questionResponses) {
				Question question = appContentServiceUtility.getQuestion(questionResponsePOJO.getQuestionId());

				HashMap<String, Boolean> optionsMap = appContentServiceUtility.getAnsweredOptionsMap(question,
						questionResponsePOJO.getOptions());

				StudentAssessment studentAssessment = studentAssessmentServices.getStudentAssessmentOfQuestionForUser(istarUserId, assessmentId, question.getId());
				//check if the user has already attempted an assessment, if true-->update the fields else create 
				if(studentAssessment!=null){
					studentAssessment = studentAssessmentServices.updateStudentAssessment(studentAssessment,
							optionsMap.get("isCorrect"), optionsMap.get("option0"), optionsMap.get("option1"),
							optionsMap.get("option2"), optionsMap.get("option3"), optionsMap.get("option4"), null, null,
							batchGroupId, questionResponsePOJO.getDuration());
				}else{
					studentAssessment = studentAssessmentServices.createStudentAssessment(assessment, question, istarUser,
							optionsMap.get("isCorrect"), optionsMap.get("option0"), optionsMap.get("option1"),
							optionsMap.get("option2"), optionsMap.get("option3"), optionsMap.get("option4"), null, null,
							batchGroupId, questionResponsePOJO.getDuration());
				}
			}
			return Response.status(Response.Status.CREATED).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("{assessmentId}/result")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserResponseOfAssessment(@PathParam("userId") int userId,
			@PathParam("assessmentId") int assessmentId) {

		try {
			StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
			List<StudentAssessment> allStudentAssessments = studentAssessmentServices
					.getStudentAssessmentForUser(userId, assessmentId);
			List<QuestionResponsePOJO> allQuestionsResponse = new ArrayList<QuestionResponsePOJO>();

			for (StudentAssessment studentAssessment : allStudentAssessments) {
				QuestionResponsePOJO questionResponsePOJO = new QuestionResponsePOJO();
				List<Integer> markedOptions = new ArrayList<Integer>();
				questionResponsePOJO.setQuestionId(studentAssessment.getQuestion().getId());

				List<AssessmentOption> allOptionsOfQuestion = new ArrayList<AssessmentOption>(
						studentAssessment.getQuestion().getAssessmentOptions());

				for (int i = 0; i < 5; i++) {
					if (i == 0 && studentAssessment.getOption1()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}

					if (i == 1 && studentAssessment.getOption2()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}

					if (i == 2 && studentAssessment.getOption3()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}

					if (i == 3 && studentAssessment.getOption4()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}

					if (i == 4 && studentAssessment.getOption5()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}
				}
				questionResponsePOJO.setOptions(markedOptions);
				questionResponsePOJO.setDuration(studentAssessment.getTimeTaken());
				allQuestionsResponse.add(questionResponsePOJO);
			}
			Gson gson = new Gson();
			String result = gson.toJson(allQuestionsResponse);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("{assessmentId}/report")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssessmentReportOfUser(@PathParam("userId") int userId, @PathParam("assessmentId") int assessmentId){
		
		try{
		AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
		AssessmentReportPOJO assessmentReportPOJO = appAssessmentServices.getAssessmentReport(userId, assessmentId);
		Gson gson = new Gson();
		String result = gson.toJson(assessmentReportPOJO);

		return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
