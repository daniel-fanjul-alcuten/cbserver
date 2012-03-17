package fanjul.daniel.cbserver.test;

import java.io.File;
import java.util.UUID;

public class AbstractTest {

    protected File getRootTestTmpDir() {
        return new File("target/test-tmp");
    }

    protected File newTestTmpDir() {
        return new File(this.getRootTestTmpDir(), UUID.randomUUID().toString());
    }
}
