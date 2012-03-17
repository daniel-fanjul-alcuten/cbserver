package fanjul.daniel.cbserver.logger;

import java.lang.reflect.Field;

import org.slf4j.Logger;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

class LoggerTypeListener implements TypeListener {

    @Override
    public <T> void hear(final TypeLiteral<T> typeLiteral, final TypeEncounter<T> typeEncounter) {
        for (final Field field : typeLiteral.getRawType().getDeclaredFields()) {
            if (field.getType() == Logger.class && field.isAnnotationPresent(InjectLogger.class)) {
                field.setAccessible(true);
                typeEncounter.register(new LoggerMembersInjector<T>(field));
            }
        }
    }
}
