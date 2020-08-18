package fi.csc.notebooks.osbuilder.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import fi.csc.notebooks.osbuilder.models.BuildStatusImage;

public final class OSJsonParser {
	
	/** Parse Build and BuildConfig related objects here **/
	
	public static BuildStatusImage parseBuildListForStatusAndImage(String respBody) {
		
		JsonElement jsonBody = JsonParser.parseString(respBody);
		JsonObject buildListObj = jsonBody.getAsJsonObject();
		
		JsonArray buildList = buildListObj.get("items").getAsJsonArray();
		
		int list_size = buildList.size();
		JsonObject latestItem;
		
		BuildStatusImage bsi = new BuildStatusImage();
		
		if (list_size>0) { // When there are multiple Builds corresponding to a BuildConfig
			latestItem = buildList.get(list_size-1).getAsJsonObject(); // Fetch the status of the latest Build
			
			String buildName = latestItem.get("metadata").getAsJsonObject().get("name").getAsString();
			bsi.setBuildName(buildName);
			
			JsonObject statusObj = latestItem.get("status").getAsJsonObject();
			
			String status = statusObj.get("phase").getAsString();
			bsi.setStatus(status);
			
			String imageUrl = statusObj.get("outputDockerImageReference").getAsString();
			bsi.setImageUrl(imageUrl);
			
		}
		
		return bsi;
		
		
		
	}
	
	public static List<String> parseBuildListForNames(String buildsJson){
		
		JsonElement jsonBody = JsonParser.parseString(buildsJson);
		JsonObject buildListObj = jsonBody.getAsJsonObject();
		JsonArray buildList = buildListObj.get("items").getAsJsonArray();
		
		List<String> res = new LinkedList<String>();
		for (JsonElement item : buildList) {
			
			String build_name = item.getAsJsonObject().get("metadata").getAsJsonObject().get("name").getAsString();
			
			res.add(build_name);
			
		}
		
		return res;
		
		
		
		
	}
	
	public static String parseBuildConfigError(String respBody){
		JsonObject root = JsonParser.parseString(respBody).getAsJsonObject();
		
		String buildName = root.get("details").getAsJsonObject().get("name").getAsString();
		
		return buildName;
		
	}
	
	
	/** Parse ImageStream related Objects **/
	
	
	public static Map<String,String> parseImageStream(String respBody) { // ImageStreamKind: ImageStream
		
		JsonElement jsonBody = JsonParser.parseString(respBody);
		JsonObject imageStreamObj = jsonBody.getAsJsonObject();
		
		Map<String,String> resp = _parseImageStreamObject(imageStreamObj);
		
		return resp;
	}
	
	public static List<Map<String,String>> parseImageStreamList(String respBody) { // ImageStreamKind: ImageStreamList
		
		List<Map<String,String>> imageUrlList = new LinkedList<Map<String,String>>();
		
		JsonElement jsonBody = JsonParser.parseString(respBody);
		JsonObject imageStreamObj = jsonBody.getAsJsonObject();	
			
		JsonArray imageStreamList = imageStreamObj.get("items").getAsJsonArray();
		Iterator<JsonElement> imageStreamItemIterator = imageStreamList.iterator();
			
		while(imageStreamItemIterator.hasNext()) {
			JsonObject imageStreamItem = imageStreamItemIterator.next().getAsJsonObject();
			imageUrlList.add(_parseImageStreamObject(imageStreamItem));
				
		}
		
		return imageUrlList;
	}
	
	
	/** Get POST body data for different requests **/
	
	public static JsonObject getPOSTBody(
			String kind, 
			String hash,
			String url, 
			Optional<String> branch, 
			Optional<String> contextDir,
			Optional<String> dockerfilePath) {
		
		
		
		
		JsonObject root = null;
		
		if (kind.equals("BuildConfig")) {
			
			String strategy = "Source"; // Default build strategy
			
			if (dockerfilePath.isPresent() && !dockerfilePath.get().isEmpty())
				strategy = "Docker";
			
			root = _readJson(kind, strategy); // Read the json file corresponding to the strategy
			root = _substituteVarsBuildConfig(root, hash, url, branch, contextDir, dockerfilePath);
		}
		return root;
		
		
	}
	
	public static JsonObject getPOSTBody(String kind, String hash) {
		
		JsonObject root = _readJson(kind);
		
		if(kind.equals("BuildRequest"))
			root = _substituteVarsBuildRequest(root, hash);
		else if(kind.equals("ImageStream"))
			root = _substituteVarsImageStream(root, hash);
		
		return root;
	}
	
	
	
