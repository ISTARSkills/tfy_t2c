package com.istarindia.android.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.istarindia.android.pojo.task.AssessmentTask;
import com.istarindia.android.pojo.task.ClassRoomSessionTask;

@XmlRootElement(name = "module")
public class ComplexObject {

	private Integer id;
	private StudentProfile studentProfile;
	private List<SkillReportPOJO> skills;
	private List<AssessmentTask> assessmentTasks;
	private List<ClassRoomSessionTask> classRoomTasks;
	private List<TaskSummaryPOJO> lessonTasks;
	
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

	@XmlElement(name = "assessment_tasks", required = false)
	public List<AssessmentTask> getAssessmentTasks() {
		return assessmentTasks;
	}

	public void setAssessmentTasks(List<AssessmentTask> assessmentTasks) {
		this.assessmentTasks = assessmentTasks;
	}

	@XmlElement(name = "classroom_tasks", required = false)
	public List<ClassRoomSessionTask> getClassRoomTasks() {
		return classRoomTasks;
	}

	public void setClassRoomTasks(List<ClassRoomSessionTask> classRoomTasks) {
		this.classRoomTasks = classRoomTasks;
	}

	@XmlElement(name = "lesson_tasks", required = false)
	public List<TaskSummaryPOJO> getLessonTasks() {
		return lessonTasks;
	}

	public void setLessonTasks(List<TaskSummaryPOJO> lessonTasks) {
		this.lessonTasks = lessonTasks;
	}
	
	
	
}
