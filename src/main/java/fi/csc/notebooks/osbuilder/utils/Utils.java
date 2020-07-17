package fi.csc.notebooks.osbuilder.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;

import fi.csc.notebooks.osbuilder.constants.SecurityConstants;


public final class Utils {

	 // Make sure this file is present or mounted as a configmap
	
	public static String NAMESPACE;
	public static String OS_ENDPOINT;
	public static String TOKEN;
	
	public static void readEnvsAndTokenFile() throws IOException, RuntimeException {
		
		OS_ENDPOINT = System.getenv("CLUSTER_ENDPOINT");
		
		if (OS_ENDPOINT == null)
			throw new RuntimeException("Environment variable for OpenShift cluster endpoint missing");
		
		NAMESPACE = new String(Files.readAllBytes(Paths.get(SecurityConstants.NAMESPACE_FILEPATH)));
		TOKEN = new String(Files.readAllBytes(Paths.get(SecurityConstants.TOKEN_FILEPATH)));
		
	}
	
	public static void readProperties() {
		
		Properties prop = null;
		
		try {
			
			InputStream input = new FileInputStream(SecurityConstants.CLUSTER_PROPERTIES_PATH);
            prop = new Properties();
            prop.load(input);
            
		}
		catch(IOException e) {
			
			e.printStackTrace();
			
		}
		
		NAMESPACE = prop.getProperty("NAMESPACE");
		OS_ENDPOINT = prop.getProperty("OS_ENDPOINT");
		TOKEN = prop.getProperty("TOKEN");
	
	}
	
	
	/**
	 * @param apiType which can be opi for openshift and apis for k8s
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
