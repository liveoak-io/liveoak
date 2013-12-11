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
import io.liveoak.container.codec.driver.RootEncodingDriver;
import io.liveoak.container.codec.json.JSONDecoder;
import io.liveoak.container.codec.json.JSONEncoder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.resource.ConfigResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

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
            System.err.println("creating dir: " + this.configDir);
            this.configDir.mkdirs();
        }

        File[] files = this.configDir.listFiles();

        for (File file : files) {
            try {
                deploy(file);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (DeploymentException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
                result.cause().printStackTrace();
            }
        });
    }

    public void addConfiguration(String id, ResourceState configuration) throws Exception {

        RequestContext requestContext = new RequestContext.Builder().returnFields(ReturnFields.ALL_RECURSIVELY).build();
        ByteBuf configBuffer = Unpooled.buffer();
        JSONEncoder encoder = new JSONEncoder();
        encoder.initialize(configBuffer);

        CountDownLatch latch = new CountDownLatch(1);

        RootEncodingDriver driver = new RootEncodingDriver(requestContext, encoder, new ResourceStateResource( configuration ), () -> {
            latch.countDown();
        });

        driver.encode();

        File configFile = new File(this.configDir, id + ".json" );
        FileOutputStream out = new FileOutputStream( configFile );

        configBuffer.readBytes( out, configBuffer.readableBytes() );
    }

    public void updateConfiguration(RootResource rootResource, ConfigResource configResource) throws Exception {
        if (configResource.parent() != rootResource) {
            throw new Exception("fancy configuration not yet supported");
        }

        File configFile = new File(this.configDir, rootResource.id() + ".json");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode tree = (ObjectNode) mapper.readTree(configFile);

        RequestContext requestContext = new RequestContext.Builder().returnFields(ReturnFields.ALL_RECURSIVELY).build();
        ByteBuf configBuffer = Unpooled.buffer();
        JSONEncoder encoder = new JSONEncoder(true);
        encoder.initialize(configBuffer);

        CountDownLatch latch = new CountDownLatch(1);

        RootEncodingDriver driver = new RootEncodingDriver(requestContext, encoder, configResource, () -> {
            latch.countDown();
        });

        driver.encode();

        latch.await();

        ObjectNode configTree = (ObjectNode) mapper.readTree(configBuffer.toString(Charset.forName("UTF-8")));
        tree.replace("config", configTree);

        JsonGenerator generator = new JsonFactory().createGenerator(configFile, JsonEncoding.UTF8);
        generator.setPrettyPrinter(new DefaultPrettyPrinter("\\n"));
        mapper.writeTree(generator, tree);
    }

    private Deployer deployer;
    private File configDir;
    private JSONDecoder decoder;
}
