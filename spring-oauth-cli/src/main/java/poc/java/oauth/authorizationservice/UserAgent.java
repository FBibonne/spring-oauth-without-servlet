package poc.java.oauth.authorizationservice;

import java.io.IOException;
import java.net.URI;

public interface UserAgent {
    URI getRedirectionUriFromAuthorizationServer(String authorizationRequestUri) throws IOException;
}
