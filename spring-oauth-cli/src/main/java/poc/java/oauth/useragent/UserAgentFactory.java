package poc.java.oauth.useragent;

import poc.java.oauth.authorizationservice.UserAgent;

public final class UserAgentFactory {
    private UserAgentFactory() {}

    public static UserAgent embededUserAgentWithSpnegoAuthent() {
        return EmbededUserAgentWithSpnegoAuthent.instance();
    }
}
