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


object CRBSimApp {

    case class Init(selfAdr: KAddress, gradientOId: Identifier) extends se.sics.kompics.Init[CRBSimApp]

}

// TODO Add causality by triggering a response message when receiving one
// TODO and capture this causality in sim. results singleton (need to decide format)
class CRBSimApp(val init: CRBSimApp.Init) extends ComponentDefinition with StrictLogging {

    val self = init match {
        case CRBSimApp.Init(self, gradientOid) => self
    }

    val timer = requires[Timer]
    val network = requires[Network]
    val crb = requires[CausalOrderReliableBroadcast]

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

    crb uponEvent {
        case CRB_Deliver(_, payload@BroadcastPayload(src, id)) => handle {
            logger.info(s"$self CRB_Delivering $payload from $src")
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

    private def sendBroadcast():String = {
        val id: String = UUID.randomUUID().toString
        logger.info(s"$self triggering crb $id")
        val payload = BroadcastPayload(self, id)
        trigger(CRB_Broadcast(payload) -> crb)
        val data = SimUtil.genPeerToIdStr(self, id)
        sent += data

        import scala.collection.JavaConverters._
        SimulationResultSingleton.getInstance().put(self.getId + SimUtil.SEND_STR, sent.asJava)
        id
    }
}
