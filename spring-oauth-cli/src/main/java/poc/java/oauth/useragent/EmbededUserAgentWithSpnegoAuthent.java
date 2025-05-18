package poc.java.oauth.useragent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AuthorizationServiceException;
import poc.java.oauth.authorizationservice.UserAgent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

record EmbededUserAgentWithSpnegoAuthent() implements UserAgent {

    private static final Logger log = LoggerFactory.getLogger(EmbededUserAgentWithSpnegoAuthent.class);

    public static UserAgent instance() {
        configureForSpnego();
        return new EmbededUserAgentWithSpnegoAuthent();
    }

    private static void configureForSpnego() {
        log.debug("Configuring properties for EmbededUserAgentWithSpnego");
        System.setProperty("sun.security.jgss.native", "true");
    }

    @Override
    public URI getRedirectionUriFromAuthorizationServer(String authorizationRequestUri) throws IOException, AuthorizationServiceException {
        log.debug("Call authorization server at {}", authorizationRequestUri);
        URL url = URI.create(authorizationRequestUri).toURL();
        var connection = url.openConnection();
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setAllowUserInteraction(true);
        ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
        connection.connect();

        URI redirectURI = redirectUriInHeaderLocation(connection);

        close(connection);

        log.debug("Authorization server replied with redirect URI {}", redirectURI);

        return redirectURI;
    }

    private void close(URLConnection connection) {
        ((HttpURLConnection) connection).disconnect();
    }

    private URI redirectUriInHeaderLocation(URLConnection connection) {
        var redirectUriAsString = connection.getHeaderField("Location");
        if (redirectUriAsString == null) {
            throw new AuthorizationServiceException("Missing location header in response from authorization server. Headers are " + connection.getHeaderFields());
        }
        return URI.create(redirectUriAsString);
    }
}
