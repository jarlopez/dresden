package dresden.sim.broadcast

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{BestEffortBroadcast, CausalOrderReliableBroadcast, PerfectLink, ReliableBroadcast}
import dresden.components.broadcast.{EagerReliableBroadcast, GossippingBasicBroadcast, NoWaitingCRB}
import dresden.components.links.PerfectP2PLink
import dresden.sim.broadcast.CRBSimManager.ExtPort
import dresden.sim.util.NoView
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


object CRBSimManager {

    case class Init(extPorts: CRBSimManager.ExtPort, selfAdr: KAddress, croupierOId: OverlayId) extends se.sics.kompics.Init[CRBSimManager]

    case class ExtPort(timerPort: Positive[Timer],
                       networkPort: Positive[Network],
                       croupierPort: Positive[CroupierPort],
                       viewUpdatePort: Negative[OverlayViewUpdatePort]
                      )

}

class CRBSimManager(val init: CRBSimManager.Init) extends ComponentDefinition with StrictLogging {
    val omngrPort = requires[OverlayMngrPort]

    val (extPorts, self, croupierId) = init match {
        case CRBSimManager.Init(e: ExtPort, s: KAddress, c: OverlayId) => (e, s, c)
    }

    val appComp = create(classOf[CRBSimApp], CRBSimApp.Init(self, croupierId))
    val pp2pl = create(classOf[PerfectP2PLink], new Init[PerfectP2PLink](self))
    val gossip = create(classOf[GossippingBasicBroadcast], new Init[GossippingBasicBroadcast](self))
    val rb = create(classOf[EagerReliableBroadcast], new Init[EagerReliableBroadcast](self))
    val crb = create(classOf[NoWaitingCRB], new Init[NoWaitingCRB](self))


    // Perfect point-to-point links
    connect(pp2pl.getNegative(classOf[Network]), extPorts.networkPort, Channel.TWO_WAY)

    // Gossipping BEB
    connect[PerfectLink](pp2pl -> gossip)
    connect(gossip.getNegative(classOf[CroupierPort]), extPorts.croupierPort, Channel.TWO_WAY)

    // Eager reliable broadcast
    connect[BestEffortBroadcast](gossip -> rb)

    // No-waiting causal reliable broadcast
    connect[ReliableBroadcast](rb -> crb)

    // Application
    connect[CausalOrderReliableBroadcast](crb -> appComp)
    connect(appComp.getNegative(classOf[Timer]), extPorts.timerPort, Channel.TWO_WAY)
    connect(appComp.getNegative(classOf[Network]), extPorts.networkPort, Channel.TWO_WAY)


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
