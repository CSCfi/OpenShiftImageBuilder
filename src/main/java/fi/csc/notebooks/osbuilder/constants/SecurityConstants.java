package fi.csc.notebooks.osbuilder.constants;


public  final class SecurityConstants {
    public static final String SECRET = "test";
    //public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final long EXPIRATION_TIME = 60000; // 1 min
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/users/sign-up";
    public static final String PRIVATE_KEY_DER_PATH = "/run/private_pebbles.der";
    public static final String PUBLIC_KEY_DER_PATH = "/run/public_pebbles.der";
    public static final String CLUSTER_PROPERTIES_PATH = "/run/cluster.properties";
    
}
