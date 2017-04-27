package com.istarindia.apps.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.SkillReportPOJO;
import com.istarindia.android.utility.AppUtility;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

public class AppServices {
	
	
	@SuppressWarnings("unchecked")
	public List<SkillReportPOJO> getSkillsMapOfUser(int istarUserId){
		
		List<SkillReportPOJO> allSkills = new ArrayList<SkillReportPOJO>();
		
		String sql = "select COALESCE(sum(user_gamification.points),0) as total_points, COALESCE(cast(sum(user_gamification.coins) as integer),0) as total_coins, so_session.name as session, so_session.id as session_id, so_module.name as module, so_module.id as module_id,  so_course.name as course, so_course.id as course_id  from user_gamification, skill_objective so_session, skill_objective so_module, skill_objective so_course where user_gamification.skill_objective=so_session.id and so_module.id=so_session.parent_skill and so_module.parent_skill=so_course.id and istar_user= :istarUserId group by session_id,session, module_id, module, course_id, course order by course_id,module_id,session_id";
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		
		List<Object[]> results = query.list();
		
		if(results.size()>0){
			for(Object[] element : results){
				Double userPoints = (Double) element[0];
				//Integer totalCoins = (Integer) element[1];
				String cmsession = (String) element[2];
				Integer cmsessionSkillObjectiveId = (Integer) element[3];
				String module = (String) element[4];
				Integer moduleSkillObjectiveId = (Integer) element[5];
				String course = (String) element[6];
				Integer courseSkillObjectiveId = (Integer) element[7];
				
				SkillReportPOJO courseSkillPOJO = null;
				SkillReportPOJO moduleSkillPOJO = null;
				SkillReportPOJO cmsessionSkillPOJO = null;
				
				for(SkillReportPOJO tempCourseSkillReport : allSkills){
					if(tempCourseSkillReport.getId()==courseSkillObjectiveId){
						courseSkillPOJO = tempCourseSkillReport;
						break;
					}
				}
				
				if(courseSkillPOJO==null){					
					courseSkillPOJO = new SkillReportPOJO();
					courseSkillPOJO.setId(courseSkillObjectiveId);
					courseSkillPOJO.setName(course);
					
					List<SkillReportPOJO> allModuleSkillReportPOJO = new ArrayList<SkillReportPOJO>();
					moduleSkillPOJO = new SkillReportPOJO();
					moduleSkillPOJO.setId(moduleSkillObjectiveId);
					moduleSkillPOJO.setName(module);
					
					List<SkillReportPOJO> allCmsessionSkillReportPOJO = new ArrayList<SkillReportPOJO>();
					cmsessionSkillPOJO = new SkillReportPOJO();
								
					cmsessionSkillPOJO.setId(cmsessionSkillObjectiveId);
					cmsessionSkillPOJO.setName(cmsession);
					cmsessionSkillPOJO.setUserPoints(userPoints);
					cmsessionSkillPOJO.setTotalPoints(getMaxPointsOfCmsessionSkill(cmsessionSkillObjectiveId));
					allCmsessionSkillReportPOJO.add(cmsessionSkillPOJO);
					moduleSkillPOJO.setSkills(allCmsessionSkillReportPOJO);
					allModuleSkillReportPOJO.add(moduleSkillPOJO);
					courseSkillPOJO.setSkills(allModuleSkillReportPOJO);
					
					allSkills.add(courseSkillPOJO);
				}else{
					
					for(SkillReportPOJO tempModuleSkillReport : courseSkillPOJO.getSkills()){
						if(tempModuleSkillReport.getId()==moduleSkillObjectiveId){
							moduleSkillPOJO = tempModuleSkillReport;
						}
					}
					
					if(moduleSkillPOJO==null){
						moduleSkillPOJO = new SkillReportPOJO();
						moduleSkillPOJO.setId(moduleSkillObjectiveId);
						moduleSkillPOJO.setName(module);
						
						List<SkillReportPOJO> allCmsessionSkillReportPOJO = new ArrayList<SkillReportPOJO>();
						cmsessionSkillPOJO = new SkillReportPOJO();
						
						cmsessionSkillPOJO.setId(cmsessionSkillObjectiveId);
						cmsessionSkillPOJO.setName(cmsession);
						cmsessionSkillPOJO.setUserPoints(userPoints);
						cmsessionSkillPOJO.setTotalPoints(getMaxPointsOfCmsessionSkill(cmsessionSkillObjectiveId));
						allCmsessionSkillReportPOJO.add(cmsessionSkillPOJO);
						moduleSkillPOJO.setSkills(allCmsessionSkillReportPOJO);
						courseSkillPOJO.getSkills().add(moduleSkillPOJO);
					}else{
						cmsessionSkillPOJO = new SkillReportPOJO();
						
						cmsessionSkillPOJO.setId(cmsessionSkillObjectiveId);
						cmsessionSkillPOJO.setName(cmsession);
						cmsessionSkillPOJO.setUserPoints(userPoints);
						cmsessionSkillPOJO.setTotalPoints(getMaxPointsOfCmsessionSkill(cmsessionSkillObjectiveId));
						moduleSkillPOJO.getSkills().add(cmsessionSkillPOJO);
					}
				}					
				courseSkillPOJO.calculateUserPoints();
				courseSkillPOJO.calculateTotalPoints();
				courseSkillPOJO.calculatePercentage();
				moduleSkillPOJO.calculateUserPoints();
				moduleSkillPOJO.calculateTotalPoints();
				moduleSkillPOJO.calculatePercentage();				
			}			
		}
		return allSkills;
	}
	
	
	public Double getMaxPointsOfCmsessionSkill(int cmsessionSkillObjectiveId){
		String sql = "select COALESCE(cast(sum(question.difficulty_level) as integer),0) as sum_difficulty_level, "
				+ "COALESCE(cast(count(distinct lesson_skill_objective.lessonid) as integer),0) as lesson_count "
				+ "from skill_objective,lesson_skill_objective,question_skill_objective,question where "
				+ "lesson_skill_objective.learning_objectiveid=skill_objective.id and "
				+ "question_skill_objective.learning_objectiveid=skill_objective.id and "
				+ "question_skill_objective.questionid=question.id and skill_objective.parent_skill= :cmsessionSkillObjectiveId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("cmsessionSkillObjectiveId", cmsessionSkillObjectiveId);
		
		Object[] result = (Object[]) query.list().get(0);
	
		Double maxPoints = 0.0;
		
		Integer difficultyLevelSum = (Integer) result[0];
		Integer numberOfLessons = (Integer) result[1];
				
				System.out.println("difficultyLevelSum->"+difficultyLevelSum+" numberOfLessons->" + numberOfLessons);
				
				try{
				Properties properties = new Properties();
				String propertyFileName = "app.properties";
				InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
					if (inputStream != null) {
						properties.load(inputStream);
						String pointsBenchmark = properties.getProperty("pointsBenchmark");				
						Integer benchmark = Integer.parseInt(pointsBenchmark);
						
						maxPoints = (difficultyLevelSum + (numberOfLessons* benchmark))*1.0;		
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return maxPoints;
	}
	
	public IstarUser assignToken(IstarUser istarUser) {
		System.out.println("Assigning Token");
		String authenticationToken = AppUtility.getRandomString(20);

		IstarUserServices istarUserServices = new IstarUserServices();
		istarUser = istarUserServices.updateAuthenticationTokenForIstarUser(istarUser, authenticationToken);

		return istarUser;
	}

	public Integer sendOTP(String mobile) throws IOException{
		
		String mobTextingURLBase = "https://mobtexting.com/app/index.php/api?method=sms.normal"
				+ "&api_key=0c9ee1130f2a27302bbef3f39360a9eba5f7e48a&sender=TLNTFY";
		
		int OTP = AppUtility.generateOTP();
		
		String message = "One Time Password to login to Talentify is " + OTP;

		System.out.println(message);
		
		String smsURL = mobTextingURLBase + "&to="+URLEncoder.encode(mobile, "UTF-8")+"&message="+URLEncoder.encode(message, "UTF-8");

		System.out.println(smsURL);
		URL urlObject = new URL(smsURL);
		InputStream inputStream = urlObject.openConnection().getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			System.out.println(line);
		}
		bufferedReader.close();

		return OTP;
	}

}
