package poc.java.oauth.cli;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.CommandResolver;
import org.springframework.shell.command.annotation.CommandScan;
import poc.java.oauth.EnableRestClientWithoutServlet;

import java.util.Collection;

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

    //API RH ? (trombi)
    @Bean
    CommandResolver commands(OpenAPI openAPI){
        var baseUri = openAPI.getServers().getFirst().getUrl();
        return () -> openAPI.getPaths().entrySet().stream()
                .flatMap(CliCommand::fromPath)
                .map(cliCommand -> CommandRegistration.builder()
                        .command(cliCommand.id())
                        .withTarget().function(cliCommand)
                        .and().build())
                .toList();
    }


}
