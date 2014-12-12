/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted provided that
 * the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the Airbitz Project.
 */

package com.airbitz.api;

import android.util.Log;

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

// Certificate pinning taken from:
// https://www.owasp.org/images/1/1f/Pubkey-pin-android.zip

// Many thanks to Nikolay Elenkov for feedback.
// Shamelessly based upon Moxie's example code (AOSP/Google did not offer code)
// http://www.thoughtcrime.org/blog/authenticity-is-broken-in-ssl-but-your-app-ha/
public final class PinManager implements X509TrustManager {

    private final String TAG = getClass().getSimpleName();

    // DER encoded public key
    private static String PUB_KEY =
        "30820122300d06092a864886f70d01010105000382010f003082010a02820" +
        "10100c58e3da35d8040dbc665926f7c38dad3dc59e58e529a9a12638dcafe" +
        "46346eaa607d875722126705944bf936ef823005094112cbcc8a3c6d2c27b" +
        "fcfe23978d46a541847064c274d9a8eee40b711ebeb3ff3434216c3661edd" +
        "bbc3b079e40a69e213c5e7740aed3cec2325411f660f4dcf967efc702ec98" +
        "6169aee5ec2bd9b60aacc70e55bf6fe1e6214736fa0f9c151ed75df999799" +
        "88a48a93c628c64e915f26a9575df67bf4fcd7c1aa3b2826f2376e36c00bd" +
        "84b3e93d5be2f7d7a2843c90d5704393094dc25c940461a29db511894bbff" +
        "1be3d6b0376c365128e20efe43723fff898bd6e43b3f4d7ad541e565dc61a" +
        "058e1a6c08f7575ff79508845f1fd0203010001";

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

        assert (chain != null);
        if (chain == null) {
            throw new IllegalArgumentException(
                    "checkServerTrusted: X509Certificate array is null");
        }

        assert (chain.length > 0);
        if (!(chain.length > 0)) {
            throw new IllegalArgumentException(
                    "checkServerTrusted: X509Certificate is empty");
        }

        // Perform customary SSL/TLS checks
        TrustManagerFactory tmf;
        try {
            tmf = TrustManagerFactory.getInstance("X509");
            tmf.init((KeyStore) null);

            for (TrustManager trustManager : tmf.getTrustManagers()) {
                ((X509TrustManager) trustManager).checkServerTrusted(
                        chain, authType);
            }

        } catch (Exception e) {
            throw new CertificateException(e);
        }

        // Hack ahead: BigInteger and toString(). We know a DER encoded Public
        // Key starts with 0x30 (ASN.1 SEQUENCE and CONSTRUCTED), so there is
        // no leading 0x00 to drop.
        RSAPublicKey pubkey = (RSAPublicKey) chain[0].getPublicKey();
        String encoded = new BigInteger(1 /* positive */, pubkey.getEncoded())
                .toString(16);

        final boolean expected = PUB_KEY.equalsIgnoreCase(encoded);
        assert(expected);
        if (!expected) {
            throw new CertificateException(
                    "checkServerTrusted: Expected public key: " + PUB_KEY
                            + ", got public key:" + encoded);
        }
    }

    public void checkClientTrusted(X509Certificate[] xcs, String string) {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}

