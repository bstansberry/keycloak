package org.keycloak.models.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.bouncycastle.openssl.PEMWriter;
import org.keycloak.models.RoleModel;
import org.keycloak.util.PemUtils;

/**
 * Set of helper methods, which are useful in various model implementations.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public final class KeycloakModelUtils {

    private KeycloakModelUtils() {
    }

    private static AtomicLong counter = new AtomicLong(1);

    public static String generateId() {
        return counter.getAndIncrement() + "-" + System.currentTimeMillis();
    }

    public static PublicKey getPublicKey(String publicKeyPem) {
        if (publicKeyPem != null) {
            try {
                return PemUtils.decodePublicKey(publicKeyPem);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    public static PrivateKey getPrivateKey(String privateKeyPem) {
        if (privateKeyPem != null) {
            try {
                return PemUtils.decodePrivateKey(privateKeyPem);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static String getPemFromKey(Key key) {
        StringWriter writer = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(writer);
        try {
            pemWriter.writeObject(key);
            pemWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String s = writer.toString();
        return PemUtils.removeBeginEnd(s);
    }

    /**
     * Deep search if given role is descendant of composite role
     *
     * @param role role to check
     * @param composite composite role
     * @param visited set of already visited roles (used for recursion)
     * @return true if "role" is descendant of "composite"
     */
    public static boolean searchFor(RoleModel role, RoleModel composite, Set<RoleModel> visited) {
        if (visited.contains(composite)) return false;
        visited.add(composite);
        Set<RoleModel> composites = composite.getComposites();
        if (composites.contains(role)) return true;
        for (RoleModel contained : composites) {
            if (!contained.isComposite()) continue;
            if (searchFor(role, contained, visited)) return true;
        }
        return false;
    }
}
