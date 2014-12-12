package io.liveoak.filesystem;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.filesystem.extension.FilesystemExtension;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ZipUploadTest extends AbstractHTTPResourceTestCase {

    static {
        setProjectRoot(HTTPFilesystemResourceTest.class);
        applicationDirectory = projectRoot;
        try {
            installTestApp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        loadExtension("fs", new FilesystemExtension());
        installTestAppResource("fs", "files", JsonNodeFactory.instance.objectNode());
    }

    @Test
    public void testUpload() throws Exception {
    }
}
