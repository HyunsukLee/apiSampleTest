package com.example.chatBot; //package name 변경 필요

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;


/*
 * @ Date : 2019.10.25
 * @ Auth : hyunsuk
 * @ Desc
 * 			Samsung chatbot 호출 api
 * 			기존 수발신 API 와 interface 만 한다
 * 
 * */
@RestController
public class ChatBotController {

	/*
	 * chatBot API
	 * 
	 * */
	@RequestMapping(value = "/chatBot", produces = "application/json")
	public searchInfo chatBot(HttpServletRequest request, HttpServletResponse response) throws Exception {

		// 전화번호 받기
		String phoneNumber = request.getParameter("phoneNumber");

		HashMap<String, Object> rMap = new HashMap();
		ObjectMapper mapper = new ObjectMapper();
		searchInfo sInfo = new searchInfo();
		
		try {
			rMap.put("I_SCH_PH",phoneNumber);
			rMap.put("I_USER_PH_FLAG", "N");
			rMap.put("I_IN_OUT","I");
			rMap.put("I_RQ_TYPE","0");
			rMap.put("I_CALL_TYPE","P");
			rMap.put("I_PH_BOOK_FLAG","N");
			rMap.put("I_ACCESS_IP","192.168.0.1");
			rMap.put("I_VERSION","G2.8.00");
			rMap.put("I_OS","7.1");
			rMap.put("test",true);				

			String json = mapper.writeValueAsString(rMap);
			String r = sendREST("https://api.whox2.com/whowho_app/v4/search/call/live", json);
			
			JSONObject jn = null;
			JSONObject jnSpam = null;
			JSONObject jnKeyword = null;
			
			jn = new JSONObject(r);
			
			String	O_SCH_SPAM			= (String)jn.get("O_SCH_SPAM");		// 스팸정보
			String	O_SAFE_PH			= (String)jn.get("O_SAFE_PH");		// 안심정보 / 표현
			String	O_SHARE_KEYWORD		= (String)jn.get("O_SHARE_KEYWORD");// 공유정보
			String	SPAM_INFO_TEXT		= "0 건";							// 스팸정보 표현
			String	SHARE_KEYWORD_TEXT	= "-";								// 공유정보 표현
			int		i					= 0;
			
				i = Integer.parseInt(O_SAFE_PH); 
			
			if(!"".equals(O_SCH_SPAM)) {
				jnSpam = new JSONObject(O_SCH_SPAM);
				
				String O_SCH_SPAM_totalCount = (String)jnSpam.get("TOTALCOUNT");
				String VALUES		= getString(jnSpam,"VALUES","");
				JSONObject jsonValues = createObject(VALUES.replaceAll("[\\[\\]]", ""));
				String name  = (String)jsonValues.get("NAME");
				int count = (int) jsonValues.get("COUNT");
				
				SPAM_INFO_TEXT = name + " " + count + "건 (총 "+ O_SCH_SPAM_totalCount +"건)";
				i++;
			}
			
			if(!"".equals(O_SHARE_KEYWORD)) {
				jnKeyword = new JSONObject(O_SHARE_KEYWORD);
				SHARE_KEYWORD_TEXT = getString(jnKeyword,"SHARE_INFO","");
				i++;
			}
			

			if(i == 0) {
				sInfo.setO_SCH_SPAM("이 번호는 정보가 없어요ㅠㅠ");
			}else {
				sInfo.setO_SCH_SPAM(SPAM_INFO_TEXT); //스팸정보
				sInfo.setO_SAFE_PH(O_SAFE_PH +" 건"); // 안심정보
				sInfo.setO_SHARE_KEYWORD(SHARE_KEYWORD_TEXT); //공유정보
			}
		}catch(Exception e) {
			e.printStackTrace();
		}			
		
		return sInfo;
	}
	
	
	/*
	 * json data call
	 * method : post
	 * 
	 * */
	public static String sendREST(String sendUrl, String jsonValue) throws IllegalStateException {

		String inputLine = null;
		StringBuffer outResult = new StringBuffer();

		  try{
			URL url = new URL(sendUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept-Charset", "UTF-8"); 
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
		      
			OutputStream os = conn.getOutputStream();
			os.write(jsonValue.getBytes("UTF-8"));
			os.flush();
		    
			// 리턴된 결과 읽기
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			while ((inputLine = in.readLine()) != null) {
				outResult.append(inputLine);
			}
		    
			conn.disconnect();
		  }catch(Exception e){
			  //System.out.println(e.getMessage(), e);
		      //e.printStackTrace();
		  }	
		  
		  return outResult.toString();
	}
	
	
	public static String getString(JSONObject json, String key, String defVal)
	{
		if(json != null)
		{
			if(json.has(key))
			{
				try
				{
					return json.getString(key).replaceAll("^null$", "");
				}
				catch (JSONException e)
				{
					//e.printStackTrace();
					//Log.w(e);
				}
			}
		}
		return defVal;
	}
	
	public static JSONObject createObject(String jsonStr)
	{
		JSONObject jsObj = null;
		if(jsonStr == null || "".equals(jsonStr))
		{
			return null;
		}
		try
		{
			jsObj = new JSONObject(jsonStr);
		}
		catch (JSONException e)
		{
			//e.printStackTrace();
			////Log.w(e);
		}
		return jsObj;
	}
	
}


class searchInfo {
	private String O_SCH_SPAM = "";
	private String O_SAFE_PH = "";
	private String O_SHARE_KEYWORD = "";

	public String getO_SHARE_KEYWORD() {
		return O_SHARE_KEYWORD;
	}

	public void setO_SHARE_KEYWORD(String o_SHARE_KEYWORD) {
		O_SHARE_KEYWORD = o_SHARE_KEYWORD;
	}

	public String getO_SCH_SPAM() {
		return O_SCH_SPAM;
	}

	public void setO_SCH_SPAM(String o_SCH_SPAM) {
		O_SCH_SPAM = o_SCH_SPAM;
	}

	public String getO_SAFE_PH() {
		return O_SAFE_PH;
	}

	public void setO_SAFE_PH(String o_SAFE_PH) {
		O_SAFE_PH = o_SAFE_PH;
	}
	
}
