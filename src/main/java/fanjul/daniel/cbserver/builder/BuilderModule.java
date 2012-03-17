package fanjul.daniel.cbserver.builder;

import com.google.inject.AbstractModule;

public class BuilderModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(Builder.class).to(BuilderImpl.class);
    }
}
