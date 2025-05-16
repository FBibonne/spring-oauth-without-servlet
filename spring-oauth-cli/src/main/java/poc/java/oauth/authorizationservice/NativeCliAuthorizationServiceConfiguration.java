package poc.java.oauth.authorizationservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import poc.java.oauth.NativeCliAuthorizationService;

@Configuration
public class NativeCliAuthorizationServiceConfiguration {

    @Bean
    public NativeCliAuthorizationService nativeCliAuthorizationService(AuthenticationManager authenticationManager, UserAgent userAgent) throws Exception {
        return new NativeAppFlowAuthorizationService(authenticationManager, userAgent);

    }

    @Bean
    public AuthenticationManager authenticationManagerSupportingOAuth2AuthorizationCode() {
        return new ProviderManager(new OAuth2AuthorizationCodeAuthenticationProvider(new RestClientAuthorizationCodeTokenResponseClient()));
    }

}
