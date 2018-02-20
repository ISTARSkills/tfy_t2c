package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.AppActivityPOJO;
import com.istarindia.android.pojo.AppInterviewPOJO;
import com.istarindia.android.pojo.AppJobPOJO;
import com.istarindia.android.pojo.AppJobStagePOJO;
import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.utility.AppPOJOUtility;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.IstarNotification;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Job;
import com.viksitpro.core.dao.entities.JobDAO;
import com.viksitpro.core.dao.entities.PMStage;
import com.viksitpro.core.dao.entities.PmStageAction;
import com.viksitpro.core.dao.entities.PmWorkflow;
import com.viksitpro.core.dao.entities.StageLog;
import com.viksitpro.core.dao.entities.StageLogDAO;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.notification.IstarNotificationServices;

public class AppJobServices {

	public List<AppJobPOJO> getAllJobsPOJOOfUser(int istarUserId){
		
		List<AppJobPOJO> allJobsPOJO = new ArrayList<AppJobPOJO>();
		
		List<Integer> allTasks = getAllJobTasksOfUser(istarUserId);
		
		for(Integer taskId: allTasks){
			AppJobPOJO appJobPOJO = getJobPOJOOfUser(taskId);			
			if(appJobPOJO!=null){
				allJobsPOJO.add(appJobPOJO);
			}
		}
		return allJobsPOJO;
	}
	
	
	public AppJobPOJO getJobPOJOOfUser(int taskId) {

		AppJobPOJO appJobPOJO = null;

		TaskServices taskServices = new TaskServices();
		Task task = taskServices.getTask(taskId);
		if (task != null && task.getItemType().equals("JOB STUDENT")) {
			Job job = getJob(task.getItemId());

			if (job != null) {
				appJobPOJO = new AppJobPOJO();

				appJobPOJO.setId(job.getId());
				appJobPOJO.setTaskId(task.getId());
				appJobPOJO.setJobName(job.getTitle());
				appJobPOJO.setJobDescription(job.getDescription());
				appJobPOJO.setCompanyLogo(job.getOrganization().getImage());
				appJobPOJO.setCompanyName(job.getOrganization().getName());

				String salaryRange = job.getMinimumSalary() + " - " + job.getMaximumSalary() + " lacs per annum";
				String maximumSalary = job.getMaximumSalary() + " lacs";
				String minimumSalary = job.getMinimumSalary() + " lacs";

				appJobPOJO.setSalaryRange(salaryRange);
				appJobPOJO.setMaximumSalary(maximumSalary);
				appJobPOJO.setMinimumSalary(minimumSalary);
				appJobPOJO.setJobLocation(convertStringToArrayList(job.getLocation(), "#!"));

				appJobPOJO.setStartDate(task.getStartDate());
				appJobPOJO.setEndDate(task.getEndDate());
				appJobPOJO.setStage(task.getState());

				HashMap<String, String> tagAndType = getTagForJobFromLastStageLog(taskId, task.getIstarUserByActor().getId());
				
				String tag = tagAndType.get("tag");
				String type = tagAndType.get("type");
				
				appJobPOJO.setTag(tag);
				appJobPOJO.setType(type);
				
				appJobPOJO.setActivities(getAllJobActivtiy(taskId));
			}
		}
		return appJobPOJO;
	}
	
	
	public Object getTaskOfJobForUser(int taskId){
		
		TaskServices taskServices = new TaskServices();
		Task task = taskServices.getTask(taskId);
		
		StageLog stageLog = getLatestStageLogOfJob(taskId);
		
		PmWorkflow pmWorkflow = task.getTaskType().getWorkflow();
		
		
		List<String> actions = new ArrayList<String>();
		//ViksitLogger.logMSG(this.getClass().getName(),stageLog.getStageType()+"--->"+stageLog.getStageName());
		for(PMStage pmStage: pmWorkflow.getStages()){
			//ViksitLogger.logMSG(this.getClass().getName(),pmStage.getName()+" are equal -->"+ pmStage.getType());
			if(pmStage.getName().equals(stageLog.getStageName()) && pmStage.getType().equals(stageLog.getStageType())){
				//ViksitLogger.logMSG(this.getClass().getName(),"are equal");
				List<PmStageAction> allStageActions = pmStage.getStageActions();				
				for(PmStageAction stageAction : allStageActions){
					//ViksitLogger.logMSG(this.getClass().getName(),stageAction.getActionDetail());
					if(stageLog.getStageType().equals("test")){
						actions.add(stageAction.getActionDetail());
					}	
				}
				break;
			}
		}
		
		if(stageLog.getStageType().equals("test")){
			try{
				Integer assessmentId = Integer.parseInt(actions.get(0));

				AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
				Assessment assessment = appAssessmentServices.getAssessment(assessmentId);
				AssessmentPOJO assessmentPOJO = null;
				AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
				if (assessment != null && assessment.getAssessmentQuestions().size() > 0) {
					assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);
				}
				return assessmentPOJO;
				}catch(NumberFormatException e){
					return null;
				}
		}else if(stageLog.getStageType().equals("interview")){
			try{
				AppInterviewPOJO appInterviewPOJO = new AppInterviewPOJO();
				
				appInterviewPOJO.setId(stageLog.getId());
				appInterviewPOJO.setStatus(stageLog.getStatus());
				appInterviewPOJO.setInterviewURL(stageLog.getUrl1());
				appInterviewPOJO.setMeetingId(stageLog.getActionId());
				appInterviewPOJO.setMeetingPassword(stageLog.getActionPassword());
				appInterviewPOJO.setStageName(stageLog.getStageName());
				appInterviewPOJO.setTaskId(stageLog.getTask().getId());
				
				return appInterviewPOJO;
			}catch(Exception e){
				return null;
			}
		}else{
			try{
				AppJobStagePOJO appJobStagePOJO = new AppJobStagePOJO();
				
				appJobStagePOJO.setId(stageLog.getId());
				appJobStagePOJO.setTaskId(stageLog.getTask().getId());
				appJobStagePOJO.setStatus(stageLog.getStatus());
				appJobStagePOJO.setType(stageLog.getStageType());
				
				String description = "No description avaialble for this task. A recruiter will contact you for the details of this round.";
				
				if(stageLog.getTask().getTaskType()!=null && stageLog.getTask().getTaskType().getWorkflow()!=null && stageLog.getTask().getTaskType().getWorkflow().getStages().size()>0){
					List<PMStage> allPmStage = stageLog.getTask().getTaskType().getWorkflow().getStages();
					
					for(PMStage pmStage: allPmStage){
						if(pmStage.getName().equals(stageLog.getStageName()) && pmStage.getType().equals(stageLog.getStageType())){
							description = pmStage.getDescription();
						}
					}
				}
				
				appJobStagePOJO.setDescription(description);
				
			}catch(Exception e){
				return null;
			}
		}	
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public StageLog getLatestStageLogOfJob(int taskId){
		
		StageLog stageLog = null;
		TaskServices taskServices = new TaskServices();
		Task task = taskServices.getTask(taskId);
		
		if(task!=null){
		String sql = "select stage_log.id from task inner join stage_log on stage_log.task=task.id and stage_log.istar_user=task.actor and stage_log.stage_name=task.state where stage_log.task="+task.getId()+" and task.item_id="+task.getItemId()+" and task.item_type='JOB STUDENT' and stage_log.istar_user="+task.getIstarUserByActor().getId();
	
		//ViksitLogger.logMSG(this.getClass().getName(),sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		
		List<Integer> result = query.list();

		if(result.size()>0){
			stageLog = getStageLog(result.get(0));
		}
		
		}
		return stageLog;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<AppActivityPOJO> getAllJobActivtiy(int taskId){
		
		List<AppActivityPOJO> allActivities = new ArrayList<AppActivityPOJO>();
		
		String sql = "select id from istar_notification where task_id="+taskId;
		
		//ViksitLogger.logMSG(this.getClass().getName(),sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		
		List<Integer> result = query.list();
		
		if(result.size()>0){
			IstarNotificationServices istarNotificationServices = new IstarNotificationServices();
			IstarUserServices istarUserServices = new IstarUserServices();
			for(Integer istarNotificationId: result){
				IstarNotification notification = istarNotificationServices.getIstarNotification(istarNotificationId);
				if(notification!=null){
					AppActivityPOJO appActivityPOJO = new AppActivityPOJO();
					
					String message = notification.getTitle();
					
					IstarUser sender = istarUserServices.getIstarUser(notification.getSenderId());
					String name;
					String image;
					if(sender.getUserProfile()!=null){
						name = sender.getUserProfile().getFirstName();
						image = getMediaURLPath()+sender.getUserProfile().getImage();
					}else{
						name = sender.getEmail();
						image = getMediaURLPath()+sender.getEmail().trim().substring(0, 1).toUpperCase() + ".png";
					}
					String type = "Recruiter";
					
					appActivityPOJO.setId(notification.getId());
					appActivityPOJO.setIstarUserId(sender.getId());
					appActivityPOJO.setUserName(name);
					appActivityPOJO.setUserImage(image);
					appActivityPOJO.setType(type);
					appActivityPOJO.setMessage(message);
					appActivityPOJO.setTime(notification.getCreatedAt());
					appActivityPOJO.setUserEmail(sender.getEmail());
					appActivityPOJO.setUserPhone(sender.getMobile()+"");
					
					allActivities.add(appActivityPOJO);
				}
			}			
		}
		return allActivities;
	}
	
	public String getMediaURLPath() {
		String mediaPath = null;
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mediaPath;
	}
	
	public HashMap<String, String> getTagForJobFromLastStageLog(int taskId, int istarUserId){
		
		HashMap<String, String> tagAndType = new HashMap<String, String>();
		List<Integer> allStageLogs = getAllStageLogsOfTasks(taskId, istarUserId);
		
		
		if(allStageLogs.size()>1){
			String tag = null;
			String type= null;
			StageLog stageLog = getStageLog(allStageLogs.get(0));
			
			String stageType = stageLog.getStageType();
			String stageName = stageLog.getStageName();
			String stageStatus = stageLog.getStatus();
			
			switch(stageType){
			case "DEFAULT":
				switch (stageName) {
				case "INVITED":
					tag = "Invited";
					type= "Invited";
					break;
				case "APPLIED":
					switch (stageStatus) {
					case "ACCEPTED":
						tag = "Waiting";
						break;
					case "APPLIED":
						tag = "Waiting";
						break;
					}
					type= "Applied";
					break;
				case "OFFERED":
					switch (stageStatus) {
					case "NOT ACCEPTED":
						tag = "Waiting";
						type= "Offered";
						break;
					case "WAITING FOR ACCEPTANCE":
						//ViksitLogger.logMSG(this.getClass().getName(),"WAITING FOR ACCEPTANCE");
						tag = "Pending";
						type= "Offered";
						break;
					case "ACCPETED":
						//ViksitLogger.logMSG(this.getClass().getName(),"ACCPETED");
						tag = "Job Offered";
						type= "Offered";
						break;
					}
					type= "Offered";
					break;
				case "REJECTED":
					tag = "Rejected";
					type= "Rejected";
					break;
				}
				break;

			case "test":
				switch (stageStatus) {
				case "NOT ATTEMPTED":
					tag = "Pending";
					break;
				case "ATTEMPTED":
					tag = "Waiting";
					break;
				}
				type= "Test";
				break;
			case "externalAssessment":
				switch (stageStatus) {
				case "NOT ATTEMPTED":
					tag = "Pending";
					break;
				case "ATTEMPTED":
					tag = "Waiting";
					break;
				}
				type= "Test";
				break;
			case "interview":
				switch (stageStatus) {
				case "NOT SCHEDULED":
					tag = "Waiting";
					break;
				case "SCHEDULED":
					tag = "Pending";
					break;
				case "ATTENDED":
					tag = "Waiting";
					break;
				}
				type= "Interview";
				break;
			case "OTHERS":
				switch (stageStatus) {
				case "NOT ATTEMPTED":
					tag = "Pending";
					break;
				case "ATTEMPTED":
					tag = "Waiting";
					break;
				}
				type= "Activity";
				break;
			}
			tagAndType.put("tag",tag);
			tagAndType.put("type", type);
		}
		return tagAndType;
	}
	
	
	public Job getJob(int jobId) {
		Job job;
		JobDAO jobDAO = new JobDAO();
		try {
			job = jobDAO.findById(jobId);
		} catch (IllegalArgumentException e) {
			job = null;
		}
		return job;
	}
	
	public StageLog getStageLog(int stageLogId) {
		StageLog stageLog;
		StageLogDAO stageLogDAO = new StageLogDAO();
		try {
			stageLog = stageLogDAO.findById(stageLogId);
		} catch (IllegalArgumentException e) {
			stageLog = null;
		}
		return stageLog;
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getAllStageLogsOfTasks(int taskId, int istarUserId){
		
		String sql = "select id from stage_log where task="+taskId+" and istar_user="+istarUserId+" order by created_at desc";
		
		//ViksitLogger.logMSG(this.getClass().getName(),sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		
		List<Integer> result = query.list();
	
		return result;
	} 
	
	@SuppressWarnings("unchecked")
	public List<Integer> getAllJobTasksOfUser(int istarUserId){
		
		String sql = "select id from task where actor="+istarUserId+" and item_type='JOB STUDENT'";
		//ViksitLogger.logMSG(this.getClass().getName(),sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		
		List<Integer> result = query.list();
	
		return result;
	}
	
	public static ArrayList<String> convertStringToArrayList(String input, String itemSeparator) {

		ArrayList<String> arrayList = new ArrayList<String>();

		if (input!=null && input.contains(itemSeparator)) {
			String[] itemArray = input.split(itemSeparator);

			for (String item : itemArray) {
				arrayList.add(item);
			}
		} else {
			//ViksitLogger.logMSG(this.getClass().getName(),"The specified itemSeparator does not exists in the input string"
					//+ "\n The input will be converted to ArrayList of size 0");
		}
		return arrayList;
	}
}
