package fanjul.daniel.cbserver.poller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fanjul.daniel.cbserver.builder.Builder;
import fanjul.daniel.cbserver.command.Command;
import fanjul.daniel.cbserver.command.Command.Result;
import fanjul.daniel.cbserver.logger.InjectLogger;

@Singleton
class PollerImpl implements Poller {

    @Inject
    Command command;

    @Inject
    Builder builder;

    @InjectLogger
    Logger logger;

    @Override
    public void poll(final File dir) throws IOException, InterruptedException {

        final String remote = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.remote"));
        final String reqprefix = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.reqprefix"));
        final String lockprefix = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git config cbserver.lockprefix"));

        final String locks = this.command.execute("git --git-dir '" + dir + "'/.git for-each-ref " + lockprefix + " --format '%(refname)'").getOutput();
        for (final String lock : locks.split("\n")) {
            if (lock.length() > 0) {
                final String ref = lock.substring(lockprefix.length());
                this.process(dir, remote, lock, ref, null);
            }
        }

        this.command.execute("git --git-dir '" + dir + "'/.git fetch --prune " + remote + " " + reqprefix + "\\*:" + reqprefix + "\\*");
        final String requests = this.command.execute("git --git-dir '" + dir + "'/.git for-each-ref " + reqprefix + " --sort=taggerdate --format '%(refname)'").getOutput();
        for (final String request : requests.split("\n")) {
            if (request.length() > 0) {
                final String tree = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git mktree", new ByteArrayInputStream(new byte[] {})));
                final String commit = this.getOutput(this.command.execute("git --git-dir '" + dir + "'/.git commit-tree " + tree, new ByteArrayInputStream(new byte[] {})));
                final String ref = request.substring(reqprefix.length());
                final String lock = lockprefix + ref;
                this.command.execute("git --git-dir '" + dir + "'/.git update-ref " + lock + " " + commit);
                this.process(dir, remote, lock, ref, request);
            }
        }
    }

    String getOutput(final Result result) throws UnsupportedEncodingException {
        return result.getOutput().replace("\n", "");
    }

    void process(final File dir, final String remote, final String lock, final String ref, final String request) throws IOException, InterruptedException {
        if (this.command.execute("git --git-dir '" + dir + "'/.git push '" + remote + "' " + lock + ":" + lock, false).isOk()) {
            try {
                final String[] split = ref.split("/");
                final String repository = split[0];
                final String changeset = split[1];
                this.builder.build(dir, repository, changeset);
            } catch (final Exception e) {
                this.logger.warn(e.getMessage(), e);
            }
            this.command.execute("git --git-dir '" + dir + "'/.git push '" + remote + "' :" + lock);
        }
        this.command.execute("git --git-dir '" + dir + "'/.git update-ref -d " + lock);
        if (request != null) {
            this.command.execute("git --git-dir '" + dir + "'/.git push '" + remote + "' :" + request);
        }
    }
}
