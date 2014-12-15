package io.liveoak.filesystem;

import java.io.File;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.filesystem.extension.FilesystemExtension;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.util.ObjectMapperFactory;
import io.liveoak.stomp.Headers;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.apache.http.HttpResponse;
import org.apache.http.entity.FileEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ZipUploadTest extends AbstractHTTPResourceTestCase {

    String ZIP_UPLOAD_PATH = "/admin/applications/testApp/resources/zipTest/upload";

    static File testDirectory;

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

        testDirectory = Files.createTempDirectory("zipTest").toFile();

        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("directory", testDirectory.getCanonicalPath());

        installTestAppResource("fs", "zipTest", config);
    }

    @Before
    public void init() throws Exception {
        if (testDirectory.exists()) {
            vertx.fileSystem().deleteSync(testDirectory.getCanonicalPath(), true);
        } else {
            fail("TEST DIRECTORY DOES NOT EXIST");
        }

        testDirectory.mkdirs();
        assertThat(testDirectory.exists()).isTrue();
        assertThat(testDirectory.list().length).isEqualTo(0);
    }

    @After
    public void tearDown() throws Exception {
        assertThat(testDirectory.exists()).isTrue();

        vertx.fileSystem().deleteSync(testDirectory.getCanonicalPath(), true);

        testDirectory.mkdirs();
        assertThat(testDirectory.exists()).isTrue();
        assertThat(testDirectory.list().length).isEqualTo(0);
    }


    @Test
    public void testUploadEmpty() throws Exception {
        assertThat(testDirectory.exists()).isTrue();
        assertThat(testDirectory.isDirectory()).isTrue();
        assertThat(testDirectory.list()).isEmpty();

        File zipFile = new File(this.getClass().getClassLoader().getResource("zip1.zip").getPath());
        FileEntity fileEntity = new FileEntity(zipFile);

        HttpResponse response = put(ZIP_UPLOAD_PATH).addHeader(Headers.CONTENT_TYPE, MediaType.ZIP.toString())
                .addHeader("Accept", MediaType.ZIP.toString())
                .setEntity(fileEntity)
                .execute();

        checkFileExists("foo.txt");
        checkDirectory("bar", 2);
        checkFileExists("bar/baz.json");
        checkDirectory("bar/bat", 1);
        checkFileExists("bar/bat/hello.html");
    }

    @Test
    public void testUploadExistingFiles() throws Exception {
        assertThat(testDirectory.isDirectory()).isTrue();
        assertThat(testDirectory.list()).isEmpty();

        File barDirectory = new File(testDirectory.getCanonicalPath() + File.separator + "bar");
        barDirectory.mkdirs();
        File newFile1 = new File(testDirectory.getCanonicalPath() + File.separator + "newFile1.json");
        writeFile("{ name: 'newFile1'}", newFile1);
        File newFile2 = new File(barDirectory.getCanonicalPath() + File.separator + "baz.json");
        writeFile("{ name: 'newFile2'}", newFile2);

        File zipFile = new File(this.getClass().getClassLoader().getResource("zip1.zip").getPath());
        FileEntity fileEntity = new FileEntity(zipFile);

        HttpResponse response = put(ZIP_UPLOAD_PATH).addHeader(Headers.CONTENT_TYPE, MediaType.ZIP.toString())
                .addHeader("Accept", MediaType.ZIP.toString())
                .setEntity(fileEntity)
                .execute();

        checkFileExists("foo.txt");
        checkFileExists("newFile1.json");
        checkFile("{ name: 'newFile1'}", new File(testDirectory.getCanonicalPath() + "/newFile1.json"));
        checkDirectory("bar", 2);
        checkFileExists("bar/baz.json");
        checkFile("{ name: 'newFile2'}", new File(testDirectory.getCanonicalPath() + "/bar/baz.json"));
        checkDirectory("bar/bat", 1);
        checkFileExists("bar/bat/hello.html");
    }

    @Test
    public void testUploadExistingWithClean() throws Exception {
        assertThat(testDirectory.isDirectory()).isTrue();
        assertThat(testDirectory.list()).isEmpty();

        File barDirectory = new File(testDirectory.getCanonicalPath() + File.separator + "bar");
        barDirectory.mkdirs();
        File newFile1 = new File(testDirectory.getCanonicalPath() + File.separator + "newFile1.json");
        writeFile("{ name: 'newFile1'}", newFile1);
        File newFile2 = new File(barDirectory.getCanonicalPath() + File.separator + "newFile2.json");
        writeFile("{ name: 'newFile2'}", newFile2);

        File zipFile = new File(this.getClass().getClassLoader().getResource("zip1.zip").getPath());
        FileEntity fileEntity = new FileEntity(zipFile);

        HttpResponse response = put(ZIP_UPLOAD_PATH + "?clean=true").addHeader(Headers.CONTENT_TYPE, MediaType.ZIP.toString())
                .addHeader("Accept", MediaType.ZIP.toString())
                .setEntity(fileEntity)
                .execute();

        checkFileExists("foo.txt");
        checkDirectory("bar", 2);
        checkFileExists("bar/baz.json");
        checkDirectory("bar/bat", 1);
        checkFileExists("bar/bat/hello.html");

        assertThat(newFile1.exists()).isFalse();
    }

    @Test
    public void testUploadFileWithOverWrite() throws Exception {
        assertThat(testDirectory.isDirectory()).isTrue();
        assertThat(testDirectory.list()).isEmpty();

        File barDirectory = new File(testDirectory.getCanonicalPath() + File.separator + "bar");
        barDirectory.mkdirs();
        File newFile1 = new File(testDirectory.getCanonicalPath() + File.separator + "newFile1.json");
        writeFile("{ name: 'newFile1'}", newFile1);
        File newFile2 = new File(barDirectory.getCanonicalPath() + File.separator + "baz.json");
        writeFile("{ name: 'newFile2'}", newFile2);

        File zipFile = new File(this.getClass().getClassLoader().getResource("zip1.zip").getPath());
        FileEntity fileEntity = new FileEntity(zipFile);

        HttpResponse response = put(ZIP_UPLOAD_PATH + "?overwrite=true").addHeader(Headers.CONTENT_TYPE, MediaType.ZIP.toString())
                .addHeader("Accept", MediaType.ZIP.toString())
                .setEntity(fileEntity)
                .execute();

        checkFileExists("foo.txt");
        checkFileExists("newFile1.json");
        checkFile("{ name: 'newFile1'}", new File(testDirectory.getCanonicalPath() + "/newFile1.json"));
        checkDirectory("bar", 2);
        checkFileExists("bar/baz.json");
        checkFile("{ hello: 'BAZ'}", new File(testDirectory.getCanonicalPath() + "/bar/baz.json"));
        checkDirectory("bar/bat", 1);
        checkFileExists("bar/bat/hello.html");
    }

    private void writeFile(String jsonString, File file) throws Exception {

        JsonNode jsonNode = ObjectMapperFactory.create().readTree(jsonString);

        ObjectMapperFactory.createWriter().writeValue(file, jsonNode);
        assertThat(file.exists()).isTrue();

        checkFile(jsonNode, file);
    }

    private void checkFile(String expected, File file) throws Exception {
        JsonNode jsonNode = ObjectMapperFactory.create().readTree(expected);
        checkFile(jsonNode, file);
    }

    private void checkFile(JsonNode expected, File file) throws Exception {
        JsonNode readValue = ObjectMapperFactory.create().readTree(file);
        assertThat(readValue.equals(expected)).isTrue();
    }

    private File checkFileExists(String fileName) throws Exception {
        File file = new File(testDirectory.getCanonicalPath() + File.separator + fileName);
        assertThat(file.exists()).isTrue();
        return file;
    }

    private File checkDirectory(String fileName, int count) throws Exception {
        File file = checkFileExists(fileName);
        assertThat(file.isDirectory()).isTrue();
        assertThat(file.list().length).isEqualTo(count);
        return file;
    }

}
