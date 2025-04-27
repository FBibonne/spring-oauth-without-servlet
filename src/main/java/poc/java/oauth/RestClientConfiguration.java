package poc.java.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Configuration
public class RestClientConfiguration {

    @Bean
    public OAuth2ClientHttpRequestInterceptor buildRequestInterceptor(List<ClientRegistration> registrations) {
        ClientRegistrationRepository clientRegistrationRepository = new InMemoryClientRegistrationRepository(registrations);
        var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository));
        return new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
    }

    @Bean
    public RestClient.Builder restClient(OAuth2ClientHttpRequestInterceptor oAuth2ClientHttpRequestInterceptor, @Value("${poc.java.oauth.restclient.registration.id}")String registrationId) {
        return RestClient.builder().requestInterceptor(oAuth2ClientHttpRequestInterceptor)
                .defaultRequest(requestHeadersSpec -> requestHeadersSpec.attributes(clientRegistrationId(registrationId)));
    }
}
