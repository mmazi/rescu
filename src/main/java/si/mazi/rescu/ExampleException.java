package si.mazi.rescu;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Matija Mazi <br/>
 * @created 5/24/13 9:04 PM
 */
public class ExampleException extends RuntimeException {
    @JsonProperty("error")
    private String error;

    @JsonProperty("result")
    private String result;

    @JsonProperty("token")
    private String token;

    public ExampleException() { }

    public String getError() {
        return error;
    }

    public String getResult() {
        return result;
    }

    public String getToken() {
        return token;
    }
}
