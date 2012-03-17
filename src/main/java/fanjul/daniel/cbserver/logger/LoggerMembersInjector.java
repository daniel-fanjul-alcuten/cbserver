package fanjul.daniel.cbserver.logger;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.MembersInjector;

class LoggerMembersInjector<T> implements MembersInjector<T> {

    final Field field;
    final Logger logger;

    LoggerMembersInjector(final Field field) {
        this.field = field;
        this.logger = LoggerFactory.getLogger(field.getDeclaringClass());
    }

    @Override
    public void injectMembers(final T instance) {
        try {
            this.field.set(instance, this.logger);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
