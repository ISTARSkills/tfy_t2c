/**
 * 
 */
package com.istarindia.apps.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.istarindia.android.pojo.trainerworkflow.ClassFeedbackByTrainer;
import com.istarindia.android.pojo.trainerworkflow.CourseContent;
import com.istarindia.android.pojo.trainerworkflow.CourseItem;
import com.istarindia.android.pojo.trainerworkflow.FeedbackPojo;
import com.istarindia.android.pojo.trainerworkflow.GroupPojo;
import com.istarindia.android.pojo.trainerworkflow.GroupStudentPojo;
import com.istarindia.android.utility.CreateZIPForItem;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.logger.ViksitLogger;
import com.viksitpro.core.utilities.AppProperies;
import com.viksitpro.core.utilities.DBUTILS;
import com.viksitpro.core.utilities.TaskItemCategory;

/**
 * @author mayank
 *
 */
public class TrainerWorkflowServices {

	public ArrayList<GroupStudentPojo> studentsInGroup(int groupId, int taskID) {
		String mediaUrlPath = AppProperies.getProperty("media_url_path");
		ArrayList<GroupStudentPojo> students = new ArrayList<>();
		DBUTILS utils = new DBUTILS();
		String sql = "SELECT 	distinct istar_user. ID, 	CASE WHEN ( 	user_profile.first_name IS NULL ) THEN 	istar_user.email ELSE 	user_profile.first_name END,  user_profile.profile_image,  CASE WHEN (attendance.status IS NULL) THEN 	'ABSENT' ELSE 	attendance.status END FROM "
				+ "	task LEFT JOIN batch_schedule_event ON ( 	task.item_id = batch_schedule_event. ID ) "
				+ "LEFT JOIN batch_students ON ( 	batch_students.batch_group_id = " + groupId + " ) "
				+ "LEFT JOIN istar_user ON ( 	batch_students.student_id = istar_user. ID )"
				+ " LEFT JOIN user_profile ON ( 	istar_user. ID = user_profile.user_id ) "
				+ "LEFT JOIN attendance ON ( 	attendance.event_id = batch_schedule_event. ID 	AND istar_user. ID = attendance.user_id )"
				+ " WHERE 	task. ID = " + taskID + " AND istar_user. ID NOTNULL";

		// ViksitLogger.logMSG(this.getClass().getName(),(sql);
		List<HashMap<String, Object>> studentData = utils.executeQuery(sql);
		for (HashMap<String, Object> row : studentData) {
			int studentId = (int) row.get("id");
			String name = row.get("first_name").toString();
			String profileImage = "/users/" + name.charAt(0) + ".png";
			if (row.get("profile_image") != null) {
				profileImage = row.get("profile_image").toString();
			}
			String status = row.get("status").toString();
			GroupStudentPojo stu = new GroupStudentPojo();
			stu.setImageUrl(mediaUrlPath + profileImage);
			stu.setStudentId(studentId);
			stu.setStudentName(name);
			if (status.equalsIgnoreCase("PRESENT")) {
				stu.setStatus(true);
			} else {
				stu.setStatus(false);
			}

			students.add(stu);
		}

		return students;
	}

	public void submitAttendance(int taskId, int istarUserId, GroupPojo attendanceResponse) {

		DBUTILS util = new DBUTILS();

		String deleteOldEntry = "delete from attendance where event_id = (select item_id from task where id = " + taskId
				+ ")";
		util.executeUpdate(deleteOldEntry);
		for (GroupStudentPojo stu : attendanceResponse.getStudents()) {
			String status = "ABSENT";
			if (stu.getStatus() != null && stu.getStatus()) {
				status = "PRESENT";
			} else {
				status = "ABSENT";
			}
			String insertIntoAttendance = "INSERT INTO attendance (id, taken_by, user_id, status, created_at, updated_at, event_id) "
					+ "VALUES ((select COALESCE(max(id),0)+1 from attendance), '" + istarUserId + "', '"
					+ stu.getStudentId() + "', '" + status + "', 'now()', 'now()', (select item_id from task where id="
					+ taskId + "));";
			util.executeUpdate(insertIntoAttendance);
		}

	}

