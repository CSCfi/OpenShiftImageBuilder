package fi.csc.notebooks.osbuilder.misc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;


import org.springframework.stereotype.Component;


import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

@Component
public class OSJsonParser {
	
	
	public JsonObject getPOSTBody(String kind, Map<String,String> params) {
		
		JsonObject root = readJson(kind);
		
		if (kind.equals("BuildConfig"))
			root = substituteVarsBuildConfig(root, params);
		if(kind.equals("ImageStream"))
			root = substituteVarsImageStream(root, params);
		return root;
		
		
	}
	
	public JsonObject getPOSTBody(String kind, String name) {
	
		JsonObject root = readJson(kind);
		
		if(kind.equals("BuildRequest"))
			root = substituteVarsBuildRequest(root, name);
		
		return root;
	}
	
	private JsonObject readJson(String kind) {
		
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
	
	
	private JsonObject substituteVarsBuildConfig(JsonObject root, Map<String,String> map) {
		
		JsonPrimitive jName = new JsonPrimitive(map.get("name"));
		JsonPrimitive jImageTag = new JsonPrimitive(map.get("imagetag"));
		JsonPrimitive jURI = new JsonPrimitive(map.get("uri"));
		
		root.get("metadata").getAsJsonObject().add("name", jName);
		
		root.get("spec").getAsJsonObject()
			.get("output")
			.getAsJsonObject()
			.get("to")
			.getAsJsonObject().add("name", jImageTag);
		
		root.get("spec").getAsJsonObject()
		.get("source").getAsJsonObject().get("git").getAsJsonObject().add("uri", jURI);
		
		return root;
		
	}
	
	
private JsonObject substituteVarsImageStream(JsonObject root, Map<String,String> map) {
		
		JsonPrimitive jName = new JsonPrimitive(map.get("name"));
		
		root.get("metadata").getAsJsonObject()
		.get("labels").getAsJsonObject()
		.add("build", jName);
		
		root.get("metadata").getAsJsonObject()
		.add("name", jName);
		
		return root;
		
	}



private JsonObject substituteVarsBuildRequest(JsonObject root, String name) {
	
	JsonPrimitive jName = new JsonPrimitive(name);
	
	root.get("metadata").getAsJsonObject()
	.add("name", jName);
	
	return root;
	
}
}
