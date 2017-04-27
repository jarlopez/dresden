package template

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{GossippingBestEffortBroadcast, PerfectLink}
import dresden.components.broadcast.GossippingBasicBroadcast
import dresden.components.links.PerfectP2PLink
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.sics.kompics.{Channel, Component, Start}
import se.sics.kompics.sl._
import se.sics.kompics.network.Network
import se.sics.kompics.timer.Timer
import se.sics.ktoolbox.cc.heartbeat.CCHeartbeatPort
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.omngr.bootstrap.BootstrapClientComp
import se.sics.ktoolbox.overlaymngr.OverlayMngrComp
import se.sics.ktoolbox.overlaymngr.OverlayMngrPort
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId
import se.sics.ktoolbox.util.network.KAddress
import se.sics.ktoolbox.util.network.nat.NatAwareAddress
import se.sics.ktoolbox.util.overlays.view.OverlayViewUpdatePort
import template.kth.app.mngr.AppMngrComp


object HostManager {
    private val LOG = LoggerFactory.getLogger(classOf[HostManager])

    case class Init(selfAdr: KAddress, bootstrapServer: KAddress, croupierId: OverlayId) extends se.sics.kompics.Init[HostManager] {
    }

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
    val appMngrComp = create(classOf[GossipSimManager],
                                GossipSimManager.Init(
                                    GossipSimManager.ExtPort(
                                        timerPort,
                                        networkPort,
                                        overlayMngrComp.getPositive(classOf[CroupierPort]),
                                        overlayMngrComp.getNegative(classOf[OverlayViewUpdatePort])
                                    ),
                                    selfAdr, croupierId))


    // Bootstrap client
    connect[Timer](timerPort -> bootstrapClientComp)
    connect[Network](networkPort -> bootstrapClientComp)

    // Application manager
    connect[OverlayMngrPort](overlayMngrComp -> appMngrComp)
}
