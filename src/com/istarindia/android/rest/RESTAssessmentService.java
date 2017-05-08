package com.istarindia.android.rest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.AssessmentReportPOJO;
import com.istarindia.android.pojo.AssessmentResponsePOJO;
import com.istarindia.android.pojo.QuestionResponsePOJO;
import com.istarindia.android.utility.AppContentServiceUtility;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.apps.services.AppAssessmentServices;
import com.istarindia.apps.services.AppBatchStudentsServices;
import com.istarindia.apps.services.ReportServices;
import com.istarindia.apps.services.StudentAssessmentServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.BatchGroup;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.Report;
import com.viksitpro.core.dao.entities.StudentAssessment;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

@Path("assessments/user/{userId}")
public class RESTAssessmentService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAssessments(@PathParam("userId") int userId) {

		try {
			List<AssessmentPOJO> allAssessmentsOfUser = new ArrayList<AssessmentPOJO>();

			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			TaskServices taskServices = new TaskServices();
			List<Task> allTaskOfUser = taskServices.getAllTaskOfActor(istarUser);

			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();

			for (Task task : allTaskOfUser) {
				if (task.getIsActive() && task.getItemType().equals("ASSESSMENT")) {
					Assessment assessment = appAssessmentServices.getAssessment(task.getItemId());
					if (assessment != null && assessment.getAssessmentQuestions().size() > 0) {
						AssessmentPOJO assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);
						allAssessmentsOfUser.add(assessmentPOJO);
					}
				}
			}
			Gson gson = new Gson();
			String result = gson.toJson(allAssessmentsOfUser);
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("{assessmentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssessment(@PathParam("assessmentId") int assessmentId) {

		try {
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			Assessment assessment = appAssessmentServices.getAssessment(assessmentId);
			AssessmentPOJO assessmentPOJO = null;
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			if (assessment != null && assessment.getAssessmentQuestions().size() > 0) {
				assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);
			}

			Gson gson = new Gson();
			String result = gson.toJson(assessmentPOJO);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("{assessmentId}/{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response submitUserAssessmentResponse(@PathParam("userId") int istarUserId,
			@PathParam("assessmentId") int assessmentId, @PathParam("taskId") int taskId,
			@FormParam("response") String questionResponsesString) {
		try {
					
			System.out.println("questionResponsesString-->"+questionResponsesString);
			
			Type listType = new TypeToken<List<QuestionResponsePOJO>>() {}.getType();
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			
			List<QuestionResponsePOJO> questionResponses = (List<QuestionResponsePOJO>) gson.fromJson(questionResponsesString, listType);
			
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);

			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			Assessment assessment = appAssessmentServices.getAssessment(assessmentId);

			StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();

			AppBatchStudentsServices appBatchStudentsServices = new AppBatchStudentsServices();
			BatchGroup batchGroupOfStudent = appBatchStudentsServices.getBatchGroupOfStudent(istarUser.getId());

			Integer batchGroupId = null;

			if (batchGroupOfStudent != null) {
				batchGroupId = batchGroupOfStudent.getId();
			}

			AppContentServiceUtility appContentServiceUtility = new AppContentServiceUtility();

			int correctAnswersCount = 0;
			int assessmentDuration = 0;
			for (QuestionResponsePOJO questionResponsePOJO : questionResponses) {
				Question question = appContentServiceUtility.getQuestion(questionResponsePOJO.getQuestionId());

				HashMap<String, Boolean> optionsMap = appContentServiceUtility.getAnsweredOptionsMap(question,
						questionResponsePOJO.getOptions());

				StudentAssessment studentAssessment = studentAssessmentServices
						.getStudentAssessmentOfQuestionForUser(istarUserId, assessmentId, question.getId());

				if (studentAssessment != null) {
					studentAssessment = studentAssessmentServices.updateStudentAssessment(studentAssessment,
							optionsMap.get("isCorrect"), optionsMap.get("option0"), optionsMap.get("option1"),
							optionsMap.get("option2"), optionsMap.get("option3"), optionsMap.get("option4"), null, null,
							batchGroupId, questionResponsePOJO.getDuration());
				} else {
					studentAssessment = studentAssessmentServices.createStudentAssessment(assessment, question,
							istarUser, optionsMap.get("isCorrect"), optionsMap.get("option0"),
							optionsMap.get("option1"), optionsMap.get("option2"), optionsMap.get("option3"),
							optionsMap.get("option4"), null, null, batchGroupId, questionResponsePOJO.getDuration());
				}

				if (optionsMap.get("isCorrect")) {
					++correctAnswersCount;
				}
				if(questionResponsePOJO.getDuration()!=null){
					assessmentDuration = assessmentDuration + questionResponsePOJO.getDuration();
				}
			}

			Double maxPoints = appAssessmentServices.getMaxPointsOfAssessment(assessment.getId());

			ReportServices reportServices = new ReportServices();
			Report report = reportServices.getAssessmentReportForUser(istarUserId, assessmentId);

			if (report == null) {
				System.out.println("Report is null, creating new report");
				reportServices.createReport(istarUser, assessment, correctAnswersCount, assessmentDuration,
						maxPoints.intValue());
			} else {
				System.out.println("Report exists, updating report");
				reportServices.updateReport(report, istarUser, assessment, correctAnswersCount, assessmentDuration,
						maxPoints.intValue());
			}

			TaskServices taskServices = new TaskServices();
			taskServices.completeTask("COMPLETED", false, taskId, istarUser.getAuthToken());

			AssessmentReportPOJO assessmentReportPOJO = appAssessmentServices.getAssessmentReport(istarUserId, assessment.getId());
			Gson gsonResult = new Gson();
			String result = gsonResult.toJson(assessmentReportPOJO);

			return Response.ok(result).build();
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
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			AssessmentResponsePOJO response = appAssessmentServices.getAssessmentResponseOfUser(assessmentId, userId);

			Gson gson = new Gson();
			String result = gson.toJson(response);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("results")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserResponseOfAllAssessment(@PathParam("userId") int userId) {

		try {
			List<AssessmentResponsePOJO> allResponse = new ArrayList<AssessmentResponsePOJO>();

			StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
			List<Integer> allAssessmentIds = studentAssessmentServices.getAllAssessmentsAttemptedByUser(userId);

			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();

			for (Integer assessmentId : allAssessmentIds) {
				AssessmentResponsePOJO response = appAssessmentServices.getAssessmentResponseOfUser(assessmentId,
						userId);
				if (response != null) {
					allResponse.add(response);
				}
			}
			Gson gson = new Gson();
			String result = gson.toJson(allResponse);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("{assessmentId}/report")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssessmentReportOfUser(@PathParam("userId") int userId,
			@PathParam("assessmentId") int assessmentId) {

		try {
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			AssessmentReportPOJO assessmentReportPOJO = appAssessmentServices.getAssessmentReport(userId, assessmentId);
			Gson gson = new Gson();
			String result = gson.toJson(assessmentReportPOJO);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("reports")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAssessmentReportOfUser(@PathParam("userId") int userId) {

		try {
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			List<AssessmentReportPOJO> allAssessmentReport = appAssessmentServices
					.getAllAssessmentReportsOfUser(userId);

			Gson gson = new Gson();
			String result = gson.toJson(allAssessmentReport);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
