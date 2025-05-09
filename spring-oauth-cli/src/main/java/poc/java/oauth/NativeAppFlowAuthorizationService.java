package poc.java.oauth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.util.MultiValueMap;

import java.net.URLConnection;

public record NativeAppFlowAuthorizationService(AuthenticationManager authenticationManager,
                                                NativeAppOAuth2AuthorizationRequestResolver auth2AuthorizationRequestResolver,
                                                UserAgent userAgent) implements AuthorizationService {

    public NativeAppFlowAuthorizationService(AuthenticationManager authenticationManager) {
        this(authenticationManager, new NativeAppOAuth2AuthorizationRequestResolver(), UserAgent.embededUserAgentWithSpnegoAuthent());
    }

    //TODO process OAuth2AuthorizationException elsewhere

    @Override
    public OAuth2AccessTokenResponse getAccessToken(ClientRegistration clientRegistration) {
        // org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.doFilterInternal#catch(ClientAuthorizationRequiredException)
        // Get authorization code
        OAuth2AuthorizationRequest authorizationRequest = this.auth2AuthorizationRequestResolver.resolve(clientRegistration);
        URLConnection response = sendAuthorization(authorizationRequest);
        // org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter.processAuthorizationResponse

        MultiValueMap<String, String> params = UrlUtils.parseQueryString(response.getURL().getQuery());

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponseUtils.convert(params,
                clientRegistration.getRedirectUri());
        OAuth2AuthorizationCodeAuthenticationToken authenticationRequest = new OAuth2AuthorizationCodeAuthenticationToken(
                clientRegistration, new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));
        OAuth2AuthorizationCodeAuthenticationToken authenticationResult;
        authenticationResult = (OAuth2AuthorizationCodeAuthenticationToken) this.authenticationManager
                .authenticate(authenticationRequest);
        return OAuth2AccessTokenResponse.withToken(authenticationRequest.getAccessToken().getTokenValue())
                .refreshToken(authenticationResult.getRefreshToken().getTokenValue())
                .tokenType(authenticationResult.getAccessToken().getTokenType())
                .scopes(authenticationResult.getAccessToken().getScopes())
                .additionalParameters(authenticationResult.getAdditionalParameters())
                .build();
    }

    private URLConnection sendAuthorization(OAuth2AuthorizationRequest authorizationRequest) {
        return sendWithUserAgent(authorizationRequest.getAuthorizationRequestUri());
    }

    private URLConnection sendWithUserAgent(String authorizationRequestUri) {
        return userAgent.requestAuthorizationCodeAuthenticatingOwner(authorizationRequestUri);
    }


}
