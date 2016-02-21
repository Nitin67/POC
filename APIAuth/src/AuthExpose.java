import java.security.MessageDigest;
import java.util.HashMap;

public class AuthExpose {
	
	static HashMap<String , APICheck> urlAccessTokenMapping=new HashMap<String, APICheck>();
	
	public String registerAPI(String url,long tim,long hc)
	{

		long unixTime = System.currentTimeMillis() / 1000L;
		MD5 m = new MD5();
		String secretKey = null;
		try {
			secretKey = m.digest(unixTime+url);
			APICheck apiCheck=new APICheck(tim, hc);
			apiCheck.setAccessToken(secretKey);
			urlAccessTokenMapping.put(url, apiCheck);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return secretKey;
	}
	
	
	class MD5 {

		public String digest(String args) throws Exception {

			if (args.length() < 1) {
				System.err.println("String to MD5 digest should be more than 1 char");
				return "";
			}
			String original = args;
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(original.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		}

	}
	
	public Boolean authorize(String url,String accessToken)
	{
		APICheck apiCheck=urlAccessTokenMapping.get(url);
		if(urlAccessTokenMapping.get(url).getAccessToken().equals(accessToken))
		{
			if(apiCheck.currentHits<apiCheck.hitConstraint)
			{
				apiCheck.currentHits++;
				return true;
			}
			else return false;
		}
		else
		{
			return false;
		}
	}
	
	public static void main(String []args)
	{
		AuthExpose authExpose=new AuthExpose();
		String token1=authExpose.registerAPI("abcd", 10, 2);
		String token2=authExpose.registerAPI("abcd", 1, 2);
		for(int i=0;i<1000;i++)
			{
				if(authExpose.authorize("abcd", token1))
					System.out.println("authorize");
				else
					System.out.println("unauthorize");
				
				
				if(authExpose.authorize("abc", token2))
					System.out.println("authorize2");
				else
					System.out.println("unauthorize2");
			}
	}

}
