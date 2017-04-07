package t2c;

import java.io.IOException;

import com.istarindia.android.utility.AppUtility;
import com.istarindia.apps.services.AppServices;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.UserProfile;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

public class Test {

	public static void main(String[] args) {
	
		try {
			new AppServices().sendOTP("9591940080");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void istarUserTest(){
		
		String email = "aeewwbc_11112311@istar.com";
		String socialMedia = "GOOGLE";
		String name = "TEST ISTAR";
		String profileImage = "student.png";		
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByEmail(email);
		
		if (istarUser == null) {

			istarUser = istarUserServices.createIstarUser(email, "test123", null, null, socialMedia);
			UserProfile userProfile = istarUserServices.createUserProfile(istarUser.getId(), null, name, null, null,
					null, profileImage, null);

			istarUser = userProfile.getIstarUser();

			if (istarUser.getUserProfile() == null) {
				System.out.println("User profile is null");
			} else {
				System.out.println("User profile is NOT null");
			}
		
		} else{
			System.out.println("User exists");
			System.out.println("UserProfile ID is "+ istarUser.getUserProfile().getId());
		}
		
		System.exit(0);
	}
	
	

}
