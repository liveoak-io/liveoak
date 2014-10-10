package io.liveoak.keycloak;

import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.PemUtils;
import org.keycloak.util.Time;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TokenUtil {

    public static final String PUBLIC_KEY_PEM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtKQ1aPU2O9gKwKlttPJYDFsVz98sS4AcVqCW6O2gMXAlmkyQuLhiD+xvm6ZLK8S99IIgdbSevFEThL01Vu/qYCDmyDd7UcLclEeAG/TEhZEBcgMEmx5vbOmjh+TXIKzGRTTUKK2NS0PyR9jzyaS7io9WpllQj1xsGUA3t11JHbCh8k4NsIr9E0Lci/ucQTNAWVWhl2zbckb6aQM11/olvGQwddgkLf5Atp5EOeApAOa7caffqpOO3TsqdiQeZEvTrgxoaHxhlv8S2KqaiKCMe/77b2yyYhUkSQ3rbdMEKD9qr4SJxkckYi3ua7PovkxK/oDjNFsWux6iqEuiD+QoYwIDAQAB";
    public static final String PRIVATE_KEY_PEM = "MIIEpQIBAAKCAQEAtKQ1aPU2O9gKwKlttPJYDFsVz98sS4AcVqCW6O2gMXAlmkyQuLhiD+xvm6ZLK8S99IIgdbSevFEThL01Vu/qYCDmyDd7UcLclEeAG/TEhZEBcgMEmx5vbOmjh+TXIKzGRTTUKK2NS0PyR9jzyaS7io9WpllQj1xsGUA3t11JHbCh8k4NsIr9E0Lci/ucQTNAWVWhl2zbckb6aQM11/olvGQwddgkLf5Atp5EOeApAOa7caffqpOO3TsqdiQeZEvTrgxoaHxhlv8S2KqaiKCMe/77b2yyYhUkSQ3rbdMEKD9qr4SJxkckYi3ua7PovkxK/oDjNFsWux6iqEuiD+QoYwIDAQABAoIBAD6HsS1Z5KImkOJMQ/ulADGWviAs4spn2GdsQ5Dx4Mf8SCf3ZQlsWhWlBKVelRrbid2/xbi5A1Gwxw7l6Dbl6b3I5dpcykVtLKnvbjs/KWAK5/MtdES17mBHKCJ8ZrOa2y9NsDs8iHKLXKzePtWIPpsiLx56QFa/6XN8NU01739k9ELEYCa+fISjofVCKImy1dB7xDizE4Iqq/WNTsHiUdkx+iRsBZTrkUx+KmacLGiUE/teQNpqplP8jFXcVBjWJRgq0G8JCPEkjVTKOcDVR/xUY9Has9WJYMAlJ97ejqAUJK9AsUBe4elGB/w5FBK0oro+8GzPGAgJHWyQfINsqzECgYEA/tIujVBjUbPONsNU04GFvHzKoxeMfDs//y2PT+aEyeci/HNfdVHecKxSLUMKVc4KYdSWA42zj9oZGv0wUHuKjaZ0Fgv52rkYmREBkVeRk5pZoWec7+tm7qY8V/3K1h+vQtj3JZzleD4lpWsBiPViNURF+dOqA/hAT42bPXEu9I8CgYEAtXoqhv+NcPlloeoY8L4a489YRg99pyDXBVcywrjdpyx8XSvFU8iLQtkUj1aiVWnJxPr/zVhZkR3eDnPGzWCA1wJh8j3jxt7U0kfEUNBLJ4kxncHdSC6gaqfrmQjRtDixrzx7GszqNivAaUvLWsys6CDPVxxnQ2J2oFD9s8U4QO0CgYEA0n7bN57VPHOUgAZhNwqCaA6J8amNQj5LgkanYPBidsp5SyMfErHbVyXyCDOeAP1f9CqA9VSDpOwb2wCVfZZgAN0kfeXMspI+MIssyVwWBGD3c2485K+HatJlfKZIfRLVWxoRJr/xip+Dx56aOQHC64+HYnUnt3nF3jFysJGjoR8CgYEAhHvfOBawR12KmJ2x/26Jau4f32XHsY8D6l4yLH2RM04CrHIb6IJrSC36GHqCoCBOsIMc4+gv2wUW6y4SYDSnWk/e+V29P53FeI3z/5tQglhh5G13Ag8oBTT5hgsuLIeHEArzRCl8gneGTFiM6IirYfsx4sJVkJf9SiRWUjsUSjECgYEAz/iUyje98hoxAMHQNAxKSzMTJsCc9mMuljNrX46kY98JC/enko2VzDxzMH//T483JR1yrKyLHVpwYZHMa2hDU4csz7HfcKOJ7FlhaaM691ynqME5dBoo0EYUUMPwxziDVvncbj89qhBot28oA0uJGRr847oaK/OwP4WBVPclHIo=";

    public static final PrivateKey PRIVATE_KEY;
    public static final PublicKey PUBLIC_KEY;

    static {
        try {
            PRIVATE_KEY = PemUtils.decodePrivateKey(PRIVATE_KEY_PEM);
            PUBLIC_KEY = PemUtils.decodePublicKey(PUBLIC_KEY_PEM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private String realm;

    public TokenUtil(String realm) {
        this.realm = realm;
    }

    public AccessToken createToken() {
        AccessToken token = new AccessToken();
        token.id("token-id");
        token.subject("user-id");
        token.audience(realm);
        token.expiration(Time.currentTime() + 300);
        token.issuedFor("app-id");
        token.issuedNow();

        token.setGivenName("given");
        token.setFamilyName("family");
        token.setEmail("email");

        token.setRealmAccess(new AccessToken.Access().roles(Collections.singleton("realm-role")));
        token.addAccess("app-id").roles(Collections.singleton("app-role"));
        token.addAccess("app2-id").roles(Collections.singleton("app-role"));

        return token;
    }

    public String toString(AccessToken token) throws Exception {
        byte[] tokenBytes = JsonSerialization.writeValueAsBytes(token);
        return new JWSBuilder().content(tokenBytes).rsa256(PRIVATE_KEY);
    }

    public String realm() {
        return this.realm;
    }

}
