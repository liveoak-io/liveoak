package org.projectodd.restafari.filesystem.aggregating;

import org.projectodd.restafari.filesystem.FileResource;
import org.projectodd.restafari.spi.MediaType;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.BinaryContentSink;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.vertx.java.core.buffer.Buffer;

import java.io.*;
import java.util.StringTokenizer;

/**
 * @author Bob McWhirter
 */
public class AggregatingResource implements BinaryResource {

    public AggregatingResource(Resource parent, String id, FileResource manifest) {
        this.parent = parent;
        this.id = id;
        this.manifest = manifest;
    }

    @Override
    public MediaType mediaType() {
        int dotLoc = this.id.lastIndexOf( '.' );
        String extension = this.id.substring( dotLoc + 1 );
        return MediaType.lookup( extension );
    }

    @Override
    public void readContent(BinaryContentSink sink) {
        File file = this.manifest.file();
        try {
            BufferedReader reader = new BufferedReader( new FileReader( file ) );

            String line = null;

            while ( ( line = reader.readLine() ) != null ) {
                line = line.trim();
                if ( line.equals( "" ) || line.startsWith( "//" ) ) {
                    continue;
                }
                if ( line.startsWith( "require" ) ) {
                    String rest = line.substring( "require".length() ).trim();
                    File sub = new File( file.getParent(), rest );

                    Buffer buffer = manifest.vertx().fileSystem().readFileSync(sub.getPath());
                    sink.accept( buffer.getByteBuf() );
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            sink.close();
        }

    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void read(String id, Responder responder) {
        responder.noSuchResource( id );
    }

    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported( this );
    }

    private Resource parent;
    private String id;
    private FileResource manifest;

}
