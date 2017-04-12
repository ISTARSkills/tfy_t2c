package com.istarindia.android.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "skillReport")
public class AssessmentReportPOJO {

	private Integer id;
	private String name;
	private Integer userScore;
	private Integer totalScore;
	private Integer	accuracy;
	private Integer batchAverage;
	private Integer usersAttemptedCount;
	private Integer totalNumberOfUsersInBatch;
	private List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
	
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
	public Integer getUserScore() {
		return userScore;
	}
	public void setUserScore(Integer userScore) {
		this.userScore = userScore;
	}
	
	@XmlAttribute(name = "totalScore", required = false)
	public Integer getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(Integer totalScore) {
		this.totalScore = totalScore;
	}
	
	@XmlAttribute(name = "accuracy", required = false)
	public Integer getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(Integer accuracy) {
		this.accuracy = accuracy;
	}
	
	@XmlAttribute(name = "batchAverage", required = false)
	public Integer getBatchAverage() {
		return batchAverage;
	}
	public void setBatchAverage(Integer batchAverage) {
		this.batchAverage = batchAverage;
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
	
	@XmlAttribute(name = "skillsReport", required = false)
	public List<SkillReportPOJO> getSkillsReport() {
		return skillsReport;
	}
	public void setSkillsReport(List<SkillReportPOJO> skillsReport) {
		this.skillsReport = skillsReport;
	}	
}
