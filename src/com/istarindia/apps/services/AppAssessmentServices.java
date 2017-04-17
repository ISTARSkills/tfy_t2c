package com.istarindia.apps.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import com.istarindia.android.pojo.AssessmentReportPOJO;
import com.istarindia.android.pojo.SkillReportPOJO;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentBenchmark;
import com.viksitpro.core.dao.entities.AssessmentDAO;
import com.viksitpro.core.dao.entities.AssessmentQuestion;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.SkillObjective;
import com.viksitpro.core.dao.entities.SkillObjectiveDAO;
import com.viksitpro.core.dao.entities.UserGamification;

public class AppAssessmentServices {

	public AssessmentReportPOJO getAssessmentReport(int istarUserId, int assessmentId) {

		AssessmentReportPOJO assessmentReportPOJO = new AssessmentReportPOJO();

		UserGamificationServices userGamificationServices = new UserGamificationServices();
		List<UserGamification> allUserGamification = userGamificationServices
				.getUserGamificationsOfUserForItem(istarUserId, assessmentId, "ASSESSMENT");

		List<SkillReportPOJO> allSkillsReport = new ArrayList<SkillReportPOJO>();

		for (UserGamification userGamification : allUserGamification) {

			SkillObjective cmsessionSkillObjective = userGamification.getSkillObjective();
			SkillObjective moduleSkillObjective = getSkillObjective(cmsessionSkillObjective.getParentSkill());

			Double totalPoints = getMaxPointsForSkillObjectiveOfAssessment(assessmentId,
					cmsessionSkillObjective.getId());
			Double userPoints = userGamification.getPoints();

			// Double totalPointsOfCmsession = 0.0;

			SkillReportPOJO moduleSkillReportPOJO = null;
			SkillReportPOJO cmsessionSkillReportPOJO = null;

			for (SkillReportPOJO tempModuleSkillReportPOJO : allSkillsReport) {
				if (tempModuleSkillReportPOJO.getId() == moduleSkillObjective.getId()) {
					moduleSkillReportPOJO = tempModuleSkillReportPOJO;
					break;
				}
			}

			if (moduleSkillReportPOJO != null) {

				for (SkillReportPOJO tempCmsessionSkillReportPOJO : moduleSkillReportPOJO.getSkills()) {
					if (tempCmsessionSkillReportPOJO.getId() == cmsessionSkillObjective.getId()) {
						cmsessionSkillReportPOJO = tempCmsessionSkillReportPOJO;
						break;
					}
				}

				if (cmsessionSkillReportPOJO != null) {

					cmsessionSkillReportPOJO.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints() + totalPoints);
					cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints() + userPoints);
					cmsessionSkillReportPOJO.setPercentage();
				} else {
					cmsessionSkillReportPOJO = new SkillReportPOJO();

					cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
					cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
					cmsessionSkillReportPOJO.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints() + totalPoints); // total
																														// points
																														// from
																														// assessment
																														// benchmark
					cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints() + userPoints); // points
					cmsessionSkillReportPOJO.setPercentage();

					moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
				}
				moduleSkillReportPOJO.setTotalPoints();
				moduleSkillReportPOJO.setUserPoints();
				moduleSkillReportPOJO.setPercentage();
			} else {
				moduleSkillReportPOJO = new SkillReportPOJO();

				moduleSkillReportPOJO.setId(moduleSkillObjective.getId());
				moduleSkillReportPOJO.setName(moduleSkillObjective.getName());
				moduleSkillReportPOJO.setSkills((new ArrayList<SkillReportPOJO>()));

				cmsessionSkillReportPOJO = new SkillReportPOJO();

				cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
				cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
				cmsessionSkillReportPOJO.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints() + totalPoints);
				cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints() + userPoints);
				cmsessionSkillReportPOJO.setPercentage();

				moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);

				moduleSkillReportPOJO.setTotalPoints();
				moduleSkillReportPOJO.setUserPoints();
				moduleSkillReportPOJO.setPercentage();

				allSkillsReport.add(moduleSkillReportPOJO);
			}
		}
		assessmentReportPOJO.setSkillsReport(allSkillsReport);

		return assessmentReportPOJO;
	}


	public HashMap<Integer,Integer> getSkillsMapOfAssessment(Assessment assessment){
		
		HashMap<Integer,Integer> skillsMap = new HashMap<Integer, Integer>();
		
		HashSet<Integer> allCmsessionSkillObjectivesOfAssessment = new HashSet<Integer>();
		
		for(AssessmentQuestion assessmentQuestion : assessment.getAssessmentQuestions()){
			Question question = assessmentQuestion.getQuestion();
			
			for(SkillObjective skillObjective : question.getSkillObjectives()){
				allCmsessionSkillObjectivesOfAssessment.add(skillObjective.getParentSkill());				
			}			
		}
		
		for(Integer cmsessionSkillObjectiveId : allCmsessionSkillObjectivesOfAssessment){
			SkillObjective skillObjective = getSkillObjective(cmsessionSkillObjectiveId);
			skillsMap.put(cmsessionSkillObjectiveId, skillObjective.getParentSkill());			
		}	
		
		System.out.println("Get Skill Maps of Assessment");
		
		return skillsMap;
	}

	@SuppressWarnings("unchecked")
	public Double getMaxPointsForSkillObjectiveOfAssessment(Integer assessmentId, Integer skillObjectiveId){
		
		String sql = "from AssessmentBenchmark assessmentBenchmark where assessment.id= :assessmentId and skillObjective.id= :skillObjectiveId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(sql);
		query.setParameter("assessmentId",assessmentId);
		query.setParameter("skillObjectiveId",skillObjectiveId);
		
		List<AssessmentBenchmark> allAssessmentBenchmark = query.list();
		
		if(allAssessmentBenchmark.size()>0){
			return allAssessmentBenchmark.get(0).getMaxPoints();
		}else{
			return 0.0;
		}		
	} 
	
/*	public HashMap<Integer, Set<Integer>> getSkillsMapOfAssessment(int assessmentId){
		
		HashMap<Integer, Set<Integer>> skillsMap = new HashMap<Integer, Set<Integer>>();
		
		Assessment assessment = getAssessment(assessmentId);	
		HashSet<Integer> allCmsessionSkillObjectivesOfAssessment = new HashSet<Integer>();
		
		for(AssessmentQuestion assessmentQuestion : assessment.getAssessmentQuestions()){
			Question question = assessmentQuestion.getQuestion();
			
			for(SkillObjective skillObjective : question.getSkillObjectives()){
				allCmsessionSkillObjectivesOfAssessment.add(skillObjective.getParentSkill());				
			}			
		}
		
		for(Integer cmsessionSkillObjectiveId : allCmsessionSkillObjectivesOfAssessment){
			SkillObjective skillObjective = getSkillObjective(cmsessionSkillObjectiveId);
			
			Integer moduleSkillObjective = skillObjective.getParentSkill();
			
			if(skillsMap.containsKey(moduleSkillObjective)){				
				skillsMap.get(moduleSkillObjective).add(cmsessionSkillObjectiveId);				
			}else{				
				Set<Integer> cmsessionSkillObjectiveSet = new HashSet<Integer>();				
				cmsessionSkillObjectiveSet.add(cmsessionSkillObjectiveId);			
				skillsMap.put(moduleSkillObjective, cmsessionSkillObjectiveSet);
			}
		}		
		return skillsMap;
	}
*/
	public Assessment getAssessment(int assessmentId) {
		Assessment assessment;
		AssessmentDAO assessmentDAO = new AssessmentDAO();
		try {
			assessment = assessmentDAO.findById(assessmentId);
		} catch (IllegalArgumentException e) {
			assessment = null;
		}
		return assessment;
	}
	
	public SkillObjective getSkillObjective(Integer skillObjectiveId) {
		SkillObjectiveDAO skillObjectiveDAO = new SkillObjectiveDAO();
		SkillObjective skillObjective;
		try {
			skillObjective = skillObjectiveDAO.findById(skillObjectiveId);
		} catch (IllegalArgumentException e) {
			skillObjective = null;
		}
		return skillObjective;
	}
	
}
