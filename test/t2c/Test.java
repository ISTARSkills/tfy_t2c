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
			updateAsessmentStats();
			System.out.println("end");
			
		} catch (Exception e) {
			e.printStackTrace();
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
		
		AppCourseServices appCourseServices = new AppCourseServices();
		StudentPlaylistServices StudentPlaylistServices= new StudentPlaylistServices();
		for(StudentPlaylist sp : (new StudentPlaylistDAO()).findAll()){
			if(sp.getModule()!=null || sp.getCmsession()!=null){
			////System.out.println("Updating--->"+ sp.getId());
			Module module = appCourseServices.getModuleOfLesson(sp.getLesson().getId());
			if(module!=null){
				List<Cmsession> allCmsessions =  appCourseServices.getCmsessionsOfModule(module.getId());
				if(allCmsessions.size()>0){
/*					sp.setModule(module);
					sp.setCmsession(allCmsessions.get(0));
					StudentPlaylistServices.updateStudentPlaylistToDAO(sp);
					//System.out.println("Updated--->"+ sp.getId());*/
					//System.out.println("UPDATE student_playlist SET module_id="+module.getId()+" , cmsession_id="+allCmsessions.get(0).getId()+" WHERE (id="+sp.getId()+");");
				}
			}
			}
		}
		//System.out.println("Finished");
	}
}
