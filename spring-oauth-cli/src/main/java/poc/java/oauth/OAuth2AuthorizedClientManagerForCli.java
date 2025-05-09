package poc.java.oauth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record OAuth2AuthorizedClientManagerForCli(AuthorizedClientServiceOAuth2AuthorizedClientManager delegate,
                                                  Map<String, UserDetails> usersCredentialsByRegistrationIds) implements OAuth2AuthorizedClientManager {

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

    private Function<OAuth2AuthorizeRequest, Map<String, Object>> chainMappers(Function<OAuth2AuthorizeRequest, Map<String, Object>> mapper1,
                                                                               Function<OAuth2AuthorizeRequest, Map<String, Object>> mapper2) {
        return authorizeRequest -> {
            Map<String, Object> attributes = new HashMap<>(mapper1.apply(authorizeRequest));
            attributes.putAll(mapper2.apply(authorizeRequest));
            return attributes;
        };
    }

    private Function<OAuth2AuthorizeRequest, Map<String, Object>> usernameAndPasswordMapper() {
        return authorizeRequest -> {
            UserDetails userDetails = usersCredentialsByRegistrationIds.get(authorizeRequest.getClientRegistrationId());
            if (userDetails == null) {
                throw new IllegalArgumentException("No user credentials found for client registration id: " + authorizeRequest.getClientRegistrationId());
            }
            return Map.of(
                    OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, userDetails.getUsername(),
                    OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, userDetails.getPassword()
            );
        };
    }

    public OAuth2AuthorizedClientManagerForCli(ClientRegistrationRepository clientRegistrationRepository, RegistrationsConfiguration registrations) {
        this(new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                        new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)),
                registrations.registrations().entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getValue().getRegistrationId(), Map.Entry::getKey)));
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizeRequest authorizeRequest) {
        return delegate.authorize(authorizeRequest);
    }

}
