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

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.AssessmentReportPOJO;
import com.istarindia.android.pojo.AssessmentResponsePOJO;
import com.istarindia.android.pojo.ComplexObject;
import com.istarindia.android.pojo.QuestionResponsePOJO;
import com.istarindia.android.utility.AppContentServiceUtility;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.apps.services.AppAssessmentServices;
import com.istarindia.apps.services.AppComplexObjectServices;
import com.istarindia.apps.services.GamificationServices;
import com.istarindia.apps.services.ReportServices;
import com.istarindia.apps.services.StudentAssessmentServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentDAO;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.IstarUserDAO;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.Report;
import com.viksitpro.core.dao.entities.StudentAssessment;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;

@Path("assessments/user/{userId}")
public class RESTAssessmentService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAssessments(@PathParam("userId") int userId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			List<AssessmentPOJO> allAssessmentsOfUser = new ArrayList<AssessmentPOJO>();

			IstarUser istarUser = new IstarUserDAO().findById(userId);

			String hql = "from Task task where actor= :actor and item_type='ASSESSMENT'";
			
			BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
			Session session = baseHibernateDAO.getSession();
			
			Query query = session.createQuery(hql);
			query.setParameter("actor", istarUser.getId());
			
			List<Task> allTaskOfUser = query.list();


			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();

			for (Task task : allTaskOfUser) {
				Assessment assessment = appAssessmentServices.getAssessment(task.getItemId());
				if (assessment != null && assessment.getAssessmentQuestions()!=null && assessment.getAssessmentQuestions().size() > 0) {
					AssessmentPOJO assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);
					allAssessmentsOfUser.add(assessmentPOJO);
				}
			}

			String result = gson.toJson(allAssessmentsOfUser);
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("{assessmentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssessment(@PathParam("assessmentId") int assessmentId) {
	
		////System.err.println("--------------------------------------------------------I LOVE KAMINI");
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			Assessment assessment = new AssessmentDAO().findById(assessmentId);
			AssessmentPOJO assessmentPOJO = null;
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			if (assessment != null && assessment.getAssessmentQuestions().size() > 0) {
				assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);
			}
			String result = gson.toJson(assessmentPOJO);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("{assessmentId}/{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response submitUserAssessmentResponse(@PathParam("userId") int istarUserId,
			@PathParam("assessmentId") int assessmentId, @PathParam("taskId") int taskId,
			@FormParam("response") String questionResponsesString) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			//System.out.println("questionResponsesString-->" + questionResponsesString);

			Type listType = new TypeToken<List<QuestionResponsePOJO>>() {
			}.getType();
			Gson gsonRequest = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

			List<QuestionResponsePOJO> questionResponses = (List<QuestionResponsePOJO>) gsonRequest
					.fromJson(questionResponsesString, listType);

			IstarUser istarUser = new IstarUserDAO().findById(istarUserId);

			Assessment assessment = new AssessmentDAO().findById(assessmentId);
			
				StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
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
						if(assessment.getRetryAble()!=null && assessment.getRetryAble())
						{
							studentAssessment = studentAssessmentServices.updateStudentAssessment(studentAssessment,
									optionsMap.get("isCorrect"), optionsMap.get("option0"), optionsMap.get("option1"),
									optionsMap.get("option2"), optionsMap.get("option3"), optionsMap.get("option4"), null, null,
									null, questionResponsePOJO.getDuration());
						}
						
					} else {
						studentAssessment = studentAssessmentServices.createStudentAssessment(assessment, question,
								istarUser, optionsMap.get("isCorrect"), optionsMap.get("option0"),
								optionsMap.get("option1"), optionsMap.get("option2"), optionsMap.get("option3"),
								optionsMap.get("option4"), null, null, null, questionResponsePOJO.getDuration());
					}

					if (optionsMap.get("isCorrect")) {
						++correctAnswersCount;
					}
					if (questionResponsePOJO.getDuration() != null) {
						assessmentDuration = assessmentDuration + questionResponsePOJO.getDuration();
					}
				}

				

				ReportServices reportServices = new ReportServices();
				Report report = reportServices.getAssessmentReportForUser(istarUserId, assessmentId);
				GamificationServices gamificationService = new GamificationServices();
				if (report == null) {
					reportServices.createReport(istarUser, assessment, correctAnswersCount, assessmentDuration,0);
					gamificationService.updateUserGamificationAfterAssessment(istarUser,assessment);
					gamificationService.updateUserPointsCoinsStatsTable(istarUserId);
					gamificationService.updateLeaderBoard(istarUserId);
					gamificationService.updateUserAssessmentPointsCoinsTable(istarUserId, assessmentId);
				} else {
					//System.err.println("Report exists, updating report");
					if(assessment.getRetryAble()!=null && assessment.getRetryAble())
					{
						reportServices.updateReport(report, istarUser, assessment, correctAnswersCount, assessmentDuration,0);
						gamificationService.updateUserGamificationAfterAssessment(istarUser,assessment);
						gamificationService.updateUserPointsCoinsStatsTable(istarUserId);
						gamificationService.updateLeaderBoard(istarUserId);
						gamificationService.updateUserAssessmentPointsCoinsTable(istarUserId, assessmentId);
					}	
				}

				TaskServices taskServices = new TaskServices();
				taskServices.completeTask("COMPLETED", false, taskId, istarUser.getAuthToken());

			AppComplexObjectServices appComplexObjectServices = new AppComplexObjectServices();
			ComplexObject complexObject = appComplexObjectServices.getComplexObjectForUser(istarUserId);

			if (complexObject == null) {
				throw new Exception();
			}
			String result = gson.toJson(complexObject);
			
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("{assessmentId}/result")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserResponseOfAssessment(@PathParam("userId") int userId,
			@PathParam("assessmentId") int assessmentId) {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			AssessmentResponsePOJO response = appAssessmentServices.getAssessmentResponseOfUser(assessmentId, userId);

			String result = gson.toJson(response);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("results")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserResponseOfAllAssessment(@PathParam("userId") int userId) {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
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
			String result = gson.toJson(allResponse);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("{assessmentId}/report")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAssessmentReportOfUser(@PathParam("userId") int userId,
			@PathParam("assessmentId") int assessmentId) {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			AssessmentReportPOJO assessmentReportPOJO = appAssessmentServices.getAssessmentReportNew(userId, assessmentId);
			String result = gson.toJson(assessmentReportPOJO);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("reports")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAssessmentReportOfUser(@PathParam("userId") int userId) {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			List<AssessmentReportPOJO> allAssessmentReport = appAssessmentServices
					.getAllAssessmentReportsOfUser(userId);

			String result = gson.toJson(allAssessmentReport);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
}
