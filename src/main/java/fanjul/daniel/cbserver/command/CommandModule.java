package fanjul.daniel.cbserver.command;

import com.google.inject.AbstractModule;

public class CommandModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(Command.class).to(CommandImpl.class);
    }
}
