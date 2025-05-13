package poc.java.oauth;

import java.net.URLConnection;

record EmbededUserAgentWithSpnegoAuthent() implements UserAgent {
    public static UserAgent instance() {
        configureForSpnego();
        return new EmbededUserAgentWithSpnegoAuthent();
    }

    private static void configureForSpnego() {
    }

    @Override
    public URLConnection requestAuthorizationCodeAuthenticatingOwner(String authorizationRequestUri) {
        return null;
    }
}
