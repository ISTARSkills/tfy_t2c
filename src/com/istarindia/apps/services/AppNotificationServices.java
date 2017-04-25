package com.istarindia.apps.services;

import java.util.ArrayList;
import java.util.List;

import com.istarindia.android.pojo.NotificationPOJO;
import com.istarindia.android.utility.AppPOJOUtility;
import com.viksitpro.core.dao.entities.IstarNotification;
import com.viksitpro.core.notification.IstarNotificationServices;

public class AppNotificationServices {

	public List<NotificationPOJO> getNotificationsForUser(int istarUserId){
		
		List<NotificationPOJO> allNotificationPOJOs = new ArrayList<NotificationPOJO>();	
		
		IstarNotificationServices istarNotificationServices = new IstarNotificationServices();
		List<IstarNotification> allNotifications = istarNotificationServices.getAllNotificationOfUser(istarUserId);
		
		AppPOJOUtility appPOJOUtility= new AppPOJOUtility();
		for(IstarNotification istarNotification : allNotifications){
			allNotificationPOJOs.add(appPOJOUtility.getNotificationPOJO(istarNotification));
		}		
		return allNotificationPOJOs;
	}

}
