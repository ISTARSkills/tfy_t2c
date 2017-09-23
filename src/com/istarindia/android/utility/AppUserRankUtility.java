package com.istarindia.android.utility;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.CourseRankPOJO;
import com.istarindia.android.pojo.StudentRankPOJO;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.istarindia.apps.services.UserGamificationServices;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.CourseDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.IstarUserDAO;
import com.viksitpro.core.dao.entities.UserGamification;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.utilities.AppProperies;
import com.viksitpro.core.utilities.DBUTILS;

public class AppUserRankUtility {
	
	public HashMap<String, Integer> getPointsAndCoinsOfUser(int istarUserId){
		
		HashMap<String, Integer> pointAndCoins = new HashMap<String, Integer>();
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);
		
		Set<UserGamification> allUserGamifications = istarUser.getUserGamifications();
		
		Double totalPoints = 0.0;
		int totalCoins = 0;
		
		for(UserGamification userGamification : allUserGamifications){			
			totalPoints = totalPoints + userGamification.getPoints();
			totalCoins = totalCoins + userGamification.getCoins();
		}

		pointAndCoins.put("points", totalPoints.intValue());
		pointAndCoins.put("coins", totalCoins);
	
		return pointAndCoins;
	}
	
	public HashMap<String, Integer> getPoinstsAndCoinsOfUserForAssessment(int istarUserId, int assesmentId){
		
		HashMap<String, Integer> pointAndCoins = new HashMap<String, Integer>();
		
		UserGamificationServices userGamificationServices= new UserGamificationServices();
		List<UserGamification> allUserGamifications = userGamificationServices.getUserGamificationsOfUserForItem(istarUserId, assesmentId, "ASSESSMENT");
		
		Double totalPoints = 0.0;
		int totalCoins = 0;
		
		for(UserGamification userGamification : allUserGamifications){			
			totalPoints = totalPoints + userGamification.getPoints();
			totalCoins = totalCoins + userGamification.getCoins();
		}

		pointAndCoins.put("points", totalPoints.intValue());
		pointAndCoins.put("coins", totalCoins);
	
		return pointAndCoins;
	}
	
	
	public CourseRankPOJO getLeaderboardForCourseOfUser(int istarUserId, int courseId) {
		String mediaUrlPath =AppProperies.getProperty("media_url_path");;
		Course course = new CourseDAO().findById(courseId);
		CourseRankPOJO courseRankPOJO = null;

		if (course != null) {
			courseRankPOJO = new CourseRankPOJO();
			courseRankPOJO.setId(course.getId());
			courseRankPOJO.setDescription(course.getCourseDescription());
			courseRankPOJO.setName(course.getCourseName());
			DBUTILS util = new DBUTILS();
			
			String findRankPoinsCoins =""
			+ "select DISTINCT "
			+ "user_profile.user_id as student_id, "
			+ "COALESCE(profile_image,'/users/'||substring(first_name from 1 for 1)||'.png') as image, "
			+ "first_name, "
			+ "COALESCE(user_points, 0) as user_points, "
			+ "COALESCE(coins, 0) as coins, "
			+ "COALESCE(percentage, 0) as percentage, "
			+ "CAST ( RANK () OVER (  ORDER BY COALESCE(percentage, 0) DESC ) AS INTEGER )  as user_rank "
			+ "from  user_profile left join leaderboard on (leaderboard.user_id = user_profile.user_id) "
			+ "where user_profile.user_id in "
			+ "("
				+ "select student_id from batch_students where batch_group_id in "
				+ "("
					+ "select batch_group_id from batch_students where student_id = "+istarUserId+""
				+ ")"
			+ ") and course_id ="+courseId+" order by user_rank";
			
			List<HashMap<String, Object>> rankData= util.executeQuery(findRankPoinsCoins);
			if(rankData.size()>0)
			{
				List<StudentRankPOJO> allStudentRanksOfABatch = new ArrayList<StudentRankPOJO>();
				for(HashMap<String, Object> stuData : rankData)
				{
					int stuId = (int)stuData.get("student_id");
					double userPoints = (double)stuData.get("user_points");;
					
					double coins = (double)stuData.get("coins");
					int rank = (int) stuData.get("user_rank");
					StudentRankPOJO studentRankPOJO = new StudentRankPOJO();
					String name=stuData.get("first_name").toString();
					String image = stuData.get("image").toString();
					studentRankPOJO.setId(stuId);
					studentRankPOJO.setName(name);
					studentRankPOJO.setImageURL(mediaUrlPath+image);
					studentRankPOJO.setPoints((int)Math.ceil(userPoints));
					studentRankPOJO.setCoins((int)coins);
					studentRankPOJO.setBatchRank(rank);
					allStudentRanksOfABatch.add(studentRankPOJO);
				}
				courseRankPOJO.setAllStudentRanks(allStudentRanksOfABatch);
			}			
		}
		return courseRankPOJO;
	}
	
	public List<CourseRankPOJO> getCourseRankPOJOForCoursesOfUsersBatch(Integer istarUserId) {
		
		List<CourseRankPOJO> allCoursesLeaderboard = new ArrayList<CourseRankPOJO>(); 
		
		StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
		List<Integer> allCourseIds =studentPlaylistServices.getCoursesforUser(istarUserId);
		
		CourseRankPOJO overAllCourseRankPOJO = getOverAllLeaderboardForUser(istarUserId);
		if(overAllCourseRankPOJO!=null){
			allCoursesLeaderboard.add(overAllCourseRankPOJO);
		}
		
		for(Integer courseId: allCourseIds){
			CourseRankPOJO courseRankPOJO = getLeaderboardForCourseOfUser(istarUserId, courseId);			
			if(courseRankPOJO!=null){
				allCoursesLeaderboard.add(courseRankPOJO);
			}
		}		
		return allCoursesLeaderboard;
	}
	
	
