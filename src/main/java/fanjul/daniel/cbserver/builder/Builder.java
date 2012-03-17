package fanjul.daniel.cbserver.builder;

import java.io.File;
import java.io.IOException;

public interface Builder {

    void build(File dir, String repository, String changeset) throws IOException, InterruptedException;
}
