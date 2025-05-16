package poc.java.oauth.useragent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import poc.java.oauth.authorizationservice.UserAgent;

@Configuration
public class UserAgentConfiguration {

    @Bean
    public UserAgent userAgent() {
        return EmbededUserAgentWithSpnegoAuthent.instance();
    }

}
