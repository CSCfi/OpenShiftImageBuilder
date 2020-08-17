package fi.csc.notebooks.osbuilder.auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.qos.logback.classic.Level;
import fi.csc.notebooks.osbuilder.constants.SecurityConstants;
import fi.csc.notebooks.osbuilder.utils.Utils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private static final Logger logger = LoggerFactory.getLogger(JWTAuthorizationFilter.class);
	ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(logger.getName());
	
    public JWTAuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(SecurityConstants.HEADER_STRING);

        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        
        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        if (authentication == null)
        	res.sendError(401, "Check authorization token");
        else {
        	SecurityContextHolder.getContext().setAuthentication(authentication);
        	chain.doFilter(req, res);
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    	
    	if(Utils.getDebugState())
    		root.setLevel(Level.DEBUG);
    	
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        if (token != null) {
            // Parse the JWT token.
        	
        	token  = token.replace(SecurityConstants.TOKEN_PREFIX, "");
        	
        	logger.debug("Checking token received : " + token);
        		
			byte[] keyBytes = null;
			KeyFactory kf = null;
			PublicKey publicKey = null;		
			try {
					keyBytes = Files.readAllBytes(Paths.get(SecurityConstants.PUBLIC_KEY_DER_PATH));
					kf = KeyFactory.getInstance("RSA");
					X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
					publicKey = kf.generatePublic(spec);
			} catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
					logger.error(e.getMessage());
					logger.error("Authentication failed, check Key or RSA Algorithm");
					return null;
			}		
					
			String parsedTokenJsonBody = "";
        	try {
        		parsedTokenJsonBody = Jwts.parser()
        			//.setSigningKey(SecurityConstants.SECRET.getBytes())
        			.setSigningKey(publicKey)
        			.parse(token)
        			.getBody().toString();
        	}
        	catch(ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
        		logger.warn(e.getMessage());
        		logger.warn(String.format("Authentication did not succeed for invalid token %s, please try again with a new token", token));
				return null;
        	}
        	
        	logger.debug("Parsed Token: " + parsedTokenJsonBody);
        	
        	JsonObject tokenBody = JsonParser.parseString(parsedTokenJsonBody).getAsJsonObject();
        	
        	String user = tokenBody.get("sub").getAsString();
        	
        	
			
            if (user != null) {
            	logger.debug("Authorization Succeeded for " + user);
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
            logger.error("The code should not reach here, if you see this message, there is a new unforeseen error with the JWT Token mechanism!");
            return null;
        }
        return null;
    }
}
