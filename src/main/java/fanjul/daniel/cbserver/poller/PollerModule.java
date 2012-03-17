package fanjul.daniel.cbserver.poller;

import com.google.inject.AbstractModule;

public class PollerModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(Poller.class).to(PollerImpl.class);
    }
}
