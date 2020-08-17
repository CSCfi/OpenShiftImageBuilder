package fi.csc.notebooks.osbuilder.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.Level;
import fi.csc.notebooks.osbuilder.models.BuildStatusImage;
import fi.csc.notebooks.osbuilder.utils.OSJsonParser;
import fi.csc.notebooks.osbuilder.utils.Utils;

@Configuration
public class OCRestClient {

	private static final Logger logger = LoggerFactory.getLogger(OCRestClient.class);
	ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(logger.getName());
	
	

	private RestTemplate rt;

	@Autowired
	public OCRestClient(){
		
		if(Utils.getDebugState())
    		root.setLevel(Level.DEBUG);
		rt = new RestTemplate();
		
		try {
			Utils.readCustomConfig();
			
		}
		catch (Exception e) {
			logger.warn(e.getMessage());
			logger.warn("Custom configuration not found, using the default namespace and service account token");
			
			try {
				Utils.readDefaultConfig();
			} catch (IOException | RuntimeException e1) {
				logger.error(e1.getMessage());
				logger.error("Required parameters for running the application not found, exiting...");
				System.exit(404); // Terminate the application, if the required configuration is not found.
			}
		}
	}

	private MultiValueMap<String, String> getHeaders() {

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		
		logger.debug("Token: " + Utils.TOKEN);
		
		headers.add("Authorization", "Bearer " + Utils.TOKEN);
		headers.add("Content-Type", "application/json");

		return headers;

	}

	public ResponseEntity<String> getBuildConfigs() {


		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders());

		String buildURL = Utils.generateOSUrl("oapi", "buildconfigs");

		logger.debug("Build URL: " + buildURL);


		ResponseEntity<String> resp = rt.exchange(buildURL, 
				HttpMethod.GET, 
				entity, 
				String.class
				);



