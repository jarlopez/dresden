package dresden.sim.crdt

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{BestEffortBroadcast, CausalOrderReliableBroadcast, PerfectLink, ReliableBroadcast}
import dresden.components.broadcast.{EagerReliableBroadcast, GossippingBasicBroadcast, NoWaitingCRB}
import dresden.components.links.PerfectP2PLink
import dresden.crdt.Ports.{GSetManagement, TwoPSetManagement}
import dresden.crdt.set.{GSet, GSetManager, TwoPSetManager}
import dresden.sim.crdt.CRDTSimManager.ExtPort
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


object CRDTSimManager {

    case class Init(extPorts: CRDTSimManager.ExtPort, selfAdr: KAddress, croupierOId: OverlayId) extends se.sics.kompics.Init[CRDTSimManager]

    case class ExtPort(timerPort: Positive[Timer],
                       networkPort: Positive[Network],
                       croupierPort: Positive[CroupierPort],
                       viewUpdatePort: Negative[OverlayViewUpdatePort]
                      )

}

class CRDTSimManager(val init: CRDTSimManager.Init) extends ComponentDefinition with StrictLogging {
    val omngrPort = requires[OverlayMngrPort]

    val (extPorts, self, croupierId) = init match {
        case CRDTSimManager.Init(e: ExtPort, s: KAddress, c: OverlayId) => (e, s, c)
    }

    val pp2pl = create(classOf[PerfectP2PLink], new Init[PerfectP2PLink](self))
    val gossip = create(classOf[GossippingBasicBroadcast], new Init[GossippingBasicBroadcast](self))
    val rb = create(classOf[EagerReliableBroadcast], new Init[EagerReliableBroadcast](self))
    val crb = create(classOf[NoWaitingCRB], new Init[NoWaitingCRB](self))
    val mngr = create(classOf[TwoPSetManager[String]], new Init[TwoPSetManager[String]](self))



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
    config.getValue("dresden.dresden.sim.crdt.target", classOf[String]) match {
        case "gset" =>
            val appComp = create(classOf[GSetSimApp], GSetSimApp.Init(self))
            connect[GSetManagement](mngr -> appComp)
        case "twopset" =>
            val appComp = create(classOf[TwoPSetSimApp], TwoPSetSimApp.Init(self))
            connect[TwoPSetManagement](mngr -> appComp)
            connect(appComp.getNegative(classOf[Timer]), extPorts.timerPort, Channel.TWO_WAY)
    }


    connect[TwoPSetManagement](mngr -> appComp)
    connect(appComp.getNegative(classOf[Timer]), extPorts.timerPort, Channel.TWO_WAY)


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
