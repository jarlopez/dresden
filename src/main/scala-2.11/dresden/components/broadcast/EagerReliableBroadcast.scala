package dresden.components.broadcast

import dresden.components.Ports._
import dresden.networking.NetAddress
import se.sics.kompics.sl.{Init, _}
import se.sics.kompics.{KompicsEvent, ComponentDefinition => _, Port => _}

case class OriginatedData(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

class EagerReliableBroadcast(init: Init[EagerReliableBroadcast]) extends ComponentDefinition {

    val beb = requires[BestEffortBroadcast]
    val rb = provides[ReliableBroadcast]

    val self: NetAddress = init match {
        case Init(s: NetAddress) => s
    }

    private var delivered = collection.mutable.Set[KompicsEvent]()

    rb uponEvent {
        case RB_Broadcast(payload) => handle {
            trigger(BEB_Broadcast(OriginatedData(self, payload)) -> beb)
        }
    }

    beb uponEvent {
        case BEB_Deliver(_, data@OriginatedData(origin, payload)) => handle {
            if (!delivered.contains(payload)) {
                delivered = delivered + payload
                trigger(RB_Deliver(origin, payload) -> rb)
                trigger(BEB_Broadcast(data) -> beb)
            }
        }
    }
}