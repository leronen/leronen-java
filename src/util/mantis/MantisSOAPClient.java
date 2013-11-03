package util.mantis;

import org.json.*;

import util.IOUtils;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;


public class MantisSOAPClient {
	    	
	public static final String URL = "http://mantis.bcplatforms.local/mantis/api/soap/mantisconnect.php";
	public static final String REQUEST_PREFIX =
	        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
	        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"+
	        " <soap:Body>\n"+
	        "  <mc_issue_get>\n"+
	        "    <username>leronen</username>\n"+
	        "    <password>poytalatka</password>\n";
	        	        	
	public static final String REQUEST_SUFFIX =
	        "  </mc_issue_get>\n"+
	        " </soap:Body>\n"+
	        "</soap:Envelope>";
	
	            	    	    
	   
    public MantisSOAPClient() throws Exception {    	
    	// no action
    }
    
    private String makeRequest(String issueId) {
        return REQUEST_PREFIX+
        	   "    <issue_id>"+issueId+"</issue_id>\n"+
        	   REQUEST_SUFFIX;
    }    
    	   	   	    	           
    private HttpURLConnection connect() throws IOException, MalformedURLException {
        URL url = new URL(URL);
        URLConnection uc = url.openConnection();
        HttpURLConnection con = (HttpURLConnection) uc;
        con.setDoOutput(true);
        con.setDoInput(true);      
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        return con;
    }
    
    private JSONObject getIssueData(String issueNumber) throws IOException {
    	HttpURLConnection con = connect();
    	writeGetIssueRequest(con, issueNumber);
    	JSONObject result = readReply(con);
    	return result;
    }
    
    private static JSONObject getRelevantData(JSONObject completeData) throws JSONException {
    	JSONObject result = new JSONObject();
    	String id = completeData.getJSONObject("id").getString("content");
    	String summary= completeData.getJSONObject("summary").getString("content");
    	result.put("id", id);
    	result.put("summary", summary);
    	return result;
    }
    
    private void writeGetIssueRequest(HttpURLConnection con, String issueNumber) throws IOException { 
        OutputStream requestOS = con.getOutputStream();
        Writer requestWriter = new OutputStreamWriter(requestOS);
        PrintWriter pw = new PrintWriter(requestWriter);        
        String request = makeRequest(issueNumber);        
        pw.println(request);
        pw.flush();                               
        pw.close();
    }
    	    
    
    /** read reply for a single request. return null if no data could be fetched */
    private JSONObject readReply(URLConnection con) throws IOException {    	
        InputStream in = con.getInputStream();
        String resultXML = IOUtils.readStream(in);                                                         

        try {
            JSONObject replyJSON = XML.toJSONObject(resultXML);            
            JSONObject soapEnvelopeJSON = replyJSON.getJSONObject("SOAP-ENV:Envelope");
            JSONObject soapBodyJSON = soapEnvelopeJSON.getJSONObject("SOAP-ENV:Body");
            JSONObject getIssueResponse = soapBodyJSON.getJSONObject("ns1:mc_issue_getResponse").getJSONObject("return");
            return getIssueResponse;	            
        }
        catch (JSONException e) {
            throw new IOException("Not parsable as json: "+e.getMessage());            
        }
    }
    
    
        
    /** fetch and output dog json for reg numbers read from stdin */
    public static void main(String[] args) throws Exception {
    	
    	List<String> ids = new ArrayList<String>();
    	ids.addAll(IOUtils.readLines());
    	
        MantisSOAPClient client = new MantisSOAPClient();
        
        JSONArray arr = new JSONArray();
        for (String issueNumber: ids) {
            JSONObject issueJSON = client.getIssueData(issueNumber);
            JSONObject relevantJSON = getRelevantData(issueJSON);
            arr.put(relevantJSON);
        }
        System.out.println(arr);
       
    }
    

    private static void log(String msg) {
    	System.err.println(msg);
    }

}
