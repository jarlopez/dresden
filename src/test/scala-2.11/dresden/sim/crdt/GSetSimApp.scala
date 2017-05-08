package dresden.sim.crdt

import com.typesafe.scalalogging.StrictLogging
import dresden.crdt.Ports._
import dresden.crdt.set.{AddOperation, GSet}
import dresden.sim.{SimUtil, SimulationResultSingleton}
import se.sics.kompics.{Start, Stop}
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.KAddress


object GSetSimApp {

    case class Init(selfAdr: KAddress) extends se.sics.kompics.Init[GSetSimApp]

}

class GSetSimApp(val init: GSetSimApp.Init) extends ComponentDefinition with StrictLogging {

    var gset: Option[GSet[String]] = None

    val self = init match {
        case GSetSimApp.Init(self) => self
    }

    val mngr = requires[GSetManagement]


    ctrl uponEvent {
        case _: Start => handle {
            logger.info(s"$self starting...")
            trigger(Get("johan") -> mngr)
        }
    }

    mngr uponEvent {
        case Response(id, crdt: GSet[String]) => handle {
            gset = Some(crdt)
            logger.info(s"$self Received $crdt")
            trigger(Op("johan", AddOperation(self.toString)) -> mngr)
        }
        case Update(id, crdt: GSet[String]) => handle {
            logger.info(s"Received CRDT update for $id")
            gset = Some(crdt)
            import scala.collection.JavaConverters._
            SimulationResultSingleton.getInstance().put(self.getId + SimUtil.GSET_STR, crdt.entries.asJava)
        }
    }

}
