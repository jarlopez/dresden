package template


import com.typesafe.scalalogging.StrictLogging
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.{ComponentDefinition => _, Handler => _, _}
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.overlaymngr.OverlayMngrPort
import se.sics.ktoolbox.overlaymngr.events.OMngrCroupier
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId
import se.sics.ktoolbox.util.network.KAddress
import se.sics.ktoolbox.util.overlays.view.{OverlayViewUpdate, OverlayViewUpdatePort}
import template.GossipSimManager.ExtPort
import template.kth.croupier.util.NoView


object GossipSimManager {

    case class Init(extPorts: GossipSimManager.ExtPort, selfAdr: KAddress, croupierOId: OverlayId) extends se.sics.kompics.Init[GossipSimManager]

    case class ExtPort(timerPort: Positive[Timer], networkPort: Positive[Network], croupierPort: Positive[CroupierPort], viewUpdatePort: Negative[OverlayViewUpdatePort])

}

class GossipSimManager(val init: GossipSimManager.Init) extends ComponentDefinition with StrictLogging {
    val omngrPort = requires[OverlayMngrPort]

    val (extPorts, selfAdr, croupierId) = init match {
        case GossipSimManager.Init(e: ExtPort, s: KAddress, c: OverlayId) => (e, s, c)
    }

    val appComp: Component = create(classOf[GossipSimApp], GossipSimApp.Init(selfAdr, croupierId))
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
