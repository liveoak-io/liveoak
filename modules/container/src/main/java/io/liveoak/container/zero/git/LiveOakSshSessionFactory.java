package io.liveoak.container.zero.git;

import java.io.File;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.liveoak.common.util.StringUtils;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;

/**
 * @author Ken Finnigan
 */
public class LiveOakSshSessionFactory extends JschConfigSessionFactory {

    private String passphrase;

    public LiveOakSshSessionFactory(String passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    protected void configure(OpenSshConfig.Host hc, Session session) {
        // Do nothing
    }

    @Override
    protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
        JSch jsch = super.getJSch(hc, fs);

        if (StringUtils.hasValue(passphrase)) {
            // If passphrase is set, add it to identity
            File sshDir = new File(fs.userHome(), ".ssh");
            if (sshDir.isDirectory()) {
                updateIdentity(jsch, new File(sshDir, "identity"));
                updateIdentity(jsch, new File(sshDir, "id_rsa"));
                updateIdentity(jsch, new File(sshDir, "id_dsa"));
            }
        }

        return jsch;
    }

    private void updateIdentity(final JSch jsch, File identity) {
        if (identity.isFile()) {
            try {
                jsch.removeIdentity(identity.getAbsolutePath());
                jsch.addIdentity(identity.getAbsolutePath(), passphrase);
            } catch (JSchException e) {
                // Pretend file didn't exist
            }
        }
    }
}
