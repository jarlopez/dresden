package dresden.system

import java.net.{InetAddress, UnknownHostException}

import se.sics.ktoolbox.util.identifiable.BasicBuilders
import se.sics.ktoolbox.util.identifiable.BasicIdentifiers
import se.sics.ktoolbox.util.identifiable.IdentifierRegistry
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId
import se.sics.ktoolbox.util.identifiable.overlay.OverlayIdFactory
import se.sics.ktoolbox.util.identifiable.overlay.OverlayRegistry
import se.sics.ktoolbox.util.network.KAddress
import se.sics.ktoolbox.util.network.basic.BasicAddress
import se.sics.ktoolbox.util.network.nat.NatAwareAddressImpl

/**
  * Scala migration of Alex Ormenisan's SystemSetup.java and ScenarioSetup.java
  */

/*
    NOTES
        - Seems like we want a bootstrap server to handle croupier membership
 */
object SystemSetup {
    val seed = 1234
    val appPort = 12345

    val croupierOwnerId: Byte = 1
    val croupierId: OverlayId = {
        BasicIdentifiers.registerDefaults2(seed)
        OverlayRegistry.initiate(
            new OverlayId.BasicTypeFactory(0.toByte),
            new OverlayId.BasicTypeComparator)
        OverlayRegistry.registerPrefix(
            OverlayId.BasicTypes.CROUPIER.name,
            croupierOwnerId)
        val croupierBaseIdFactory = IdentifierRegistry.lookup(BasicIdentifiers.Values.OVERLAY.toString)
        val croupierIdFactory = new OverlayIdFactory(
            croupierBaseIdFactory,
            OverlayId.BasicTypes.CROUPIER,
            croupierOwnerId)
        croupierIdFactory.id(new BasicBuilders.StringBuilder("0"))
    }

    def getNodeAdr(nodeIp: String, baseNodeId: Int): KAddress = {
        val nodeId = BasicIdentifiers.nodeId(new BasicBuilders.IntBuilder(baseNodeId))
        NatAwareAddressImpl.open(new BasicAddress(InetAddress.getByName(nodeIp), appPort, nodeId))
    }

    def getNodeSeed(nodeId: Int): Long = {
        seed + nodeId
    }
}
