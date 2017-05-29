package t2c;

import java.io.IOException;
import java.util.List;

import com.istarindia.android.utility.AppUtility;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.AppServices;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.dao.entities.Cmsession;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.entities.StudentPlaylistDAO;
import com.viksitpro.core.dao.entities.UserProfile;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

public class Test {

	public static void main(String[] args) {
	
		try {
			test();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void test(){
		
		AppCourseServices appCourseServices = new AppCourseServices();
		StudentPlaylistServices StudentPlaylistServices= new StudentPlaylistServices();
		for(StudentPlaylist sp : (new StudentPlaylistDAO()).findAll()){
			if(sp.getModule()!=null || sp.getCmsession()!=null){
			//System.out.println("Updating--->"+ sp.getId());
			Module module = appCourseServices.getModuleOfLesson(sp.getLesson().getId());
			if(module!=null){
				List<Cmsession> allCmsessions =  appCourseServices.getCmsessionsOfModule(module.getId());
				if(allCmsessions.size()>0){
/*					sp.setModule(module);
					sp.setCmsession(allCmsessions.get(0));
					StudentPlaylistServices.updateStudentPlaylistToDAO(sp);
					System.out.println("Updated--->"+ sp.getId());*/
					System.out.println("UPDATE student_playlist SET module_id="+module.getId()+" , cmsession_id="+allCmsessions.get(0).getId()+" WHERE (id="+sp.getId()+");");
				}
			}
			}
		}
		System.out.println("Finished");
	}
}
