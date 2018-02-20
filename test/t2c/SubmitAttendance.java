package t2c;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.istarindia.android.pojo.trainerworkflow.ClassFeedbackByTrainer;
import com.istarindia.android.pojo.trainerworkflow.FeedbackPojo;
import com.istarindia.apps.services.TrainerWorkflowServices;
import com.viksitpro.core.utilities.DBUTILS;

public class SubmitAttendance {

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		
		submitFeedback(271);
		
	}

	private static void submitFeedback(int orgId) {
		// TODO Auto-generated method stub
		DBUTILS util = new DBUTILS();
		String getTaskBeforeToday ="select * from task where item_id in (select id from batch_schedule_event where type = 'BATCH_SCHEDULE_EVENT_TRAINER' and batch_group_id in (select id from batch_group where college_id = "+orgId+") and eventdate <  '2017-07-28 09:32:00' ) and item_type = 'CLASSROOM_SESSION'";
		List<HashMap<String, Object>> events = util.executeQuery(getTaskBeforeToday);
		ClassFeedbackByTrainer feedbackResponse = new ClassFeedbackByTrainer();
		TrainerWorkflowServices serv = new TrainerWorkflowServices();
		for(HashMap<String, Object> row: events)
		{
			
			int task_id = (int)row.get("id");
			int actor = (int)row.get("actor");
			ArrayList<FeedbackPojo> feedback = new ArrayList<>();
			feedback.add(new FeedbackPojo("noise",randomNumber()));
			feedback.add(new FeedbackPojo("attendance",randomNumber()));
			feedback.add(new FeedbackPojo("content",randomNumber()));
			feedback.add(new FeedbackPojo("sick",randomNumber()));
			feedback.add(new FeedbackPojo("assignment",randomNumber()));
			feedback.add(new FeedbackPojo("internals",randomNumber()));
			feedback.add(new FeedbackPojo("internet",randomNumber()));
			feedback.add(new FeedbackPojo("electricity",randomNumber()));
			feedback.add(new FeedbackPojo("time",randomNumber()));
			feedback.add(new FeedbackPojo("projector",randomNumber()));
			feedback.add(new FeedbackPojo("comment","Please Give Your FeedBack!!!"));
			feedbackResponse.setFeedbacks(feedback);
			
			submitFeedbackByTrainer(task_id, actor, feedbackResponse);
			
		}
	}

	public static String randomNumber() {
		float leftLimit = 0F;
	    float rightLimit = 5F;
	    float generatedFloat = leftLimit + new Random().nextFloat() * (rightLimit - leftLimit);
	    DecimalFormat df = new DecimalFormat();
	    df.setMaximumFractionDigits(1);
		return df.format(generatedFloat);
	}

	public static void submitFeedbackByTrainer(int taskId, int istarUserId, ClassFeedbackByTrainer feedbackResponse) {
		
		HashMap<String, String> feedbackData= new HashMap<>(); 
		feedbackData.put("", "0");
		for(FeedbackPojo pojo : feedbackResponse.getFeedbacks())
		{
			feedbackData.put(pojo.getName().toLowerCase(), pojo.getRating());
		}
		DBUTILS util = new DBUTILS();
		String checkIfExist ="delete from trainer_feedback where event_id = (select item_id from task where id = "+taskId+")";
		util.executeUpdate(checkIfExist);
		float avgRating = 5;
		float totalRating = Float.parseFloat(feedbackData.get("noise")) +Float.parseFloat(feedbackData.get("attendance")) +Float.parseFloat(feedbackData.get("sick")) +Float.parseFloat(feedbackData.get("content")) +Float.parseFloat(feedbackData.get("assignment")) +Float.parseFloat(feedbackData.get("internals")) +Float.parseFloat(feedbackData.get("internet")) +Float.parseFloat(feedbackData.get("electricity")) +Float.parseFloat(feedbackData.get("time"));
			avgRating = totalRating/9;
		String insertFeedback="INSERT INTO trainer_feedback (id, user_id, rating, comments, event_id, noise, attendance, sick, content, assignment, internals, internet, electricity, time, projector) "
				+ "VALUES ((select COALESCE(max(id),0)+1 from trainer_feedback), "+istarUserId+","
						+ " "+avgRating+", "
								+ "'"+feedbackData.get("comment")+"',"
										+ " ( select item_id from task where id="+taskId+"),"
												+ " "+Float.parseFloat(feedbackData.get("noise"))+","
														+ " "+Float.parseFloat(feedbackData.get("attendance"))+","
																+ " "+Float.parseFloat(feedbackData.get("sick"))+","
																		+ " "+Float.parseFloat(feedbackData.get("content"))+","
																				+ " "+Float.parseFloat(feedbackData.get("assignment"))+","
																						+ " "+Float.parseFloat(feedbackData.get("internals"))+", "
																								+ ""+Float.parseFloat(feedbackData.get("internet"))+","
																										+ " "+Float.parseFloat(feedbackData.get("electricity"))+", "
																												+ ""+Float.parseFloat(feedbackData.get("time"))+","
																														+ ""+Float.parseFloat(feedbackData.get("projector"))+");";
		util.executeUpdate(insertFeedback);
		
		
		
		String updateTaskAsCompleted = "update task set is_active = 'f' where id="+taskId;
		util.executeUpdate(updateTaskAsCompleted);
		
		
	}

}
