package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Report;
import com.viksitpro.core.dao.entities.ReportDAO;
import com.viksitpro.core.utilities.DBUTILS;

public class ReportServices {
	
	public Report createReport(IstarUser istarUser, Assessment assessment, Integer score,  Integer timeTaken, Integer totalPoints){
		
		Report report = new Report();
		
		java.util.Date date = new java.util.Date();
		Timestamp current = new Timestamp(date.getTime());
		
		report.setIstarUser(istarUser);
		report.setAssessment(assessment);
		report.setTotalPoints(totalPoints);
		report.setCreatedAt(current);
		report.setScore(score);
		report.setTimeTaken(timeTaken);		
		
		report = saveReportToDAO(report);
		
		return report;
	}
	
	public Report updateReport(Report report, IstarUser istarUser, Assessment assessment, Integer score,  Integer timeTaken, Integer totalPoints){

		java.util.Date date = new java.util.Date();
		Timestamp current = new Timestamp(date.getTime());
		
		report.setIstarUser(istarUser);
		report.setAssessment(assessment);
		report.setTotalPoints(totalPoints);
		report.setScore(score);
		report.setTimeTaken(timeTaken);	
		report.setCreatedAt(current);
		
		report = updateReportToDAO(report);
		
		return report;
	}
	
	@SuppressWarnings("unchecked")
	public Report getAssessmentReportForUser(int istarUserId, int assessmentId){
		
		Report report = null;
		String hql = "from Report report where istarUser.id= :istarUserId and assessment.id= :assessmentId";
				
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUserId",istarUserId);
		query.setParameter("assessmentId", assessmentId);
		
		List<Report> allReports = query.list();
		
		if(allReports.size()>0){
			report = allReports.get(0);
		}
		return report;
	}
	
	public Report saveReportToDAO(Report report) {

		ReportDAO reportDAO = new ReportDAO();

		Session reportSession = reportDAO.getSession();
		Transaction reportTransaction = null;
		try {
			reportTransaction = reportSession.beginTransaction();
			reportSession.save(report);
			reportTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (reportTransaction != null)
				reportTransaction.rollback();
		} finally {
			reportSession.close();
		}
		return report;
	}

	public Report updateReportToDAO(Report report) {

		ReportDAO reportDAO = new ReportDAO();

		Session reportSession = reportDAO.getSession();
		Transaction reportTransaction = null;
		try {
			reportTransaction = reportSession.beginTransaction();
			reportSession.update(report);
			reportTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (reportTransaction != null)
				reportTransaction.rollback();
		} finally {
			reportSession.close();
		}
		return report;
	}

	


}
