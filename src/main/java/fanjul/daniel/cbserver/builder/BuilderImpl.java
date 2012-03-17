package fanjul.daniel.cbserver.builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fanjul.daniel.cbserver.command.Command;
import fanjul.daniel.cbserver.command.Command.Result;
import fanjul.daniel.cbserver.constants.Constants;

@Singleton
class BuilderImpl implements Builder {

    @Inject
    Command command;

    @Override
    public void build(final File dir, final String repository, final String changeset) throws IOException, InterruptedException {

        final String remote = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.remote"));
        final String hgurl = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git config cbserver." + repository + ".hgurl"));
        final File hgdir = new File(this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git config cbserver." + repository + ".hgdir")));
        final String refprefix = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git config cbserver." + repository + ".refprefix"));
        final String refsuffix = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git config cbserver." + repository + ".refsuffix"));
        final String logsuffix = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git config cbserver." + repository + ".logsuffix"));

        if (!this.command.execute("git ls-remote --exit-code '" + remote + "' " + refprefix + changeset + logsuffix, false).isOk()) {

            if (!hgdir.exists()) {
                hgdir.mkdirs();
                this.command.execute("hg init '" + hgdir + "'");
            }

            this.command.execute("hg pull '" + hgurl + "' -R '" + hgdir + "' -r " + changeset, false);
            this.command.execute("hg update" + " -R '" + hgdir + "' -C " + changeset);

            final Result result = this.command.execute("ant -f '" + hgdir + "'/build.xml", false);
            if (result.isOk()) {
                final File build = new File(hgdir, "build");
                build.mkdirs();
                this.command.execute("git --git-dir '" + dir + "'/.git --work-tree '" + build + "' add -A");
                this.command.execute("git --git-dir '" + dir + "'/.git --work-tree '" + build + "' commit --allow-empty -m 'build for " + changeset + "'");
            }

            final StringBuilder sb = new StringBuilder();
            sb.append("$ ");
            sb.append(result.getCommand());
            sb.append('\n');
            sb.append(result.getOutput());
            sb.append(result.getErrput());
            final String hash = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git hash-object -w --stdin", new ByteArrayInputStream(sb.toString().getBytes(Constants.ENCODING))));

            this.command.execute("git --git-dir '" + dir + "'/.git push '" + remote + "' " + hash + ":" + refprefix + changeset + logsuffix + (result.isOk() ? " " + "HEAD:" + refprefix + changeset + refsuffix : ""));
        }
    }

    String getOutput(final Result result) throws UnsupportedEncodingException {
        return result.getOutput().replace("\n", "");
    }
}