public CourseRankPOJO getOverAllLeaderboardForUser(int istarUserId){
	String mediaUrlPath =AppProperies.getProperty("media_url_path");;	
	CourseRankPOJO overallRankPOJO = null;
	DBUTILS util = new DBUTILS();		
	String findRankPoinsCoins ="select DISTINCT "
			+ "user_profile.user_id as student_id, "
			+ "COALESCE(profile_image,'/users/'||substring(first_name from 1 for 1)||'.png') as image, "
			+ "first_name, 	COALESCE(sum(user_points),0) as user_points, "
			+ "COALESCE(sum(coins),0) as coins, "
			+ "CAST ( RANK () OVER ( ORDER BY 	COALESCE(avg(percentage),0) DESC ) AS INTEGER )  as user_rank "
			+ "from  "
			+ "user_profile left join leaderboard on (leaderboard.user_id = user_profile.user_id) "
			+ "where user_profile.user_id in "
			+ "("
				+ "select "
				+ "student_id "
				+ "from "
				+ "batch_students "
				+ "where batch_group_id in "
					+ "("
						+ "select batch_group_id from batch_students where student_id = "+istarUserId
					+ ")"
			+ ") group by student_id,first_name, image order by user_rank";
	System.out.println(findRankPoinsCoins);
	List<HashMap<String, Object>> rankData= util.executeQuery(findRankPoinsCoins);
	if(rankData.size()>0)
	{
		overallRankPOJO = new CourseRankPOJO();
		overallRankPOJO.setId(0);
		overallRankPOJO.setDescription("Overall");
		overallRankPOJO.setName("All Roles");
		List<StudentRankPOJO> allStudentRanksOfABatch = new ArrayList<StudentRankPOJO>();
		for(HashMap<String, Object> stuData : rankData)
		{
			int stuId = (int)stuData.get("student_id");
			double userPoints = (double)stuData.get("user_points");;
			
			double coins = (double)stuData.get("coins");
			int rank = (int) stuData.get("user_rank");
			StudentRankPOJO studentRankPOJO = new StudentRankPOJO();
			String name=stuData.get("first_name").toString();
			String image = stuData.get("image").toString();
			studentRankPOJO.setId(stuId);
			studentRankPOJO.setName(name);
			studentRankPOJO.setImageURL(mediaUrlPath+image);
			studentRankPOJO.setPoints((int)Math.ceil(userPoints));
			studentRankPOJO.setCoins((int)coins);
			studentRankPOJO.setBatchRank(rank);
			allStudentRanksOfABatch.add(studentRankPOJO);
			}
			overallRankPOJO.setAllStudentRanks(allStudentRanksOfABatch);
		}		
		return overallRankPOJO;
	}	
	
