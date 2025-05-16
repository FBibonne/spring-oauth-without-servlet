package poc.java.oauth.useragent;

import poc.java.oauth.authorizationservice.UserAgent;

import java.io.IOException;
import java.net.*;

record EmbededUserAgentWithSpnegoAuthent() implements UserAgent {
    public static UserAgent instance() {
        configureForSpnego();
        return new EmbededUserAgentWithSpnegoAuthent();
    }

    private static void configureForSpnego() {
        System.setProperty("java.security.krb5.conf","krb5.conf");
        System.setProperty("java.security.auth.login.config", "spnegoLogin.conf");
        System.setProperty("sun.security.jgss.native","true");
    }

    @Override
    public URLConnection requestAuthorizationCodeAuthenticatingOwner(String authorizationRequestUri) throws IOException {
        URL url = URI.create(authorizationRequestUri).toURL();
        var connection = url.openConnection();
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
        connection.connect();
        return connection;
    }
}
