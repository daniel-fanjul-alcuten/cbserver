package fanjul.daniel.cbserver.command;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import fanjul.daniel.cbserver.command.Command.Result;
import fanjul.daniel.cbserver.constants.Constants;

class ResultImpl implements Result {

    final String command;
    final int status;
    final String output;
    String errput;

    ResultImpl(final String command, final int status, final ByteArrayOutputStream outbuffer, final ByteArrayOutputStream errbuffer) throws UnsupportedEncodingException {
        this.command = command;
        this.status = status;
        this.output = outbuffer.toString(Constants.ENCODING);
        this.errput = errbuffer.toString(Constants.ENCODING);
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public boolean isOk() {
        return this.status == 0;
    }

    @Override
    public String getOutput() {
        return this.output;
    }

    @Override
    public String getErrput() {
        return this.errput;
    }
}