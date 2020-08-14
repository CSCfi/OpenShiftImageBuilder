package fi.csc.notebooks.osbuilder.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import fi.csc.notebooks.osbuilder.constants.SecurityConstants;


public final class Utils {

	 // Make sure this file is present or mounted as a ConfigMap
	
	public static String NAMESPACE;
	public static String OS_ENDPOINT;
	public static String TOKEN;
	
	
	public static void readDefaultConfig() throws IOException, RuntimeException {
		
		OS_ENDPOINT = System.getenv("OPENSHIFT_CLUSTER_ENDPOINT");
		
		if (OS_ENDPOINT == null)
			throw new RuntimeException("Environment variable for OpenShift cluster endpoint missing");
		
			NAMESPACE = new String(Files.readAllBytes(Paths.get(SecurityConstants.NAMESPACE_FILEPATH)));
			TOKEN = new String(Files.readAllBytes(Paths.get(SecurityConstants.TOKEN_FILEPATH)));
		
		
	}
	
	public static void readCustomConfig() throws RuntimeException {
		
		NAMESPACE = System.getenv("OPENSHIFT_CUSTOM_PROJECT");
		if (NAMESPACE == null || NAMESPACE.isEmpty())
			throw new RuntimeException("Environment variable for Namespace missing");
		
		TOKEN = System.getenv("OPENSHIFT_SERVICE_ACCOUNT_TOKEN");
		if (TOKEN == null || TOKEN.isEmpty())
			throw new RuntimeException("Environment variable for Token missing");
		
		OS_ENDPOINT = System.getenv("OPENSHIFT_CLUSTER_ENDPOINT");
		if (OS_ENDPOINT == null || OS_ENDPOINT.isEmpty())
			throw new RuntimeException("Environment variable for cluster endpoint missing");
	
		
	
	}
	
	/**
	 * Get the current debug state for the application
	 * @return boolean value of the debug state
	 */
	
	public static boolean getDebugState() {
		
		/* IMPORTANT: 'DEBUG' env var also activates the Spring's native debug messages, which becomes too verbose */
		String _DEBUG = System.getenv("APP_DEBUG"); // Hence, we use APP_DEBUG as the env var here
		return Boolean.valueOf(_DEBUG); 
		
	}
	
	/**
	 * @param apiType which can be opi for OpenShift and apis for k8s
	 * @return
	 */
	public static String generateOSUrl(String apiType, String resource) {
		
		
		if (apiType.equals("apis"))
		{
			
			if(resource.equals("buildconfigs") || resource.equals("builds"))
				apiType = apiType + "/build.openshift.io";
			if(resource.equals("imagestreams"))
				apiType = apiType + "/image.openshift.io";
		}
		
		
		return String.format("%s%s%s%s%s%s", 
				OS_ENDPOINT,
				apiType,
				"/v1/namespaces/",
				NAMESPACE,
				"/",
				resource
				);
		
		
	}
	
/**
 * Generates url for unique resource based on name
 * @param apiType
 * @param resource
 * @param name
 * @return
 */
public static  String generateOSUrl(String apiType, String resource, String name) {
		
		if (apiType.equals("apis"))
		{
			
			if(resource.equals("buildconfigs") || resource.equals("builds"))
				apiType = apiType + "/build.openshift.io";
			if(resource.equals("imagestreams"))
				apiType = apiType + "/image.openshift.io";
		}
		
		return String.format("%s%s%s%s%s%s%s%s", 
				OS_ENDPOINT,
				apiType,
				"/v1/namespaces/",
				NAMESPACE,
				"/",
				resource,
				"/",
				name
				);
		
	}

public String generateOAUTHUrl() {
	
	return String.format("%s%s", 
			OS_ENDPOINT,
			"oauth/authorize"
			);
	}


/**
 * Generates unique hash based on given parameters to uniquely identify the buildconfigs and images
 * @param url
 * @param branch
 * @param contextDir
 * @return
 */
public static String generateHash(String url, Optional<String> branch, Optional<String> contextDir) {
	
	StringBuilder sb = new StringBuilder(url);
	if (url.endsWith("/"))
		sb.deleteCharAt(sb.length()-1);
	
	
	if (branch.isPresent() && !branch.get().isEmpty())
		sb.append(branch.get());
	else
		sb.append("master"); // the default branch
	
	if (contextDir.isPresent() && !contextDir.get().isEmpty())
		sb.append(contextDir.get());
	
	return DigestUtils.sha1Hex(sb.toString());
	
	
}


}
