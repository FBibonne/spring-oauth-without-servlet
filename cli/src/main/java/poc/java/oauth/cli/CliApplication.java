package poc.java.oauth.cli;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.CommandResolver;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.web.client.RestClient;
import poc.java.oauth.EnableRestClientWithoutServlet;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;

import static org.springframework.shell.command.CommandRegistration.OptionArity.EXACTLY_ONE;
import static org.springframework.shell.command.CommandRegistration.OptionArity.ZERO_OR_ONE;

@SpringBootApplication
@CommandScan
@EnableConfigurationProperties(OAuth2ClientProperties.class)
@EnableRestClientWithoutServlet
public class CliApplication {

    public static void main(String[] args) {
        SpringApplication.run(CliApplication.class, args);
    }

    @Bean
    @ConditionalOnMissingBean(value=ClientRegistration.class, parameterizedContainer = Collection.class)
    Collection<ClientRegistration> clientRegistrationRepository(OAuth2ClientProperties properties) {
        return (new OAuth2ClientPropertiesMapper(properties)).asClientRegistrations().values();
    }

    @Bean
    OpenAPI openAPI(@Value("${poc.java.oauth.openapi.spec.resource}")String openapiSpecResource)  {
        return (new OpenAPIV3Parser()).read(Path.of(openapiSpecResource).toUri().toString());
    }


    @Bean
    CommandResolver commands(OpenAPI openAPI, RestClient.Builder restCLientBuilder) {
        final URI baseUri = URI.create(openAPI.getServers().getFirst().getUrl());
        return () -> openAPI.getPaths().entrySet().stream()
                .flatMap(entryPath -> CliCommand.fromPath(entryPath, baseUri, restCLientBuilder))
                .map(cliCommand -> {
                    CommandRegistration.Builder builder = CommandRegistration.builder();
                    builder.command(cliCommand.id()).withTarget().function(cliCommand);
                    cliCommand.parameters().forEach(urlParameter -> builder.withOption()
                            .longNames(urlParameter.name())
                            .arity(urlParameter.required()?EXACTLY_ONE:ZERO_OR_ONE)
                    );
                    return builder.build();
                })
                .toList();
    }




}
