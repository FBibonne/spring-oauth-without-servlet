package poc.java.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RestClientWithoutServletTest {

    public static final String CLIENT_REGISTRATION_ID = "client-registration";
    private static final String CLIENT_ID = ;
    private static final String CLIENT_SECRET = ;
    private static final String SCOPE = ;
    private static final String CLIENT_NAME= ;

    private static String redirectUri;


    @Test
    void testRestClientWithoutServlet() {
        ApplicationContext context=new AnnotationConfigApplicationContext(RestClientWithoutServletConfiguration.class);
        assertThatCode(()->Class.forName("jakarta.servlet.http.HttpServlet")).isInstanceOf(ClassNotFoundException.class);
        assertThat(context).isNotInstanceOf(WebApplicationContext.class);

        var restClientBuilder = context.getBean(RestClient.Builder.class);
        restClientBuilder.build().
    }

    @Configuration
    @EnableRestClientWithoutServlet
    @TestPropertySource(properties = {"poc.java.oauth.restclient.registration.id=" + CLIENT_REGISTRATION_ID})
    static class RestClientWithoutServletConfiguration{

        @Bean
        List<ClientRegistration > clientRegistrations() {
            var clientRegistration = ClientRegistration.withRegistrationId(CLIENT_REGISTRATION_ID);
            clientRegistration.clientId(CLIENT_ID);
            clientRegistration.clientSecret(CLIENT_SECRET);
                    clientRegistration.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                    clientRegistration.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
                    clientRegistration.redirectUri(redirectUri);
                    clientRegistration.scope(SCOPE);
                    clientRegistration.clientName(CLIENT_NAME);
            return List.of(
                    clientRegistration.build()
            );
        }

    }

}
