package poc.java.oauth.authorizationservice;

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
import poc.java.oauth.NativeCliAuthorizationService;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;

public record NativeAppFlowAuthorizationService(AuthenticationManager authenticationManager,
                                                NativeAppOAuth2AuthorizationRequestResolver auth2AuthorizationRequestResolver,
                                                UserAgent userAgent) implements NativeCliAuthorizationService {

    public NativeAppFlowAuthorizationService(AuthenticationManager authenticationManager, UserAgent userAgent) {
        this(authenticationManager, new NativeAppOAuth2AuthorizationRequestResolver(), userAgent);
    }

    //TODO process OAuth2AuthorizationException elsewhere

    @Override
    public OAuth2AccessTokenResponse getAccessToken(ClientRegistration clientRegistration) throws AuthorizationServiceException {
        // org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.doFilterInternal#catch(ClientAuthorizationRequiredException)
        // Get authorization code
        try {
            OAuth2AuthorizationRequest authorizationRequest = this.auth2AuthorizationRequestResolver.resolve(clientRegistration);
            URLConnection response = sendAuthorization(authorizationRequest);
            // org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter.processAuthorizationResponse

            MultiValueMap<String, String> reponseParams = queryParamsFromLocationHeaderFrom(response);

            OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponseUtils.convert(reponseParams,
                    clientRegistration.getRedirectUri());
            OAuth2AuthorizationCodeAuthenticationToken authenticationRequest = new OAuth2AuthorizationCodeAuthenticationToken(
                    clientRegistration, new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));
            OAuth2AuthorizationCodeAuthenticationToken authenticationResult;
            authenticationResult = (OAuth2AuthorizationCodeAuthenticationToken) this.authenticationManager
                    .authenticate(authenticationRequest);
            OAuth2AccessToken accessToken = authenticationResult.getAccessToken();
            return OAuth2AccessTokenResponse.withToken(accessToken.getTokenValue())
                    .refreshToken(authenticationResult.getRefreshToken().getTokenValue())
                    .tokenType(accessToken.getTokenType())
                    .scopes(accessToken.getScopes())
                    .additionalParameters(authenticationResult.getAdditionalParameters())
                    .build();
        } catch (IOException e) {
            throw new AuthorizationServiceException(e.getMessage(), e);
        }
    }

    private MultiValueMap<String, String> queryParamsFromLocationHeaderFrom(URLConnection response) {
        var redirectUriAsString=response.getHeaderField("Location");
        if (redirectUriAsString == null) {
            throw new AuthorizationServiceException("Missing location header in response from authorization server. Headers are "+response.getHeaderFields());
        }
        var redirectUri = URI.create(redirectUriAsString);
        return UrlUtils.parseQueryString(redirectUri.getQuery());
    }

    private URLConnection sendAuthorization(OAuth2AuthorizationRequest authorizationRequest) throws IOException {
        return sendWithUserAgent(authorizationRequest.getAuthorizationRequestUri());
    }

    private URLConnection sendWithUserAgent(String authorizationRequestUri) throws IOException {
        return userAgent.requestAuthorizationCodeAuthenticatingOwner(authorizationRequestUri);
    }


}
