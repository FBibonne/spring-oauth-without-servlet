package poc.java.oauth;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Import(OAuth2ClientProperties.class)
public class SecurityConfiguration {

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations = new ArrayList<>(
                new OAuth2ClientPropertiesMapper(properties).asClientRegistrations().values());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    @Bean
    public ClientHttpRequestInterceptor buildRequestInterceptor(OAuth2ClientProperties properties) {
        ClientRegistrationRepository clientRegistrationRepository = clientRegistrationRepository(properties);
        var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository));
        return new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
    }
}
