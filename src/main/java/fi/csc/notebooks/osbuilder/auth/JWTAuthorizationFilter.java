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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fi.csc.notebooks.osbuilder.constants.SecurityConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

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
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        if (token != null) {
            // parse the token.
        	
        	token  = token.replace(SecurityConstants.TOKEN_PREFIX, "");
        	//System.out.println("DEBUG: " + token);
        		
        	byte[] keyBytes = null;
			try {
				keyBytes = Files.readAllBytes(Paths.get(SecurityConstants.PUBLIC_KEY_DER_PATH));
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			 X509EncodedKeySpec spec =
				      new X509EncodedKeySpec(keyBytes);
				    KeyFactory kf = null;
					try {
						kf = KeyFactory.getInstance("RSA");
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    PublicKey publicKey = null;
					try {
						publicKey = kf.generatePublic(spec);
					} catch (InvalidKeySpecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
			
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
        		System.out.println(e.getMessage());
        		return null;
        	}
        	JsonObject tokenBody = JsonParser.parseString(parsedTokenJsonBody).getAsJsonObject();
        	//System.out.println("DEBUG: " + tokenBody);
        	
        	String user = tokenBody.get("sub").getAsString();
        	
        	
			
            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
            return null;
        }
        return null;
    }
}
