package dresden.sim.crdt

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import dresden.crdt.Ports._
import dresden.crdt.set.{ORSet, TwoPSet}
import dresden.crdt.set.ORSetManager.{AddOperation, RemoveOperation}
import dresden.sim.SimUtil.DresdenTimeout
import dresden.sim.{SimUtil, SimulationResultSingleton}
import se.sics.kompics.Start
import se.sics.kompics.sl._
import se.sics.kompics.timer.{SchedulePeriodicTimeout, Timer}
import se.sics.ktoolbox.util.network.KAddress


object ORSetSimApp {

    case class Init(selfAdr: KAddress) extends se.sics.kompics.Init[ORSetSimApp]

}

class ORSetSimApp(val init: ORSetSimApp.Init) extends ComponentDefinition with StrictLogging {

    val mngr = requires[ORSetManagement]
    val timer = requires[Timer]

    var orset: Option[ORSet[String]] = None

    val self = init match {
        case ORSetSimApp.Init(self) => self
    }

    private val period: Long = 1000 // TODO
    private var timerIds: Set[UUID] = Set.empty[UUID]

    private var numSends: Int = 0
    private var maxSends: Int = 20


    ctrl uponEvent {
        case _: Start => handle {
            logger.info(s"$self starting...")
            val spt = new SchedulePeriodicTimeout(0, period)
            val timeout = DresdenTimeout(spt)
            spt.setTimeoutEvent(timeout)
            trigger(spt -> timer)
            timerIds += timeout.getTimeoutId

            // Fetch our set
            trigger(Get(SimUtil.CRDT_SET_KEY) -> mngr)
        }
    }

    timer uponEvent {
        case DresdenTimeout(_) => handle {
            // Either send another 'add' or remove a random
            if (math.random < 0.3 && orset.get.entries.nonEmpty) {
                removeRandom()
            } else {
                sendAdd()
            }
        }
    }

    mngr uponEvent {
        case Response(id, crdt: ORSet[String]) => handle {
            orset = Some(crdt)
            logger.info(s"$self Received $crdt")
            sendAdd()
        }
        case Update(id, crdt: ORSet[String]) => handle {
            logger.info(s"Received CRDT update for $id")
            orset = Some(crdt)
            import scala.collection.JavaConverters._
            SimulationResultSingleton.getInstance().put(self.getId + SimUtil.ORSET_STR, crdt.entries.asJava)
        }
        case anything => handle {
            logger.warn(s"Received anything! $anything")
        }
    }

    private def sendAdd(): Unit = {
        if (numSends < maxSends) {
            logger.debug(s"$self Triggering send")

            trigger(Op(SimUtil.CRDT_SET_KEY, AddOperation(self.toString + SimUtil.DELIM_STR + numSends)) -> mngr)
            numSends += 1
        }
    }

    private def removeRandom(): Unit = {
        if (numSends < maxSends) {
            logger.debug(s"$self Triggering remove")
            val it = random[String](orset.get.elements())
            trigger(Op(SimUtil.CRDT_SET_KEY, RemoveOperation(it)) -> mngr)
            numSends += 1
        }
    }

    def random[T](s: Set[T]): T = {
        val n = util.Random.nextInt(s.size)
        s.iterator.drop(n).next
    }

}
