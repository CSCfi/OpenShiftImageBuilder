package fi.csc.notebooks.osbuilder.constants;


public  final class SecurityConstants {
    public static final String SECRET = "test";
    //public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final long EXPIRATION_TIME = 36_000_000; // 10 hours
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/users/sign-up";
    public static final String PRIVATE_KEY_DER_PATH = "/config/keys/private_key.der";
    public static final String PUBLIC_KEY_DER_PATH = "/config/keys/public_key.der";
    
    public static final String NAMESPACE_FILEPATH = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
    public static final String TOKEN_FILEPATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";
    
    public static final String CLUSTER_PROPERTIES_PATH = "/config/cluster.properties";
    
}
