package com.istarindia.android.pojo;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "studentProfile")
public class StudentProfile {

	private Integer id;
	private String authenticationToken;
	private String password;
	private String loginType;
	private Boolean isVerified;
	private String email;
	private String firstName;
	private String lastName;
	private Date dateOfBirth;
	private String gender;
	private Long mobile;
	private String location;
	private String profileImage;
	private Integer coins;
	private Integer experiencePoints;
	private Integer batchRank;
	private String underGraduationSpecializationName;
	private String underGraduationDegree;
	private String underGraduationCollege;
	private Integer underGraduationYear;
	private String postGraduationSpecializationName;
	private String postGraduationDegree;
	private String postGraduationCollege;
	private Integer postGraduationYear;
	private List<String> preferredLocations;
	private String resumeURL;

	public StudentProfile(){
		
	}
	
	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlAttribute(name = "token", required = false)
	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public void setAuthenticationToken(String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}
	
	@XmlAttribute(name = "password", required = false)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlAttribute(name = "loginType", required = false)	
	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}
	
	@XmlAttribute(name = "isVerified", required = false)	
	public Boolean getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(Boolean isVerified) {
		this.isVerified = isVerified;
	}

	@XmlAttribute(name = "email", required = false)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@XmlAttribute(name = "firstName", required = false)
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@XmlAttribute(name = "lastName", required = false)
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@XmlAttribute(name = "dateOfBirth", required = false)
	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@XmlAttribute(name = "gender", required = false)
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@XmlAttribute(name = "mobile", required = false)
	public Long getMobile() {
		return mobile;
	}

	public void setMobile(Long mobile) {
		this.mobile = mobile;
	}

	@XmlAttribute(name = "location", required = false)
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@XmlAttribute(name = "profileImage", required = false)
	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	@XmlAttribute(name = "coins", required = false)
	public Integer getCoins() {
		return coins;
	}

	public void setCoins(Integer coins) {
		this.coins = coins;
	}

	@XmlAttribute(name = "experiencePoints", required = false)
	public Integer getExperiencePoints() {
		return experiencePoints;
	}

	public void setExperiencePoints(Integer experiencePoints) {
		this.experiencePoints = experiencePoints;
	}

	@XmlAttribute(name = "batchRank", required = false)
	public Integer getBatchRank() {
		return batchRank;
	}

	public void setBatchRank(Integer batchRank) {
		this.batchRank = batchRank;
	}

	@XmlAttribute(name = "underGraduationSpecializationName", required = false)
	public String getUnderGraduationSpecializationName() {
		return underGraduationSpecializationName;
	}

	public void setUnderGraduationSpecializationName(String underGraduationSpecializationName) {
		this.underGraduationSpecializationName = underGraduationSpecializationName;
	}

	@XmlAttribute(name = "underGraduationDegree", required = false)
	public String getUnderGraduationDegree() {
		return underGraduationDegree;
	}

	public void setUnderGraduationDegree(String underGraduationDegree) {
		this.underGraduationDegree = underGraduationDegree;
	}

	@XmlAttribute(name = "underGraduationCollege", required = false)
	public String getUnderGraduationCollege() {
		return underGraduationCollege;
	}

	public void setUnderGraduationCollege(String underGraduationCollege) {
		this.underGraduationCollege = underGraduationCollege;
	}

	@XmlAttribute(name = "postGraduationSpecializationName", required = false)
	public String getPostGraduationSpecializationName() {
		return postGraduationSpecializationName;
	}

	public void setPostGraduationSpecializationName(String postGraduationSpecializationName) {
		this.postGraduationSpecializationName = postGraduationSpecializationName;
	}

	@XmlAttribute(name = "postGraduationDegree", required = false)
	public String getPostGraduationDegree() {
		return postGraduationDegree;
	}

	public void setPostGraduationDegree(String postGraduationDegree) {
		this.postGraduationDegree = postGraduationDegree;
	}

	@XmlAttribute(name = "postGraduationCollege", required = false)
	public String getPostGraduationCollege() {
		return postGraduationCollege;
	}

	public void setPostGraduationCollege(String postGraduationCollege) {
		this.postGraduationCollege = postGraduationCollege;
	}

	@XmlAttribute(name = "resumeURL", required = false)
	public String getResumeURL() {
		return resumeURL;
	}

	public void setResumeURL(String resumeURL) {
		this.resumeURL = resumeURL;
	}

	@XmlAttribute(name = "underGraduationYear", required = false)
	public Integer getUnderGraduationYear() {
		return underGraduationYear;
	}

	public void setUnderGraduationYear(Integer underGraduationYear) {
		this.underGraduationYear = underGraduationYear;
	}

	@XmlAttribute(name = "postGraduationYear", required = false)
	public Integer getPostGraduationYear() {
		return postGraduationYear;
	}

	public void setPostGraduationYear(Integer postGraduationYear) {
		this.postGraduationYear = postGraduationYear;
	}

	public List<String> getPreferredLocations() {
		return preferredLocations;
	}

	public void setPreferredLocations(List<String> preferredLocations) {
		this.preferredLocations = preferredLocations;
	}
	
	
}
