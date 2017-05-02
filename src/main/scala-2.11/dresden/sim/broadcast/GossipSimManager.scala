package dresden.sim.broadcast

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{BestEffortBroadcast, PerfectLink}
import dresden.components.broadcast.GossippingBasicBroadcast
import dresden.components.links.PerfectP2PLink
import dresden.sim.broadcast.GossipSimManager.ExtPort
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.{Channel, Negative, Positive, Start}
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.overlaymngr.OverlayMngrPort
import se.sics.ktoolbox.overlaymngr.events.OMngrCroupier
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId
import se.sics.ktoolbox.util.network.KAddress
import se.sics.ktoolbox.util.overlays.view.{OverlayViewUpdate, OverlayViewUpdatePort}
import template.kth.croupier.util.NoView


case object GossipSim
case object RBSim

object GossipSimManager {

    case class Init(extPorts: GossipSimManager.ExtPort, selfAdr: KAddress, croupierOId: OverlayId) extends se.sics.kompics.Init[GossipSimManager]

    case class ExtPort(timerPort: Positive[Timer],
                       networkPort: Positive[Network],
                       croupierPort: Positive[CroupierPort],
                       viewUpdatePort: Negative[OverlayViewUpdatePort]
                      )

}

class GossipSimManager(val init: GossipSimManager.Init) extends ComponentDefinition with StrictLogging {
    val omngrPort = requires[OverlayMngrPort]

    val (extPorts, self, croupierId) = init match {
        case GossipSimManager.Init(e: ExtPort, s: KAddress, c: OverlayId) => (e, s, c)
    }

    val appComp = create(classOf[GossipSimApp], GossipSimApp.Init(self, croupierId))
    val pp2pl = create(classOf[PerfectP2PLink], new Init[PerfectP2PLink](self))
    val gossip = create(classOf[GossippingBasicBroadcast], new Init[GossippingBasicBroadcast](self))


    // Perfect links
    connect(pp2pl.getNegative(classOf[Network]), extPorts.networkPort, Channel.TWO_WAY)

    // Gossipping broadcast
    connect[PerfectLink](pp2pl -> gossip)
    connect(gossip.getNegative(classOf[CroupierPort]), extPorts.croupierPort, Channel.TWO_WAY)

    // Application
    connect[BestEffortBroadcast](gossip -> appComp)
    connect(appComp.getNegative(classOf[Timer]), extPorts.timerPort, Channel.TWO_WAY)
    connect(appComp.getNegative(classOf[Network]), extPorts.networkPort, Channel.TWO_WAY)
    connect(appComp.getNegative(classOf[CroupierPort]), extPorts.croupierPort, Channel.TWO_WAY)


    var pendingCroupierConnReq: OMngrCroupier.ConnectRequest = _
    ctrl uponEvent {
        case _: Start => handle {
            pendingCroupierConnReq = new OMngrCroupier.ConnectRequest(croupierId, false)
            trigger(pendingCroupierConnReq, omngrPort)
        }
    }

    omngrPort uponEvent {
        case _: OMngrCroupier.ConnectResponse => handle {
            trigger(new OverlayViewUpdate.Indication[NoView](croupierId, false, new NoView), extPorts.viewUpdatePort)
        }
    }

}
