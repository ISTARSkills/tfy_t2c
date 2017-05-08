package com.istarindia.android.utility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.CourseRankPOJO;
import com.istarindia.android.pojo.StudentRankPOJO;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.UserGamificationServices;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.UserGamification;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

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
	
	
	@SuppressWarnings("unchecked")
	public List<CourseRankPOJO> getCourseRankPOJOForCoursesOfUsersBatch(Integer istarUserId) {

		List<CourseRankPOJO> allCourseRanks = new ArrayList<CourseRankPOJO>();
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		String sql = "select *, cast(row_number() over (partition by course_id order by user_points desc) as integer) as course_rank, (sum(user_points) over (partition by course_id))/count(*) over (partition by course_id) as course_batch_average, cast(row_number() over (order by user_points desc) as integer) as overall_rank, sum(user_points) over (partition by istar_user) as overall_points from (select istar_user, course_id,  sum(points) as user_points from (select istar_user, skill_objective, cast(points as numeric), timestamp, cmsession_id, module_id, course_id, item_id,count(batch_group_id) from user_gamification where (istar_user, timestamp,course_id, item_id) in (select istar_user, max(timestamp),course_id,item_id  from user_gamification where batch_group_id in (select distinct batch_group_id from user_gamification where istar_user="+istarUserId+") group by istar_user, course_id,item_id) group by istar_user, skill_objective, cast(points as numeric), timestamp, cmsession_id, module_id, course_id, item_id order by istar_user,item_id) as temptable group by istar_user, course_id order by user_points desc, istar_user) as another";
		
		System.out.println("Leaderboard Query->" + sql);
		SQLQuery query = session.createSQLQuery(sql);
		List<Object[]> result = query.list();
		
		if(result.size()>0){
			AppCourseServices appCourseServices = new AppCourseServices();
			IstarUserServices istarUserServices = new IstarUserServices();
			HashMap<Integer, Course> allCourses = new HashMap<Integer, Course>();
			
			CourseRankPOJO overallRankPOJO = new CourseRankPOJO();
			overallRankPOJO.setId(0);
			overallRankPOJO.setDescription("Overall");
			overallRankPOJO.setName("All Roles");
			
			for(Object[] row : result){
				Integer batchStudentId = (Integer) row[0];
				Integer courseId = (Integer) row[1];
				Double courseUserPoints = ((BigDecimal) row[2]).doubleValue();
				Integer courseRank = (Integer) row[3];
				Integer overallRank = (Integer) row[5];
				Double overallUserPoints = ((BigDecimal) row[6]).doubleValue();
				
				Course course = null;
				CourseRankPOJO courseRankPOJO= null;
				
				if(allCourses.containsKey(courseId)){
					course = allCourses.get(courseId);
					for(CourseRankPOJO tempCourseRankPOJO : allCourseRanks){
						if(tempCourseRankPOJO.getId()==courseId){
							courseRankPOJO = tempCourseRankPOJO;
							break;
						}
					}
				}else{
					course = appCourseServices.getCourse(courseId);
					allCourses.put(courseId, course);
					
					courseRankPOJO = new CourseRankPOJO();
					
					courseRankPOJO.setId(course.getId());
					courseRankPOJO.setDescription(course.getCourseDescription());
					courseRankPOJO.setName(course.getCourseName());
					
					allCourseRanks.add(courseRankPOJO);
				}
				
				StudentRankPOJO studentRankPOJOForCourse = new StudentRankPOJO();
				IstarUser istarUser = istarUserServices.getIstarUser(batchStudentId);
				
				studentRankPOJOForCourse.setId(batchStudentId);
				if(istarUser.getUserProfile()!=null){
					studentRankPOJOForCourse.setName(istarUser.getUserProfile().getFirstName());
					studentRankPOJOForCourse.setImageURL(istarUser.getUserProfile().getProfileImage());
				}else{
					studentRankPOJOForCourse.setName(istarUser.getEmail());
					studentRankPOJOForCourse.setImageURL("http://api.talentify.in/video/android_images/" + istarUser.getEmail().substring(0, 1).toUpperCase() + ".png");
				}
				studentRankPOJOForCourse.setPoints(courseUserPoints.intValue());
				studentRankPOJOForCourse.setBatchRank(courseRank);
				studentRankPOJOForCourse.setCoins(0);
				
				courseRankPOJO.getAllStudentRanks().add(studentRankPOJOForCourse);
				
				StudentRankPOJO studentRankPOJOOverall = new StudentRankPOJO();
				
				studentRankPOJOOverall.setId(batchStudentId);
				if(istarUser.getUserProfile()!=null){
					studentRankPOJOOverall.setName(istarUser.getUserProfile().getFirstName());
					studentRankPOJOOverall.setImageURL(istarUser.getUserProfile().getProfileImage());
				}else{
					studentRankPOJOOverall.setName(istarUser.getEmail());
					studentRankPOJOOverall.setImageURL("http://api.talentify.in/video/android_images/" + istarUser.getEmail().substring(0, 1).toUpperCase() + ".png");
				}
				studentRankPOJOOverall.setPoints(overallUserPoints.intValue());
				studentRankPOJOOverall.setBatchRank(overallRank);
				studentRankPOJOOverall.setCoins(0);
				
				overallRankPOJO.getAllStudentRanks().add(studentRankPOJOOverall);
			}
			allCourseRanks.add(overallRankPOJO);
		}
		return allCourseRanks;
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
				studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getProfileImage());
				}else{
				studentRankPOJO.setName(istarUserInBatch.getEmail());
				studentRankPOJO.setImageURL("http://api.talentify.in/video/android_images/" + istarUserInBatch.getEmail().substring(0, 1).toUpperCase() + ".png");
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
	
	@SuppressWarnings("unchecked")
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
				studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getProfileImage());
				}else{
				studentRankPOJO.setName(istarUserInBatch.getEmail());
				studentRankPOJO.setImageURL("http://api.talentify.in/video/android_images/" + istarUserInBatch.getEmail().substring(0, 1).toUpperCase() + ".png");
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
	}
	
	
	@SuppressWarnings("rawtypes")
	public StudentRankPOJO getStudentRankPOJOForCourseOfAUser(Integer istarUserId, Integer courseId){
		
		StudentRankPOJO studentRankPOJO = null;
		
		String sql = "select * from (select *, COALESCE(cast(row_number() over (order by total_points desc) as integer),0) as rank from "
				+ "(select user_gamification.istar_user, cast(sum(user_gamification.points) as integer)as total_points, cast(sum(user_gamification.coins) as integer) as total_coins "
				+ "from assessment,user_gamification where user_gamification.item_id=assessment.id and assessment.course_id= :courseId  and user_gamification.istar_user in "
				+ "(select student_id from batch_students where batch_group_id in "
				+ "(select batch_group_id from batch_students where batch_students.student_id= :istarUserId)) "
				+ "group by user_gamification.istar_user order by total_points desc) as batch_ranks) as user_rank where istar_user=:istarUserId";
		
		System.out.println("Student Rank pojo "+sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId",istarUserId);
		query.setParameter("courseId",courseId);
		
		List results = query.list();
		
		if(results.size() > 0){
		Object[] studentData = (Object[]) results.get(0);
		
		Integer istarUserInBatchId = (Integer) studentData[0];
		Integer points = (Integer) studentData[1];
		Integer coins = (Integer) studentData[2];
		Integer rank = (Integer) studentData[3];
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUserInBatch = istarUserServices.getIstarUser(istarUserInBatchId);
		
		studentRankPOJO = new StudentRankPOJO();

		studentRankPOJO.setId(istarUserInBatch.getId());
		
		if(istarUserInBatch.getUserProfile()!=null){
			studentRankPOJO.setName(istarUserInBatch.getUserProfile().getFirstName());
			studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getProfileImage());
		}else{
			studentRankPOJO.setName(istarUserInBatch.getEmail());
		}
		
		studentRankPOJO.setPoints(points);
		studentRankPOJO.setCoins(coins);
		studentRankPOJO.setBatchRank(rank);
		}
		return studentRankPOJO;
	}
	
	
	@SuppressWarnings("rawtypes")
	public StudentRankPOJO getStudentRankPOJOOfAUser(Integer istarUserId){
		
		StudentRankPOJO studentRankPOJO = null;
		
		String sql = "select * from (select *, COALESCE(cast(row_number() over (order by total_points desc) as integer),0) as rank from "
				+ "(select user_gamification.istar_user, cast(sum(user_gamification.points) as integer)as total_points, cast(sum(user_gamification.coins) as integer) as total_coins "
				+ "from assessment,user_gamification where user_gamification.item_id=assessment.id  and user_gamification.istar_user in "
				+ "(select student_id from batch_students where batch_group_id in "
				+ "(select batch_group_id from batch_students where batch_students.student_id= :istarUserId)) "
				+ "group by user_gamification.istar_user order by total_points desc) as batch_ranks) as user_rank where istar_user=:istarUserId";
		
		System.out.println("Student Rank pojo "+sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId",istarUserId);
		
		List results = query.list();
		
		if(results.size() > 0){
		Object[] studentData = (Object[]) results.get(0);
		
		Integer istarUserInBatchId = (Integer) studentData[0];
		Integer points = (Integer) studentData[1];
		Integer coins = (Integer) studentData[2];
		Integer rank = (Integer) studentData[3];
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUserInBatch = istarUserServices.getIstarUser(istarUserInBatchId);
		
		studentRankPOJO = new StudentRankPOJO();

		studentRankPOJO.setId(istarUserInBatch.getId());
		
		if(istarUserInBatch.getUserProfile()!=null){
			studentRankPOJO.setName(istarUserInBatch.getUserProfile().getFirstName());
			studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getProfileImage());
		}else{
			studentRankPOJO.setName(istarUserInBatch.getEmail());
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
				
				//System.out.println("allUserGamifications->" + allUserGamifications.size());
				
				for (UserGamification userGamification : allUserGamifications) {
					
					//System.out.println("userGamification.getPoints()->"+userGamification.getPoints());
					//System.out.println("userGamification.getCoins()->"+userGamification.getCoins());
					
					totalPoints = totalPoints + userGamification.getPoints();
					totalCoins = totalCoins + userGamification.getCoins();
					
					System.out.println("tPoints->"+totalPoints);
					System.out.println("tCoins->"+totalCoins);
				}
			}

			StudentRankPOJO studentRankPOJO = new StudentRankPOJO();

			System.out.println("totalPoints->"+totalPoints);
			System.out.println("totalCoins->"+totalCoins);
			
			studentRankPOJO.setId(istarUserInBatch.getId());
			if(istarUserInBatch.getUserProfile()!=null){
			studentRankPOJO.setName(istarUserInBatch.getUserProfile().getFirstName());
			studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getProfileImage());
			}
			studentRankPOJO.setPoints(totalPoints.intValue());
			studentRankPOJO.setCoins(totalCoins);

			allStudentRanksOfABatch.add(studentRankPOJO);
		}		
		Collections.sort(allStudentRanksOfABatch);
		return allStudentRanksOfABatch;
	}*/
	
}