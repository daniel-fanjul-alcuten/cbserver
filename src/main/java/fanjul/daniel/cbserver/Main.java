package fanjul.daniel.cbserver;

import java.io.File;

import ch.qos.logback.classic.Logger;

import com.google.inject.Guice;
import com.google.inject.Inject;

import fanjul.daniel.cbserver.builder.BuilderModule;
import fanjul.daniel.cbserver.command.CommandModule;
import fanjul.daniel.cbserver.logger.InjectLogger;
import fanjul.daniel.cbserver.logger.LoggerModule;
import fanjul.daniel.cbserver.poller.Poller;
import fanjul.daniel.cbserver.poller.PollerModule;

public class Main {

    public static void main(final String[] args) {
        Guice.createInjector(new LoggerModule(), new PollerModule(), new BuilderModule(), new CommandModule()).getInstance(Main.class).run(args);
    }

    @Inject
    Poller poller;

    @InjectLogger
    Logger logger;

    void run(final String[] args) {

        final File dir = new File(".").getAbsoluteFile();

        while (true) {
            try {
                this.poller.poll(dir);
            } catch (final Exception e) {
                this.logger.warn(e.getMessage(), e);
            }
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                this.logger.warn(e.getMessage(), e);
            }
        }
    }
}
