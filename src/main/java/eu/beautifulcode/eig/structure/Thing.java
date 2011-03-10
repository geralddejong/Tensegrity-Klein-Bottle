package eu.beautifulcode.eig.structure;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Something we don't know about, but at least we can serialize it
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface Thing {
    void save(DataOutputStream dos) throws IOException;

    public interface Factory {
        void setFabric(Fabric fabric);
        Thing createFresh(Object target, String descriminator);
        Thing restoreExisting(DataInputStream dis, Object target) throws IOException;
    }
}
