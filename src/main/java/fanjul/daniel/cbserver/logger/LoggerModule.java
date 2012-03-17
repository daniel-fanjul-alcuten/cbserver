package fanjul.daniel.cbserver.logger;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class LoggerModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bindListener(Matchers.any(), new LoggerTypeListener());
    }
}
