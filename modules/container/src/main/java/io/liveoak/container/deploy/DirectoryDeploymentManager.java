package io.liveoak.container.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.driver.StateEncodingDriver;
import io.liveoak.common.codec.json.JSONDecoder;
import io.liveoak.common.codec.json.JSONEncoder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.container.DeploymentException;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class DirectoryDeploymentManager {

    public DirectoryDeploymentManager(Deployer deployer, File configDir) {
        this.deployer = deployer;
        this.configDir = configDir;
        this.decoder = new JSONDecoder(true);
    }

    public void start() {
        if (!this.configDir.exists()) {
            log.infof("creating dir: %s", this.configDir);
            this.configDir.mkdirs();
        }

        File[] files = this.configDir.listFiles();

        for (File file : files) {
            try {
                deploy(file);
            } catch (IOException e) {
                log.errorf(e, "IOException during deployment of file %s", file.getName());
            } catch (DeploymentException e) {
                log.errorf(e, "Deployment failed for file %s", file.getName());
            }
        }
    }

    public void deploy(File file) throws IOException, DeploymentException {
        String id = file.getName();

        if (id.endsWith(".json")) {
            id = id.substring(0, id.length() - 5);
        }

        InputStream in = new FileInputStream(file);
        ByteBufOutputStream out = new ByteBufOutputStream(Unpooled.buffer());

        byte[] transfer = new byte[1024];
        int len = 0;

        while ((len = in.read(transfer)) >= 0) {
            out.write(transfer, 0, len);
        }

        out.close();
        in.close();

        ResourceState descriptor = decoder.decode(out.buffer());

        this.deployer.deploy(id, descriptor, (result) -> {
            if (result.cause() != null) {
                log.errorf(result.cause(), "Error deploying resource file %s", file.getName());
            }
        });
    }

    public void addConfiguration(String id, ResourceState configuration) throws Exception {

        RequestContext requestContext = new RequestContext.Builder().returnFields(ReturnFields.ALL_RECURSIVELY).build();
        ByteBuf configBuffer = Unpooled.buffer();
        JSONEncoder encoder = new JSONEncoder();
        encoder.initialize(configBuffer);

//        CountDownLatch latch = new CountDownLatch(1);
//
        StateEncodingDriver driver = new StateEncodingDriver(new RequestContext.Builder().build(), encoder, configuration);
        driver.encode();
        driver.close();
//
//        latch.countDown();

        File configFile = new File(this.configDir, id + ".json" );
        FileOutputStream out = new FileOutputStream( configFile );

        configBuffer.readBytes( out, configBuffer.readableBytes() );

    }

    public void deleteConfiguration(String id) {
        File configFile = new File(this.configDir, id + ".json" );
        if ( configFile.exists() ) {
            configFile.delete();
        }
    }

    public void updateConfiguration(RootResource rootResource, ResourceState configResource) throws Exception {

//        if (configResource.parent() != rootResource) {
//            throw new Exception("fancy configuration not yet supported");
//        }

        File configFile = new File(this.configDir, rootResource.id() + ".json");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode tree = (ObjectNode) mapper.readTree(configFile);

        RequestContext requestContext = new RequestContext.Builder().returnFields(ReturnFields.ALL_RECURSIVELY).build();
        ByteBuf configBuffer = Unpooled.buffer();
        JSONEncoder encoder = new JSONEncoder(true);
        encoder.initialize(configBuffer);


//        CountDownLatch latch = new CountDownLatch(1);


        StateEncodingDriver driver = new StateEncodingDriver(new RequestContext.Builder().build(), encoder, configResource);

        driver.encode();
        driver.close();
//
//        latch.countDown();
//
//        latch.await();

        ObjectNode configTree = (ObjectNode) mapper.readTree(configBuffer.toString(Charset.forName("UTF-8")));
        tree.replace("config", configTree);

        JsonGenerator generator = new JsonFactory().createGenerator(configFile, JsonEncoding.UTF8);
        generator.setPrettyPrinter(new DefaultPrettyPrinter("\\n"));
        mapper.writeTree(generator, tree);
    }

    private Deployer deployer;
    private File configDir;
    private JSONDecoder decoder;

    private static final Logger log = Logger.getLogger(DirectoryDeploymentManager.class);
}