/*	@SuppressWarnings("unchecked")
	public List<CourseRankPOJO> getCourseRankPOJOForCoursesOfUsersBatch(Integer istarUserId, Set<Integer> allCourses){
		
		List<CourseRankPOJO> allCourseRanks = new ArrayList<CourseRankPOJO>();
		
		AppCourseServices appCourseServices = new AppCourseServices();
		IstarUserServices istarUserServices = new IstarUserServices();
		
		for(Integer courseId: allCourses){
						
			Course course = appCourseServices.getCourse(courseId);			
			if(course!=null){
				CourseRankPOJO courseRankPOJO = new CourseRankPOJO();
				
				courseRankPOJO.setId(course.getId());
				courseRankPOJO.setName(course.getCourseName());
				courseRankPOJO.setImageURL(course.getImage_url());
				courseRankPOJO.setDescription(course.getCourseDescription());
				
			List<StudentRankPOJO> allStudentRanksOfABatch = new ArrayList<StudentRankPOJO>();
			
			String sql = "select *, COALESCE(cast(row_number() over (order by total_points desc) as integer),0) as rank from "
					+ "(select user_gamification.istar_user, cast(sum(user_gamification.points) as integer)as total_points, cast(sum(user_gamification.coins) as integer) as total_coins "
					+ "from assessment,user_gamification where user_gamification.item_id=assessment.id and assessment.course_id= :courseId  and user_gamification.istar_user in "
					+ "(select student_id from batch_students where batch_group_id in "
					+ "(select batch_group_id from batch_students where batch_students.student_id= :istarUserId)) "
					+ "group by user_gamification.istar_user order by total_points desc) as batch_ranks";
			
			BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
			Session session = baseHibernateDAO.getSession();
			
			SQLQuery query = session.createSQLQuery(sql);
			query.setParameter("istarUserId",istarUserId);
			query.setParameter("courseId",course.getId());
			
			List<Object[]> allStudentsWithPoints = query.list();
						
			for(Object[] studentData : allStudentsWithPoints){
				
				Integer istarUserInBatchId = (Integer) studentData[0];
				Integer points = (Integer) studentData[1];
				Integer coins = (Integer) studentData[2];
				Integer rank = (Integer) studentData[3];
				
				IstarUser istarUserInBatch = istarUserServices.getIstarUser(istarUserInBatchId);
				
				StudentRankPOJO studentRankPOJO = new StudentRankPOJO();

				studentRankPOJO.setId(istarUserInBatch.getId());
				if(istarUserInBatch.getUserProfile()!=null){
				studentRankPOJO.setName(istarUserInBatch.getUserProfile().getFirstName());
				studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getImage());
				}else{
				studentRankPOJO.setName(istarUserInBatch.getEmail());
				studentRankPOJO.setImageURL("http://cdn.talentify.in/video/android_images/" + istarUserInBatch.getEmail().substring(0, 1).toUpperCase() + ".png");
				}
				studentRankPOJO.setPoints(points);
				studentRankPOJO.setCoins(coins);
				studentRankPOJO.setBatchRank(rank);

				allStudentRanksOfABatch.add(studentRankPOJO);
			}
			Collections.sort(allStudentRanksOfABatch);
			courseRankPOJO.setAllStudentRanks(allStudentRanksOfABatch);
			allCourseRanks.add(courseRankPOJO);
			}
		}
		return allCourseRanks;
	}*/
	
