package com.istarindia.apps.services;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.entities.StudentPlaylistDAO;

public class StudentPlaylistServices {
	
	public StudentPlaylist updateStatus(StudentPlaylist studentPlaylist, String status){
		
		studentPlaylist.setStatus(status);		
		studentPlaylist = updateStudentPlaylistToDAO(studentPlaylist);
		
		return studentPlaylist;
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


