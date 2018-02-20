package t2c;

import java.util.List;

import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.dao.entities.Cmsession;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.entities.StudentPlaylistDAO;

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
			////ViksitLogger.logMSG(this.getClass().getName(),"Updating--->"+ sp.getId());
			Module module = appCourseServices.getModuleOfLesson(sp.getLesson().getId());
			if(module!=null){
				List<Cmsession> allCmsessions =  appCourseServices.getCmsessionsOfModule(module.getId());
				if(allCmsessions.size()>0){
/*					sp.setModule(module);
					sp.setCmsession(allCmsessions.get(0));
					StudentPlaylistServices.updateStudentPlaylistToDAO(sp);
					//ViksitLogger.logMSG(this.getClass().getName(),"Updated--->"+ sp.getId());*/
					//ViksitLogger.logMSG(this.getClass().getName(),"UPDATE student_playlist SET module_id="+module.getId()+" , cmsession_id="+allCmsessions.get(0).getId()+" WHERE (id="+sp.getId()+");");
				}
			}
			}
		}
		//ViksitLogger.logMSG(this.getClass().getName(),"Finished");
	}
}
