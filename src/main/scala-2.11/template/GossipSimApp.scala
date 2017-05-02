package template

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{BEB_Broadcast, BEB_Deliver, BestEffortBroadcast}
import dresden.networking.{PingMessage, PongMessage}
import dresden.sim.SimUtil
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timeout, Timer}
import se.sics.kompics.{KompicsEvent, Start}
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.util.identifiable.Identifier
import se.sics.ktoolbox.util.network.KAddress
import template.kth.app.sim.SimulationResultSingleton


object GossipSimApp {

    case class Init(selfAdr: KAddress, gradientOId: Identifier) extends se.sics.kompics.Init[GossipSimApp]

    case class Ping() extends KompicsEvent

    case class Pong() extends KompicsEvent

}

class GossipSimApp(val init: GossipSimApp.Init) extends ComponentDefinition with StrictLogging {

    val self = init match {
        case GossipSimApp.Init(self, gradientOid) => self
    }

    val timer = requires[Timer]
    val network = requires[Network]
    val croupier = requires[CroupierPort]
    val gossip = requires[BestEffortBroadcast]

    private var sent = Set.empty[String]
    private var received = Set.empty[String]

    private var timerId: Option[UUID] = None
    private val period: Long = 2000 // TODO

    private def sendGossip() = {
        val id: String = UUID.randomUUID().toString
        logger.info(s"$self triggering gossip $id")
        val payload = GossipPayload(self, id)
        trigger(BEB_Broadcast(payload) -> gossip)
        sent += id

        import scala.collection.JavaConverters._
        SimulationResultSingleton.getInstance().put(self.getId + SimUtil.SEND_STR, sent.asJava)
    }

    ctrl uponEvent {
        case _: Start => handle {
            logger.info(s"$self starting...")
            val spt = new SchedulePeriodicTimeout(0, period)
            val timeout = DresdenTimeout(spt)
            spt.setTimeoutEvent(timeout)
            trigger(spt -> timer)
            timerId = Some(timeout.getTimeoutId)
        }
    }

    timer uponEvent {
        case DresdenTimeout(_) => handle {
            sendGossip()
            killTimer()
        }
    }

    gossip uponEvent {
        case BEB_Deliver(_, payload@GossipPayload(from, id)) => handle {
            if (received.contains(id)) {
                logger.warn(s"Duplicated GBEB Deliver message $payload")
            } else {
                received += id
                logger.info(s"$self received gossip $id")

                import scala.collection.JavaConverters._
                SimulationResultSingleton.getInstance().put(self.getId + SimUtil.RECV_STR,  received.asJava)
            }
        }
        case anything => handle {
            logger.warn(s"$self unexpected gossip event: $anything")
        }
    }

    private def killTimer() = {
        timerId match {
            case Some(id) =>
                trigger(new CancelPeriodicTimeout(id) -> timer)
            case None => // Nothing to do
        }
    }

    override def tearDown(): Unit = {
        killTimer()
    }
}

case class DresdenTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)
case class GossipPayload(from: KAddress, id: String) extends KompicsEvent {
    override def equals(o: Any) = o match {
        case that: GossipPayload => that.id.equals(this.id)
        case _ => false
    }
    override def hashCode = id.hashCode
}