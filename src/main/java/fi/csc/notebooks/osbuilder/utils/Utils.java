package fi.csc.notebooks.osbuilder.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import fi.csc.notebooks.osbuilder.constants.SecurityConstants;


public final class Utils {

	
	/* All the essential variables are listed here **/
	
	public static String NAMESPACE;
	public static String OS_CLUSTER_URL;
	public static String OS_IMAGE_REGISTRY_URL;
	public static String TOKEN;
	
	
	public static void readDefaultConfig() throws IOException, RuntimeException {
		
		OS_CLUSTER_URL = System.getenv("OPENSHIFT_CLUSTER_URL");
		OS_IMAGE_REGISTRY_URL = System.getenv("OPENSHIFT_IMAGE_REGISTRY_URL");
		
		if (OS_CLUSTER_URL == null || OS_CLUSTER_URL.isEmpty())
			throw new RuntimeException("Environment variable for OpenShift Cluster URL missing");
		if (OS_IMAGE_REGISTRY_URL == null || OS_IMAGE_REGISTRY_URL.isEmpty())
			throw new RuntimeException("Environment variable for OpenShift Image Registry URL missing");
		
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
		
		OS_CLUSTER_URL = System.getenv("OPENSHIFT_CLUSTER_URL");
		if (OS_CLUSTER_URL == null || OS_CLUSTER_URL.isEmpty())
			throw new RuntimeException("Environment variable for Cluster Url missing");
		
		OS_IMAGE_REGISTRY_URL = System.getenv("OPENSHIFT_IMAGE_REGISTRY_URL");
		if (OS_IMAGE_REGISTRY_URL == null || OS_IMAGE_REGISTRY_URL.isEmpty())
			throw new RuntimeException("Environment variable for OpenShift Image Registry Url missing");
	
		
	
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
				OS_CLUSTER_URL,
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
				OS_CLUSTER_URL,
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
			OS_CLUSTER_URL,
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
public static String generateHash(String url, Optional<String> branch, Optional<String> contextDir, Optional<String> dockerfilePath) {
	
	if(url.isEmpty())
		return "";
	
	StringBuilder sb = new StringBuilder(url);
	if (url.endsWith("/"))
		sb.deleteCharAt(sb.length()-1);
	
	
	if (branch.isPresent() && !branch.get().isEmpty())
		sb.append(branch.get());
	else
		sb.append("master"); // the default branch
	
	if (contextDir.isPresent() && !contextDir.get().isEmpty())
		sb.append(contextDir.get());
	
	if (dockerfilePath.isPresent() && !dockerfilePath.get().isEmpty())
		sb.append(dockerfilePath.get());
	
	return DigestUtils.sha1Hex(sb.toString());
	
	
}


}
