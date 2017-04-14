package com.istarindia.apps.services;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.SkillObjective;
import com.viksitpro.core.dao.entities.UserGamification;
import com.viksitpro.core.dao.entities.UserGamificationDAO;

public class UserGamificationServices {
	
	public UserGamification createUserGamification(SkillObjective skillObjective, IstarUser istarUser, Integer points, Integer coins, 
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
	}
	
	@SuppressWarnings("unchecked")
	public List<UserGamification> getUserGamificationsOfUserForItem(int istarUserId, int itemId, String itemType){
		
		String hql = "from UserGamification userGamification where istarUser.id= :istarUser and itemId= :itemId and itemType= :itemType";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		query.setParameter("itemId", itemId);
		query.setParameter("itemType", itemType);
		
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
