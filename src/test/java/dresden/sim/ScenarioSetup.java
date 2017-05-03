package dresden.sim;

import dresden.sim.system.SystemSetup;
import se.sics.ktoolbox.util.identifiable.BasicBuilders;
import se.sics.ktoolbox.util.identifiable.BasicIdentifiers;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.basic.BasicAddress;
import se.sics.ktoolbox.util.network.nat.NatAwareAddressImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ScenarioSetup {

    public static final long scenarioSeed = 1234;
    public static final int appPort = 12345;
    public static final KAddress bootstrapServer;
    public static final OverlayId croupierOId;

    static {
        croupierOId = SystemSetup.setup();
        Identifier bootstrapId = BasicIdentifiers.nodeId(new BasicBuilders.IntBuilder(0));
        try {
            bootstrapServer = NatAwareAddressImpl.open(new BasicAddress(InetAddress.getByName("193.0.0.1"), appPort,
                    bootstrapId));
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static KAddress getNodeAdr(String nodeIp, int baseNodeId) {
        try {
            Identifier nodeId = BasicIdentifiers.nodeId(new BasicBuilders.IntBuilder(baseNodeId));
            return NatAwareAddressImpl.open(new BasicAddress(InetAddress.getByName(nodeIp), appPort, nodeId));
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static long getNodeSeed(int nodeId) {
        return scenarioSeed + nodeId;
    }
}
