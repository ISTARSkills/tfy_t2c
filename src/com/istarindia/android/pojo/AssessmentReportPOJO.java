package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "skillReport")
public class AssessmentReportPOJO {

	private Integer id;
	private String name;
	private Double userScore;
	private Double totalScore;
	private Double	accuracy;
	private Double batchAverage;
	private Integer usersAttemptedCount;
	private Integer totalNumberOfUsersInBatch;
	private Integer totalNumberOfQuestions;
	private Integer totalNumberOfCorrectlyAnsweredQuestions;
	private String message;
	private String messageDescription;
	private List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
	private AssessmentResponsePOJO assessmentResponse;
	
	public AssessmentReportPOJO(){
		
	}
	
	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlAttribute(name = "name", required = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute(name = "userScore", required = false)
	public Double getUserScore() {
		return userScore;
	}
	public void setUserScore(Double userScore) {
		this.userScore = Math.round(userScore*100.0)/100.0;
	}
	
	@XmlAttribute(name = "totalScore", required = false)
	public Double getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(Double totalScore) {
		this.totalScore = Math.round(totalScore*100.0)/100.0;
	}
	
	@XmlAttribute(name = "accuracy", required = false)
	public Double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}
	
	@XmlAttribute(name = "batchAverage", required = false)
	public Double getBatchAverage() {
		return batchAverage;
	}
	public void setBatchAverage(Double batchAverage) {
		this.batchAverage = Math.round(batchAverage*100.0)/100.0;
	}
	
	@XmlAttribute(name = "usersAttemptedCount", required = false)
	public Integer getUsersAttemptedCount() {
		return usersAttemptedCount;
	}
	public void setUsersAttemptedCount(Integer usersAttemptedCount) {
		this.usersAttemptedCount = usersAttemptedCount;
	}
	
	@XmlAttribute(name = "totalNumberOfUsersInBatch", required = false)
	public Integer getTotalNumberOfUsersInBatch() {
		return totalNumberOfUsersInBatch;
	}
	public void setTotalNumberOfUsersInBatch(Integer totalNumberOfUsersInBatch) {
		this.totalNumberOfUsersInBatch = totalNumberOfUsersInBatch;
	}
	
	@XmlElement(name = "skillsReport", required = false)
	public List<SkillReportPOJO> getSkillsReport() {
		return skillsReport;
	}
	public void setSkillsReport(List<SkillReportPOJO> skillsReport) {
		this.skillsReport = skillsReport;
	}
	
	@XmlAttribute(name = "totalNumberOfQuestions", required = false)
	public Integer getTotalNumberOfQuestions() {
		return totalNumberOfQuestions;
	}
	public void setTotalNumberOfQuestions(Integer totalNumberOfQuestions) {
		this.totalNumberOfQuestions = totalNumberOfQuestions;
	}
	
	@XmlAttribute(name = "totalNumberOfCorrectlyAnsweredQuestions", required = false)
	public Integer getTotalNumberOfCorrectlyAnsweredQuestions() {
		return totalNumberOfCorrectlyAnsweredQuestions;
	}
	public void setTotalNumberOfCorrectlyAnsweredQuestions(Integer totalNumberOfCorrectlyAnsweredQuestions) {
		this.totalNumberOfCorrectlyAnsweredQuestions = totalNumberOfCorrectlyAnsweredQuestions;
	}		
	
	@XmlAttribute(name = "message", required = false)
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@XmlAttribute(name = "messageDescription", required = false)
	public String getMessageDescription() {
		return messageDescription;
	}

	public void setMessageDescription(String messageDescription) {
		this.messageDescription = messageDescription;
	}
	
	@XmlAttribute(name = "messageDescription", required = false)
	public AssessmentResponsePOJO getAssessmentResponse() {
		return assessmentResponse;
	}

	public void setAssessmentResponse(AssessmentResponsePOJO assessmentResponse) {
		this.assessmentResponse = assessmentResponse;
	}

	public void generateMessageAndDescription(Integer cutOffMarks){
		
		if(this.accuracy > cutOffMarks){
		this.message = "You passed the assessment!" ;
		this.messageDescription = "Make sure you check out the full report to find out where you went wrong.";
		}else{
		this.message = 	"Sorry! You failed the assessment.";
		this.messageDescription = "You need atleast " + cutOffMarks + "% to pass. Brush up on the concepts and try again.";
		}		
	}
	
	
	public void calculateUserScore() {
		double userScore = 0.0;
		for(SkillReportPOJO skillReportPOJO : this.skillsReport){
			userScore = userScore + skillReportPOJO.getUserPoints();
		}
		this.userScore = Math.round(userScore*100.0)/100.0;
	}
	
	public void calculateTotalScore() {
		double totalScore = 0.0;
		for(SkillReportPOJO skillReportPOJO : this.skillsReport){
			totalScore = totalScore + skillReportPOJO.getTotalPoints();
		}
		this.totalScore = Math.round(totalScore*100.0)/100.0;
	}
	
	public void calculateAccuracy() {
		this.accuracy = (double) (Math.round((((double) this.totalNumberOfCorrectlyAnsweredQuestions)/this.totalNumberOfQuestions)*100.0*100.0)/100.0);	
		}
}
