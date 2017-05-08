package dresden.sim

import com.typesafe.scalalogging.StrictLogging
import dresden.sim.broadcast.{CRBSimManager, GossipSimManager, RBSimManager}
import dresden.sim.crdt.{CRDTSimManager}
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.ktoolbox.cc.heartbeat.CCHeartbeatPort
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.omngr.bootstrap.BootstrapClientComp
import se.sics.ktoolbox.overlaymngr.{OverlayMngrComp, OverlayMngrPort}
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId
import se.sics.ktoolbox.util.network.KAddress
import se.sics.ktoolbox.util.network.nat.NatAwareAddress
import se.sics.ktoolbox.util.overlays.view.OverlayViewUpdatePort


object HostManager {

    case class Init(selfAdr: KAddress, bootstrapServer: KAddress, croupierId: OverlayId) extends se.sics.kompics.Init[HostManager]

}

class HostManager(val init: HostManager.Init) extends ComponentDefinition with StrictLogging {

    private val (selfAdr, bootstrapServer, croupierId) = init match {
        case HostManager.Init(s, b, c) =>
            (s, b, c)
    }

    val timerPort = requires[Timer]
    val networkPort = requires[Network]
    val bootstrapClientComp = create(classOf[BootstrapClientComp], new BootstrapClientComp.Init(selfAdr, bootstrapServer))
    val overlayMngrComp = create(classOf[OverlayMngrComp], new OverlayMngrComp.Init(selfAdr.asInstanceOf[NatAwareAddress], new OverlayMngrComp.ExtPort(timerPort, networkPort, bootstrapClientComp.getPositive(classOf[CCHeartbeatPort]))))

    val appMngrComp = config.getValue("dresden.dresden.sim.type", classOf[String]) match {
        case "rb" =>
            create(classOf[RBSimManager],
                RBSimManager.Init(
                    RBSimManager.ExtPort(
                        timerPort,
                        networkPort,
                        overlayMngrComp.getPositive(classOf[CroupierPort]),
                        overlayMngrComp.getNegative(classOf[OverlayViewUpdatePort])
                    ),
                    selfAdr, croupierId))
        case "gossip" =>
            create(classOf[GossipSimManager],
                GossipSimManager.Init(
                    GossipSimManager.ExtPort(
                        timerPort,
                        networkPort,
                        overlayMngrComp.getPositive(classOf[CroupierPort]),
                        overlayMngrComp.getNegative(classOf[OverlayViewUpdatePort])
                    ),
                    selfAdr, croupierId))
        case "crb" =>
            create(classOf[CRBSimManager],
                CRBSimManager.Init(
                    CRBSimManager.ExtPort(
                        timerPort,
                        networkPort,
                        overlayMngrComp.getPositive(classOf[CroupierPort]),
                        overlayMngrComp.getNegative(classOf[OverlayViewUpdatePort])
                    ),
                    selfAdr, croupierId))
        case "crdt" =>
            create(classOf[CRDTSimManager],
                CRDTSimManager.Init(
                    CRDTSimManager.ExtPort(
                        timerPort,
                        networkPort,
                        overlayMngrComp.getPositive(classOf[CroupierPort]),
                        overlayMngrComp.getNegative(classOf[OverlayViewUpdatePort])
                    ),
                    selfAdr, croupierId))
    }

    // Bootstrap client
    connect[Timer](timerPort -> bootstrapClientComp)
    connect[Network](networkPort -> bootstrapClientComp)

    // Application manager
    connect[OverlayMngrPort](overlayMngrComp -> appMngrComp)
}
