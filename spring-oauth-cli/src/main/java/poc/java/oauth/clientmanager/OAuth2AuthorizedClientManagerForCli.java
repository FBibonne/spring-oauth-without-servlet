package poc.java.oauth.clientmanager;

import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import poc.java.oauth.NativeCliAuthorizationService;

import java.util.Collection;
import java.util.List;

public record OAuth2AuthorizedClientManagerForCli(AuthorizedClientServiceOAuth2AuthorizedClientManager delegate, NativeCliAuthorizationService authorizationService) implements OAuth2AuthorizedClientManager {

    public OAuth2AuthorizedClientManagerForCli {
        OAuth2AuthorizedClientProviderBuilder oAuth2AuthorizedClientProviderBuilder = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(builder -> builder.accessTokenResponseClient(new RestClientClientCredentialsTokenResponseClient()))
                .provider(new MyAuthorizationCodeOAuth2AuthorizedClientProvider(authorizationService));
        // this.builders.computeIfAbsent(AuthorizationCodeOAuth2AuthorizedClientProvider.class, (k) -> new AuthorizationCodeGrantBuilder());
        // this.builders.computeIfAbsent(provider.getClass(), (k) -> () -> provider);

        delegate.setAuthorizedClientProvider(oAuth2AuthorizedClientProviderBuilder
                .build());
        delegate.setContextAttributesMapper(new AuthorizedClientServiceOAuth2AuthorizedClientManager.DefaultContextAttributesMapper());
    }

    public OAuth2AuthorizedClientManagerForCli(ClientRegistrationRepository clientRegistrationRepository, NativeCliAuthorizationService authorizationService) {
        this(new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                        new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)), authorizationService);
    }

    public OAuth2AuthorizedClientManagerForCli(Collection<ClientRegistration> registrations, NativeCliAuthorizationService authorizationService) {
        this(new InMemoryClientRegistrationRepository(List.copyOf(registrations)), authorizationService);
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizeRequest authorizeRequest) {
        return delegate.authorize(authorizeRequest);
    }

}
