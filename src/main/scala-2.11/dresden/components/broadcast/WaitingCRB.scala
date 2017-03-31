package dresden.components.broadcast

import dresden.components.Ports._
import dresden.networking.NetAddress
import dresden.util.VectorClock
import se.sics.kompics.sl.{Init, _}
import se.sics.kompics.{KompicsEvent, ComponentDefinition => _, Port => _}

import scala.collection.mutable.ListBuffer

case class DataMessage(timestamp: VectorClock, payload: KompicsEvent) extends KompicsEvent

class WaitingCRB(init: Init[WaitingCRB]) extends ComponentDefinition {

    val rb = requires[ReliableBroadcast]
    val crb = provides[CausalOrderReliableBroadcast]

    val (self, vec) = init match {
        case Init(s: NetAddress, t: Set[NetAddress]@unchecked) => (s, VectorClock.empty(t.toSeq))
    }

    var pending: ListBuffer[(NetAddress, DataMessage)] = ListBuffer()
    var lsn = 0


    crb uponEvent {
        case x: CRB_Broadcast => handle {
            val w = VectorClock.apply(vec)
            w.set(self, lsn)
            lsn = lsn + 1
            trigger(RB_Broadcast(DataMessage(w, x.payload)) -> rb)
        }
    }

    rb uponEvent {
        case RB_Deliver(src: NetAddress, msg: DataMessage) => handle {
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