		return resp;

	}

	public ResponseEntity<String> getBuildConfig(String name) {


		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders());

		String buildURL = Utils.generateOSUrl("apis", "buildconfigs", name);

		logger.debug("Build URL: " + buildURL);

		ResponseEntity<String> resp = rt.exchange(buildURL, 
				HttpMethod.GET, 
				entity, 
				String.class
				);
		return resp;

	}

	//  https://rahti.csc.fi:8443/apis/build.openshift.io/v1/namespaces/your-namespace/builds?labelSelector=buildconfig
	public ResponseEntity<String> getBuilds(String buildId) {


		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders());

		String buildURL = Utils.generateOSUrl("apis", "builds") + "?labelSelector={label}";
		
		logger.debug("Build URL: " + buildURL);

		ResponseEntity<String> resp = rt.exchange(buildURL, 
				HttpMethod.GET, 
				entity, 
				String.class,
				String.format("buildconfig=%s", buildId)
				);



		return resp;

	}

	public ResponseEntity<BuildStatusImage> getBuildStatus(String buildConfigName) {


		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders());

		String buildURL = Utils.generateOSUrl("apis", "builds") + "?labelSelector={label}";

		logger.debug("Build URL: " + buildURL);

		ResponseEntity<String> resp = rt.exchange(buildURL, 
				HttpMethod.GET, 
				entity, 
				String.class,
				String.format("buildconfig=%s", buildConfigName)
				);


		BuildStatusImage bsi = OSJsonParser.parseBuildListForStatusAndImage(resp.getBody());
		
		logger.debug("Build Name : " + bsi.getBuildName() + ", Status : " + bsi.getStatus());

		return new ResponseEntity<BuildStatusImage>(bsi, HttpStatus.OK);

	}

	public ResponseEntity<String> getBuildLogs(String buildName) {

		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders());

		String buildLogURL = Utils.generateOSUrl("apis", "builds", buildName) + "/log";

		ResponseEntity<String> resp = rt.exchange(buildLogURL, 
				HttpMethod.GET, 
				entity, 
				String.class
				);

		return resp;

	}

	public ResponseEntity<List<Map<String,String>>> getImageStreams() {


		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders());

		String imageStreamURL = Utils.generateOSUrl("apis", "imagestreams");

		logger.debug("ImageStream URL: " + imageStreamURL);
		
		ResponseEntity<List<Map<String,String>>> resp = null;

		try {

			ResponseEntity<String> _resp = rt.exchange(
					imageStreamURL, 
					HttpMethod.GET, 
					entity,
					String.class
					);

			resp = new ResponseEntity<List<Map<String,String>>>(
					OSJsonParser.parseImageStreamList(_resp.getBody()), 
					_resp.getStatusCode());

		}
		catch(HttpClientErrorException ex) {

			logger.error(ex.getMessage());
			Map<String, String> ex_map = new HashMap<String, String>();
			ex_map.put("message", ex.getMessage());
			List<Map<String,String>> ex_list = new LinkedList<Map<String,String>>();
			resp = new ResponseEntity<List<Map<String,String>>>(ex_list, ex.getStatusCode());

		}

		return resp;

	}

	public ResponseEntity<Map<String,String>> getImageStream(String imageName) {


		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders());

		String imageStreamURL = Utils.generateOSUrl("apis", "imagestreams", imageName);

		logger.debug("ImageStream URL: " + imageStreamURL);
		
		ResponseEntity<Map<String,String>> res = null;

		try {
			ResponseEntity<String> resp = rt.exchange(
					imageStreamURL, 
					HttpMethod.GET, 
					entity, 
					String.class
					//String.format("build=%s", imageName)
					);
			if (resp.getStatusCode().is2xxSuccessful())
				res = new ResponseEntity<Map<String,String>>(
						OSJsonParser.parseImageStream(resp.getBody()), 
						HttpStatus.OK);
		}
		catch (HttpClientErrorException ex) {
			logger.error(ex.getMessage());
			Map<String, String> ex_map = new HashMap<String, String>();
			ex_map.put("message", ex.getMessage());
			res = new ResponseEntity<Map<String,String>>(ex_map, ex.getStatusCode());
		}

		return res;
	}


	public ResponseEntity<String> postBuildConfig(
			String hash, 
			String uri, 
			Optional<String> branch, 
			Optional<String> contextDir,
			Optional<String> dockerfilePath) throws URISyntaxException {

		String buildURL = Utils.generateOSUrl("oapi", "buildconfigs");

		ResponseEntity<String> resp = null;

		if(uri.isEmpty())
			return new ResponseEntity<String>("URL Parameter is missing", HttpStatus.UNPROCESSABLE_ENTITY);

		RequestEntity<String> e = new RequestEntity<String>(
				OSJsonParser.getPOSTBody("BuildConfig", hash, uri, branch, contextDir, dockerfilePath).toString(),
				this.getHeaders(), 
				HttpMethod.POST, 
				new URI(buildURL) 
				); 

		try {
			resp = rt.exchange(e, String.class);
		}
		catch(HttpClientErrorException ex) {

			logger.error(ex.getMessage());
			resp = new ResponseEntity<String>(OSJsonParser.parseBuildConfigError(ex.getResponseBodyAsString()), ex.getStatusCode());
		}
		return resp;
	}

	

	/** The build needs an image stream as well to output the final image
	 * @param params (name)
	 * @return
	 * @throws URISyntaxException
	 */
	public ResponseEntity<String> postImageStreamConfig(String hash) throws URISyntaxException {


		String imageStreamURL = Utils.generateOSUrl("apis", "imagestreams");

		if(hash.isEmpty())
			return new ResponseEntity<String>("Computed hash parameter was empty", HttpStatus.UNPROCESSABLE_ENTITY);

		RequestEntity<String> e = new RequestEntity<String>(
				OSJsonParser.getPOSTBody("ImageStream", hash).toString(),
				this.getHeaders(), 
				HttpMethod.POST, 
				new URI(imageStreamURL) 
				); 

		ResponseEntity<String> resp = rt.exchange(e, String.class);

		return resp;
	}


	public ResponseEntity<String> postBuildRequest(String hash) throws URISyntaxException, ValidationException{

		String buildRequestURL = Utils.generateOSUrl("apis", "buildconfigs", hash) + "/instantiate";

		RequestEntity<String> e = new RequestEntity<String>(
				OSJsonParser.getPOSTBody("BuildRequest", hash).toString(),
				this.getHeaders(), 
				HttpMethod.POST, 
				new URI(buildRequestURL) 
				);

		ResponseEntity<String> resp = rt.exchange(e, String.class);

		return resp;

	}
	
	/**
	 * This method should be used, only if the build hasn't been triggered 
	 * or all the build pods have been deleted
	 * @param params : Make sure the 'name' key exists
	 * @return response
	 * @throws URISyntaxException 
	 */
	public ResponseEntity<String> deleteBuildConfig(String hash) throws URISyntaxException{

		String buildConfigDeleteURL = Utils.generateOSUrl("apis", "buildconfigs", hash);
		logger.debug(buildConfigDeleteURL);
		
		ResponseEntity<String> resp = null;
		
		try {
		RequestEntity<String> e = new RequestEntity<String>(
				this.getHeaders(), 
				HttpMethod.DELETE, 
				new URI(buildConfigDeleteURL)
				);

		resp = rt.exchange(e, String.class);
		}
		
		catch(HttpClientErrorException ex) {
			logger.error(ex.getMessage());
			return new ResponseEntity<String>(ex.getResponseBodyAsString(), ex.getStatusCode());
		}
		
		return resp;
	}


	
	/**
	 * This method deletes the build with the provided build name
	 * 
	 * @param params : Make sure the 'name' key exists
	 * @return response
	 * @throws URISyntaxException 
	 */
	
	public ResponseEntity<String> deleteBuild(String buildName) throws URISyntaxException{

		String buildDeleteURL = Utils.generateOSUrl("apis", "builds", buildName);

		ResponseEntity<String> resp = null;
		try {
			
			RequestEntity<String> e = new RequestEntity<String>(
					this.getHeaders(), 
					HttpMethod.DELETE, 
					new URI(buildDeleteURL)
			);

			resp = rt.exchange(e, String.class);

		}
		
		catch(HttpClientErrorException ex) {
			logger.error(ex.getMessage());
			resp = new ResponseEntity<String>(ex.getResponseBodyAsString(), ex.getStatusCode());
			
		}
		return resp;
	}
	
	
	public ResponseEntity<String> deleteAllBuilds(String buildConfigName) throws URISyntaxException{

		
		String buildsJson = getBuilds(buildConfigName).getBody();
		
		List<String> buildNames = OSJsonParser.parseBuildListForNames(buildsJson);
		
		Iterator<String> buildNamesIterator = buildNames.iterator();
		
		
		while (buildNamesIterator.hasNext()) {
			
			String build_name = buildNamesIterator.next();
			
			String buildDeleteURL = Utils.generateOSUrl("apis", "builds", build_name);
			
			logger.debug("BuildDeleteURL: " + buildDeleteURL);

			
			try {
				
				RequestEntity<String> e = new RequestEntity<String>(
						this.getHeaders(), 
						HttpMethod.DELETE, 
						new URI(buildDeleteURL)
				);

				rt.exchange(e, String.class);
			}
			
			catch(HttpClientErrorException ex) {
				logger.error(ex.getMessage());
				return new ResponseEntity<String>(ex.getResponseBodyAsString(), ex.getStatusCode());
			}
			
		}
		
		return new ResponseEntity<String>("All builds deleted", HttpStatus.OK);
		
	}
	
	public ResponseEntity<String> deleteImage(String buildConfigName) throws URISyntaxException{
	
		String imageDeleteURL = Utils.generateOSUrl("apis", "imagestreams", buildConfigName);
		
		ResponseEntity<String> resp = null;
	
		try {
			
			RequestEntity<String> e = new RequestEntity<String>(
					this.getHeaders(), 
					HttpMethod.DELETE, 
					new URI(imageDeleteURL)
			);

			resp = rt.exchange(e, String.class);
		}
		
		catch(HttpClientErrorException ex) {
			logger.error(ex.getMessage());
			return new ResponseEntity<String>(ex.getResponseBodyAsString(), ex.getStatusCode());
		}
		
		return resp;
		
	}


}


