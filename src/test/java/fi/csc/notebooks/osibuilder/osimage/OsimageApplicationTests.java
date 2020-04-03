package fi.csc.notebooks.osibuilder.osimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.ValidationException;

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
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", "custom_mock");
		params.put("imagetag", "custom_mock:latest");
		params.put("uri", "https://github.com/mockrepo");
		
		
		Mockito
		.when(client.postBuildConfig(params))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		
		Mockito
		.when(client.postImageStreamConfig(params))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		/** When URL is absent **/
		Map<String, String> params_nourl = new HashMap<String, String>();
		params_nourl.put("name", "custom_mock");
		params_nourl.put("imagetag", "custom_mock:latest");
		
		Mockito
		.when(client.postBuildConfig(params_nourl))
		.thenReturn(new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE));
		
		/** When the build config creation succeeds but imagestream creation fails **/
		
		Map<String,String> params_imagestream_error = new HashMap<String, String>(params);
		params_imagestream_error.replace("name", "imagename_error");
		
		Mockito
		.when(client.postBuildConfig(params_imagestream_error))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		Mockito
		.when(client.postImageStreamConfig(params_imagestream_error))
		.thenReturn(new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE));
		
		/** Parameters for starting the build **/
		
		String buildName = "custom_mock";
		
		Mockito
		.when(client.postBuildRequest(buildName))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		Mockito
		.when(client.postBuildRequest("non_existent_build"))
		.thenReturn(new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE));
		
		/** Parameters for getting the image **/
		
		Mockito
		.when(client.getImageStream("custom_mock"))
		.thenReturn(new ResponseEntity<String>(HttpStatus.CREATED));
		
		/** Delete build **/
		
		Mockito
		.when(client.deleteBuildConfig("custom"))
		.thenReturn(new ResponseEntity<String>(HttpStatus.OK));
	}
	
	@Test
	void buildRequestTest() throws Exception {
		
		mvc.perform(
				post("/builds/start/custom_mock")
				)
		.andExpect(status().isCreated());
		
		
		mvc.perform(
				post("/builds/start/non_existent_build")
				)
		.andExpect(status().isNotAcceptable());
		
	}
	
	
	@Test
	void buildAndImagetest() throws Exception {
		
		
		mvc.perform(
				post("/builds/create")
				.param("name", "custom_mock")
				.param("imagetag", "custom_mock:latest")
				.param("uri", "https://github.com/mockrepo")
				)
		.andExpect(status().isCreated());
		
		
		mvc.perform(
				post("/builds/create")
				.param("name", "custom_mock")
				.param("imagetag", "custom_mock:latest")
				)
		.andExpect(status().isNotAcceptable());
		
		mvc.perform(
				post("/builds/create")
				.param("name", "imagename_error")
				.param("imagetag", "custom_mock:latest")
				.param("uri", "https://github.com/mockrepo")
				)
		.andExpect(status().isNotAcceptable());
		
	}
	
	@Test 
	void getImageStream() throws Exception {
		
		mvc.perform(
				get("/builds/image/custom_mock")
				)
		.andExpect(status().isCreated());
		
		
	}
	
	@Test
	void buildDeleteTest() throws Exception {
		
		mvc.perform(
				delete("/builds/delete/custom")
				)
		.andExpect(status().isOk());
		
		
	}
	
	
	@Test
	void buildConfigJsonBodyTest() {
		
		Map<String,String> params = new HashMap<String,String>();
		params.put("name","custom_mock");
		params.put("imagetag", "custom_mock:latest");
		params.put("uri", "https://github.com/testrepo");
		
		JsonObject root = OSJsonParser.getPOSTBody("BuildConfig", params);
		
		String actual_name = root.get("metadata").getAsJsonObject().get("name").getAsString();
		
		assertEquals("custom_mock", actual_name);
		
		String actual_image_tag = root.get("spec").getAsJsonObject()
				.get("output").getAsJsonObject()
				.get("to").getAsJsonObject()
				.get("name").getAsString();
		
		assertEquals("custom_mock:latest", actual_image_tag);
		
		String actual_uri = root.get("spec").getAsJsonObject()
				.get("source").getAsJsonObject()
				.get("git").getAsJsonObject()
				.get("uri").getAsString();
		
		
		
		assertEquals("https://github.com/testrepo", actual_uri);
	}

	@Test
	void ImageStreamJsonBodyTest() {
		
		Map<String,String> params = new HashMap<String,String>();
		params.put("name","custom_mock");
		
		JsonObject root = OSJsonParser.getPOSTBody("ImageStream", params);
		
		String actual_name = root.get("metadata").getAsJsonObject().get("name").getAsString();
		
		assertEquals("custom_mock", actual_name);
		
	}
	
	
	@Test
	void BuildRequestJsonBodyTest() {
		
		String expected_name = "custom_name";
		
		JsonObject root = OSJsonParser.getPOSTBody("BuildRequest", expected_name);
		
		String actual_name = root.get("metadata").getAsJsonObject().get("name").getAsString();
		
		assertEquals(expected_name, actual_name);
		
	}
}
