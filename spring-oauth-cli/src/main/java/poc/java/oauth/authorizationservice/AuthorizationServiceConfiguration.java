package poc.java.oauth.authorizationservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import poc.java.oauth.CustomOAuth2AuthorizationCodeAuthorizationService;

@Configuration
public class AuthorizationServiceConfiguration {

    @Bean
    public CustomOAuth2AuthorizationCodeAuthorizationService customOAuth2AuthorizationCodeAuthorizationService(AuthenticationManager authenticationManager, UserAgent userAgent) {
        return new DefaultCustomOAuth2AuthorizationCodeAuthorizationService(authenticationManager, userAgent);

    }

    @Bean
    public AuthenticationManager authenticationManagerSupportingOAuth2AuthorizationCode() {
        return new ProviderManager(new OAuth2AuthorizationCodeAuthenticationProvider(new RestClientAuthorizationCodeTokenResponseClient()));
    }

}
