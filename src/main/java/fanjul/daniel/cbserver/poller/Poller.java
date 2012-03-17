package fanjul.daniel.cbserver.poller;

import java.io.File;
import java.io.IOException;

public interface Poller {

    void poll(File dir) throws IOException, InterruptedException;
}
