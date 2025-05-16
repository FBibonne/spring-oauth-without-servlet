package poc.java.oauth;

import org.springframework.context.annotation.Import;
import poc.java.oauth.authorizationservice.NativeCliAuthorizationServiceConfiguration;
import poc.java.oauth.useragent.UserAgentConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({RestClientConfiguration.class, NativeCliAuthorizationServiceConfiguration.class, UserAgentConfiguration.class})
public @interface EnableRestClientWithoutServlet {
}
