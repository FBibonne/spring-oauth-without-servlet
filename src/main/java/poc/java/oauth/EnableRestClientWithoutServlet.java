package poc.java.oauth;

import org.springframework.context.annotation.Import;

@Import(RestClientConfiguration.class)
public @interface EnableRestClientWithoutServlet {
}
