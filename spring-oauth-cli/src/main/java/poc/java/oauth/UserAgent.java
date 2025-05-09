package poc.java.oauth;

import java.net.URLConnection;

public interface UserAgent {
    static UserAgent embededUserAgentWithSpnegoAuthent() {
        return EmbededUserAgentWithSpnegoAuthent.instance();
    }

    URLConnection requestAuthorizationCodeAuthenticatingOwner(String authorizationRequestUri);


}
