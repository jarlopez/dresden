package dresden.sim.crdt

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{BestEffortBroadcast, CausalOrderReliableBroadcast, PerfectLink, ReliableBroadcast}
import dresden.components.broadcast.{EagerReliableBroadcast, GossippingBasicBroadcast, NoWaitingCRB}
import dresden.components.links.PerfectP2PLink
import dresden.crdt.Ports.GSetManagement
import dresden.crdt.set.{GSet, GSetManager}
import dresden.sim.crdt.GSetSimManager.ExtPort
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


object GSetSimManager {

    case class Init(extPorts: GSetSimManager.ExtPort, selfAdr: KAddress, croupierOId: OverlayId) extends se.sics.kompics.Init[GSetSimManager]

    case class ExtPort(timerPort: Positive[Timer],
                       networkPort: Positive[Network],
                       croupierPort: Positive[CroupierPort],
                       viewUpdatePort: Negative[OverlayViewUpdatePort]
                      )

}

class GSetSimManager(val init: GSetSimManager.Init) extends ComponentDefinition with StrictLogging {
    val omngrPort = requires[OverlayMngrPort]

    val (extPorts, self, croupierId) = init match {
        case GSetSimManager.Init(e: ExtPort, s: KAddress, c: OverlayId) => (e, s, c)
    }

    val pp2pl = create(classOf[PerfectP2PLink], new Init[PerfectP2PLink](self))
    val gossip = create(classOf[GossippingBasicBroadcast], new Init[GossippingBasicBroadcast](self))
    val rb = create(classOf[EagerReliableBroadcast], new Init[EagerReliableBroadcast](self))
    val crb = create(classOf[NoWaitingCRB], new Init[NoWaitingCRB](self))
//    val mngr = create(classOf[GSetManager[String]], new CRDTManager.Init(self)) // XXX FIXME TODO AAAA
    val mngr = create(classOf[GSetManager[String]], new Init[GSetManager[String]](self))
    val appComp = create(classOf[GSetSimApp], GSetSimApp.Init(self))


    // Perfect point-to-point links
    connect(pp2pl.getNegative(classOf[Network]), extPorts.networkPort, Channel.TWO_WAY)

    // Gossipping BEB
    connect[PerfectLink](pp2pl -> gossip)
    connect(gossip.getNegative(classOf[CroupierPort]), extPorts.croupierPort, Channel.TWO_WAY)

    // Eager reliable broadcast
    connect[BestEffortBroadcast](gossip -> rb)

    // No-waiting causal reliable broadcast
    connect[ReliableBroadcast](rb -> crb)

    // CRDT Manager
    connect[CausalOrderReliableBroadcast](crb -> mngr)

    // Application
    connect[GSetManagement](mngr -> appComp)


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
