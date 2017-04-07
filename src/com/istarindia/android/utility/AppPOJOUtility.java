package com.istarindia.android.utility;

import com.istarindia.android.pojo.StudentProfile;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.pojo.recruiter.IstarUserPOJO;

public class AppPOJOUtility {

	public IstarUserPOJO getIstarUserPOJO(IstarUser istarUser) {

		IstarUserPOJO istarUserPOJO = new IstarUserPOJO();

		istarUserPOJO.setIstarUserId(istarUser.getId());
		istarUserPOJO.setAuthenticationToken(istarUser.getAuthToken());
		istarUserPOJO.setEmail(istarUser.getEmail());
		istarUserPOJO.setIsVerified(istarUser.getIsVerified());

		if (istarUser.getUserRoles().iterator().hasNext()) {
			istarUserPOJO.setRole(istarUser.getUserRoles().iterator().next().getRole().getRoleName());
		}

		if (istarUser.getUserProfile() != null) {
			istarUserPOJO.setName(istarUser.getUserProfile().getFirstName());
		}

		return istarUserPOJO;
	}

	public StudentProfile getStudentProfile(IstarUser student) {
System.out.println("POJO service");
		StudentProfile studentProfile = new StudentProfile();

		studentProfile.setId(student.getId());
		studentProfile.setMobile(student.getMobile());
		studentProfile.setEmail(student.getEmail());
		studentProfile.setAuthenticationToken(student.getAuthToken());
		studentProfile.setIsVerified(student.getIsVerified());
		studentProfile.setLoginType(student.getLoginType());

		if (student.getUserProfile() != null) {
			System.out.println("FIRST NAME POJO is->" + student.getUserProfile().getFirstName());
			studentProfile.setFirstName(student.getUserProfile().getFirstName());
			studentProfile.setLastName(student.getUserProfile().getLastName());
			studentProfile.setGender(student.getUserProfile().getGender());
			studentProfile.setDateOfBirth(student.getUserProfile().getDob());
			
			if(student.getUserProfile().getAddress()!=null){
			studentProfile.setLocation(student.getUserProfile().getAddress().getPincode().getCity());
			}
			/*
			 * studentPOJO.setProfileImage(student.getUserProfile().
			 * getProfileImage());
			 */
			if(student.getUserProfile().getProfileImage()==null){
			studentProfile.setProfileImage("/root/recruiter/pictures/student.png");
			}else{
			studentProfile.setProfileImage(student.getUserProfile().getProfileImage());
			}
		}

		if (student.getProfessionalProfile() != null) {
			studentProfile.setUnderGraduationDegree(student.getProfessionalProfile().getUnderGraduateDegreeName());
			studentProfile.setPostGraduationDegree(student.getProfessionalProfile().getPgDegreeName());
			studentProfile.setUnderGraduationCollege(student.getProfessionalProfile().getUnderGraduationCollege());
			studentProfile.setPostGraduationCollege(student.getProfessionalProfile().getPostGraduationCollege());
			studentProfile.setUnderGraduationSpecializationName(
					student.getProfessionalProfile().getUnderGraduationSpecializationName());
			studentProfile.setPostGraduationSpecializationName(
					student.getProfessionalProfile().getPostGraduationSpecializationName());
			studentProfile.setResumeURL(student.getProfessionalProfile().getResumeUrl());
			studentProfile.setCoins(250);
			studentProfile.setExperiencePoints(3450);
			studentProfile.setBatchRank(2);
		}
		return studentProfile;
	}
}
