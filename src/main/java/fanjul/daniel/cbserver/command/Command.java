package fanjul.daniel.cbserver.command;

import java.io.IOException;
import java.io.InputStream;

public interface Command {

    interface Result {

        String getCommand();

        int getStatus();

        boolean isOk();

        String getOutput();

        String getErrput();
    }

    Result execute(final String command) throws IOException, InterruptedException;

    Result execute(final String command, final InputStream inputStream) throws IOException, InterruptedException;

    Result execute(final String command, final boolean checkStatus) throws IOException, InterruptedException;

    Result execute(final String command, final boolean checkStatus, final InputStream inputStream) throws IOException, InterruptedException;
}