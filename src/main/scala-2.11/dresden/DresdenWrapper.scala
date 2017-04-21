package dresden

import com.typesafe.scalalogging.StrictLogging
import dresden.DresdenWrapper.ExtPort
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.{Handler, ComponentDefinition => _, Init => _, _}
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.overlaymngr.OverlayMngrPort
import se.sics.ktoolbox.overlaymngr.events.OMngrCroupier
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId
import se.sics.ktoolbox.util.network.KAddress
import se.sics.ktoolbox.util.overlays.view.{OverlayViewUpdate, OverlayViewUpdatePort}
import template.kth.croupier.util.NoView


class DresdenWrapper(init: Init[DresdenWrapper]) extends ComponentDefinition with StrictLogging {

    val overlayManager: PositivePort[OverlayMngrPort] = requires[OverlayMngrPort]
//    val omngrPort = requires(classOf[OverlayMngrPort])

    private var app = None: Option[Component]
    private var croupierConnReq = None: Option[OMngrCroupier.ConnectRequest]

    private val handleCroupierConnected: Handler[_ <: KompicsEvent] = new Handler[OMngrCroupier.ConnectResponse]() {
        def handle(event: OMngrCroupier.ConnectResponse) {
            logger.info("Overlays connected")
            connectApp()
            trigger(Start.event, app.get.control)
            trigger(new OverlayViewUpdate.Indication[NoView](croupierId, false, new NoView), ext.viewUpdate)
        }
    }

    private val (croupierId, self, ext) = init match {
        case Init(crId: OverlayId, s: KAddress, e: ExtPort) => {
//            subscribe(handleCroupierConnected, omngrPort)
            (crId, s, e)
        }
    }

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
        connect(app.get.getNegative(classOf[Timer]), ext.timer, Channel.TWO_WAY)
        connect(app.get.getNegative(classOf[Network]), ext.network, Channel.TWO_WAY)
        connect(app.get.getNegative(classOf[CroupierPort]), ext.croupier, Channel.TWO_WAY)
    }
}

object DresdenWrapper {
    case class ExtPort(timer: Positive[Timer], network: Positive[Network], croupier: Positive[CroupierPort], viewUpdate: Negative[OverlayViewUpdatePort])
}