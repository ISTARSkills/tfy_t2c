package com.istarindia.android.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "module")
public class ComplexObject {

	private Integer id;
	private String userType;
	private String userCategory;
	private StudentProfile studentProfile;
	private List<SkillReportPOJO> skills;
	private List<TaskSummaryPOJO> tasks;
	private List<AssessmentPOJO> assessments;
	private List<AssessmentReportPOJO> assessmentReports;
	//private List<AssessmentResponsePOJO> assessmentResponses;
	private List<CoursePOJO> courses;
	private List<CourseRankPOJO> leaderboards;
	private List<DailyTaskPOJO> events;
	private List<NotificationPOJO> notifications;
	
	public ComplexObject(){
		
	}

	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlElement(name = "studentProfile", required = false)
	public StudentProfile getStudentProfile() {
		return studentProfile;
	}

	public void setStudentProfile(StudentProfile studentProfile) {
		this.studentProfile = studentProfile;
	}

	@XmlElement(name = "skills", required = false)
	public List<SkillReportPOJO> getSkills() {
		return skills;
	}

	public void setSkills(List<SkillReportPOJO> skills) {
		this.skills = skills;
	}

	@XmlElement(name = "tasks", required = false)
	public List<TaskSummaryPOJO> getTasks() {
		return tasks;
	}

	public void setTasks(List<TaskSummaryPOJO> tasks) {
		this.tasks = tasks;
	}

	@XmlElement(name = "assessments", required = false)
	public List<AssessmentPOJO> getAssessments() {
		return assessments;
	}

	public void setAssessments(List<AssessmentPOJO> assessments) {
		this.assessments = assessments;
	}

	@XmlElement(name = "assessmentReports", required = false)
	public List<AssessmentReportPOJO> getAssessmentReports() {
		return assessmentReports;
	}

	public void setAssessmentReports(List<AssessmentReportPOJO> assessmentReports) {
		this.assessmentReports = assessmentReports;
	}

/*	@XmlElement(name = "assessmentResponses", required = false)
	public List<AssessmentResponsePOJO> getAssessmentResponses() {
		return assessmentResponses;
	}

	public void setAssessmentResponses(List<AssessmentResponsePOJO> assessmentResponses) {
		this.assessmentResponses = assessmentResponses;
	}*/

	@XmlElement(name = "courses", required = false)
	public List<CoursePOJO> getCourses() {
		return courses;
	}

	public void setCourses(List<CoursePOJO> courses) {
		this.courses = courses;
	}

	@XmlElement(name = "leaderboards", required = false)
	public List<CourseRankPOJO> getLeaderboards() {
		return leaderboards;
	}

	public void setLeaderboards(List<CourseRankPOJO> leaderboards) {
		this.leaderboards = leaderboards;
	}

	@XmlElement(name = "events", required = false)
	public List<DailyTaskPOJO> getEvents() {
		return events;
	}

	public void setEvents(List<DailyTaskPOJO> events) {
		this.events = events;
	}

	@XmlElement(name = "notifications", required = false)
	public List<NotificationPOJO> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<NotificationPOJO> notifications) {
		this.notifications = notifications;
	}

	@XmlAttribute(name = "user_type", required = false)
	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	@XmlAttribute(name = "user_category", required = false)
	public String getUserCategory() {
		return userCategory;
	}

	public void setUserCategory(String userCategory) {
		this.userCategory = userCategory;
	}
	
	
	
	
	
}
