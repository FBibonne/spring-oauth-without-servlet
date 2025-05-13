package poc.java.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Configuration
public class RestClientConfiguration {

    @Bean
    public OAuth2ClientHttpRequestInterceptor buildRequestInterceptor(Collection<ClientRegistration> registrations) {
        ClientRegistrationRepository clientRegistrationRepository = new InMemoryClientRegistrationRepository(List.copyOf(registrations));
        OAuth2AuthorizedClientManager authorizedClientManager = new OAuth2AuthorizedClientManagerForCli(clientRegistrationRepository);
        OAuth2ClientHttpRequestInterceptor oAuth2ClientHttpRequestInterceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        oAuth2ClientHttpRequestInterceptor.setPrincipalResolver(new RequestAttributePrincipalResolver());
        return oAuth2ClientHttpRequestInterceptor;
    }

    @Bean
    public RestClient.Builder restClient(OAuth2ClientHttpRequestInterceptor oAuth2ClientHttpRequestInterceptor, @Value("${poc.java.oauth.restclient.registration.id}")String registrationId) {
        return RestClient.builder().requestInterceptor(oAuth2ClientHttpRequestInterceptor)
                .defaultRequest(requestHeadersSpec -> requestHeadersSpec.attributes(clientRegistrationId(registrationId)));
    }
}
