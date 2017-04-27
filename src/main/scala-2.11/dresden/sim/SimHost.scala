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

    val timer = requires[Timer]
    val network = requires[Network]
    val (self, bootstrap, croupierId) = init match {
        case Init(s: KAddress, bs: KAddress, c: OverlayId) => (s, bs, c)
    }
    var bootstrapClient = None: Option[Component]
    var overlayManager = None: Option[Component]
    var dresden = None: Option[Component]

    def this(init: SimHostInit) {
        this(new Init[SimHost](init.selfAdr, init.bootstrapServer, init.croupierId))
    }

    ctrl uponEvent {
        case _: Start => handle {
            logger.info("Starting simulation host")
            createBootstrapClient()
            createOverlayManager()
            createApp()
        }
    }

    def createBootstrapClient(): Unit = {
        bootstrapClient = Some(create(classOf[BootstrapClientComp], new BootstrapClientComp.Init(self, bootstrap)))
        connect(bootstrapClient.get.getNegative(classOf[Timer]), timer, Channel.TWO_WAY)
        connect(bootstrapClient.get.getNegative(classOf[Network]), network, Channel.TWO_WAY)
    }

    def createOverlayManager(): Unit = {
        bootstrapClient match {
            case None => // Shhh
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
        val extPorts = GossipSimWrapper.ExtPort(
            timer,
            network,
            overlayManager.get.getPositive(classOf[CroupierPort]),
            overlayManager.get.getNegative(classOf[OverlayViewUpdatePort])
        )
        dresden = Some(create(classOf[GossipSimWrapper], new Init[GossipSimWrapper](croupierId, self, extPorts)))
        connect(dresden.get.getNegative(classOf[OverlayMngrPort]), overlayManager.get.getPositive(classOf[OverlayMngrPort]), Channel.TWO_WAY)
    }

}

class SimHostInit(val selfAdr: KAddress, val bootstrapServer: KAddress, val croupierId: OverlayId) extends se.sics.kompics.Init[SimHost]
