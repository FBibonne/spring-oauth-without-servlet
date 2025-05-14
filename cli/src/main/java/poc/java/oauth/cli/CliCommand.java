package poc.java.oauth.cli;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpMethod;
import org.springframework.shell.command.CommandContext;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CliCommand(String id, OperationMethod operationMethod, RestClient restClient,
                         Set<UrlParameter> parameters) implements Function<CommandContext, String> {

    CliCommand(String id, OperationMethod operationMethod, UriComponentsBuilder uriBuilder, Set<UrlParameter> parameters, RestClient.Builder restCLientBuilder) {
        this(id, operationMethod, restCLientBuilder.uriBuilderFactory(new DefaultUriBuilderFactory(uriBuilder)).build(), parameters);
    }


    @Override
    public String apply(CommandContext commandContext) {
        return restClient.method(operationMethod.restClientMethod())
                .uri("/",
                        parameters.stream()
                                .<Pair<String, String>>mapMulti((parameter, downstream) -> {
                                    String value = commandContext.getOptionValue(parameter.name());
                                    if (value != null) {
                                        downstream.accept(Pair.of(parameter.name(), value));
                                    }
                                    if (parameter.required()) {
                                        throw new IllegalStateException("Required parameter " + parameter.name() + " is not present");
                                    }
                                })
                                .collect(Collectors.toMap(Pair::getKey, Pair::getValue))
                ).retrieve()
                .body(String.class);

    }

    public static Stream<CliCommand> fromPath(Map.Entry<String, PathItem> pathEntry, URI serverUri, RestClient.Builder restCLientBuilder) {
        final String uriSuffix = pathEntry.getKey();
        return Stream.of(OperationMethod.values())
                .mapMulti((operationMethod, downstream) -> CliCommand.of(serverUri, uriSuffix, pathEntry.getValue(), operationMethod, restCLientBuilder)
                        .ifPresent(downstream));

    }

    private static Optional<CliCommand> of(URI serverUri, String uriSuffix, PathItem pathItem, OperationMethod operationMethod, RestClient.Builder restCLientBuilder) {
        Operation operation = operationMethod.toOperation.apply(pathItem);
        if (operation == null) {
            return Optional.empty();
        }
        var id = operation.getOperationId();
        return Optional.of(new CliCommand(id, operationMethod, UriComponentsBuilder.fromUri(serverUri).path(uriSuffix),
                operation.getParameters().stream().map(UrlParameter::new).collect(Collectors.toSet()), restCLientBuilder));
    }

    record UrlParameter(String name, boolean required) {
        UrlParameter(Parameter parameter) {
            this(parameter.getName(), parameter.getRequired());
        }
    }

    enum OperationMethod {
        GET(PathItem::getGet, HttpMethod.GET), POST(PathItem::getPost, HttpMethod.POST), PUT(PathItem::getPut, HttpMethod.PUT), PATCH(PathItem::getPatch, HttpMethod.PATCH), DELETE(PathItem::getDelete, HttpMethod.DELETE);

        private final Function<PathItem, Operation> toOperation;
        private final HttpMethod restClientMethod;

        OperationMethod(Function<PathItem, Operation> toOperation, HttpMethod restClientMethod) {
            this.toOperation = toOperation;
            this.restClientMethod = restClientMethod;
        }

        public HttpMethod restClientMethod() {
            return this.restClientMethod;
        }
    }
}
