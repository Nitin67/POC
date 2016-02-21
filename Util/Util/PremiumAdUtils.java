package premiumad.util;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;

import com.premiumad.request.EditPremiumAdRequest;

import models.premiumAD.Ad;

public class PremiumAdUtils {
    
    private static final Logger log = play.Logger.underlying();

	  public static boolean isAdChangedOnEdit(Ad ad, EditPremiumAdRequest requestParamMap) {
		    // TODO: Need to Improve this once Shailender refactor Validator
		    if (ad.getCategoryId() != requestParamMap.getSubcatid()) {
		      return true;
		    }

		    if (!ad.getAreaId().equals(requestParamMap.getCityId())) {
		      return true;
		    }

		    if (!(ad.getLocation() != null && ad.getLocation().equals(requestParamMap.getLocation()))) {
		      return true;
		    }
		    // rray('mTopicGlobalId','parentGlobalCategoryId','attributeString','areaId','md5hashTitle','md5hashDescription','location','image1','image2','image3','image4','image5','image6','image7','image8','userNickname','email','mobile','NCACreationValidation');
		    // $compareUserObj = array('nickname','email','mobile','firstName');
		    return true;
		  }
	  
	  
	  
	 public static <T> T updateDao(T oldT,T newT,Class<T> clas)
	 {

			Class cls = clas;
			Field fieldlist[] = cls.getDeclaredFields();
			Field.setAccessible(fieldlist, true);
			for (Field field : fieldlist) {
				   try {
					Object oldVal=field.get(oldT);
					Object newVal=field.get(newT);
					if(newVal!=null && !newVal.equals(oldVal))
						field.set(oldVal, newVal);
					
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error("Unable to update dao due to IllegalArgumentException and IllegalAccessException", e);
				}
				  
			}
		
		return oldT;
	 }
	 public static String getHostName() {
		    try {
		      return InetAddress.getLocalHost().getHostName();
		    } catch (UnknownHostException e) {
		      play.Logger.error("Error getting host name: " + e.getMessage());
		      return null;
		    }
		  }
}
