package poc.java.oauth;

import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public interface NativeCliAuthorizationService {
    OAuth2AccessTokenResponse getAccessToken(ClientRegistration clientRegistration) throws AuthorizationServiceException;
}
