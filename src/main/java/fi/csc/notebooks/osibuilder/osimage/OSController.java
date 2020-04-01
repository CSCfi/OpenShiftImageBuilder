package fi.csc.notebooks.osibuilder.osimage;

import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.bind.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.csc.notebooks.osbuilder.client.OCRestClient;

@SpringBootApplication(scanBasePackages = {
"fi.csc.notebooks.osbuilder.client"})
@RestController
@CrossOrigin
@RequestMapping(value="/builds")
public class OSController {

	
	@Autowired
	OCRestClient client;
	
	
	@GetMapping()
	ResponseEntity<String> getData(@RequestParam Map<String, String> params) {
			
		return client.getBuildConfigs();
		
	}
	
	@PostMapping("/create")
	ResponseEntity<String> postBuild(@RequestParam Map<String, String> params) throws URISyntaxException{
		
		ResponseEntity<String> build_resp = client.postBuildConfig(params);
		if (!build_resp.getStatusCode().is2xxSuccessful()) // Error
			return build_resp;
		
		ResponseEntity<String> image_resp = client.postImageStreamConfig(params);
		if (!image_resp.getStatusCode().is2xxSuccessful())  // Error
		{
			try {
			client.deleteBuildConfig(params.get("name")); // Backtrack the created build config object
			}
			catch (Exception e) {
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return image_resp;
		}
		
		
		ResponseEntity<String> result = new ResponseEntity<String>(HttpStatus.CREATED); // OK
		return result;
	}
	
	@PostMapping("/start/{buildName}")
	ResponseEntity<String> startBuild(@PathVariable String buildName) throws URISyntaxException{
		
		
		ResponseEntity<String> result;
		try {
			result = client.postBuildRequest(buildName);
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
		}
		
		return result;
		
		
	}
	
	@DeleteMapping("/delete/{buildName}")
	ResponseEntity<String> deleteBuild(@PathVariable String buildName) throws URISyntaxException{
		
		ResponseEntity<String> resp = client.deleteBuildConfig(buildName);
		
		return resp;
	}
		
		
}
