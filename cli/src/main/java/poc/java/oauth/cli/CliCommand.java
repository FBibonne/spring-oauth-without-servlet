package poc.java.oauth.cli;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.shell.command.CommandContext;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CliCommand(String id, OperationMethod operationMethod, RestClient restClient, Set<UrlParameter> parameters) implements Function<CommandContext, String> {

    CliCommand(String id, OperationMethod operationMethod, UriComponentsBuilder uriBuilder, Set<UrlParameter> parameters, RestClient.Builder restCLientBuilder){
        this(id, operationMethod, restCLientBuilder.uriBuilderFactory(new DefaultUriBuilderFactory(uriBuilder)).build(), parameters);
    }


    @Override
    public String apply(CommandContext commandContext) {
        return "to be implemented";
    }

    public static Stream<CliCommand> fromPath(Map.Entry<String, PathItem> pathEntry, URI serverUri, RestClient.Builder restCLientBuilder) {
        final String uriSuffix = pathEntry.getKey();
        return Stream.of(OperationMethod.values())
                        .map(operationMethod -> CliCommand.of(serverUri, uriSuffix, pathEntry.getValue(), operationMethod, restCLientBuilder));

    }

    private static CliCommand of(URI serverUri, String uriSuffix, PathItem pathItem, OperationMethod operationMethod, RestClient.Builder restCLientBuilder) {
        Operation operation = operationMethod.toOperation.apply(pathItem);
        var id = operation.getOperationId();
        return new CliCommand(id, operationMethod, UriComponentsBuilder.fromUri(serverUri).path(uriSuffix),
                operation.getParameters().stream().map(UrlParameter::new).collect(Collectors.toSet()), restCLientBuilder);
    }

    record UrlParameter(String name, boolean required) {
        UrlParameter(Parameter parameter){
            this(parameter.getName(), parameter.getRequired());
        }
    }

    enum OperationMethod {
        GET(PathItem::getGet), POST(PathItem::getPost), PUT(PathItem::getPut), PATCH(PathItem::getPatch), DELETE(PathItem::getDelete);

        private final Function<PathItem, Operation> toOperation;

        OperationMethod(Function<PathItem, Operation> toOperation) {
            this.toOperation = toOperation;
        }
    }
}
