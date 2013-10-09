package org.projectodd.restafari.stomp.server.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompException;
import org.projectodd.restafari.stomp.common.AbstractControlFrameHandler;
import org.projectodd.restafari.stomp.common.StompControlFrame;
import org.projectodd.restafari.stomp.common.StompFrame;
import org.projectodd.restafari.stomp.server.Heartbeat;
import org.projectodd.restafari.stomp.server.ServerContext;
import org.projectodd.restafari.stomp.server.StompConnection;

import java.util.regex.Pattern;

/**
 * @author Bob McWhirter
 */
public class ConnectHandler extends AbstractControlFrameHandler {

    static final AttributeKey<StompConnection> CONNECTION = new AttributeKey<>("stomp-connection");

    public ConnectHandler(ServerContext serverContext) {
        super(Stomp.Command.CONNECT);
        this.serverContext = serverContext;
    }


    @Override
    public void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame frame) {
        try {
            Stomp.Version version = checkVersion(frame);
            Heartbeat hb = checkHeartbeat(frame, version);
            Headers headers = frame.getHeaders();
            String hostHeader = headers.get(Headers.HOST);

            checkHost(headers, version);
            StompConnection stompConnection = new StompConnection();
            StompFrame connected = StompFrame.newConnectedFrame(stompConnection.getConnectionId(), version);
            if (hb != null) {
                connected.setHeader(Headers.HEARTBEAT, hb.getServerSend() + "," + hb.getServerReceive());
            }
            this.serverContext.handleConnect(stompConnection);
            ctx.attr( CONNECTION ).set( stompConnection );
            ctx.write(connected);
            ctx.flush();
        } catch (StompException e) {
            // HANDLE
        }
    }

    private Heartbeat checkHeartbeat(StompFrame frame, Stomp.Version version) throws StompException {
        Heartbeat hb = null;
        String heartBeat = frame.getHeader(Headers.HEARTBEAT);
        if (!version.isBefore(Stomp.Version.VERSION_1_1) && heartBeat != null && !heartBeat.equals("")) {
            if (!HEART_BEAT_PATTERN.matcher(heartBeat).matches()) {
                throw new StompException("Heartbeat must be specified in msec as two comma-separated values.");
            }
            String[] components = heartBeat.split(",");
            try {
                hb = new Heartbeat();
                hb.setClientReceive(Integer.parseInt(components[0]));
                hb.setClientSend(Integer.parseInt(components[1]));
            } catch (Exception ex) {
                throw new StompException("Heartbeat values must be integers.");
            }
        }
        return hb;
    }

    private String checkHost(Headers headers, Stomp.Version version) throws StompException {
        String host = headers.get(Headers.HOST);
        if (version.isAfter(Stomp.Version.VERSION_1_0) && (host == null || "".equals(host))) {
            throw new StompException("Must specify host in STOMP protocol 1.1 and above.");
        }
        return host;
    }

    private Stomp.Version checkVersion(StompFrame frame) throws StompException {
        String acceptVersion = frame.getHeader(Headers.ACCEPT_VERSION);
        if (acceptVersion == null) {
            return Stomp.Version.VERSION_1_0;
        } else if (!VERSION_PATTERN.matcher(acceptVersion).matches()) {
            throw new StompException("Accept-version header value must be an incrementing comma-separated list.");
        }
        String[] versions = acceptVersion.split(",");
        Stomp.Version selectedVersion = null;
        for (int i = versions.length - 1; i >= 0; i--) {
            if ((selectedVersion = Stomp.Version.forVersionString(versions[i])) != null)
                break;
        }
        if (selectedVersion == null) {
            // no matching version found - handleSend error frame
            throw new StompException("Supported protocol versions are " + Stomp.Version.supportedVersions());
        }
        return selectedVersion;

    }

    private static final Pattern HEART_BEAT_PATTERN = Pattern.compile("^\\d+,\\d+$");
    private static final Pattern VERSION_PATTERN = Pattern.compile("^([^\\s]+,)*[^\\s]*$");

    private ServerContext serverContext;

}
