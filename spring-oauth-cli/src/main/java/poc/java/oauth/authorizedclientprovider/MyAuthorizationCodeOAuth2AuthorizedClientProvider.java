package poc.java.oauth.authorizedclientprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.Assert;
import poc.java.oauth.CustomOAuth2AuthorizationCodeAuthorizationService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public record MyAuthorizationCodeOAuth2AuthorizedClientProvider(Clock clock, Duration clockSkew, CustomOAuth2AuthorizationCodeAuthorizationService customOAuth2AuthorizationCodeAuthorizationService) implements OAuth2AuthorizedClientProvider {

    private static final Logger log = LoggerFactory.getLogger(MyAuthorizationCodeOAuth2AuthorizedClientProvider.class);


    public MyAuthorizationCodeOAuth2AuthorizedClientProvider{
        clock = Clock.systemUTC();
        clockSkew = Duration.ofSeconds(60);
    }

    public MyAuthorizationCodeOAuth2AuthorizedClientProvider(CustomOAuth2AuthorizationCodeAuthorizationService customOAuth2AuthorizationCodeAuthorizationService){
        this(null, null, customOAuth2AuthorizationCodeAuthorizationService);
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
                //Unlike AuthorizationCodeOAuth2AuthorizedClientProvider does, we test token expiration like in others OAuth2AuthorizedClientProviders
                || ( context.getAuthorizedClient() != null && !hasTokenExpired(context.getAuthorizedClient().getAccessToken()))
        ){
            // negation of condition in org.springframework.security.oauth2.client.AuthorizationCodeOAuth2AuthorizedClientProvider.authorize
            log.debug("yet an authorized client in authorization context");
            return null;
        }

        log.debug("No authorized client in authorization context : start authorization");

        ClientRegistration clientRegistration = context.getClientRegistration();

        OAuth2AccessTokenResponse tokenResponse = getTokenResponse(clientRegistration);

        return new OAuth2AuthorizedClient(clientRegistration, context.getPrincipal().getName(),
                tokenResponse.getAccessToken());
    }

    private OAuth2AccessTokenResponse getTokenResponse(ClientRegistration clientRegistration) {
        return customOAuth2AuthorizationCodeAuthorizationService.getAccessToken(clientRegistration);
    }

    private boolean hasTokenExpired(OAuth2Token token) {
        Instant tokenExpiresAt = token ==null ? null :  token.getExpiresAt();
        return tokenExpiresAt==null || this.clock.instant().isAfter(tokenExpiresAt.minus(this.clockSkew));
    }

}
