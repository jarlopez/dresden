package dresden.sim

import com.typesafe.scalalogging.StrictLogging
import dresden.DresdenWrapper
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.{Channel, Component, Start}
import se.sics.ktoolbox.cc.heartbeat.CCHeartbeatPort
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.omngr.bootstrap.BootstrapClientComp
import se.sics.ktoolbox.overlaymngr.{OverlayMngrComp, OverlayMngrPort}
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId
import se.sics.ktoolbox.util.network.KAddress
import se.sics.ktoolbox.util.network.nat.NatAwareAddress
import se.sics.ktoolbox.util.overlays.view.OverlayViewUpdatePort

class SimHost(init: Init[SimHost]) extends ComponentDefinition with StrictLogging {

    def this(init: SimHostInit) {
        this(new Init[SimHost](init.selfAdr, init.bootstrapServer, init.croupierId))
    }

    val timer = requires[Timer]
    val network = requires[Network]

    var bootstrapClient = None: Option[Component]
    var overlayManager = None: Option[Component]
    var dresden = None: Option[Component]

    val (self, bootstrap, croupierId) = init match {
        case Init(s: KAddress, bs: KAddress, c: OverlayId) => (s, bs, c)
    }

    ctrl uponEvent {
        case _: Start => handle {
            logger.info("Starting simulation host")
            createBootstrapClient()
            createOverlayManager()
            createApp()

            // TODO Is this needed?
            trigger(Start.event -> bootstrapClient.get.control)
            trigger(Start.event -> overlayManager.get.control)
            trigger(Start.event -> dresden.get.control)
        }
    }

    def createBootstrapClient(): Unit = {
        bootstrapClient = Some(create(classOf[BootstrapClientComp], new BootstrapClientComp.Init(self, bootstrap)))
        connect(bootstrapClient.get.getNegative(classOf[Timer]), timer, Channel.TWO_WAY)
        connect(bootstrapClient.get.getNegative(classOf[Network]), network, Channel.TWO_WAY)
    }

    def createOverlayManager(): Unit = {
        bootstrapClient match {
            case Some(bsClient) =>
                val extPorts = new OverlayMngrComp.ExtPort(
                    timer,
                    network,
                    bsClient.getPositive(classOf[CCHeartbeatPort]))
                overlayManager = Some(create(
                    classOf[OverlayMngrComp],
                    new OverlayMngrComp.Init(self.asInstanceOf[NatAwareAddress], extPorts)))
        }
    }

    def createApp(): Unit = {
        val extPorts = DresdenWrapper.ExtPort(
            timer,
            network,
            overlayManager.get.getPositive(classOf[CroupierPort]),
            overlayManager.get.getNegative(classOf[OverlayViewUpdatePort])
        )
        dresden = Some(create(classOf[DresdenWrapper], new Init[DresdenWrapper](croupierId, self, extPorts)))
        connect[OverlayMngrPort](overlayManager.get -> dresden.get)
    }


}

class SimHostInit(val selfAdr: KAddress, val bootstrapServer: KAddress, val croupierId: OverlayId) extends se.sics.kompics.Init[SimHost]
