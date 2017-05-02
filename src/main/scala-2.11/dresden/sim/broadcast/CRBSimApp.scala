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


object CRBSimApp {

    case class Init(selfAdr: KAddress, gradientOId: Identifier) extends se.sics.kompics.Init[CRBSimApp]

    case class Ping() extends KompicsEvent

    case class Pong() extends KompicsEvent

}

class CRBSimApp(val init: CRBSimApp.Init) extends ComponentDefinition with StrictLogging {

    val self = init match {
        case CRBSimApp.Init(self, gradientOid) => self
    }

    val timer = requires[Timer]
    val network = requires[Network]
    val crb = requires[CausalOrderReliableBroadcast]

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
            sendBroadcast()
            killTimer()
        }
    }

    crb uponEvent {
        case CRB_Deliver(_, payload@BroadcastPayload(src, id)) => handle {
            logger.info(s"$self CRB_Delivering $payload from $src")
            received += id

            import scala.collection.JavaConverters._
            SimulationResultSingleton.getInstance().put(self.getId + SimUtil.RECV_STR, received.asJava)
        }
    }

    private def killTimer() = {
        timerId match {
            case Some(id) =>
                trigger(new CancelPeriodicTimeout(id) -> timer)
            case None => // Nothing to do
        }
    }

    private def sendBroadcast() = {
        val id: String = UUID.randomUUID().toString
        logger.info(s"$self triggering crb $id")
        val payload = BroadcastPayload(self, id)
        trigger(CRB_Broadcast(payload) -> crb)
        sent += id

        import scala.collection.JavaConverters._
        SimulationResultSingleton.getInstance().put(self.getId + SimUtil.SEND_STR, sent.asJava)
    }
}
