package fi.csc.notebooks.osbuilder.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;


public final class Utils {

	public static final String PROPERTIES_FILEPATH = "/run/os.properties";
	
	public static Properties readProperties() {
		
		Properties prop = null;
		
		try {
			InputStream input = new FileInputStream(PROPERTIES_FILEPATH);

            prop = new Properties();

            prop.load(input);
            
		}
		catch(IOException e) {
			
			e.printStackTrace();
			
		}
		return prop;
	}
	
	
	/**
	 * @param apiType which can be opi for openshift and api for k8s
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
	
public static  String generateOSUrl(String apiType, String resource, String name) {
		
		Properties props = Utils.readProperties();
		
		if (apiType.equals("apis"))
		{
			
			if(resource.equals("buildconfigs"))
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

public static String generateHash(String url, Optional<String> branch, Optional<String> contextDir) {
	
	StringBuilder sb = new StringBuilder(url);
	
	if (branch.isPresent())
		sb.append(branch.get());
	
	if (contextDir.isPresent())
		sb.append(contextDir.get());
	
	return DigestUtils.sha1Hex(sb.toString());
	
	
}


}
