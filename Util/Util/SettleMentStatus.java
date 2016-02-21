package premiumad.util;

import org.apache.commons.lang3.StringUtils;


public enum SettleMentStatus {
	STARTED,
	IN_PROGRESS,
	FINISHED;
	
	public static SettleMentStatus fromString(String s) {
        for (SettleMentStatus e : values()) {
            if (StringUtils.endsWithIgnoreCase(s, e.name())) {
                return e;
            }
        }
        return null;
    }
}
