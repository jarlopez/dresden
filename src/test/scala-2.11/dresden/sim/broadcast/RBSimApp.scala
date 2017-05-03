package dresden.sim.broadcast

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports._
import dresden.sim.{SimUtil, SimulationResultSingleton}
import dresden.sim.SimUtil.{BroadcastPayload, DresdenTimeout}
import se.sics.kompics.Start
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timer}
import se.sics.ktoolbox.util.identifiable.Identifier
import se.sics.ktoolbox.util.network.KAddress

import scala.collection.mutable.ListBuffer


object RBSimApp {

    case class Init(selfAdr: KAddress, gradientOId: Identifier) extends se.sics.kompics.Init[RBSimApp]

}

class RBSimApp(val init: RBSimApp.Init) extends ComponentDefinition with StrictLogging {

    val self = init match {
        case RBSimApp.Init(self, gradientOid) => self
    }

    val timer = requires[Timer]
    val network = requires[Network]
    val rb = requires[ReliableBroadcast]

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
            sendBroadcast()
            killTimer()
        }
    }

    rb uponEvent {
        case RB_Deliver(_, payload@BroadcastPayload(src, id)) => handle {
            logger.info(s"$self RB_Delivering $payload from $src")
            val data = SimUtil.genPeerToIdStr(src, id)
            received += data

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
        logger.info(s"$self triggering rb $id")
        val payload = BroadcastPayload(self, id)
        trigger(RB_Broadcast(payload) -> rb)
        val data = SimUtil.genPeerToIdStr(self, id)
        sent += data

        import scala.collection.JavaConverters._
        SimulationResultSingleton.getInstance().put(self.getId + SimUtil.SEND_STR, sent.asJava)
    }
}
