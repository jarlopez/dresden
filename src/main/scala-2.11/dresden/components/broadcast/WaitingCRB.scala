package dresden.components.broadcast

import dresden.components.Ports._
import dresden.util.VectorClock
import se.sics.kompics.sl.{Init, _}
import se.sics.kompics.{KompicsEvent, ComponentDefinition => _, Port => _}
import se.sics.ktoolbox.util.network.KAddress

import scala.collection.mutable.ListBuffer

object WaitingCRB {

    case class DataMessage(timestamp: VectorClock, payload: KompicsEvent) extends KompicsEvent

}

class WaitingCRB(init: Init[WaitingCRB]) extends ComponentDefinition {

    val rb = requires[ReliableBroadcast]
    val crb = provides[CausalOrderReliableBroadcast]

    val (self, vec) = init match {
        case Init(s: KAddress, t: Set[KAddress]@unchecked) => (s, VectorClock.empty(t.toSeq))
    }

    var pending: ListBuffer[(KAddress, WaitingCRB.DataMessage)] = ListBuffer()
    var lsn = 0


    crb uponEvent {
        case x: CRB_Broadcast => handle {
            val w = VectorClock.apply(vec)
            w.set(self, lsn)
            lsn = lsn + 1
            trigger(RB_Broadcast(WaitingCRB.DataMessage(w, x.payload)) -> rb)
        }
    }

    rb uponEvent {
        case RB_Deliver(src: KAddress, msg: WaitingCRB.DataMessage) => handle {
            pending += ((src, msg))
            for (it@(address, message) <- pending.sortWith(_._2.timestamp <= _._2.timestamp)) {
                if (message.timestamp <= vec) {
                    pending = pending - it
                    vec.inc(address)
                    trigger(CRB_Deliver(address, message.payload) -> crb)
                }
            }
        }
    }
}
