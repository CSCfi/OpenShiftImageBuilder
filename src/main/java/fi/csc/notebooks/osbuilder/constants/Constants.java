package fi.csc.notebooks.osbuilder.constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public final class Constants {

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
		
		Properties props = Constants.readProperties();
		
		if (apiType.equals("apis"))
		{
			
			if(resource.equals("buildconfigs"))
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
	
public String generateOSUrl(String apiType, String resource, String name) {
		
		Properties props = Constants.readProperties();
		
		return String.format("%s%s%s%s%s%s", 
				props.getProperty("OS_ENDPOINT"),
				apiType,
				"/v1/namespaces/",
				props.getProperty("NAMESPACE"),
				"/",
				resource,
				"/",
				name,
				"/instantiate"
				);
		
	}

public String generateOAUTHUrl() {
	
	Properties props = Constants.readProperties();
	
	return String.format("%s%s", 
			props.getProperty("OS_ENDPOINT"),
			"oauth/authorize"
			);
	}
}
