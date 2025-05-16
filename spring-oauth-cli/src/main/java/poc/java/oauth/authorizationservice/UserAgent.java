package poc.java.oauth.authorizationservice;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;

public interface UserAgent {

    URLConnection requestAuthorizationCodeAuthenticatingOwner(String authorizationRequestUri) throws IOException;


}
