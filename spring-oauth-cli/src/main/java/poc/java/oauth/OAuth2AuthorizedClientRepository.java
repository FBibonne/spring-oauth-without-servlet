package poc.java.oauth;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

// org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository without web context
public interface OAuth2AuthorizedClientRepository {

    /**
     * Returns the {@link OAuth2AuthorizedClient} associated to the provided client
     * registration identifier and End-User {@link Authentication} (Resource Owner) or
     * {@code null} if not available.
     *
     * @param clientRegistrationId the identifier for the client's registration
     * @param principal            the End-User {@link Authentication} (Resource Owner)
     * @param <T>                  a type of OAuth2AuthorizedClient
     * @return the {@link OAuth2AuthorizedClient} or {@code null} if not available
     */
    <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, Authentication principal);

    /**
     * Saves the {@link OAuth2AuthorizedClient} associating it to the provided End-User
     * {@link Authentication} (Resource Owner).
     *
     * @param authorizedClient the authorized client
     * @param principal        the End-User {@link Authentication} (Resource Owner)
     */
    void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal);

    /**
     * Removes the {@link OAuth2AuthorizedClient} associated to the provided client
     * registration identifier and End-User {@link Authentication} (Resource Owner).
     *
     * @param clientRegistrationId the identifier for the client's registration
     * @param principal            the End-User {@link Authentication} (Resource Owner)
     */
    void removeAuthorizedClient(String clientRegistrationId, Authentication principal);

    static OAuth2AuthorizedClientRepository inMemory(OAuth2AuthorizedClientService authorizedClientService) {
        return new InMemoryOAuth2AuthorizedClientRepository(authorizedClientService);
    }


}
