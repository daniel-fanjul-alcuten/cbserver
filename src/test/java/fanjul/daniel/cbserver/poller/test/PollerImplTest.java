package fanjul.daniel.cbserver.poller.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import fanjul.daniel.cbserver.builder.Builder;
import fanjul.daniel.cbserver.command.Command;
import fanjul.daniel.cbserver.command.CommandModule;
import fanjul.daniel.cbserver.logger.LoggerModule;
import fanjul.daniel.cbserver.poller.Poller;
import fanjul.daniel.cbserver.poller.PollerModule;
import fanjul.daniel.cbserver.test.AbstractTest;

public class PollerImplTest extends AbstractTest {

    Injector injector;
    Builder builder;
    Command command;
    Poller poller;

    @Before
    public void setup() {
        this.builder = mock(Builder.class);
        this.injector = Guice.createInjector(new LoggerModule(), new CommandModule(), new PollerModule(), new AbstractModule() {

            @Override
            protected void configure() {
                this.bind(Builder.class).toInstance(PollerImplTest.this.builder);
            }
        });
        this.command = this.injector.getInstance(Command.class);
        this.poller = this.injector.getInstance(Poller.class);
    }

    @Test
    public void testInjection() throws Exception {
        assertSame(this.poller, this.injector.getInstance(Poller.class));
    }

    @Test
    public void testPoll() throws Exception {

        final File origin = this.newTestTmpDir();
        origin.mkdirs();
        this.command.execute("git init --bare '" + origin + "'");
        final String blob1 = this.command.execute("git --git-dir '" + origin + "' hash-object -w --stdin", new ByteArrayInputStream(new byte[] {})).getOutput().replace("\n", "");
        final String tag1 = this.command.execute("git --git-dir '" + origin + "' mktag", new ByteArrayInputStream(("object " + blob1 + "\ntype blob\ntag foo\ntagger foo <foo@bar.com> 2 +0000\n\n").getBytes())).getOutput().replace("\n", "");
        this.command.execute("git --git-dir '" + origin + "' update-ref refs/requests/repo/changeset1 " + tag1);
        final String tag2 = this.command.execute("git --git-dir '" + origin + "' mktag", new ByteArrayInputStream(("object " + blob1 + "\ntype blob\ntag foo\ntagger foo <foo@bar.com> 1 +0000\n\n").getBytes())).getOutput().replace("\n", "");
        this.command.execute("git --git-dir '" + origin + "' update-ref refs/requests/repo/changeset2 " + tag2);

        final File dir = this.newTestTmpDir();
        dir.mkdirs();
        this.command.execute("git init '" + dir + "'");
        this.command.execute("git --git-dir '" + dir + "'/.git remote add origin '" + origin + "'");
        this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.remote origin");
        this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.reqprefix refs/requests/");
        this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.lockprefix refs/locks/");
        final String blob2 = this.command.execute("git --git-dir '" + dir + "'/.git hash-object -w --stdin", new ByteArrayInputStream(new byte[] {})).getOutput().replace("\n", "");
        this.command.execute("git --git-dir '" + dir + "'/.git update-ref refs/locks/repo/changeset3 " + blob2);

        this.poller.poll(dir);

        final InOrder o = inOrder(this.builder);
        o.verify(this.builder).build(dir, "repo", "changeset3");
        o.verify(this.builder).build(dir, "repo", "changeset2");
        o.verify(this.builder).build(dir, "repo", "changeset1");
        verifyNoMoreInteractions(this.builder);
        assertFalse(this.command.execute("git --git-dir '" + origin + "' show-ref refs/requests/repo/changeset1", false).isOk());
        assertFalse(this.command.execute("git --git-dir '" + origin + "' show-ref refs/requests/repo/changeset2", false).isOk());
        assertFalse(this.command.execute("git --git-dir '" + origin + "' show-ref refs/requests/repo/changeset3", false).isOk());
        assertFalse(this.command.execute("git --git-dir '" + origin + "' show-ref refs/lock/repo/changeset1", false).isOk());
        assertFalse(this.command.execute("git --git-dir '" + origin + "' show-ref refs/lock/repo/changeset2", false).isOk());
        assertFalse(this.command.execute("git --git-dir '" + origin + "' show-ref refs/lock/repo/changeset3", false).isOk());
        assertTrue(this.command.execute("git --git-dir '" + dir + "'/.git show-ref refs/requests/repo/changeset1", false).isOk());
        assertTrue(this.command.execute("git --git-dir '" + dir + "'/.git show-ref refs/requests/repo/changeset2", false).isOk());
        assertFalse(this.command.execute("git --git-dir '" + dir + "'/.git show-ref refs/requests/repo/changeset3", false).isOk());
        assertFalse(this.command.execute("git --git-dir '" + dir + "'/.git show-ref refs/lock/repo/changeset1", false).isOk());
        assertFalse(this.command.execute("git --git-dir '" + dir + "'/.git show-ref refs/lock/repo/changeset2", false).isOk());
        assertFalse(this.command.execute("git --git-dir '" + dir + "'/.git show-ref refs/lock/repo/changeset3", false).isOk());
    }
}
