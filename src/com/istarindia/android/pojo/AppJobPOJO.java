package com.istarindia.android.pojo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.internal.txw2.annotation.XmlCDATA;

@XmlRootElement(name = "job")
public class AppJobPOJO {

	private Integer id;
	private String jobName;
	private String companyName;
	private String companyLogo;
	private String jobDescription;
	private ArrayList<String> jobLocation = new ArrayList<String>();
	private Integer taskId;
	private Timestamp startDate;
	private Timestamp endDate;
	private String stage;
	private String type;
	private String tag;
	private String salaryRange;
	private String minimumSalary;
	private String maximumSalary;
	private List<AppActivityPOJO> activities = new ArrayList<AppActivityPOJO>();

	public AppJobPOJO(){
		
	}

	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlAttribute(name = "jobName", required = false)
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@XmlAttribute(name = "companyName", required = false)
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	@XmlAttribute(name = "companyLogo", required = false)
	public String getCompanyLogo() {
		return companyLogo;
	}

	public void setCompanyLogo(String companyLogo) {
		this.companyLogo = companyLogo;
	}

	@XmlCDATA
	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	@XmlAttribute(name = "jobLocation", required = false)
	public ArrayList<String> getJobLocation() {
		return jobLocation;
	}

	public void setJobLocation(ArrayList<String> jobLocation) {
		this.jobLocation = jobLocation;
	}

	@XmlAttribute(name = "taskId", required = false)
	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	@XmlAttribute(name = "startDate", required = false)
	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	@XmlAttribute(name = "endDate", required = false)
	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	@XmlAttribute(name = "stage", required = false)
	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	@XmlAttribute(name = "type", required = false)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlAttribute(name = "tag", required = false)
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	@XmlAttribute(name = "salaryRange", required = false)
	public String getSalaryRange() {
		return salaryRange;
	}

	public void setSalaryRange(String salaryRange) {
		this.salaryRange = salaryRange;
	}

	@XmlAttribute(name = "minimumSalary", required = false)
	public String getMinimumSalary() {
		return minimumSalary;
	}

	public void setMinimumSalary(String minimumSalary) {
		this.minimumSalary = minimumSalary;
	}

	@XmlAttribute(name = "maximumSalary", required = false)
	public String getMaximumSalary() {
		return maximumSalary;
	}

	public void setMaximumSalary(String maximumSalary) {
		this.maximumSalary = maximumSalary;
	}

	@XmlAttribute(name = "activities", required = false)
	public List<AppActivityPOJO> getActivities() {
		return activities;
	}

	public void setActivities(List<AppActivityPOJO> activities) {
		this.activities = activities;
	}
	
	
}

