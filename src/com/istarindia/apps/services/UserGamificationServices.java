package com.istarindia.apps.services;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.SkillObjective;
import com.viksitpro.core.dao.entities.UserGamification;
import com.viksitpro.core.dao.entities.UserGamificationDAO;

public class UserGamificationServices {
	
/*	public UserGamification createUserGamification11(SkillObjective skillObjective, IstarUser istarUser, Double points, Integer coins, 
			Integer itemId, String itemType){
	
		UserGamification userGamification = new UserGamification();
		
		java.util.Date date = new java.util.Date();
		Timestamp current = new Timestamp(date.getTime());
		
		userGamification.setSkillObjective(skillObjective);
		userGamification.setIstarUser(istarUser);
		userGamification.setCoins(coins);
		userGamification.setPoints(points);
		userGamification.setItemId(itemId);
		userGamification.setItemType(itemType);
		userGamification.setCreatedAt(current);
		userGamification.setUpdatedAt(current);
		
		userGamification = saveUserGamificationToDAO(userGamification);
		
		return userGamification;
	}*/
	
	@SuppressWarnings("unchecked")
	public List<UserGamification> getUserGamificationsOfUserForItem(int istarUserId, int itemId, String itemType){
		
		String hql = "from UserGamification userGamification where istarUser.id= :istarUser and itemId= :itemId and itemType= :itemType order by itemId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		query.setParameter("itemId", itemId);
		query.setParameter("itemType", itemType);
		
		List<UserGamification> allUserGamifications = query.list();
				
		return allUserGamifications;
	}
	
	@SuppressWarnings("unchecked")
	public List<UserGamification> getUserGamificationsOfUserForItemType(int istarUserId, String itemType){
		
		String hql = "from UserGamification userGamification where istarUser.id= :istarUser and itemType= :itemType order by itemId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		query.setParameter("itemType", itemType);
		
		List<UserGamification> allUserGamifications = query.list();
				
		return allUserGamifications;
	}
	
	public Double getTotalPointsOfUserForItem(int istarUserId, int itemId, String itemType){
		
		String hql = "select COALESCE ((select sum(points) from user_gamification where istar_user= :istarUser and item_id= :itemId and item_type= :itemType),0)";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(hql);
		query.setParameter("istarUser",istarUserId);
		query.setParameter("itemId", itemId);
		query.setParameter("itemType", itemType);
		
		Double points = (Double) query.list().get(0);
				
		return points;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<UserGamification> getAllUserGamificationsOfUser(int istarUserId){
		
		String hql = "from UserGamification userGamification where istarUser.id= :istarUser";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		
		List<UserGamification> allUserGamifications = query.list();
				
		return allUserGamifications;
	}
	
	public UserGamification getUserGamification(Integer userGamificationId){
		UserGamificationDAO userGamificationDAO = new UserGamificationDAO();
		UserGamification userGamification;
		try{
		userGamification = userGamificationDAO.findById(userGamificationId);
		}catch(IllegalArgumentException e){
			userGamification = null;
		}
		return userGamification;
	}

	public UserGamification saveUserGamificationToDAO(UserGamification userGamification) {

		UserGamificationDAO userGamificationDAO = new UserGamificationDAO();

		Session userGamificationSession = userGamificationDAO.getSession();
		Transaction userGamificationTransaction = null;
		try {
			userGamificationTransaction = userGamificationSession.beginTransaction();
			userGamificationSession.save(userGamification);
			userGamificationTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (userGamificationTransaction != null)
				userGamificationTransaction.rollback();
		} finally {
			userGamificationSession.close();
		}
		return userGamification;
	}

	public UserGamification updateUserGamificationToDAO(UserGamification userGamification) {

		UserGamificationDAO userGamificationDAO = new UserGamificationDAO();

		Session userGamificationSession = userGamificationDAO.getSession();
		Transaction userGamificationTransaction = null;
		try {
			userGamificationTransaction = userGamificationSession.beginTransaction();
			userGamificationSession.update(userGamification);
			userGamificationTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (userGamificationTransaction != null)
				userGamificationTransaction.rollback();
		} finally {
			userGamificationSession.close();
		}
		return userGamification;
	}
}
