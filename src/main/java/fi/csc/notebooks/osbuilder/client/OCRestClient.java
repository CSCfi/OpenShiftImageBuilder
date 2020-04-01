package fi.csc.notebooks.osbuilder.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import fi.csc.notebooks.osbuilder.constants.Constants;
import fi.csc.notebooks.osbuilder.misc.OSJsonParser;

@Service
public class OCRestClient {

	
	private RestTemplate rt;
	private OSJsonParser parser;
	
	@Autowired
	public OCRestClient(){
		rt = new RestTemplate();
		parser = new OSJsonParser();
		
	}
	
	private MultiValueMap<String, String> getHeaders() {
		
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		Properties props = Constants.readProperties();
		headers.add("Authorization", "Bearer " + props.getProperty("TOKEN"));
		headers.add("Content-Type", "application/json");
		
		return headers;
		
	}
	
	public ResponseEntity<String> getBuildConfigs() {
		
		
		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders());
		
		String buildURL = Constants.generateOSUrl("oapi", "buildconfigs");
		
		System.out.println(buildURL);
		
		
		ResponseEntity<String> resp = rt.exchange(buildURL, 
				HttpMethod.GET, 
				entity, 
				String.class
				);
		
		
		System.out.println(resp);
		
		return resp;
		
	}
	
	public ResponseEntity<String> postBuildConfig(Map<String, String> params) throws URISyntaxException {
		
		String buildURL = Constants.generateOSUrl("oapi", "buildconfigs");
		
		System.out.println(buildURL);
		
		
		
		if(!params.containsKey("name"))
			throw new RuntimeException("The parameter name is missing");
		if(!params.containsKey("uri"))
			throw new RuntimeException("The parameter uri is missing");
		
		String imagetag = params.get("name") + ":latest";
		params.put("imagetag", imagetag);
		
		System.out.println(parser.getPOSTBody("BuildConfig", params));
		RequestEntity<String> e = new RequestEntity<String>(
				parser.getPOSTBody("BuildConfig", params).toString(),
				this.getHeaders(), 
				HttpMethod.POST, 
				new URI(buildURL) 
				); 
				
		ResponseEntity<String> resp = rt.exchange(e, String.class);
		
		return resp;
	}
	
	/**
	 * This method should be used, only if the build hasn't been triggered 
	 * or the build pods have been deleted
	 * @param params : Make sure the 'name' key exists
	 * @return response
	 * @throws URISyntaxException 
	 */
	public ResponseEntity<String> deleteBuildConfig(String name) throws URISyntaxException{
		
		String buildURL = Constants.generateOSUrl("oapi", "buildconfigs");
		
		URI buildDeleteURL = new URI(buildURL + "/" + name);
		
		RequestEntity<String> e = new RequestEntity<String>(
				this.getHeaders(), 
				HttpMethod.DELETE, 
				buildDeleteURL
				);
		
		ResponseEntity<String> resp = rt.exchange(e, String.class);
		
		return resp;
	}
	
/** The build needs an image stream as well to output the final image
 * @param params (name)
 * @return
 * @throws URISyntaxException
 */
public ResponseEntity<String> postImageStreamConfig(Map<String, String> params) throws URISyntaxException {
		
		
		String imageStreamURL = Constants.generateOSUrl("apis", "imagestreams");
				
		
		System.out.println(imageStreamURL);
		
		OSJsonParser parser = new OSJsonParser();
		
		if(!params.containsKey("name"))
			throw new RuntimeException("The parameter name is missing");
		
		RequestEntity<String> e = new RequestEntity<String>(
				parser.getPOSTBody("ImageStream", params).toString(),
				this.getHeaders(), 
				HttpMethod.POST, 
				new URI(imageStreamURL) 
				); 
				
		ResponseEntity<String> resp = rt.exchange(e, String.class);
		
		return resp;
	}


public ResponseEntity<String> postBuildRequest(String name) throws URISyntaxException, ValidationException{
	
	String buildRequestURL = Constants.generateOSUrl("apis", "buildconfigs") + "/" + name + "/instantiate";
	
	RequestEntity<String> e = new RequestEntity<String>(
			parser.getPOSTBody("BuildRequest", name).toString(),
			this.getHeaders(), 
			HttpMethod.POST, 
			new URI(buildRequestURL) 
			);
	
	ResponseEntity<String> resp = rt.exchange(e, String.class);
	
	return resp;
	
}

}


