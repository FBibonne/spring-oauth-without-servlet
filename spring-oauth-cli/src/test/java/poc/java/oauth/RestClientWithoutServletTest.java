package poc.java.oauth;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RestClientWithoutServletTest {

    public static final String CLIENT_REGISTRATION_ID = "client-registration";
    private static final String CLIENT_ID = "poc-java-client-test";
    private static final String CLIENT_SECRET = "pocjavaclientverysecret";
    private static final String[] SCOPE = {"openid", "profile"};
    private static final String CLIENT_NAME = "name-for-poc-java-client-test";
    private static final String TOKEN_ENDPOINT = "http://localhost:9000/oauth2/token";
    private static final String AUTHORIZATION_ENDPOINT = "http://localhost:9000/oauth2/authorize";

    WireMockServer wireMockServer;
    final String reponseBody = "Hello";

    private static String redirectUri;


    @BeforeEach
    public void setupWireMock() {
        wireMockServer = new WireMockServer(wireMockConfig()
                .dynamicPort()
                .networkTrafficListener(new ConsoleNotifyingWiremockNetworkTrafficListener())
        );
        wireMockServer.stubFor(get("/hello").andMatching(new RequestMatcherExtension() {
                    @Override
                    public MatchResult match(Request request, Parameters parameters) {
                        return MatchResult.of(! request.containsHeader("Authorization"));
                    }
                })
                .willReturn(aResponse()
                        .withStatus(401)));

        wireMockServer.stubFor(get("/hello").withHeader("Authorization", containing("Bearer "))
                .willReturn(aResponse()
                        .withBody(reponseBody)
                        .withStatus(200)));
        wireMockServer.start();
    }

    @AfterEach
    void tearDownWireMock() {
        wireMockServer.stop();
    }

    @Test
    void testRestClientWithoutServlet() {

        redirectUri = setRedirectUriTemporarily();
        String wiremockBaseUrl = wireMockServer.baseUrl();

        System.setProperty("poc.java.oauth.restclient.registration.id", CLIENT_REGISTRATION_ID);


        ApplicationContext context = new AnnotationConfigApplicationContext(RestClientWithoutServletConfiguration.class);
        assertThatCode(() -> Class.forName("jakarta.servlet.http.HttpServlet")).isInstanceOf(ClassNotFoundException.class);
        assertThat(context).isNotInstanceOf(WebApplicationContext.class);

        var restClientBuilder = context.getBean(RestClient.Builder.class);

        var response = restClientBuilder.build().get().uri(wiremockBaseUrl + "/hello")
                //.headers(httpHeaders -> httpHeaders.setBasicAuth("owner_user", "owner_password"))
                //.attributes(RequestAttributePrincipalResolver.principal("owner_user"))
                .retrieve()
                .toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasToString(reponseBody);
    }

    //TODO Set in RestClientConfiguration
    private static String setRedirectUriTemporarily() {
        return "http://localhost:11111/fake";
    }

    @Configuration
    @EnableRestClientWithoutServlet
    static class RestClientWithoutServletConfiguration {

        @Bean
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        Collection<ClientRegistration> registrationsConfiguration() {
            return List.of(
                    ClientRegistration.withRegistrationId(CLIENT_REGISTRATION_ID)
                            .clientId(CLIENT_ID)
                            .clientSecret(CLIENT_SECRET)
                            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .authorizationUri(AUTHORIZATION_ENDPOINT)
                            .redirectUri(redirectUri)
                            .scope(SCOPE)
                            .tokenUri(TOKEN_ENDPOINT)
                            .clientName(CLIENT_NAME).build()
            );
        }

    }

}
