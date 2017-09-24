package t2c;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.istarindia.android.utility.AppUtility;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.AppServices;
import com.istarindia.apps.services.GamificationServices;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentDAO;
import com.viksitpro.core.dao.entities.Cmsession;
import com.viksitpro.core.dao.entities.Context;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.IstarUserDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.entities.StudentPlaylistDAO;
import com.viksitpro.core.dao.entities.UserProfile;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.utilities.DBUTILS;

public class Test {

	public static void main(String[] args) {
	
		try {
			//test();
			System.out.println("start");
			//updateleaderboard();
			//checkLessonAssessMap();
			//checkarraytostring();
			//updateAsessmentStats();
			updateUserGamificationForLesson();
			updateUserGamificationForAssess();
			updateLeaderBoard();
			System.out.println("end");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static void updateLeaderBoard() {
		// TODO Auto-generated method stub
		DBUTILS util =new DBUTILS();
		GamificationServices serv =  new GamificationServices();
		String sql ="select distinct user_id from user_points_coins";
		List<HashMap<String, Object>> dd= util.executeQuery(sql);
		for(HashMap<String, Object> row: dd)
		{
			int userId = (int)row.get("user_id");
			serv.updateLeaderBoard(userId);
		}
	}

	private static void updateUserGamificationForAssess() {
		// TODO Auto-generated method stub
		DBUTILS util = new DBUTILS();
		GamificationServices serv= new GamificationServices();
		String sql="select DISTINCT user_id, assessment_id from report";
		List<HashMap<String, Object>> dd = util.executeQuery(sql);
		for(HashMap<String, Object> row: dd)
		{
			int stuId = (int)row.get("user_id");
			int assessment_id = (int)row.get("assessment_id");
			System.out.println("assessment_id "+assessment_id+" student id "+stuId);
			serv.updateUserGamificationAfterAssessment(new IstarUserDAO().findById(stuId), new AssessmentDAO().findById(assessment_id));
		}
	}

	private static void updateUserGamificationForLesson() {
		// TODO Auto-generated method stub
		DBUTILS util = new DBUTILS();
		GamificationServices serv= new GamificationServices();
		String sql="select DISTINCT student_id, lesson_id from student_playlist where status ='COMPLETED'";
		List<HashMap<String, Object>> dd = util.executeQuery(sql);
		for(HashMap<String, Object> row: dd)
		{
			int stuId = (int)row.get("student_id");
			int lessonId = (int)row.get("lesson_id");
			System.out.println("lesson "+lessonId+" student id "+stuId);
			serv.updatePointsAndCoinsOnLessonComplete(new IstarUserDAO().findById(stuId), new LessonDAO().findById(lessonId));
		}
	}
	
	private static void updateAsessmentStats() {
		// TODO Auto-generated method stub
		GamificationServices serv= new GamificationServices();
		String sql ="select DISTINCT istar_user, item_id from user_gamification where item_type ='ASSESSMENT'";
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> data = util.executeQuery(sql);
		for(HashMap<String, Object> row: data)
		{
			int istarUserId = (int)row.get("istar_user");
			int assessmentId = (int)row.get("item_id");
			serv.updateUserAssessmentPointsCoinsTable(istarUserId, assessmentId);
		}
	}

	private static void checkarraytostring() {
		// TODO Auto-generated method stub
		StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
		List<StudentPlaylist> allStudentPlaylist = studentPlaylistServices.getStudentPlaylistOfUserForCourse(10461, 111);
		ArrayList<Lesson> lessons =(ArrayList<Lesson>) allStudentPlaylist.stream() .map(StudentPlaylist::getLesson).collect(Collectors.toList());
		System.out.println(lessons.size());
		System.out.println(lessons.stream() .map(Lesson::getId).collect(Collectors.toList()));
		
	}

	private static void checkLessonAssessMap() {
		// TODO Auto-generated method stub
		for(Lesson assess : (List<Lesson>)new LessonDAO().findAll())
		{
			if(assess.getType().equalsIgnoreCase("ASSESSMENT")) {
				System.out.println(assess.getAssessment().getId());
			}
			
		}
	}

	private static void updateleaderboard() {
		// TODO Auto-generated method stub
		GamificationServices serv= new GamificationServices();
		String sql ="select distinct user_id from user_points_coins";
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> data = util.executeQuery(sql);
		for(HashMap<String, Object> row: data)
		{
			serv.updateLeaderBoard((int)row.get("user_id"));
		}
	}

	public static void test(){
		
		//System.out.println("Finished");
	}
}
