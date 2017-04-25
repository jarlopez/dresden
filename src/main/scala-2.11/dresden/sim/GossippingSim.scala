package dresden.sim

import com.typesafe.scalalogging.StrictLogging
import dresden.Dresden
import dresden.components.Ports.{GossippingBestEffortBroadcast, PerfectLink}
import dresden.components.broadcast.GossippingBasicBroadcast
import dresden.components.links.PerfectP2PLink
import dresden.sim.GossippingSim.ExtPort
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.{ComponentDefinition => _, Init => _, _}
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.overlaymngr.OverlayMngrPort
import se.sics.ktoolbox.overlaymngr.events.OMngrCroupier
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId
import se.sics.ktoolbox.util.network.KAddress
import se.sics.ktoolbox.util.overlays.view.{OverlayViewUpdate, OverlayViewUpdatePort}
import template.kth.croupier.util.NoView


class GossippingSim(init: Init[GossippingSim]) extends ComponentDefinition with StrictLogging {

    val overlayManager: PositivePort[OverlayMngrPort] = requires[OverlayMngrPort]

    private val (croupierId, self, ext) = init match {
        case Init(crId: OverlayId, s: KAddress, e: ExtPort) =>
            (crId, s, e)
    }

    val gossip = create(classOf[GossippingBasicBroadcast], new Init[GossippingBasicBroadcast](self))
    val pp2p = create(classOf[PerfectP2PLink], new Init[PerfectP2PLink](self))

    private var app = None: Option[Component]
    private var croupierConnReq = None: Option[OMngrCroupier.ConnectRequest]

    ctrl uponEvent {
        case _: Start => handle {
            logger.info("Starting gossipping simulation")
            croupierConnReq = Some(new OMngrCroupier.ConnectRequest(croupierId, false))
            trigger(croupierConnReq.get, overlayManager)
        }
    }

    overlayManager uponEvent {
        case ev: OMngrCroupier.ConnectResponse => handle {
            logger.info("Overlays connected")
            connectApp()
            app match {
                case Some(mainApp) =>
                    trigger(Start.event -> mainApp.control())
                    trigger(new OverlayViewUpdate.Indication[NoView](croupierId, false, new NoView), ext.viewUpdate)
                case None =>
                    logger.error("Application component does not exist. Exiting")
                    throw new RuntimeException("Application component is None")
            }

        }
    }

    def connectApp(): Unit = {
        app = Some(create(classOf[GossipSimApp], Init[GossipSimApp](self)))
        app match {
            case Some(it) =>
                // Perfect link
                connect(pp2p.getNegative(classOf[Network]), ext.network, Channel.TWO_WAY)

                // Gossipping broadcast
                connect[PerfectLink](pp2p -> gossip)
                connect(gossip.getNegative(classOf[CroupierPort]), ext.croupier, Channel.TWO_WAY)

                // App
                connect(it.getNegative(classOf[Timer]), ext.timer, Channel.TWO_WAY)
                connect(it.getNegative(classOf[Network]), ext.network, Channel.TWO_WAY)
                connect[GossippingBestEffortBroadcast](gossip -> it)
            case None =>
                logger.error("Failed to create application. Exiting")
                throw new RuntimeException("Application component is None")
        }
    }
}

object GossippingSim {

    case class ExtPort(timer: Positive[Timer], network: Positive[Network], croupier: Positive[CroupierPort], viewUpdate: Negative[OverlayViewUpdatePort])

}