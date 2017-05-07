package dresden.sim.crdt

import com.typesafe.scalalogging.StrictLogging
import dresden.crdt.Ports.{GSetManagement, Get, Op, Response}
import dresden.crdt.set.{AddOperation, GSet}
import se.sics.kompics.Start
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.KAddress


object GSetSimApp {

    case class Init(selfAdr: KAddress) extends se.sics.kompics.Init[GSetSimApp]

}

class GSetSimApp(val init: GSetSimApp.Init) extends ComponentDefinition with StrictLogging {

    val self = init match {
        case GSetSimApp.Init(self) => self
    }

    val mngr = requires[GSetManagement]

    override def tearDown(): Unit = {
    }

    ctrl uponEvent {
        case _: Start => handle {
            logger.info(s"$self starting...")
            trigger(Get("johan") -> mngr)
        }
    }

    mngr uponEvent {
        case Response(id, crdt) => handle {
            logger.info(s"$self Received $crdt")
            trigger(Op("johan", AddOperation(self.toString)) -> mngr)

        }
    }

}
