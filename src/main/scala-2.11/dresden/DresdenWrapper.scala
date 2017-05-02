package dresden

import com.typesafe.scalalogging.StrictLogging
import dresden.DresdenWrapper.ExtPort
import dresden.components.Ports.{BestEffortBroadcast, PerfectLink}
import dresden.components.broadcast.GossippingBasicBroadcast
import dresden.components.links.PerfectP2PLink
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


class DresdenWrapper(init: Init[DresdenWrapper]) extends ComponentDefinition with StrictLogging {

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
            logger.debug("Starting Dresden")
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
        app = Some(create(classOf[Dresden], Init[Dresden](self)))
        app match {
            case Some(dresden) =>
                // Perfect link
                connect(pp2p.getNegative(classOf[Network]), ext.network, Channel.TWO_WAY)

                // Gossipping broadcast
                connect[PerfectLink](pp2p -> gossip)
                connect(gossip.getNegative(classOf[CroupierPort]), ext.croupier, Channel.TWO_WAY)

                // App
                connect(dresden.getNegative(classOf[Timer]), ext.timer, Channel.TWO_WAY)
                connect(dresden.getNegative(classOf[Network]), ext.network, Channel.TWO_WAY)
                connect(dresden.getNegative(classOf[CroupierPort]), ext.croupier, Channel.TWO_WAY)
                connect[BestEffortBroadcast](gossip -> dresden)
            case None =>
                logger.error("Failed to create application. Exiting")
                throw new RuntimeException("Application component is None")
        }
    }
}

object DresdenWrapper {

    case class ExtPort(timer: Positive[Timer], network: Positive[Network], croupier: Positive[CroupierPort], viewUpdate: Negative[OverlayViewUpdatePort])

}