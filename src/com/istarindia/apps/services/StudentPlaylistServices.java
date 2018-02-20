package com.istarindia.apps.services;

import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.entities.StudentPlaylistDAO;

public class StudentPlaylistServices {
	
	public StudentPlaylist updateStatus(StudentPlaylist studentPlaylist, String status){
		
		studentPlaylist.setStatus(status);		
		studentPlaylist = updateStudentPlaylistToDAO(studentPlaylist);
		
		return studentPlaylist;
	}
	
	@SuppressWarnings("unchecked")
	public List<StudentPlaylist> getStudentPlaylistOfUser(int istarUserId){

		String hql = "from StudentPlaylist studentPlaylist where istarUser.id= :istarUser";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		
		List<StudentPlaylist> allStudentPlaylist = query.list();

		return allStudentPlaylist;		
	}
	
	@SuppressWarnings("unchecked")
	public StudentPlaylist getStudentPlaylistOfUserForLessonOfCourse(int istarUserId, int courseId, int lessonId){
		long previousTime = System.currentTimeMillis();
		String hql = "from StudentPlaylist studentPlaylist where istarUser.id= :istarUserId and course.id= :courseId and lesson.id= :lessonId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUserId",istarUserId);
		query.setParameter("courseId",courseId);
		query.setParameter("lessonId",lessonId);
		
		List<StudentPlaylist> allStudentPlaylist = query.list();

		if(allStudentPlaylist.size()>0){
			//ViksitLogger.logMSG(this.getClass().getName(),("getStudentPlaylistOfUserForLessonOfCourse->" + "Time->"+(System.currentTimeMillis()-previousTime));
			return allStudentPlaylist.get(0);
		}else{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<StudentPlaylist> getStudentPlaylistOfUserForCourse(int istarUserId, int courseId){
		//long previousTime = System.currentTimeMillis();
		String hql = "from StudentPlaylist studentPlaylist where istarUser.id= :istarUserId and course.id= :courseId order by id";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUserId",istarUserId);
		query.setParameter("courseId",courseId);
		
		List<StudentPlaylist> allStudentPlaylist = query.list();

		return allStudentPlaylist;
	}
	
	@SuppressWarnings("unchecked")
	public StudentPlaylist getStudentPlaylistOfUserForLesson(int istarUserId, int lessonId){
		long previousTime = System.currentTimeMillis();
		String hql = "from StudentPlaylist studentPlaylist where istarUser.id= :istarUserId and lesson.id= :lessonId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUserId",istarUserId);
		query.setParameter("lessonId",lessonId);
		
		List<StudentPlaylist> allStudentPlaylist = query.list();

		if(allStudentPlaylist.size()>0){
			//ViksitLogger.logMSG(this.getClass().getName(),("getStudentPlaylistOfUserForLesson->" + "Time->"+(System.currentTimeMillis()-previousTime));
			return allStudentPlaylist.get(0);
		}else{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getCoursesforUser(int istarUserId){

		String sql = "select distinct course_id from student_playlist where student_id= :istarUser";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUser",istarUserId);
		
		List<Integer> allCourses = query.list();
		
		return allCourses;		
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Integer, Integer> getLessonsOfUserForCourse(int istarUserId, int courseId){
		
		HashMap<Integer, Integer> lessonPlaylists = new HashMap<Integer, Integer>();
		
		String sql = "select distinct lesson_id, id from student_playlist where student_id= :istarUserId and course_id= :courseId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId",istarUserId);
		query.setParameter("courseId",courseId);
		
		List<Object[]> result = query.list();
		
		for(Object[] obj : result){
			lessonPlaylists.put((Integer)obj[0], (Integer)obj[1]); 
		}		
		return lessonPlaylists;		
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getCompletedLessonsOfUserForCourse(int istarUserId, int courseId){
		
		String sql = "select distinct lesson_id from student_playlist where student_id= :istarUserId and course_id= :courseId and status!='INCOMPLETE'";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId",istarUserId);
		query.setParameter("courseId",courseId);
		
		List<Integer> allLessons = query.list();
		
		return allLessons;		
	}
	
	public StudentPlaylist getStudentPlaylist(Integer studentPlaylistId){
		StudentPlaylistDAO studentPlaylistDAO = new StudentPlaylistDAO();
		StudentPlaylist studentPlaylist;
		try{
		studentPlaylist = studentPlaylistDAO.findById(studentPlaylistId);
		}catch(IllegalArgumentException e){
			studentPlaylist = null;
		}
		return studentPlaylist;
	}

	public StudentPlaylist saveStudentPlaylistToDAO(StudentPlaylist studentPlaylist) {

		StudentPlaylistDAO studentPlaylistDAO = new StudentPlaylistDAO();

		Session studentPlaylistSession = studentPlaylistDAO.getSession();
		Transaction studentPlaylistTransaction = null;
		try {
			studentPlaylistTransaction = studentPlaylistSession.beginTransaction();
			studentPlaylistSession.save(studentPlaylist);
			studentPlaylistTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (studentPlaylistTransaction != null)
				studentPlaylistTransaction.rollback();
		} finally {
			studentPlaylistSession.close();
		}
		return studentPlaylist;
	}

	public StudentPlaylist updateStudentPlaylistToDAO(StudentPlaylist studentPlaylist) {

		StudentPlaylistDAO studentPlaylistDAO = new StudentPlaylistDAO();

		Session studentPlaylistSession = studentPlaylistDAO.getSession();
		Transaction studentPlaylistTransaction = null;
		try {
			studentPlaylistTransaction = studentPlaylistSession.beginTransaction();
			studentPlaylistSession.update(studentPlaylist);
			studentPlaylistTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (studentPlaylistTransaction != null)
				studentPlaylistTransaction.rollback();
		} finally {
			studentPlaylistSession.close();
		}
		return studentPlaylist;
	}
}


