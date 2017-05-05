package dresden.components.broadcast

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports._
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.KAddress

import scala.collection.mutable.ListBuffer

// TODO Data type?
object NoWaitingCRB {

    case class DataMessage(past: List[Any], payload: KompicsEvent) extends KompicsEvent

}

class NoWaitingCRB(init: Init[NoWaitingCRB]) extends ComponentDefinition with StrictLogging {
    val crb = provides[CausalOrderReliableBroadcast]

    val rb = requires[ReliableBroadcast]

    val (self) = init match {
        case Init(s: KAddress) => s
    }

    private var delivered = Set.empty[KompicsEvent]
    private var past = ListBuffer.empty[Any] // TODO Data type?

    crb uponEvent {
        case CRB_Broadcast(payload) => handle {
            logger.info(s"$self broadcasting $payload with $past")
            trigger(RB_Broadcast(NoWaitingCRB.DataMessage(past.toList, payload)) -> rb)
            past += ((self, payload))
        }
    }

    rb uponEvent {
        case RB_Deliver(src, payload@NoWaitingCRB.DataMessage(mpast, msg)) => handle {
            if (!delivered.contains(msg)) {
                for ((s: KAddress, n: KompicsEvent) <- mpast) {
                    if (!delivered.contains(n)) {
                        logger.debug(s"$self delivering $payload")

                        trigger(CRB_Deliver(s, n) -> crb)
                        delivered += n
                        if (!past.contains((s, n))) {
                            past += ((s, n))
                        }
                    }
                }
                trigger(CRB_Deliver(src, msg) -> crb)
                delivered += msg
                if (!past.contains((src, msg))) {
                    past += ((src, msg))
                }
            }
        }
    }
}
