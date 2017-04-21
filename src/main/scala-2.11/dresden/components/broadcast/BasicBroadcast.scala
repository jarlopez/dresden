package dresden.components.broadcast

import dresden.components.Ports._
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.KAddress

class BasicBroadcast(init: Init[BasicBroadcast]) extends ComponentDefinition {

    val pLink = requires[PerfectLink]

    val beb = provides[BestEffortBroadcast]

    val (self, topology) = init match {
        case Init(s: KAddress, t: Set[KAddress]@unchecked) => (s, t)
    }

    beb uponEvent {
        case x: BEB_Broadcast => handle {
            for (q <- topology) {
                trigger(PL_Send(q, x) -> pLink)
            }
        }
    }

    pLink uponEvent {
        case PL_Deliver(src, BEB_Broadcast(payload)) => handle {
            trigger(BEB_Deliver(src, payload) -> beb)
        }
    }
}