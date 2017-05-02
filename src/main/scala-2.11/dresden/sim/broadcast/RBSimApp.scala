package dresden.sim.broadcast

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports._
import dresden.sim.SimUtil
import dresden.sim.SimUtil.{BroadcastPayload, DresdenTimeout}
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timer}
import se.sics.kompics.{KompicsEvent, Start}
import se.sics.ktoolbox.util.identifiable.Identifier
import se.sics.ktoolbox.util.network.KAddress
import template.kth.app.sim.SimulationResultSingleton


object RBSimApp {

    case class Init(selfAdr: KAddress, gradientOId: Identifier) extends se.sics.kompics.Init[RBSimApp]

    case class Ping() extends KompicsEvent

    case class Pong() extends KompicsEvent

}

class RBSimApp(val init: RBSimApp.Init) extends ComponentDefinition with StrictLogging {

    val self = init match {
        case RBSimApp.Init(self, gradientOid) => self
    }

    val timer = requires[Timer]
    val network = requires[Network]
    val rb = requires[ReliableBroadcast]
    private val period: Long = 2000 // TODO
    private var sent = Set.empty[String]
    private var received = Set.empty[String]
    private var timerId: Option[UUID] = None

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

    rb uponEvent {
        case RB_Deliver(src, payload) => handle {
            logger.info(s"$self RB_Delivering $payload from $src")
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
        logger.info(s"$self triggering rb $id")
        val payload = BroadcastPayload(self, id)
        trigger(BEB_Broadcast(payload) -> rb)
        sent += id

        import scala.collection.JavaConverters._
        SimulationResultSingleton.getInstance().put(self.getId + SimUtil.SEND_STR, sent.asJava)
    }
}