/*	@SuppressWarnings("unchecked")
	public CourseRankPOJO getCourseRankPOJOForCoursesOfUsersBatch(Integer istarUserId, Integer courseId){
		
		CourseRankPOJO courseRankPOJO = null;
		
		AppCourseServices appCourseServices = new AppCourseServices();
		IstarUserServices istarUserServices = new IstarUserServices();

			Course course = appCourseServices.getCourse(courseId);			
			if(course!=null){
				courseRankPOJO = new CourseRankPOJO();
				
				courseRankPOJO.setId(course.getId());
				courseRankPOJO.setName(course.getCourseName());
				courseRankPOJO.setImageURL(course.getImage_url());
				courseRankPOJO.setDescription(course.getCourseDescription());
				
			List<StudentRankPOJO> allStudentRanksOfABatch = new ArrayList<StudentRankPOJO>();
			
			String sql = "select *, COALESCE(cast(row_number() over (order by total_points desc) as integer),0) as rank from "
					+ "(select user_gamification.istar_user, cast(sum(user_gamification.points) as integer)as total_points, cast(sum(user_gamification.coins) as integer) as total_coins "
					+ "from assessment,user_gamification where user_gamification.item_id=assessment.id and course_id= :courseId  and user_gamification.istar_user in "
					+ "(select student_id from batch_students where batch_group_id in "
					+ "(select batch_group_id from batch_students where batch_students.student_id= :istarUserId)) "
					+ "group by user_gamification.istar_user order by total_points desc) as batch_ranks";
			
			BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
			Session session = baseHibernateDAO.getSession();
			
			SQLQuery query = session.createSQLQuery(sql);
			query.setParameter("istarUserId",istarUserId);
			query.setParameter("courseId",course.getId());
			
			List<Object[]> allStudentsWithPoints = query.list();
						
			for(Object[] studentData : allStudentsWithPoints){
				
				Integer istarUserInBatchId = (Integer) studentData[0];
				Integer points = (Integer) studentData[1];
				Integer coins = (Integer) studentData[2];
				Integer rank = (Integer) studentData[3];
				
				IstarUser istarUserInBatch = istarUserServices.getIstarUser(istarUserInBatchId);
				
				StudentRankPOJO studentRankPOJO = new StudentRankPOJO();

				studentRankPOJO.setId(istarUserInBatch.getId());
				if(istarUserInBatch.getUserProfile()!=null){
				studentRankPOJO.setName(istarUserInBatch.getUserProfile().getFirstName());
				studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getImage());
				}else{
				studentRankPOJO.setName(istarUserInBatch.getEmail());
				studentRankPOJO.setImageURL("http://cdn.talentify.in/video/android_images/" + istarUserInBatch.getEmail().substring(0, 1).toUpperCase() + ".png");
				}
				studentRankPOJO.setPoints(points);
				studentRankPOJO.setCoins(coins);
				studentRankPOJO.setBatchRank(rank);

				allStudentRanksOfABatch.add(studentRankPOJO);
			}
			Collections.sort(allStudentRanksOfABatch);
			courseRankPOJO.setAllStudentRanks(allStudentRanksOfABatch);
			}
		return courseRankPOJO;
	}*/
	
	
	@SuppressWarnings("rawtypes")
	public StudentRankPOJO getStudentRankPOJOForCourseOfAUser(Integer istarUserId, Integer courseId){
		
		StudentRankPOJO studentRankPOJO = null;
		
		String sql = "select * from (select *, cast(row_number() over (order by points desc) as integer) from (with usr_gmfct as (select istar_user, points,coins, skill_objective, item_id, item_type, item_id, cmsession_id, module_id, course_id, count(batch_group_id) from user_gamification where course_id = "+courseId+" and batch_group_id in (select distinct batch_group_id from batch_students where student_id="+istarUserId+" and course_id="+courseId+") and (istar_user,item_id, item_type, timestamp) in (select istar_user, item_id, item_type, max(timestamp) from user_gamification where istar_user in (select distinct student_id from batch_students where batch_group_id in (select distinct batch_group_id from batch_students where student_id="+istarUserId+")) group by istar_user, item_id,item_type) group by istar_user, skill_objective, points, coins, item_id, item_type, cmsession_id, module_id, course_id),students_of_batch as (select distinct student_id from batch_students where batch_group_id in (select distinct batch_group_id from batch_students where student_id="+istarUserId+")) select students_of_batch.student_id, cast(COALESCE(sum(points),0) as numeric) as points, cast(COALESCE(sum(coins),0) as numeric) as coins from students_of_batch left join usr_gmfct on students_of_batch.student_id=usr_gmfct.istar_user group by students_of_batch.student_id order by points desc) as temptable) as another_temp where student_id="+istarUserId;
		
		//System.out.println("Student Rank pojo for course-->"+sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);

		List results = query.list();
		
		if(results.size() > 0){
		Object[] row = (Object[]) results.get(0);
		
		Integer batchStudentId = (Integer) row[0];
		Double courseUserPoints = ((BigDecimal) row[1]).doubleValue();
		Double courseUserCoins = ((BigDecimal) row[2]).doubleValue();
		Integer courseRank = (Integer) row[3];
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUserInBatch = istarUserServices.getIstarUser(batchStudentId);
		
		studentRankPOJO = new StudentRankPOJO();

		studentRankPOJO.setId(istarUserInBatch.getId());
		
		if(istarUserInBatch.getUserProfile()!=null){
			studentRankPOJO.setName(istarUserInBatch.getUserProfile().getFirstName());
			studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getImage());
		}else{
			studentRankPOJO.setName(istarUserInBatch.getEmail());
		}
		
		studentRankPOJO.setPoints(courseUserPoints.intValue());
		studentRankPOJO.setCoins(courseUserCoins.intValue());
		studentRankPOJO.setBatchRank(courseRank);
		}
		return studentRankPOJO;
	}
	
	
	@SuppressWarnings("rawtypes")
	public StudentRankPOJO getStudentRankPOJOOfAUser(Integer istarUserId){
		
		StudentRankPOJO studentRankPOJO = null;
		
		//String sql = "select * from (select istar_user, cast(row_number() over (order by user_points desc) as integer) as overall_rank, sum(user_points) over (partition by istar_user) as overall_points, sum(user_coins) over (partition by istar_user) as overall_coins from (select istar_user, course_id,  sum(points) as user_points, sum(coins) as user_coins from (select istar_user, skill_objective, cast(points as numeric), cast(coins as numeric), timestamp, cmsession_id, module_id, course_id, item_id,count(batch_group_id) from user_gamification where (istar_user, timestamp,course_id, item_id) in (select istar_user, max(timestamp),course_id,item_id  from user_gamification where batch_group_id in (select distinct batch_group_id from user_gamification where istar_user="+istarUserId+") group by istar_user, course_id,item_id) group by istar_user, skill_objective, cast(points as numeric), cast(coins as numeric), timestamp, cmsession_id, module_id, course_id, item_id order by istar_user,item_id) as temptable group by istar_user, course_id order by user_points desc, istar_user) as another) as onemore where istar_user="+istarUserId;
		String sql = "select * from (select *, CAST (ROW_NUMBER () OVER (ORDER BY overall_points DESC) AS INTEGER) AS overall_rank from (SELECT istar_user, SUM (user_points) as overall_points, SUM (user_coins)  aS overall_coins FROM (SELECT istar_user, course_id, SUM (points) AS user_points, SUM (coins) AS user_coins FROM (SELECT istar_user, skill_objective, CAST (points AS NUMERIC), CAST (coins AS NUMERIC), TIMESTAMP, cmsession_id, module_id, course_id, item_id, item_type, COUNT (batch_group_id) FROM user_gamification WHERE (istar_user, TIMESTAMP, course_id, item_id,item_type ) IN (SELECT istar_user, MAX (TIMESTAMP), course_id, item_id,item_type FROM user_gamification WHERE batch_group_id IN (SELECT DISTINCT batch_group_id FROM user_gamification WHERE istar_user ="+istarUserId+" ) GROUP BY istar_user, course_id, item_id,item_type ) GROUP BY istar_user, skill_objective, CAST (points AS NUMERIC), CAST (coins AS NUMERIC), TIMESTAMP, cmsession_id, module_id, course_id, item_id,item_type ORDER BY istar_user, item_id,item_type ) AS temptable GROUP BY istar_user, course_id ORDER BY user_points DESC, istar_user ) AS another group by istar_user) as yet_another) as one_last where istar_user="+istarUserId;
		//System.out.println("Student Rank pojo "+sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		
		List results = query.list();
		
		if(results.size() > 0){
		Object[] studentData = (Object[]) results.get(0);
		
		//Integer istarUserInBatchId = (Integer) studentData[0];
		
		Integer points = ((BigDecimal) studentData[1]).intValue();
		Integer coins = ((BigDecimal) studentData[2]).intValue();
		Integer rank = (Integer) studentData[3];		
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);
		
		studentRankPOJO = new StudentRankPOJO();

		studentRankPOJO.setId(istarUser.getId());
		
		if(istarUser.getUserProfile()!=null){
			studentRankPOJO.setName(istarUser.getUserProfile().getFirstName());
			studentRankPOJO.setImageURL(istarUser.getUserProfile().getImage());
		}else{
			studentRankPOJO.setName(istarUser.getEmail());
		}
		
		studentRankPOJO.setPoints(points);
		studentRankPOJO.setCoins(coins);
		studentRankPOJO.setBatchRank(rank);
		}
		return studentRankPOJO;
	}
	
