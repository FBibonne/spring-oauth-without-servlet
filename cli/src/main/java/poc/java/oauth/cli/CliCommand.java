package poc.java.oauth.cli;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.springframework.shell.command.CommandContext;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public record CliCommand(String id) implements Function<CommandContext, String> {

    private static final Function<PathItem, Operation>[] toOperations = {PathItem::getDelete,};

    @Override
    public String apply(CommandContext commandContext) {
        return "to be implemented";
    }

    public static Stream<CliCommand> fromPath(Map.Entry<String, PathItem> pathEntry) {
        String uriSuffix = pathEntry.getKey();
        pathEntry.getValue().getDelete().getOperationId()
    }
}
