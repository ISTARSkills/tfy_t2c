package com.istarindia.android.utility;

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
			
			String sql = "select *, cast(rank() over (order by total_points desc) as integer) from "
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
			allCourseRanks.add(courseRankPOJO);
			}
		}
		return allCourseRanks;
	}
	
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
			
			String sql = "select *, cast(rank() over (order by total_points desc) as integer) from "
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
		
		String sql = "select * from (select *, cast(rank() over (order by total_points desc) as integer) from "
				+ "(select user_gamification.istar_user, cast(sum(user_gamification.points) as integer)as total_points, cast(sum(user_gamification.coins) as integer) as total_coins "
				+ "from assessment,user_gamification where user_gamification.item_id=assessment.id and course_id= :courseId  and user_gamification.istar_user in "
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