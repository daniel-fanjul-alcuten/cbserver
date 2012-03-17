package fanjul.daniel.cbserver.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;

import com.google.inject.Singleton;

import fanjul.daniel.cbserver.logger.InjectLogger;

@Singleton
class CommandImpl implements Command {

    @InjectLogger
    Logger logger;

    @Override
    public ResultImpl execute(final String command) throws IOException, InterruptedException {
        return this.execute(command, true);
    }

    @Override
    public ResultImpl execute(final String command, final InputStream inputStream) throws IOException, InterruptedException {
        return this.execute(command, true, inputStream);
    }

    @Override
    public ResultImpl execute(final String command, final boolean checkStatus) throws IOException, InterruptedException {
        return this.execute(command, checkStatus, null);
    }

    @Override
    public ResultImpl execute(final String command, final boolean checkStatus, final InputStream inputStream) throws IOException, InterruptedException {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("command: " + command);
        }

        final Process process = Runtime.getRuntime().exec(new String[] { "sh", "-c", command, });

        final ByteArrayOutputStream outbuffer = new ByteArrayOutputStream();
        final Thread outthread = this.copyInThread(process.getInputStream(), outbuffer);

        final ByteArrayOutputStream errbuffer = new ByteArrayOutputStream();
        final Thread errthread = this.copyInThread(process.getErrorStream(), errbuffer);

        if (inputStream != null) {
            this.copy(inputStream, process.getOutputStream());
        } else {
            try {
                process.getOutputStream().close();
            } catch (final IOException e) {
                this.logger.warn(e.getMessage(), e);
            }
        }

        final int status = process.waitFor();
        outthread.join();
        errthread.join();

        final ResultImpl result = new ResultImpl(command, status, outbuffer, errbuffer);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("output: " + result.getOutput());
            this.logger.trace("errput: " + result.getErrput());
            this.logger.trace("status: " + status);
        }

        if (checkStatus && status != 0) {
            throw new CommandException(result);
        }

        return result;
    }

    Thread copyInThread(final InputStream in, final OutputStream out) {

        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                CommandImpl.this.copy(in, out);
            }
        });
        thread.start();
        return thread;
    }

    void copy(final InputStream in, final OutputStream out) {
        try {
            final byte[] buffer = new byte[4 * 1024];
            int bytes = in.read(buffer, 0, buffer.length);
            while (bytes >= 0) {
                out.write(buffer, 0, bytes);
                bytes = in.read(buffer, 0, buffer.length);
            }
        } catch (final IOException e) {
            this.logger.warn(e.getMessage(), e);
        }
        try {
            in.close();
        } catch (final IOException e) {
            this.logger.warn(e.getMessage(), e);
        }
        try {
            out.close();
        } catch (final IOException e) {
            this.logger.warn(e.getMessage(), e);
        }
    }
}
