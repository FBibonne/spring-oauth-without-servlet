package poc.java.oauth.clientmanager;

import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.Assert;
import poc.java.oauth.NativeCliAuthorizationService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

record MyAuthorizationCodeOAuth2AuthorizedClientProvider(Clock clock, Duration clockSkew, NativeCliAuthorizationService nativeCliAuthorizationService) implements OAuth2AuthorizedClientProvider {

    MyAuthorizationCodeOAuth2AuthorizedClientProvider{
        clock = Clock.systemUTC();
        clockSkew = Duration.ofSeconds(60);
    }

    MyAuthorizationCodeOAuth2AuthorizedClientProvider(NativeCliAuthorizationService authorizationService){
        this(null, null, authorizationService);
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        /*
        Attempt to authorize (or re-authorize) the client in the provided context. Implementations must return null if authorization is not supported for the specified client, e.g. the provider doesn't support the authorization grant type configured for the client.
Specified by:
authorize in interface OAuth2AuthorizedClientProvider
Params:
context – the context that holds authorization-specific state for the client
Returns:
the OAuth2AuthorizedClient or null if authorization is not supported for the specified client
         */
        Assert.notNull(context, "context cannot be null");
        if ((! AuthorizationGrantType.AUTHORIZATION_CODE.equals(
                context.getClientRegistration().getAuthorizationGrantType()))
                || context.getAuthorizedClient() != null
        ){
            // negation of condition in org.springframework.security.oauth2.client.AuthorizationCodeOAuth2AuthorizedClientProvider.authorize
            return null;
        }

        //

        ClientRegistration clientRegistration = context.getClientRegistration();

        OAuth2AccessTokenResponse tokenResponse = getTokenResponse(clientRegistration);

        return new OAuth2AuthorizedClient(clientRegistration, context.getPrincipal().getName(),
                tokenResponse.getAccessToken());
    }

    private OAuth2AccessTokenResponse getTokenResponse(ClientRegistration clientRegistration) {
        return nativeCliAuthorizationService.getAccessToken(clientRegistration);
    }

    private boolean hasTokenExpired(OAuth2Token token) {
        Instant tokenExpiresAt = token.getExpiresAt();
        return tokenExpiresAt==null || this.clock.instant().isAfter(tokenExpiresAt.minus(this.clockSkew));
    }

}
