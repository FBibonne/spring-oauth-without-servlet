package poc.java.oauth.cli;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.shell.command.CommandContext;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public record CliCommand(String id, OperationMethod operationMethod, RestClient restClient,
                         UriComponentsBuilder uriComponentsBuilder,
                         Set<UrlParameter> parameters) implements Function<CommandContext, String> {

    CliCommand(String id, OperationMethod operationMethod, UriComponentsBuilder uriBuilder, Set<UrlParameter> parameters, RestClient.Builder restCLientBuilder) {
        this(id, operationMethod, restCLientBuilder.requestFactory(new HttpComponentsClientHttpRequestFactory()).build(), uriBuilder, parameters);
    }


    @Override
    public String apply(CommandContext commandContext) {
        // TODO Refactor this ugly statement !
        return restClient.method(operationMethod.restClientMethod())
                .uri(uriComponentsBuilder.cloneBuilder()
                        .queryParams(MultiValueMap.fromSingleValue(parameters.stream().filter(UrlParameter::isQueryParam)
                                .filter(param -> commandContext.hasMappedOption(param.name))
                                .collect(toMap(UrlParameter::name, param -> commandContext.<String>getOptionValue(param.name))))
                        ).build(
                                parameters.stream()
                                        .<Pair<String, String>>mapMulti((parameter, downstream) -> {
                                            String value = commandContext.getOptionValue(parameter.name());
                                            if (value != null) {
                                                downstream.accept(Pair.of(parameter.name(), value));
                                                return;
                                            }
                                            if (parameter.required()) {
                                                throw new IllegalStateException("Required parameter " + parameter.name() + " is not present");
                                            }
                                        })
                                        .collect(toMap(Pair::getKey, Pair::getValue))
                        )
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
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(serverUri).path(uriSuffix);
        List<Parameter> operationParameters = operation.getParameters();
        return Optional.of(new CliCommand(id, operationMethod, uriBuilder,
                operationParametersToUrlParameters(operationParameters), restCLientBuilder));
    }

    private static Set<UrlParameter> operationParametersToUrlParameters(List<Parameter> parameters) {
        if (parameters == null) {
            return Set.of();
        }
        return parameters.stream().map(UrlParameter::new).collect(Collectors.toSet());
    }

    record UrlParameter(String name, boolean required, ComponentType componentType) {
        UrlParameter(Parameter parameter) {
            this(parameter.getName(), parameter.getRequired(), ComponentType.fromOpenApiIdentifier(parameter.getIn()));
        }

        boolean isQueryParam() {
            return componentType == ComponentType.QUERY;
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

    enum ComponentType {
        QUERY("query"), PATH("path");
        final String openApiIdentifier;

        ComponentType(String openApiIdentifier) {
            this.openApiIdentifier = openApiIdentifier;
        }

        public static ComponentType fromOpenApiIdentifier(String in) {
            for (ComponentType componentType : ComponentType.values()) {
                if (componentType.openApiIdentifier.equals(in)) {
                    return componentType;
                }
            }
            throw new IllegalArgumentException(in + " is not a valid `in` destination for a parameter");
        }

        public String getOpenApiIdentifier() {
            return openApiIdentifier;
        }
    }
}
