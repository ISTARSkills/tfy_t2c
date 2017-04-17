package com.istarindia.android.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import com.istarindia.android.pojo.CourseRankPOJO;
import com.istarindia.android.pojo.StudentRankPOJO;
import com.istarindia.apps.services.UserGamificationServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.BatchGroup;
import com.viksitpro.core.dao.entities.BatchStudents;
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
	
	public List<CourseRankPOJO> getCourseRankPOJOForCoursesOfUsersBatch(Integer istarUserId, Set<Integer> allCourses){
		
		List<CourseRankPOJO> allCourseRanks = new ArrayList<CourseRankPOJO>();
		
		AppContentServiceUtility appContentServiceUtility = new AppContentServiceUtility();
		
		for(Integer courseId: allCourses){
			Course course = appContentServiceUtility.getCourse(courseId);			
			if(course!=null){
				System.out.println("Course--->" + course.getCourseName() + course.getId());
				
				CourseRankPOJO courseRankPOJO = new CourseRankPOJO();
				List<StudentRankPOJO> allStudentRankPOJOOfCourse =assignRankToUsersForACourseOfUsersBatch(istarUserId, courseId);
				
				courseRankPOJO.setId(course.getId());
				courseRankPOJO.setName(course.getCourseName());
				courseRankPOJO.setImageURL(course.getImage_url());
				courseRankPOJO.setDescription(course.getCourseDescription());
				courseRankPOJO.setAllStudentRanks(allStudentRankPOJOOfCourse);
				
				System.out.println("Course--->" + allStudentRankPOJOOfCourse.size());
				
				allCourseRanks.add(courseRankPOJO);
				
				System.out.println("Course--->" + allCourseRanks.size());
			}		
		}
		return allCourseRanks;
	}
	
	
	public List<StudentRankPOJO> assignRankToUsersForACourseOfUsersBatch(Integer istarUserId, Integer courseId){
		
		List<StudentRankPOJO> allRankedStudentRankPOJOs = getStudentRankPOJOForACourseOfUsersBatch(istarUserId, courseId);
		List<StudentRankPOJO> rankedPOJOs = new ArrayList<StudentRankPOJO>();
		
		Collections.sort(allRankedStudentRankPOJOs);
		
		for(int i=0; i < allRankedStudentRankPOJOs.size(); i++){			
			StudentRankPOJO studentRankPOJO = allRankedStudentRankPOJOs.get(i);
			studentRankPOJO.setBatchRank((i+1));			
			rankedPOJOs.add(studentRankPOJO);
		}
		return rankedPOJOs;
	}
	
	public List<StudentRankPOJO> getStudentRankPOJOForACourseOfUsersBatch(Integer istarUserId, Integer courseId) {

		List<StudentRankPOJO> allStudentRanksOfABatch = new ArrayList<StudentRankPOJO>();

		AppContentServiceUtility appContentServiceUtility = new AppContentServiceUtility();
		UserGamificationServices userGamificationServices = new UserGamificationServices();

		List<IstarUser> allUsersOfBatch = getBatchColleaguesOfUsers(istarUserId);
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
			studentRankPOJO.setName(istarUserInBatch.getUserProfile().getFirstName());
			studentRankPOJO.setImageURL(istarUserInBatch.getUserProfile().getProfileImage());
			studentRankPOJO.setPoints(totalPoints.intValue());
			studentRankPOJO.setCoins(totalCoins);

			allStudentRanksOfABatch.add(studentRankPOJO);
		}		
		Collections.sort(allStudentRanksOfABatch);
		return allStudentRanksOfABatch;
	}
	
	public List<IstarUser> getBatchColleaguesOfUsers(Integer istarUserId){
		
		List<IstarUser> allUsersOfABatch = new ArrayList<IstarUser>();		
		List<BatchStudents> allbatchStudents = getBatchStudentsOfUser(istarUserId);
				
		if(allbatchStudents.size()>0){
			BatchGroup batchGroup = allbatchStudents.get(0).getBatchGroup();
			
			for(BatchStudents batchStudent : getBatchStudentsOfABatchGroup(batchGroup)){				
				allUsersOfABatch.add(batchStudent.getIstarUser());
			}	
		}
		return allUsersOfABatch;		
	}
	
	public BatchGroup getBatchGroupOfUser(Integer istarUserId){
		
		BatchGroup batchGroup = null;
		
		List<BatchStudents> allbatchStudents = getBatchStudentsOfUser(istarUserId);
		
		if(allbatchStudents.size()>0){
			batchGroup = allbatchStudents.get(0).getBatchGroup();
		}
		
		return batchGroup;
	}
	
	@SuppressWarnings("unchecked")
	public List<BatchStudents> getBatchStudentsOfUser(Integer istarUserId){
				
		String hql = "from BatchStudents batchStudents where istarUser.id= :istarUser";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		
		List<BatchStudents> allBatchStudents = query.list();

		return allBatchStudents;
	}
	
	@SuppressWarnings("unchecked")
	public List<BatchStudents> getBatchStudentsOfABatchGroup(BatchGroup batchGroup){
		
		String hql = "from BatchStudents batchStudents where batchGroup.id= :batchGroup";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("batchGroup",batchGroup.getId());
		
		List<BatchStudents> allBatchStudents = query.list();
		
		return allBatchStudents;
	}
}
