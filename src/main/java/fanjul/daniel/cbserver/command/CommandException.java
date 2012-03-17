package fanjul.daniel.cbserver.command;

import fanjul.daniel.cbserver.command.Command.Result;

public class CommandException extends RuntimeException {

    private static final long serialVersionUID = -149596573482354856L;

    final Result result;

    public CommandException(final Result result) {
        super("command " + result.getCommand() + " exited with status " + result.getStatus());
        this.result = result;
    }

    public Result getResult() {
        return this.result;
    }
}
