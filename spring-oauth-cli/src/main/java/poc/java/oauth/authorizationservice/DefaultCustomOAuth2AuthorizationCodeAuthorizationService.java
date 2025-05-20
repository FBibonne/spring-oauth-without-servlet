package poc.java.oauth.authorizationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.util.MultiValueMap;
import poc.java.oauth.CustomOAuth2AuthorizationCodeAuthorizationService;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;

public record DefaultCustomOAuth2AuthorizationCodeAuthorizationService(AuthenticationManager authenticationManager,
                                                                       CustomOAuth2AuthorizationRequestResolver auth2AuthorizationRequestResolver,
                                                                       UserAgent userAgent, Clock clock) implements CustomOAuth2AuthorizationCodeAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultCustomOAuth2AuthorizationCodeAuthorizationService.class);

    public DefaultCustomOAuth2AuthorizationCodeAuthorizationService(AuthenticationManager authenticationManager, UserAgent userAgent) {
        this(authenticationManager, new CustomOAuth2AuthorizationRequestResolver(), userAgent, Clock.systemUTC());
    }

    @Override
    public OAuth2AccessTokenResponse getAccessToken(ClientRegistration clientRegistration) throws AuthorizationServiceException {
        // org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.doFilterInternal#catch(ClientAuthorizationRequiredException)
        // Get authorization code
        try {
            log.debug("Call authorization server from client registration {}", clientRegistration);
            OAuth2AuthorizationRequest authorizationRequest = this.auth2AuthorizationRequestResolver.resolve(clientRegistration);
            String authorizationEndpointCallUri = authorizationRequest.getAuthorizationRequestUri();
            URI redirectionUriFromAuthorizationServer = userAgent.getRedirectionUriFromAuthorizationServer(authorizationEndpointCallUri);

            // org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter.processAuthorizationResponse
            MultiValueMap<String, String> reponseParams = UrlUtils.parseQueryString(redirectionUriFromAuthorizationServer.getQuery());

            OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponseUtils.convert(reponseParams,
                    clientRegistration.getRedirectUri());
            OAuth2AuthorizationCodeAuthenticationToken authenticationRequest = new OAuth2AuthorizationCodeAuthenticationToken(
                    clientRegistration, new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));
            log.debug("Call token endpoint with authentication manager {} to process OAuth2AuthorizationCodeAuthenticationToken {}", authenticationManager, authenticationRequest);
            OAuth2AuthorizationCodeAuthenticationToken authenticationResult = (OAuth2AuthorizationCodeAuthenticationToken) this.authenticationManager
                    .authenticate(authenticationRequest);
            OAuth2AccessToken accessToken = authenticationResult.getAccessToken();
            log.debug("Got access token valid until {} with refresh token {}", accessToken.getExpiresAt(), authenticationResult.getRefreshToken());
            return OAuth2AccessTokenResponse.withToken(accessToken.getTokenValue())
                    .expiresIn(clock.instant().until(accessToken.getExpiresAt()).getSeconds())
                    .refreshToken(authenticationResult.getRefreshToken().getTokenValue())
                    .tokenType(accessToken.getTokenType())
                    .scopes(accessToken.getScopes())
                    .additionalParameters(authenticationResult.getAdditionalParameters())
                    .build();
        } catch (IOException e) {
            throw new AuthorizationServiceException(e.getMessage(), e);
        }
    }

}
