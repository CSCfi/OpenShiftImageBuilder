package fi.csc.notebooks.osbuilder.misc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import fi.csc.notebooks.osbuilder.models.BuildStatusImage;
import fi.csc.notebooks.osbuilder.utils.Utils;

public final class OSJsonParser {
	
	
	
	public static BuildStatusImage parseBuildList(String respBody) {
		
		JsonElement jsonBody = JsonParser.parseString(respBody);
		JsonObject buildListObj = jsonBody.getAsJsonObject();
		
		JsonArray buildList = buildListObj.get("items").getAsJsonArray();
		
		int list_size = buildList.size();
		JsonObject latestItem;
		
		BuildStatusImage bsi = new BuildStatusImage();
		
		if (list_size>0) {
			latestItem = buildList.get(list_size-1).getAsJsonObject();
			
			JsonObject statusObj = latestItem.get("status").getAsJsonObject();
			
			String status = statusObj.get("phase").getAsString();
			bsi.setStatus(status);
			
			String imageUrl = statusObj.get("outputDockerImageReference").getAsString();
			bsi.setImageUrl(imageUrl);
			
		}
		
		return bsi;
		
		
		
	}
	
	private static String parseImageDockerUrl(JsonElement imageStreamItem) {
		
		String imageUrl = imageStreamItem.getAsJsonObject()
				.get("status").getAsJsonObject()
				.get("dockerImageRepository").getAsString();
		
		return imageUrl;
	}
	public static List<String> parseImageStream(String respBody) {
		
		List<String> imageUrlList = new LinkedList<String>();
		
		JsonElement jsonBody = JsonParser.parseString(respBody);
		JsonObject imageStreamObj = jsonBody.getAsJsonObject();
		String imageStreamKind = imageStreamObj.get("kind").getAsString();
		
		
		
		if (imageStreamKind.contentEquals("ImageStreamList")) {
			
			JsonArray imageStreamList = imageStreamObj.get("items").getAsJsonArray();
			Iterator<JsonElement> imageStreamItemIterator = imageStreamList.iterator();
			
			while(imageStreamItemIterator.hasNext()) {
				JsonElement imageStreamItem = imageStreamItemIterator.next();
				imageUrlList.add(parseImageDockerUrl(imageStreamItem));
				
			}
		}
		
		else if(imageStreamKind.contentEquals("ImageStream")) {
			imageUrlList.add(parseImageDockerUrl(jsonBody));
		}
		return imageUrlList;
	}
	
	public static JsonObject getPOSTBody(
			String kind, 
			String hash,
			String url, 
			Optional<String> branch, 
			Optional<String> contextDir) {
		
		JsonObject root = readJson(kind);
		
		if (kind.equals("BuildConfig"))
			root = substituteVarsBuildConfig(root, hash, url, branch, contextDir);
		return root;
		
		
	}
	
	public static JsonObject getPOSTBody(String kind, String hash) {
	
		JsonObject root = readJson(kind);
		
		if(kind.equals("BuildRequest"))
			root = substituteVarsBuildRequest(root, hash);
		else if(kind.equals("ImageStream"))
			root = substituteVarsImageStream(root, hash);
		
		return root;
	}
	
	private static JsonObject readJson(String kind) {
		
		String filename = "";
		
		if (kind.contentEquals("BuildConfig"))
			filename = kind + ".json";
		if (kind.contentEquals("ImageStream"))
			filename = kind + ".json";
		if (kind.contentEquals("BuildRequest"))
			filename = kind + ".json";
		
		JsonObject root = null;
		
		
		try {
			root = JsonParser.parseReader(new FileReader(filename)).getAsJsonObject();
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		
		return root;
		
		
	}
	
	
	private static JsonObject substituteVarsBuildConfig(
			JsonObject root,
			String hash,
			String url, 
			Optional<String> branch, 
			Optional<String> contextDir) {
		
		JsonPrimitive jName = new JsonPrimitive(hash);
		JsonPrimitive jImageTag = new JsonPrimitive(hash +":latest");
		JsonPrimitive jURI = new JsonPrimitive(url);
		
		root.get("metadata").getAsJsonObject().add("name", jName);
		
		root.get("spec").getAsJsonObject()
			.get("output")
			.getAsJsonObject()
			.get("to")
			.getAsJsonObject().add("name", jImageTag);
		
		JsonObject git = root.get("spec").getAsJsonObject()
		.get("source").getAsJsonObject().get("git").getAsJsonObject();
		
		git.add("uri", jURI);
		
		if (branch.isPresent())
			git.add("ref", new JsonPrimitive(branch.get()));
		
		if (contextDir.isPresent())
			git.add("contextDir", new JsonPrimitive(contextDir.get()));
		
		
		
		return root;
		
	}
	
	
private static JsonObject substituteVarsImageStream(JsonObject root, String hash) {
		
		JsonPrimitive jName = new JsonPrimitive(hash);
		
		root.get("metadata").getAsJsonObject()
		.get("labels").getAsJsonObject()
		.add("build", jName);
		
		root.get("metadata").getAsJsonObject()
		.add("name", jName);
		
		return root;
		
	}



private static JsonObject substituteVarsBuildRequest(JsonObject root, String hash) {
	
	JsonPrimitive jName = new JsonPrimitive(hash);
	
	root.get("metadata").getAsJsonObject()
	.add("name", jName);
	
	return root;
	
}

}
