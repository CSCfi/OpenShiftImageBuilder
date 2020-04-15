package fi.csc.notebooks.osibuilder.osimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.ValidationException;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.google.gson.JsonObject;

import fi.csc.notebooks.osbuilder.client.OCRestClient;
import fi.csc.notebooks.osbuilder.misc.OSJsonParser;
import fi.csc.notebooks.osbuilder.models.BuildStatusImage;
import fi.csc.notebooks.osbuilder.utils.Utils;

@WebMvcTest
class OsimageApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	MockMvc mvc;
		
	@MockBean
	OCRestClient client;
	
	
	@BeforeEach
	void setUp() throws URISyntaxException, ValidationException {
		
		String uri = "https://github.com/mockrepo";
		Optional<String> branch = Optional.of("master");
		Optional<String> contextDir = Optional.of("/home");
		String hash_all = Utils.generateHash(uri, branch, contextDir);
		
		
		Mockito
		.when(client.postBuildConfig(hash_all, uri, branch, contextDir))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		
		Mockito
		.when(client.postImageStreamConfig(hash_all))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		/** When URL is absent **/
		
		String hash_nouri = Utils.generateHash("", branch, contextDir);
		
		Mockito
		.when(client.postBuildConfig(hash_nouri, "", branch, contextDir))
		.thenReturn(new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE));
		
		
		/** When the branch is missing , then openshift takes the default master **/
		
		Optional<String> nobranch = Optional.empty();
		String hash_nobranch = Utils.generateHash(uri, nobranch, contextDir);
		
		Mockito
		.when(client.postBuildConfig(hash_nobranch, uri, nobranch, contextDir))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		/** When the contextdir is missing, openshift takes the root dir **/
		
		Optional<String> no_dir = Optional.empty();
		String hash_nodir = Utils.generateHash(uri, branch, no_dir);
		
		Mockito
		.when(client.postBuildConfig(hash_nodir, uri, branch, no_dir))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		/** When both the branch and contextDir are missing, openshift takes the default values **/
		
		String hash_nobranch_nodir = Utils.generateHash(uri, nobranch, no_dir);
		
		Mockito
		.when(client.postBuildConfig(hash_nobranch_nodir, uri, nobranch, contextDir))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		
		/** When the build config creation succeeds but imagestream creation fails **/
		
		String uri_image_error = "https://github.com/mock_image_error_repo";
		
		String hash_imagestream_error = Utils.generateHash(uri_image_error, branch, contextDir);
		
		Mockito
		.when(client.postBuildConfig(hash_imagestream_error, uri_image_error, branch, contextDir))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		Mockito
		.when(client.postImageStreamConfig(hash_imagestream_error))
		.thenReturn(new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE));
		
		/** Parameters for starting the build **/
		
		Mockito
		.when(client.postBuildRequest(hash_all))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		String uri_another = "https://github.com/anothermockrepo";
		String hash_build_does_not_exist = Utils.generateHash(uri_another, branch, contextDir);
		
		Mockito
		.when(client.postBuildRequest(hash_build_does_not_exist))
		.thenReturn(new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE));
		
		/** Setup for getting the builds and build status **/
		
		Mockito
		.when(client.getBuilds(hash_all))
		.thenReturn(new ResponseEntity<String>(HttpStatus.OK));
		
		BuildStatusImage bsi = new BuildStatusImage();
		bsi.setImageUrl("dockerImageUrl1");
		bsi.setStatus("running");
		
		Mockito
		.when(client.getBuildsStatus(hash_all))
		.thenReturn(new ResponseEntity<BuildStatusImage>(bsi, HttpStatus.OK));
		
		
		
		/** Parameters for getting the image **/
		
		List<Map<String,String>> imageUrls = new LinkedList<Map<String,String>>();
		Map<String,String> map1 = new HashMap<String, String>();
		map1.put("imageUrl", "dockerImageUrl1");
		map1.put("imageName", "dockerImage1");
		Map<String,String> map2 = new HashMap<String, String>();
		map2.put("imageUrl", "dockerImageUrl1");
		map2.put("imageName", "dockerImage1");
		imageUrls.add(map1);
		imageUrls.add(map2);
		
		Mockito
		.when(client.getImageStreams())
		.thenReturn(new ResponseEntity<List<Map<String,String>>>(imageUrls, HttpStatus.OK));
		
		Map<String,String> map3 = new HashMap<String, String>();
		map3.put("imageUrl", "dockerImageUrl1");
		map3.put("imageName", "dockerImage1");
		
		Mockito
		.when(client.getImageStream(hash_all))
		.thenReturn(new ResponseEntity<Map<String,String>>(map3, HttpStatus.OK));
		
		/** Delete build **/
		
		Mockito
		.when(client.deleteBuildConfig(hash_all))
		.thenReturn(new ResponseEntity<String>(HttpStatus.OK));
	}
	
	@Test
	void buildRequestTest() throws Exception {
		
		String uri = "https://github.com/mockrepo";
		Optional<String> branch = Optional.of("master");
		Optional<String> contextDir = Optional.of("/home");
		String hash_all = Utils.generateHash(uri, branch, contextDir);
		
		mvc.perform(
				post(String.format("%s%s","/api/builds/start/",hash_all))
				)
		.andExpect(status().isCreated());
		
		
		String uri_another = "https://github.com/anothermockrepo";
		String hash_build_does_not_exist = Utils.generateHash(uri_another, branch, contextDir);
		
		mvc.perform(
				post(String.format("%s%s","/api/builds/start/",hash_build_does_not_exist))
				)
		.andExpect(status().isNotAcceptable());
		
	}
	
	
	@Test
	void buildAndImagetest() throws Exception {
		
		String uri = "https://github.com/mockrepo";
		Optional<String> branch = Optional.of("master");
		Optional<String> contextDir = Optional.of("/home");
		String hash_all = Utils.generateHash(uri, branch, contextDir);
		
		
		mvc.perform(
				post("/api/buildconfigs")
				.param("url", "https://github.com/mockrepo")
				.param("branch", "master")
				.param("contextDir", "/home")
				)
		.andExpect(status().isCreated());
		
		
		mvc.perform(
				post("/api/buildconfigs")
				.param("url", "")
				.param("branch", "master")
				.param("contextDir", "/home")
				)
		.andExpect(status().isNotAcceptable());
		
		
	}
	
	@Test 
	void getImageStream() throws Exception {
		
		/*
		String hash_all = Utils.generateHash("https://github.com/mockrepo", 
				Optional.of("master"), 
				Optional.of("/home"));
		*/
		
		mvc.perform(
				get("/api/images")
				)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$").isArray())
		.andExpect(jsonPath("$", Matchers.hasSize(2)));
		
		mvc.perform(
				get("/api/images")
				.param("url", "https://github.com/mockrepo")
				.param("branch", "master")
				.param("contextDir", "/home")
				)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$").isArray())
		.andExpect(jsonPath("$", Matchers.hasSize(2)));
		
		/*
		mvc.perform(
				get(String.format("%s%s", "/api/images/", hash_all))
				)
		.andExpect(status().isCreated());
		*/
		
	}
	
	@Test
	void buildTest() throws Exception {
		
		String hash_all = Utils.generateHash("https://github.com/mockrepo", 
				Optional.of("master"), 
				Optional.of("/home"));
		
		mvc.perform(
				get(String.format("%s%s", "/api/builds/", hash_all))
				)
		.andExpect(status().isOk());
		
		mvc.perform(
				get(String.format("%s%s", "/api/builds/status/", hash_all))
				)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$", Matchers.aMapWithSize(3)))
		.andExpect(jsonPath("$", Matchers.allOf(Matchers.hasKey("status"), Matchers.hasKey("imageUrl"))));
						
	}
	
	@Test
	void buildDeleteTest() throws Exception {
		
		mvc.perform(
				delete("/api/builds/delete/custom")
				)
		.andExpect(status().isOk());
		
		
	}
	
	
	@Test
	void buildConfigJsonBodyTest() {
		
		String uri = "https://github.com/mockrepo";
		Optional<String> branch = Optional.of("master");
		Optional<String> contextDir = Optional.of("/home");
		String hash_all = Utils.generateHash(uri, branch, contextDir);
		
		JsonObject root = OSJsonParser.getPOSTBody("BuildConfig", hash_all, uri, branch, contextDir);
		
		String actual_name = root.get("metadata").getAsJsonObject().get("name").getAsString();
		
		assertEquals(hash_all, actual_name);
		
		String actual_image_tag = root.get("spec").getAsJsonObject()
				.get("output").getAsJsonObject()
				.get("to").getAsJsonObject()
				.get("name").getAsString();
		
		assertEquals(hash_all +":latest", actual_image_tag);
		
		String actual_uri = root.get("spec").getAsJsonObject()
				.get("source").getAsJsonObject()
				.get("git").getAsJsonObject()
				.get("uri").getAsString();
		
		
		
		assertEquals(uri, actual_uri);
	}

	@Test
	void ImageStreamJsonBodyTest() {
		
		String hash_all = Utils.generateHash("https://github.com/mockrepo", Optional.of("master"), Optional.of("/home"));
		
		JsonObject root = OSJsonParser.getPOSTBody("ImageStream", hash_all);
		
		String actual_name = root.get("metadata").getAsJsonObject().get("name").getAsString();
		
		assertEquals(hash_all, actual_name);
		
	}
	
	
	@Test
	void BuildRequestJsonBodyTest() {
		
		String hash_all = Utils.generateHash("https://github.com/mockrepo", Optional.of("master"), Optional.of("/home"));
		
		JsonObject root = OSJsonParser.getPOSTBody("BuildRequest", hash_all);
		
		String actual_name = root.get("metadata").getAsJsonObject().get("name").getAsString();
		
		assertEquals(hash_all, actual_name);
		
	}
}