/*	public List<StudentRankPOJO> assignRankToUsersForACourseOfUsersBatch(Integer istarUserId, Integer courseId){
		
		List<StudentRankPOJO> allRankedStudentRankPOJOs = getStudentRankPOJOForACourseOfUsersBatch(istarUserId, courseId);
		List<StudentRankPOJO> rankedPOJOs = new ArrayList<StudentRankPOJO>();
		
		Collections.sort(allRankedStudentRankPOJOs);
		
		for(int i=0; i < allRankedStudentRankPOJOs.size(); i++){			
			StudentRankPOJO studentRankPOJO = allRankedStudentRankPOJOs.get(i);
			studentRankPOJO.setBatchRank((i+1));			
			rankedPOJOs.add(studentRankPOJO);
		}
		return rankedPOJOs;
	}*/
	
/*	public List<StudentRankPOJO> getStudentRankPOJOForACourseOfUsersBatch(Integer istarUserId, Integer courseId) {

		List<StudentRankPOJO> allStudentRanksOfABatch = new ArrayList<StudentRankPOJO>();

		AppContentServiceUtility appContentServiceUtility = new AppContentServiceUtility();
		UserGamificationServices userGamificationServices = new UserGamificationServices();
		AppBatchStudentsServices appBatchStudentsServices = new AppBatchStudentsServices();
		
		List<IstarUser> allUsersOfBatch = appBatchStudentsServices.getBatchColleaguesOfUsers(istarUserId);
		List<Assessment> allAssessments = appContentServiceUtility.getAssessmentsOfACourse(courseId);

		for (IstarUser istarUserInBatch : allUsersOfBatch) {
			Double totalPoints = 0.0;
			int totalCoins = 0;

			for (Assessment assessment : allAssessments) {
				List<UserGamification> allUserGamifications = userGamificationServices
						.getUserGamificationsOfUserForItem(istarUserInBatch.getId(), assessment.getId(), "ASSESSMENT");
				
				////System.out.println("allUserGamifications->" + allUserGamifications.size());
				
				for (UserGamification userGamification : allUserGamifications) {
					
					////System.out.println("userGamification.getPoints()->"+userGamification.getPoints());
					////System.out.println("userGamification.getCoins()->"+userGamification.getCoins());
					
					totalPoints = totalPoints + userGamification.getPoints();
					totalCoins = totalCoins + userGamification.getCoins();
					
					//System.out.println("tPoints->"+totalPoints);
					//System.out.println("tCoins->"+totalCoins);
				}
			}

			StudentRankPOJO studentRankPOJO = new StudentRankPOJO();

			//System.out.println("totalPoints->"+totalPoints);
			//System.out.println("totalCoins->"+totalCoins);
			
			studentRankPOJO.setId(istarUserInBatch.getId());
			if(istarUserInBatch.getUserProfile()!=null){
			studentRankPOJO.setName(istarUserInBatch.getUserProfile().getFirstName());
			studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getImage());
			}
			studentRankPOJO.setPoints(totalPoints.intValue());
			studentRankPOJO.setCoins(totalCoins);

			allStudentRanksOfABatch.add(studentRankPOJO);
		}		
		Collections.sort(allStudentRanksOfABatch);
		return allStudentRanksOfABatch;
	}*/
	
}