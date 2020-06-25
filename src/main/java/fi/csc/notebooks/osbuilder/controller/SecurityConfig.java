package fi.csc.notebooks.osbuilder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import fi.csc.notebooks.osbuilder.auth.JWTAuthenticationFilter;
import fi.csc.notebooks.osbuilder.auth.JWTAuthorizationFilter;
import fi.csc.notebooks.osbuilder.data.UserDetailsServiceImpl;



@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	
	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
	
	http.cors().and().headers().frameOptions().disable().
	and().csrf().disable().authorizeRequests()
    	.antMatchers(HttpMethod.GET, "/*", "/h2-console/*", "/favicon.ico", "/login/*")
    	.permitAll()
    	.antMatchers(HttpMethod.POST, "/*", "/h2-console/*", "/login", "/users/signup")
    	.permitAll()
    .anyRequest().authenticated()
    .and()
    .addFilter(new JWTAuthenticationFilter(authenticationManager()))
    .addFilter(new JWTAuthorizationFilter(authenticationManager()))
    // this disables session creation with Spring Security
    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
}
	
	@Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }
	
	
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
	

}