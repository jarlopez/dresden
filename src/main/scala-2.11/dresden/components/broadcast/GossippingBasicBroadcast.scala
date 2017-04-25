package dresden.components.broadcast


import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports._
import dresden.components.links.PP2PPayload
import dresden.sim.GossipPayload
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.croupier.event.CroupierSample
import se.sics.ktoolbox.util.network.KAddress


class GossippingBasicBroadcast(init: Init[GossippingBasicBroadcast]) extends ComponentDefinition with StrictLogging {


    type BasicSample = CroupierPort

    val gbeb = provides[GossippingBestEffortBroadcast]

    val pp2p = requires[PerfectLink]
    val bs = requires[BasicSample]

    val self = init match {
        case Init(s: KAddress) => s
    }

//    private var past = Set.empty[ProcMsgPair]
    private var past = Set.empty[Any]

    gbeb uponEvent {
        case x: GBEB_Broadcast => handle {
//            past += ProcMsgPair(self, x.payload)
            past += ((self.toString, x.payload))
        }
    }

    bs uponEvent {
        case croupierSample: CroupierSample[_] => handle {
            if (!croupierSample.publicSample.isEmpty) {
                import collection.JavaConversions._
                val peers = croupierSample.publicSample.values().map { it => it.getSource }
                for (p: KAddress <- peers) {
                    trigger(PL_Send(p, PP2PPayload(HistoryRequest)) -> pp2p)
                }
            }
        }
    }

    pp2p uponEvent {
        case PL_Deliver(p, HistoryRequest) => handle {
            logger.debug(s"$self Received HistoryRequest")
            trigger(PL_Send(p, PP2PPayload(HistoryResponse(past))) -> pp2p)
        }
        case PL_Deliver(_, HistoryResponse(history)) => handle {
            logger.debug(s"$self Received HistoryResponse")

            // FIXME This doesn't subtract correctly due to the GossipPayload instances differing

            // TODO Avoid this O(nm) crap!
            def existsInPast(it: Any) = it match {
                case (addr: KAddress, payload: KompicsEvent) =>
                    past.exists {
                        case (addr2: KAddress, payload2: KompicsEvent) =>
                            addr.sameHostAs(addr2) && payload.equals(payload2)
                        case _ => false
                    }
                case _ => false
            }

            val unseen: Set[Any] = history.diff(past)

            logger.debug(s"Unseen: ${unseen} vs past: ${past.size}")
            for ( (pp: KAddress, m: GossipPayload) <- unseen) {
                trigger(GBEB_Deliver(pp, m) -> gbeb)
            }
            past = past union unseen
        }
        case anything => handle {
            logger.warn(s"Unexpected event in gossipping broadcast $anything")
        }

    }
}
case class ProcMsgPair(src: KAddress, payload: KompicsEvent)

case object HistoryRequest extends KompicsEvent
//case class HistoryResponse(past: Set[ProcMsgPair]) extends KompicsEvent
case class HistoryResponse(past: Set[Any]) extends KompicsEvent