	public CourseContent getCourseContent(Integer courseId, int taskId) {

		String mediaPath = "";
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

		String mediaLessonPath = AppProperies.getProperty("mediaLessonPath");

		DBUTILS util = new DBUTILS();
		CourseContent courseContent = new CourseContent();
		Integer prevItemOrderId = null;
		Integer curentItemOrderId = null;
		Integer nextItemOrderId = null;
		Integer currentItemId = null;
		Long currentItemSlideId = null;
		String getCurrentPrevAndNextItemForCourse = "select lesson_id , slide_id  as slide_id from slide_change_log , batch_schedule_event, task where task.item_id = batch_schedule_event.id and batch_schedule_event.course_id= slide_change_log.course_id and slide_change_log.batch_group_id = batch_schedule_event.batch_group_id and task.id = "
				+ taskId + " order by slide_change_log.id desc  limit 1";
		// ViksitLogger.logMSG(this.getClass().getName(),("getCurrentPrevAndNextItemForCourse>>>>>>"+getCurrentPrevAndNextItemForCourse);
		List<HashMap<String, Object>> itemStats = util.executeQuery(getCurrentPrevAndNextItemForCourse);
		if (itemStats.size() > 0) {
			currentItemId = (int) itemStats.get(0).get("lesson_id");
			currentItemSlideId = ((BigInteger) itemStats.get(0).get("slide_id")).longValue();
		}

		String getCourseId = "select T1.* , 	CAST ( 		ROW_NUMBER () OVER () AS INTEGER 	) - 1 AS order_id   from (SELECT 	lesson. ID, 	lesson.title FROM 	module_course, 	cmsession_module, 	lesson_cmsession, 	lesson WHERE 	module_course.course_id = "
				+ courseId
				+ " AND cmsession_module.module_id = module_course.module_id AND lesson_cmsession.cmsession_id = cmsession_module.cmsession_id AND lesson_cmsession.lesson_id = lesson. ID AND lesson.is_published = 't' and  lesson.is_deleted != 't' AND lesson.category IN ('ILT', 'BOTH') AND lesson. TYPE != 'ASSESSMENT' ORDER BY 	module_course.oid, 	cmsession_module.oid, 	lesson_cmsession.oid ) T1";
		// ViksitLogger.logMSG(this.getClass().getName(),("getCourseId>>>"+getCourseId);
		List<HashMap<String, Object>> courseData = util.executeQuery(getCourseId);

		if (courseData.size() > 0) {
			ArrayList<CourseItem> courseItems = new ArrayList<>();
			for (HashMap<String, Object> row : courseData) {
				CourseItem item = new CourseItem();
				int itemId = (int) row.get("id");

				File zipFile = new File(mediaLessonPath + itemId + ".zip");

				if (!zipFile.exists()) {
					ViksitLogger.logMSG(this.getClass().getName(),"Newly created Zip for lesson ---> " + itemId);
					CreateZIPForItem createZIPForItem = new CreateZIPForItem();
					try {
						Lesson lesson = new LessonDAO().findById(itemId);
						createZIPForItem.generateXMLForLesson(lesson);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				String itemName = row.get("title").toString().trim();
				int orderId = (int) row.get("order_id");
				item.setItemId(itemId);
				item.setItemName(itemName);
				item.setItemType(TaskItemCategory.LESSON);
				item.setOrderId(orderId);
				item.setItemUrl(mediaPath + "/lessonXMLs/" + itemId + ".zip");
				if (currentItemId != null && itemId == currentItemId) {
					curentItemOrderId = orderId;
					if (orderId + 1 < courseData.size()) {
						nextItemOrderId = orderId + 1;
					}
					if (orderId - 1 >= 0) {
						prevItemOrderId = orderId - 1;
					}
				}

				courseItems.add(item);
			}
			if (curentItemOrderId == null) {
				curentItemOrderId = 0;
			}
			courseContent.setCurrentItemOrderId(curentItemOrderId);
			courseContent.setNextItemOrderId(nextItemOrderId);
			courseContent.setPreviousItemOrderId(prevItemOrderId);
			courseContent.setItems(courseItems);
			courseContent.setCourseId(courseId);
			courseContent.setCurrentItemSlideId(currentItemSlideId);
			courseContent.setContentUrl(mediaPath + "/courseZIPs/" + courseId + ".zip");

		}
		return courseContent;
	}

	public void submitFeedbackByTrainer(int taskId, int istarUserId, ClassFeedbackByTrainer feedbackResponse) {

		HashMap<String, String> feedbackData = new HashMap<>();

		float avgRating = 5;
		float projector = 5;
		float internet = 5;
		float noise = 0;
		float attendance = 5;
		float content = 5;
		float electricity = 5;
		float talentify_rating = 5;
		float time = 5;
		float internals = 0;
		float assignment = 0;
		float sick = 0;

		feedbackData.put("", "0");
		for (FeedbackPojo pojo : feedbackResponse.getFeedbacks()) {
			feedbackData.put(pojo.getName().toLowerCase(), pojo.getRating());

			switch (pojo.getName().toLowerCase()) {
			case "projector":
				projector = Float.parseFloat(pojo.getRating());
				break;
			case "internet":
				internet = Float.parseFloat(pojo.getRating());
				break;
			case "noise":
				noise = Float.parseFloat(pojo.getRating());
				break;
			case "attendance":
				attendance = Float.parseFloat(pojo.getRating());
				break;
			case "content":
				content = Float.parseFloat(pojo.getRating());
				break;
			case "electricity":
				electricity = Float.parseFloat(pojo.getRating());
				break;
			case "talentify_rating":
				talentify_rating = Float.parseFloat(pojo.getRating());
				break;
			case "time":
				time = Float.parseFloat(pojo.getRating());
				break;
			case "internals":
				internals = Float.parseFloat(pojo.getRating());
				break;
			case "assignment":
				assignment = Float.parseFloat(pojo.getRating());
				break;
			case "sick":
				sick = Float.parseFloat(pojo.getRating());
				break;
			}

		}
		DBUTILS util = new DBUTILS();
		String checkIfExist = "delete from trainer_feedback where event_id = (select item_id from task where id = "
				+ taskId + ")";
		util.executeUpdate(checkIfExist);

		float totalRating = Float.parseFloat(feedbackData.get("noise"))
				+ Float.parseFloat(feedbackData.get("attendance")) + Float.parseFloat(feedbackData.get("content"))
				+ Float.parseFloat(feedbackData.get("internet")) + Float.parseFloat(feedbackData.get("electricity"))
				+ Float.parseFloat(feedbackData.get("time")) + Float.parseFloat(feedbackData.get("talentify_rating"))
				+ Float.parseFloat(feedbackData.get("projector"));
		avgRating = totalRating / 8;
		String insertFeedback = "INSERT INTO trainer_feedback (id, user_id, rating, comments, event_id, noise, attendance, sick, content, assignment, internals, internet, electricity, time, projector,talentify_rating) "
				+ "VALUES ((select COALESCE(max(id),0)+1 from trainer_feedback), " + istarUserId + "," + " " + avgRating
				+ ", " + "'" + feedbackData.get("comment") + "'," + " ( select item_id from task where id=" + taskId
				+ ")," + " " + noise + "," + " " + attendance + "," + " " + sick + "," + " " + content + "," + " "
				+ assignment + "," + " " + internals + ", " + "" + internet + "," + " " + electricity + ", " + "" + time
				+ "," + "" + projector + "," + "" + talentify_rating + ");";

		util.executeUpdate(insertFeedback);

		String updateTaskAsCompleted = "update task set is_active = 'f' where id=" + taskId;
		util.executeUpdate(updateTaskAsCompleted);

	}

	public void submitFeedbackByStudent(Integer taskId, Integer student_id, Integer eventId,
			ClassFeedbackByTrainer feedbackResponse) {

		HashMap<String, String> feedbackData = new HashMap<>();
		DBUTILS dbutils = new DBUTILS();
		Integer trainer_id = null;
		Integer batch_id = null;

		String sql = "SELECT actor_id,batch_group_id from batch_schedule_event where type ='BATCH_SCHEDULE_EVENT_TRAINER' and batch_group_code in (SELECT batch_group_code from batch_schedule_event where id="
				+ eventId + ")";

		List<HashMap<String, Object>> hashMaps = dbutils.executeQuery(sql);

		for (HashMap<String, Object> hashMap : hashMaps) {
			trainer_id = (int) hashMap.get("actor_id");
			batch_id = (int) hashMap.get("batch_group_id");
		}

		feedbackData.put("", "0");
		for (FeedbackPojo pojo : feedbackResponse.getFeedbacks()) {
			feedbackData.put(pojo.getName().toLowerCase(), pojo.getRating());
		}

		float projector = 5;
		float internet = 5;
		float trainer_knowledge = 5;
		float trainer_too_fast = 5;
		float class_control_by_trainer = 5;
		float too_tough_content = 5;
		float too_much_theoritic = 5;
		float no_fun_in_class = 5;
		float enough_examples = 5;
		float outside_disturbance = 5;
		float rating = 5;
		float food = 5;
		float hostel = 5;
		String comments = "";

		if (feedbackData != null && feedbackData.size() != 0) {
			for (String key : feedbackData.keySet()) {
				if (!key.equalsIgnoreCase("comment")) {
					float skill_value = Float.parseFloat(feedbackData.get(key));

					switch (key) {
					case "projector":
						projector = skill_value;
						break;
					case "internet":
						internet = skill_value;
						break;
					case "trainer_knowledge":
						trainer_knowledge = skill_value;
						break;
					case "trainer_too_fast":
						trainer_too_fast = skill_value;
						break;
					case "class_control_by_trainer":
						class_control_by_trainer = skill_value;
						break;
					case "too_tough_content":
						too_tough_content = skill_value;
						break;
					case "too_much_theoritic":
						too_much_theoritic = skill_value;
						break;
					case "no_fun_in_class":
						no_fun_in_class = skill_value;
						break;
					case "enough_examples":
						enough_examples = skill_value;
						break;
					case "outside_disturbance":
						outside_disturbance = skill_value;
						break;
					case "food":
						food = skill_value;
						break;
					case "hostel":
						hostel = skill_value;
						break;
					}
				} else {
					comments = feedbackData.get(key);
				}

			}
		}

		rating = ((projector + internet + trainer_knowledge + class_control_by_trainer + too_tough_content
				+ no_fun_in_class + food + hostel) / 8);

		// check if exist ---> delete existed data
		String checkAlreadyGiven = "SELECT * from student_feedback where student_id=" + student_id + " and event_id="
				+ eventId + " and trainer_id=" + trainer_id;
		List<HashMap<String, Object>> data = dbutils.executeQuery(checkAlreadyGiven);
		if (data != null && data.size() != 0) {
			String deleteFeedback = "DELETE from student_feedback where student_id=" + student_id + " and event_id="
					+ eventId + " and trainer_id=" + trainer_id;
			dbutils.executeUpdate(deleteFeedback);
		}

		// insert
		sql = "INSERT INTO student_feedback (id, batch_id, student_id, projector, internet, trainer_knowledge, trainer_too_fast, class_control_by_trainer, too_tough_content, too_much_theoritic, no_fun_in_class, enough_examples, outside_disturbance, rating, event_id, trainer_id, comment, food, hostel) VALUES ((select COALESCE(max(id),0)+1 from student_feedback),"
				+ " " + batch_id + ", " + student_id + ", " + projector + ", " + internet + ", " + trainer_knowledge
				+ ", " + trainer_too_fast + ", " + class_control_by_trainer + ", " + too_tough_content + ", "
				+ too_much_theoritic + ", " + no_fun_in_class + ", " + enough_examples + ", " + outside_disturbance
				+ ", " + rating + ", " + eventId + ", " + trainer_id + ", '" + comments + "', '" + food + "', '"
				+ hostel + "');";

		ViksitLogger.logMSG(this.getClass().getName(),(sql));
		dbutils.executeUpdate(sql);

		String updateTaskAsCompleted = "update task set is_active = 'f' where id=" + taskId;
		dbutils.executeUpdate(updateTaskAsCompleted);

	}

	public void updateState(int taskId, int istarUserId, String state) {

		DBUTILS util = new DBUTILS();
		String fineCourseBGeventId = "select batch_group_id, course_id, id from batch_schedule_event where id = (select item_id from task where id ="
				+ taskId + ")";
		List<HashMap<String, Object>> detail = util.executeQuery(fineCourseBGeventId);
		if (detail.size() > 0) {

			String bgId = detail.get(0).get("batch_group_id").toString();
			String courseId = detail.get(0).get("course_id").toString();
			String id = detail.get(0).get("id").toString();
			String insertIntoLog = "INSERT INTO status_change_log (id, trainer_id, course_id,  created_at, updated_at,  event_type, event_status, event_id, batch_group_id) "
					+ "VALUES ((select COALESCE(max(id),0)+1 from status_change_log), " + istarUserId + ", " + courseId
					+ ", now(),now(), 'STATUS_CHANGED', '" + state + "', " + id + ",  " + bgId + ");";
			util.executeUpdate(insertIntoLog);

			String updateBSE = "update batch_schedule_event set status ='" + state
					+ "' where id=(select item_id from task where id =" + taskId + ")";
			util.executeUpdate(updateBSE);

		}

	}
}
