package fi.csc.notebooks.osibuilder.osimage;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan(basePackageClasses = OSController.class)
public class OsimageApplication {

	public static void main(String[] args) {
		SpringApplication.run(OsimageApplication.class, args);
	}

}
