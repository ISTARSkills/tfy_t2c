package com.istarindia.android.utility;


import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.pojo.recruiter.IstarUserPOJO;
import com.viksitpro.core.pojo.recruiter.StudentPOJO;

public class AppPOJOUtility {

	public IstarUserPOJO getIstarUserPOJO(IstarUser istarUser){
		
		IstarUserPOJO istarUserPOJO = new IstarUserPOJO();

		istarUserPOJO.setIstarUserId(istarUser.getId());
		istarUserPOJO.setAuthenticationToken(istarUser.getAuthToken());
		istarUserPOJO.setEmail(istarUser.getEmail());

		if (istarUser.getUserRoles().iterator().hasNext()) {
			istarUserPOJO.setRole(istarUser.getUserRoles().iterator().next().getRole().getRoleName());
		}

		if (istarUser.getUserProfile() != null) {
			istarUserPOJO.setName(istarUser.getUserProfile().getFirstName());
		}
		
		return istarUserPOJO;
	}
	
	public StudentPOJO getStudentPOJO(IstarUser student) {

		StudentPOJO studentPOJO = new StudentPOJO();

		studentPOJO.setIstarUser(student.getId());

		if (student.getUserProfile() != null) {
			studentPOJO.setFirstName(student.getUserProfile().getFirstName());
			studentPOJO.setLastName(student.getUserProfile().getLastName());
			studentPOJO.setGender(student.getUserProfile().getGender());
			studentPOJO.setLocation(student.getUserProfile().getAddress().getPincode().getCity());
			/*studentPOJO.setProfileImage(student.getUserProfile().getProfileImage());*/
			studentPOJO.setProfileImage("/root/recruiter/pictures/student.png");
		}

		if (student.getProfessionalProfile() != null) {
			studentPOJO.setUnderGraduationDegree(student.getProfessionalProfile().getUnderGraduateDegreeName());
			studentPOJO.setPostGraduationDegree(student.getProfessionalProfile().getPgDegreeName());
			studentPOJO.setUnderGraduationCollege(
					student.getProfessionalProfile().getUnderGraduationCollege());
			studentPOJO.setPostGraduationCollege(
					student.getProfessionalProfile().getPostGraduationCollege());
			studentPOJO.setUnderGraduationSpecializationName(
					student.getProfessionalProfile().getUnderGraduationSpecializationName());
			studentPOJO.setPostGraduationSpecializationName(
					student.getProfessionalProfile().getPostGraduationSpecializationName());
			studentPOJO.setResumeURL(student.getProfessionalProfile().getResumeUrl());
		}
		return studentPOJO;
	}
}
