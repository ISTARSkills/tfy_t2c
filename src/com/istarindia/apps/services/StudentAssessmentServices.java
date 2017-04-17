package com.istarindia.apps.services;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.StudentAssessment;
import com.viksitpro.core.dao.entities.StudentAssessmentDAO;

public class StudentAssessmentServices {

	public StudentAssessment createStudentAssessment(Assessment assessment, Question question, IstarUser istarUser, Boolean correct, 
			Boolean option1, Boolean option2, Boolean option3, Boolean option4, Boolean option5, Integer countryId, 
			Integer organizationId, Integer batchGroupId, Integer timeTaken){
		
		StudentAssessment studentAssessment = new StudentAssessment();
		
		studentAssessment.setAssessment(assessment);
		studentAssessment.setQuestion(question);
		studentAssessment.setIstarUser(istarUser);
		studentAssessment.setCorrect(correct);
		studentAssessment.setOption1(option1);
		studentAssessment.setOption2(option2);
		studentAssessment.setOption3(option3);
		studentAssessment.setOption4(option4);
		studentAssessment.setOption5(option5);
		studentAssessment.setCountryId(countryId);
		studentAssessment.setOrganizationId(organizationId);
		studentAssessment.setBatchGroupId(batchGroupId);
		studentAssessment.setTimeTaken(timeTaken);
		
		studentAssessment = saveStudentAssessmentToDAO(studentAssessment);
		
		return studentAssessment;
	}
	
	public StudentAssessment getStudentAssessment(int studentAssessmentId){
		StudentAssessmentDAO studentAssessmentDAO = new StudentAssessmentDAO();
		StudentAssessment studentAssessment;
		try{
		studentAssessment = studentAssessmentDAO.findById(studentAssessmentId);
		}catch(IllegalArgumentException e){
			studentAssessment = null;
		}
		return studentAssessment;
	}
	
	@SuppressWarnings("unchecked")
	public List<StudentAssessment> getStudentAssessmentForUser(int istarUserId, int assessmentId) {

		List<StudentAssessment> allStudentAssessment = new ArrayList<StudentAssessment>();

		String hql = "from StudentAssessment studentAssessment where assessment.id= :assessment and istarUser.id= :istarUser";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		Query query = session.createQuery(hql);
		query.setParameter("assessment", assessmentId);
		query.setParameter("istarUser", istarUserId);

		allStudentAssessment = query.list();

		return allStudentAssessment;
	}

	public StudentAssessment saveStudentAssessmentToDAO(StudentAssessment studentAssessment) {

		StudentAssessmentDAO studentAssessmentDAO = new StudentAssessmentDAO();

		Session studentAssessmentSession = studentAssessmentDAO.getSession();
		Transaction studentAssessmentTransaction = null;
		try {
			studentAssessmentTransaction = studentAssessmentSession.beginTransaction();
			studentAssessmentSession.save(studentAssessment);
			studentAssessmentTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (studentAssessmentTransaction != null)
				studentAssessmentTransaction.rollback();
		} finally {
			studentAssessmentSession.close();
		}
		return studentAssessment;
	}

	public StudentAssessment updateStudentAssessmentToDAO(StudentAssessment studentAssessment) {

		StudentAssessmentDAO studentAssessmentDAO = new StudentAssessmentDAO();

		Session studentAssessmentSession = studentAssessmentDAO.getSession();
		Transaction studentAssessmentTransaction = null;
		try {
			studentAssessmentTransaction = studentAssessmentSession.beginTransaction();
			studentAssessmentSession.update(studentAssessment);
			studentAssessmentTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (studentAssessmentTransaction != null)
				studentAssessmentTransaction.rollback();
		} finally {
			studentAssessmentSession.close();
		}
		return studentAssessment;
	}
}
