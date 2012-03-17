package fanjul.daniel.cbserver.builder.test;

import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fanjul.daniel.cbserver.builder.Builder;
import fanjul.daniel.cbserver.builder.BuilderModule;
import fanjul.daniel.cbserver.command.Command;
import fanjul.daniel.cbserver.command.CommandModule;
import fanjul.daniel.cbserver.logger.LoggerModule;
import fanjul.daniel.cbserver.test.AbstractTest;

public class BuilderImplTest extends AbstractTest {

    Injector injector;
    Command command;
    Builder builder;

    @Before
    public void setup() {
        this.injector = Guice.createInjector(new LoggerModule(), new CommandModule(), new BuilderModule());
        this.command = this.injector.getInstance(Command.class);
        this.builder = this.injector.getInstance(Builder.class);
    }

    @Test
    public void testInjection() throws Exception {
        assertSame(this.builder, this.injector.getInstance(Builder.class));
    }

    @Test
    public void testBuild() throws Exception {

        final File origin = this.newTestTmpDir();
        origin.mkdirs();
        this.command.execute("git --git-dir '" + origin + "' init --bare");

        final File hgurl = this.newTestTmpDir();
        this.command.execute("hg init '" + hgurl + "'");
        final PrintStream printStream = new PrintStream(new File(hgurl, "build.xml"));
        printStream.println("<project basedir=\".\" default=\"build\">");
        printStream.println("<target name=\"build\">");
        printStream.println("<mkdir dir=\"build\" />");
        printStream.println("<echo message=\"bar\" file=\"build/foo\" />");
        printStream.println("</target>");
        printStream.println("</project>");
        printStream.close();
        this.command.execute("hg -R '" + hgurl + "' add '" + hgurl + "'/build.xml");
        this.command.execute("hg -R '" + hgurl + "' commit -m build.xml");
        final String changeset = this.command.execute("hg -R '" + hgurl + "' id -i").getOutput().replace("\n", "");

        final File dir = this.newTestTmpDir();
        dir.mkdirs();
        this.command.execute("git init '" + dir + "'");
        this.command.execute("git --git-dir '" + dir + "'/.git remote add origin '" + origin + "'");
        this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.remote origin");
        this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.repo.hgurl " + hgurl);
        final File hgdir = this.newTestTmpDir();
        this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.repo.hgdir " + hgdir);
        this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.repo.refprefix refs/build/hg/repo/");
        this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.repo.refsuffix /build");
        this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.repo.logsuffix /log");

        this.builder.build(dir, "repo", changeset);
        this.command.execute("git --git-dir '" + origin + "' show-ref refs/build/hg/repo/" + changeset + "/build");
        this.command.execute("git --git-dir '" + origin + "' show-ref refs/build/hg/repo/" + changeset + "/log");
    }
}
