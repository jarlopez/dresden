package dresden.sim.broadcast

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{BEB_Broadcast, BEB_Deliver, BestEffortBroadcast}
import dresden.sim.SimUtil
import dresden.sim.SimUtil.{BroadcastPayload, DresdenTimeout}
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timer}
import se.sics.kompics.{KompicsEvent, Start}
import se.sics.ktoolbox.util.identifiable.Identifier
import se.sics.ktoolbox.util.network.KAddress
import template.kth.app.sim.SimulationResultSingleton

import scala.collection.mutable.ListBuffer


object GossipSimApp {

    case class Init(selfAdr: KAddress, gradientOId: Identifier) extends se.sics.kompics.Init[GossipSimApp]

}

class GossipSimApp(val init: GossipSimApp.Init) extends ComponentDefinition with StrictLogging {

    val self = init match {
        case GossipSimApp.Init(self, gradientOid) => self
    }

    val timer = requires[Timer]
    val network = requires[Network]
    val gossip = requires[BestEffortBroadcast]

    private val period: Long = 2000 // TODO
    private var timerId: Option[UUID] = None

    private var sent = new ListBuffer[String]()
    private var received = new ListBuffer[String]()

    override def tearDown(): Unit = {
        killTimer()
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
        case BEB_Deliver(_, payload@BroadcastPayload(from, id)) => handle {
            if (received.contains(id)) {
                logger.warn(s"Duplicated GBEB Deliver message $payload")
            } else {
                received += id
                logger.info(s"$self received gossip $id")

                import scala.collection.JavaConverters._
                SimulationResultSingleton.getInstance().put(self.getId + SimUtil.RECV_STR, received.toList.asJava)
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

    private def sendGossip() = {
        val id: String = UUID.randomUUID().toString
        logger.info(s"$self triggering gossip $id")
        val payload = BroadcastPayload(self, id)
        trigger(BEB_Broadcast(payload) -> gossip)
        sent += id

        import scala.collection.JavaConverters._
        SimulationResultSingleton.getInstance().put(self.getId + SimUtil.SEND_STR, sent.toList.asJava)
    }
}
