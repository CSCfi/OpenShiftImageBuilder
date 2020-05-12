package fi.csc.notebooks.osbuilder.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import fi.csc.notebooks.osbuilder.client.OCRestClient;
import fi.csc.notebooks.osbuilder.data.ApplicationUserRepository;
import fi.csc.notebooks.osbuilder.data.UserDetailsServiceImpl;
import fi.csc.notebooks.osbuilder.models.ApplicationUser;

@SpringBootApplication(scanBasePackages = {
"fi.csc.notebooks.osbuilder.client",
"fi.csc.notebooks.osbuilder.auth"})
@ComponentScan(basePackageClasses = { 
		OSController.class, 
		OCRestClient.class,
		UserDetailsServiceImpl.class,
		UserController.class})
@EnableJpaRepositories(basePackageClasses = ApplicationUserRepository.class)
@EntityScan(basePackageClasses = ApplicationUser.class)
public class OsimageApplication {
	
	
	@Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	public static void main(String[] args) {
		
		SpringApplication.run(OsimageApplication.class, args);
	}

}