	/** All helper functions go here **/
	
	
	private static JsonObject _readJsonFile(String filename) {
		
	
		
		JsonObject root = null;
		
			
		try {
			/* NOTE: HAVE to read it as STREAM , not FILE
			 * Filepaths are not recognized when the app is packaged as a jar
			 */
			InputStream in = OSJsonParser.class.getClassLoader().getResourceAsStream(filename); 
			root = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
		} catch (JsonIOException | JsonSyntaxException e) {
			e.printStackTrace();
			
		}
		
		return root;
		
	}
	
	private static JsonObject _readJson(String kind, String strategy) {
		
		String filename = "";
		
		if (!kind.contentEquals("BuildConfig"))
			throw new Error("Wrong method called for BuildConfig");
		
		filename = kind + strategy + ".json";
		
		return _readJsonFile(filename);
		
		
	}
	
	private static JsonObject _readJson(String kind) {
		
		String filename = "";
		
		if (kind.contentEquals("ImageStream"))
			filename = kind + ".json";
		if (kind.contentEquals("BuildRequest"))
			filename = kind + ".json";
		
	
		return _readJsonFile(filename);
		
		
	}
	
	
	/**
	 * This method is used for putting the values obtained from the user
	 * to the BuildConfig object json file
	 * @param root
	 * @param hash
	 * @param url
	 * @param branch
	 * @param contextDir
	 * @param dockerfilePath
	 * @return
	 */
	private static JsonObject _substituteVarsBuildConfig( // By default, BuildConfigSource.json file will be read
			JsonObject root,
			String hash,
			String url, 
			Optional<String> branch, 
			Optional<String> contextDir,
			Optional<String> dockerfilePath) {
		
		JsonPrimitive jName = new JsonPrimitive(hash);
		JsonPrimitive jImageTag = new JsonPrimitive(hash +":latest");
		JsonPrimitive jURI = new JsonPrimitive(url);
		
		root.get("metadata").getAsJsonObject().add("name", jName);
		
		root.get("spec").getAsJsonObject()
			.get("output")
			.getAsJsonObject()
			.get("to")
			.getAsJsonObject().add("name", jImageTag);
		
		JsonObject source = root.get("spec").getAsJsonObject()
				.get("source").getAsJsonObject();
			
		JsonObject git = source.get("git").getAsJsonObject();
		
		git.add("uri", jURI);
		
		if (branch.isPresent() && !branch.get().isEmpty())
			git.add("ref", new JsonPrimitive(branch.get()));
		
		if (contextDir.isPresent() && !contextDir.get().isEmpty())
			source.add("contextDir", new JsonPrimitive(contextDir.get()));
		
		if (dockerfilePath.isPresent() && !dockerfilePath.get().isEmpty()) { // If this option is present, it means BuildConfigDocker.json file has been read
			
			JsonObject strategy = root.get("spec").getAsJsonObject()
					.get("strategy").getAsJsonObject();
			
			strategy.get("dockerStrategy").getAsJsonObject()
			// Replace/Fill the value of the docker file path, this can be dir (openshift will auto search for dockerfile), or can put the name of the file as well
			.addProperty("dockerfilePath", dockerfilePath.get()); 
		}
		
		
		return root;
		
	}
	
	
private static JsonObject _substituteVarsImageStream(JsonObject root, String hash) {
		
		JsonPrimitive jName = new JsonPrimitive(hash);
		
		root.get("metadata").getAsJsonObject()
		.get("labels").getAsJsonObject()
		.add("build", jName);
		
		root.get("metadata").getAsJsonObject()
		.add("name", jName);
		
		return root;
		
	}



private static JsonObject _substituteVarsBuildRequest(JsonObject root, String hash) {
	
	JsonPrimitive jName = new JsonPrimitive(hash);
	
	root.get("metadata").getAsJsonObject()
	.add("name", jName);
	
	return root;
	
}

private static Map<String, String> _parseImageStreamObject(JsonObject imageStreamObj){
	
	String imageName = imageStreamObj.get("metadata").getAsJsonObject().get("name").getAsString();
	String imageUrl = imageStreamObj.getAsJsonObject()
			.get("status").getAsJsonObject()
			.get("dockerImageRepository").getAsString();
	
	Map<String, String> resp = new HashMap<String, String>();
	resp.put("imageName", imageName);
	resp.put("imageUrl", imageUrl);
	
	return resp;
	
}

}
