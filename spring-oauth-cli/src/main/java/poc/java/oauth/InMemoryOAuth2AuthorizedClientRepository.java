package poc.java.oauth;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import java.util.HashMap;
import java.util.Map;

// Copy implementation org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository
// and copy implementation org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository with in memory map instead of session
final class InMemoryOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final Map<String, OAuth2AuthorizedClient> authorizedClients = new HashMap<>();
    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    InMemoryOAuth2AuthorizedClientRepository(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, Authentication principal) {
        if (this.isPrincipalAuthenticated(principal)) {
            return this.authorizedClientService.loadAuthorizedClient(clientRegistrationId, principal.getName());
        }
        return (T) this.getAuthorizedClients().get(clientRegistrationId);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        if (this.isPrincipalAuthenticated(principal)) {
            this.authorizedClientService.saveAuthorizedClient(authorizedClient, principal);
        }
        else {
            Map<String, OAuth2AuthorizedClient> authorizedClients = this.getAuthorizedClients();
            authorizedClients.put(authorizedClient.getClientRegistration().getRegistrationId(), authorizedClient);
        }

    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, Authentication principal) {
        if (this.isPrincipalAuthenticated(principal)) {
            this.authorizedClientService.removeAuthorizedClient(clientRegistrationId, principal.getName());
        }
        else {
            Map<String, OAuth2AuthorizedClient> authorizedClients = this.getAuthorizedClients();
            authorizedClients.remove(clientRegistrationId);
        }
    }

    private boolean isPrincipalAuthenticated(Authentication authentication) {
        return this.authenticationTrustResolver.isAuthenticated(authentication);
    }

    private Map<String, OAuth2AuthorizedClient> getAuthorizedClients() {
        return this.authorizedClients;
    }

}
