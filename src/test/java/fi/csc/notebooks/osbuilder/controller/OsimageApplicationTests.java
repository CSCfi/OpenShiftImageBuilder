package fi.csc.notebooks.osbuilder.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import fi.csc.notebooks.osbuilder.data.ApplicationUserRepository;
import fi.csc.notebooks.osbuilder.data.UserDetailsServiceImpl;
import fi.csc.notebooks.osbuilder.models.BuildStatusImage;
import fi.csc.notebooks.osbuilder.utils.OSJsonParser;
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
	
	@MockBean
	UserDetailsServiceImpl userDetailsServiceImpl;
	
	@MockBean
	ApplicationUserRepository applicationUserRepository;
	
	@BeforeEach
	void setUp() throws URISyntaxException, ValidationException {
		
		/*
		mvc = MockMvcBuilders
				.webAppContextSetup(context)
				.apply(SecurityMockMvcConfigurers.springSecurity())
				.build();
		*/
		String uri = "https://github.com/mockrepo";
		Optional<String> branch = Optional.of("master");
		Optional<String> contextDir = Optional.of("/home");
		Optional<String> dockerfilePath = Optional.empty();
		String hash_all = Utils.generateHash(uri, branch, contextDir);
		
		
		Mockito
		.when(client.postBuildConfig(hash_all, uri, branch, contextDir, dockerfilePath))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		
		Mockito
		.when(client.postImageStreamConfig(hash_all))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		/** When URL is absent **/
		
		String hash_nouri = Utils.generateHash("", branch, contextDir);
		
		Mockito
		.when(client.postBuildConfig(hash_nouri, "", branch, contextDir, dockerfilePath))
		.thenReturn(new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE));
		
		Mockito
		.when(client.postImageStreamConfig(hash_nouri))
		.thenReturn(new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE));
		
		
		/** When the branch is missing , then openshift takes the default master **/
		
		Optional<String> nobranch = Optional.empty();
		String hash_nobranch = Utils.generateHash(uri, nobranch, contextDir);
		
		Mockito
		.when(client.postBuildConfig(hash_nobranch, uri, nobranch, contextDir, dockerfilePath))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		/** When the contextdir is missing, openshift takes the root dir **/
		
		Optional<String> no_dir = Optional.empty();
		String hash_nodir = Utils.generateHash(uri, branch, no_dir);
		
		Mockito
		.when(client.postBuildConfig(hash_nodir, uri, branch, no_dir, dockerfilePath))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		/** When both the branch and contextDir are missing, openshift takes the default values **/
		
		String hash_nobranch_nodir = Utils.generateHash(uri, nobranch, no_dir);
		
		Mockito
		.when(client.postBuildConfig(hash_nobranch_nodir, uri, nobranch, contextDir, dockerfilePath))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		
		/** When the build config creation succeeds but imagestream creation fails **/
		
		String uri_image_error = "https://github.com/mock_image_error_repo";
		
		String hash_imagestream_error = Utils.generateHash(uri_image_error, branch, contextDir);
		
		Mockito
		.when(client.postBuildConfig(hash_imagestream_error, uri_image_error, branch, contextDir, dockerfilePath))
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
		bsi.setBuildName(hash_all+"-1");
		bsi.setImageUrl("dockerImageUrl1");
		bsi.setStatus("running");
		
		Mockito
		.when(client.getBuildStatus(hash_all))
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
		
		/** Delete buildconfigs and builds **/
		
		Mockito
		.when(client.deleteBuildConfig(hash_all))
		.thenReturn(new ResponseEntity<String>(HttpStatus.OK));
		
		String buildName = hash_all + "-1";
		Mockito
		.when(client.deleteBuild(buildName))
		.thenReturn(new ResponseEntity<String>(HttpStatus.OK));
		
		Mockito
		.when(client.deleteAllBuilds(hash_all))
		.thenReturn(new ResponseEntity<String>(HttpStatus.OK));
		
	}
	
	@Test
	void buildRequestTest() throws Exception {
		
		String uri = "https://github.com/mockrepo";
		Optional<String> branch = Optional.of("master");
		Optional<String> contextDir = Optional.of("/home");
		String hash_all = Utils.generateHash(uri, branch, contextDir);
		
		mvc.perform(
				post(String.format("%s%s","/api/build/",hash_all)).with(user("test"))
				)
		.andExpect(status().isCreated());
		
		
		String uri_another = "https://github.com/anothermockrepo";
		String hash_build_does_not_exist = Utils.generateHash(uri_another, branch, contextDir);
		
		mvc.perform(
				post(String.format("%s%s","/api/build/",hash_build_does_not_exist)).with(user("test"))
				)
		.andExpect(status().isNotAcceptable());
		
	}
	
	
	@Test
	void buildAndImagetest() throws Exception {
			
		mvc.perform(
				post("/api/buildconfig").with(user("test"))
				.param("url", "https://github.com/mockrepo")
				.param("branch", "master")
				.param("contextDir", "/home")
				)
		.andExpect(status().isCreated());
		
		
		mvc.perform(
				post("/api/buildconfig").with(user("test"))
				.param("url", "")
				.param("branch", "master")
				.param("contextDir", "/home")
				)
		.andExpect(status().isNotAcceptable());
		
		
	}
	
	@Test 
	void getImageStream() throws Exception {
		
		mvc.perform(
				get("/api/images").with(user("test"))
				)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$").isArray())
		.andExpect(jsonPath("$", Matchers.hasSize(2)));
		
		mvc.perform(
				get("/api/images").with(user("test"))
				.param("url", "https://github.com/mockrepo")
				.param("branch", "master")
				.param("contextDir", "/home")
				)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$").isArray())
		.andExpect(jsonPath("$", Matchers.hasSize(2)));
		
	}
	
	@Test
	void buildTest() throws Exception {
		
		String hash_all = Utils.generateHash("https://github.com/mockrepo", 
				Optional.of("master"), 
				Optional.of("/home"));
		
		mvc.perform(
				get(String.format("%s%s", "/api/build/", hash_all)).with(user("test"))
				)
		.andExpect(status().isOk());
		
		mvc.perform(
				get(String.format("%s%s", "/api/build/status/", hash_all)).with(user("test"))
				)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$", Matchers.aMapWithSize(3)))
		.andExpect(jsonPath("$", Matchers.allOf(Matchers.hasKey("buildName"), Matchers.hasKey("status"), Matchers.hasKey("imageUrl"))));
						
	}
	
	@Test
	void buildConfigDeleteTest() throws Exception {
		
		String uri = "https://github.com/mockrepo";
		Optional<String> branch = Optional.of("master");
		Optional<String> contextDir = Optional.of("/home");
		String hash_all = Utils.generateHash(uri, branch, contextDir);
		
		mvc.perform(
				delete("/api/buildconfig/" + hash_all).with(user("test"))
				)
		.andExpect(status().isOk());
		
		
	}
	
	@Test
	void buildDeleteTest() throws Exception {
		
		String uri = "https://github.com/mockrepo";
		Optional<String> branch = Optional.of("master");
		Optional<String> contextDir = Optional.of("/home");
		String hash_all = Utils.generateHash(uri, branch, contextDir);
		
		
		mvc.perform(
				delete("/api/build/" + hash_all + "-1").with(user("test"))
				)
		.andExpect(status().isOk());
		
		mvc.perform(
				delete("/api/builds/" + hash_all).with(user("test"))
				)
		.andExpect(status().isOk());
		
	}
	
	
	
	@Test
	void buildConfigJsonBodyTest() {
		
		String uri = "https://github.com/mockrepo";
		Optional<String> branch = Optional.of("master");
		Optional<String> contextDir = Optional.of("/home");
		Optional<String> dockerfilePath = Optional.empty();
		String hash_all = Utils.generateHash(uri, branch, contextDir);
		
		JsonObject root = OSJsonParser.getPOSTBody("BuildConfig", hash_all, uri, branch, contextDir, dockerfilePath);
		
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
