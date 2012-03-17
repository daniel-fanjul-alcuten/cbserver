package fanjul.daniel.cbserver.command.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fanjul.daniel.cbserver.command.Command;
import fanjul.daniel.cbserver.command.CommandModule;
import fanjul.daniel.cbserver.constants.Constants;
import fanjul.daniel.cbserver.logger.LoggerModule;
import fanjul.daniel.cbserver.test.AbstractTest;

public class CommandImplTest extends AbstractTest {

    Injector injector;
    Command command;

    @Before
    public void setup() {
        this.injector = Guice.createInjector(new LoggerModule(), new CommandModule());
        this.command = this.injector.getInstance(Command.class);
    }

    @Test
    public void testInjection() throws Exception {
        assertSame(this.command, this.injector.getInstance(Command.class));
    }

    @Test
    public void testExecute() throws Exception {

        final File dir = this.newTestTmpDir();
        dir.mkdirs();
        this.command.execute("git init --bare '" + dir + "'");

        final ByteArrayInputStream foo = new ByteArrayInputStream("foo".getBytes(Constants.CHARSET));
        assertEquals("19102815663d23f8b75a47e7a01965dcdc96468c\n", this.command.execute("git --git-dir '" + dir + "' hash-object -w --stdin", foo).getOutput());

        assertEquals(1, this.command.execute("git --git-dir '" + dir + "' show-ref non-existent-ref", false).getStatus());
    }
}
