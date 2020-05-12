package fi.csc.notebooks.osbuilder.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;

import fi.csc.notebooks.osbuilder.constants.SecurityConstants;


public final class Utils {

	 // Make sure this file is present or mounted as a configmap
	
	public static Properties readProperties() {
		
		Properties prop = null;
		
		try {
			InputStream input = new FileInputStream(SecurityConstants.CLUSTER_PROPERTIES_PATH);

            prop = new Properties();

            prop.load(input);
            
		}
		catch(IOException e) {
			
			e.printStackTrace();
			
		}
		return prop;
	}
	
	
	/**
	 * @param apiType which can be opi for openshift and apis for k8s
	 * @return
	 */
	public static String generateOSUrl(String apiType, String resource) {
		
		Properties props = Utils.readProperties();
		
		if (apiType.equals("apis"))
		{
			
			if(resource.equals("buildconfigs") || resource.equals("builds"))
				apiType = apiType + "/build.openshift.io";
			if(resource.equals("imagestreams"))
				apiType = apiType + "/image.openshift.io";
		}
		
		
		return String.format("%s%s%s%s%s%s", 
				props.getProperty("OS_ENDPOINT"),
				apiType,
				"/v1/namespaces/",
				props.getProperty("NAMESPACE"),
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
		
		Properties props = Utils.readProperties();
		
		if (apiType.equals("apis"))
		{
			
			if(resource.equals("buildconfigs") || resource.equals("builds"))
				apiType = apiType + "/build.openshift.io";
			if(resource.equals("imagestreams"))
				apiType = apiType + "/image.openshift.io";
		}
		
		return String.format("%s%s%s%s%s%s%s%s", 
				props.getProperty("OS_ENDPOINT"),
				apiType,
				"/v1/namespaces/",
				props.getProperty("NAMESPACE"),
				"/",
				resource,
				"/",
				name
				);
		
	}

public String generateOAUTHUrl() {
	
	Properties props = Utils.readProperties();
	
	return String.format("%s%s", 
			props.getProperty("OS_ENDPOINT"),
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
