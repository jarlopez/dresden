package dresden.components.broadcast


import dresden.components.Ports._
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.croupier.event.CroupierSample
import se.sics.ktoolbox.util.network.KAddress


class GossippingBasicBroadcast(init: Init[GossippingBasicBroadcast]) extends ComponentDefinition {
    type BasicSample = CroupierPort

    val gbeb = provides[GossippingBestEffortBroadcast]

    val pp2p = requires[PerfectLink]
    val bs = requires[BasicSample]

    val self = init match {
        case Init(s: KAddress) => s
    }

    private var past = Set.empty[Any]

    gbeb uponEvent {
        case x: GBEB_Broadcast => handle {
            past += (self, x.payload)
        }
    }

    bs uponEvent {
        case croupierSample: CroupierSample[_] => handle {
            if (!croupierSample.publicSample.isEmpty) {
                import collection.JavaConversions._
                val peers = croupierSample.publicSample.values().map { it => it.getSource }
                for (p: KAddress <- peers) {
                    trigger(PL_Send(p, HistoryRequest) -> pp2p)
                }
            }
        }
    }

    pp2p uponEvent {
        case PL_Deliver(p, HistoryRequest) => handle {
            trigger(PL_Send(p, HistoryResponse(past)) -> pp2p)
        }
        case PL_Deliver(p, HistoryResponse(history)) => handle {
            val unseen = history - past
            for ( (pp: KAddress, m: KompicsEvent) <- unseen) {
                trigger(GBEB_Deliver(pp, m) -> gbeb)
            }
            past = past union unseen
        }

    }
}

case object HistoryRequest extends KompicsEvent
case class HistoryResponse(past: Set[Any]) extends KompicsEvent
