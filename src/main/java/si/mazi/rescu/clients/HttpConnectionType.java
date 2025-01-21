package si.mazi.rescu.clients;

public enum HttpConnectionType {
    
    /** java.net.HttpURLConnection */
    java,
    /** org.apache.httpcomponents:httpclient */
    apache
    ;
    
    public static final HttpConnectionType DEFAULT = java;
}
