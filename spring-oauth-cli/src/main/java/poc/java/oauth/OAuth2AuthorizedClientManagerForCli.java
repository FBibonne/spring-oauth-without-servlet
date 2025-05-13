package poc.java.oauth;

import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

public record OAuth2AuthorizedClientManagerForCli(AuthorizedClientServiceOAuth2AuthorizedClientManager delegate) implements OAuth2AuthorizedClientManager {

    public OAuth2AuthorizedClientManagerForCli {
        OAuth2AuthorizedClientProviderBuilder oAuth2AuthorizedClientProviderBuilder = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(builder -> builder.accessTokenResponseClient(new RestClientClientCredentialsTokenResponseClient()))
                .provider(new MyAuthorizationCodeOAuth2AuthorizedClientProvider(null));
        // this.builders.computeIfAbsent(AuthorizationCodeOAuth2AuthorizedClientProvider.class, (k) -> new AuthorizationCodeGrantBuilder());
        // this.builders.computeIfAbsent(provider.getClass(), (k) -> () -> provider);

        delegate.setAuthorizedClientProvider(oAuth2AuthorizedClientProviderBuilder
                .build());
        delegate.setContextAttributesMapper(new AuthorizedClientServiceOAuth2AuthorizedClientManager.DefaultContextAttributesMapper());
    }

    public OAuth2AuthorizedClientManagerForCli(ClientRegistrationRepository clientRegistrationRepository) {
        this(new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                        new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)));
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizeRequest authorizeRequest) {
        return delegate.authorize(authorizeRequest);
    }

}
