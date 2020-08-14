package fi.csc.notebooks.osbuilder.controller;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.classic.Level;
import fi.csc.notebooks.osbuilder.data.ApplicationUserRepository;
import fi.csc.notebooks.osbuilder.models.ApplicationUser;
import fi.csc.notebooks.osbuilder.utils.Utils;

@RestController
@RequestMapping("/users")
public class UserController {

    private ApplicationUserRepository applicationUserRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(logger.getName());
    
	@PostConstruct
	public void initialize() {
		if(Utils.getDebugState())
    		root.setLevel(Level.DEBUG);
	}

    public UserController(ApplicationUserRepository applicationUserRepository,
                          BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody ApplicationUser user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        if (applicationUserRepository.findByUsername(user.getUsername()) != null)
        	return new ResponseEntity<String>("Username already exists", HttpStatus.CONFLICT);
        
        applicationUserRepository.save(user);
        
        logger.info("Created user with username: " + user.getUsername());
        
        return new ResponseEntity<String>(HttpStatus.OK);
        
    }
    
    @GetMapping("/tokenvalidity")
    public void checkTokenValidity() {
    	// If the user is authorized to reach this method, he would get 200 OK, otherwise 401
    }
}