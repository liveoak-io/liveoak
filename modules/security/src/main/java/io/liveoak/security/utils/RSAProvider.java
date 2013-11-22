/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.utils;

import io.liveoak.security.impl.JsonWebToken;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RSAProvider {

    public static String getJavaAlgorithm(String alg) {
        switch (alg) {
            case "RS256":
                return "SHA256withRSA";
            case "RS384":
                return "SHA384withRSA";
            case "RS512":
                return "SHA512withRSA";
            default:
                throw new IllegalArgumentException("Not an RSA Algorithm");
        }
    }

    public static Signature getSignature(String alg) {
        try {
            return Signature.getInstance(getJavaAlgorithm(alg));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sign(byte[] data, String algorithm, PrivateKey privateKey) {
        try {
            Signature signature = getSignature(algorithm);
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(JsonWebToken token, PublicKey publicKey) {
        try {
            Signature verifier = getSignature(token.getHeader().getAlgorithm());
            verifier.initVerify(publicKey);
            verifier.update(token.getClaimsBytes());
            return verifier.verify(token.getSignatureBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}

