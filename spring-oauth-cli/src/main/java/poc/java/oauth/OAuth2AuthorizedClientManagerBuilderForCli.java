package poc.java.oauth;

import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import poc.java.oauth.authorizedclientprovider.MyAuthorizationCodeOAuth2AuthorizedClientProvider;

import java.util.Collection;
import java.util.List;

public class OAuth2AuthorizedClientManagerBuilderForCli {

    private OAuth2AuthorizedClientManagerBuilderForCli(){}

    public static OAuth2AuthorizedClientManager buildFrom(Collection<ClientRegistration> registrations, CustomOAuth2AuthorizationCodeAuthorizationService authorizationService){
        ClientRegistrationRepository clientRegistrationRepository = new InMemoryClientRegistrationRepository(List.copyOf(registrations));
        AuthorizedClientServiceOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository));
        OAuth2AuthorizedClientProviderBuilder oAuth2AuthorizedClientProviderBuilder = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(builder -> builder.accessTokenResponseClient(new RestClientClientCredentialsTokenResponseClient()))
                .provider(new MyAuthorizationCodeOAuth2AuthorizedClientProvider(authorizationService));
        oAuth2AuthorizedClientManager.setAuthorizedClientProvider(oAuth2AuthorizedClientProviderBuilder
                .build());
        oAuth2AuthorizedClientManager.setContextAttributesMapper(new AuthorizedClientServiceOAuth2AuthorizedClientManager.DefaultContextAttributesMapper());
        return oAuth2AuthorizedClientManager;
    }

}
