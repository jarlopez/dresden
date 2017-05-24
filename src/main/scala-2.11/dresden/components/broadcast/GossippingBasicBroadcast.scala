package dresden.components.broadcast


import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports._
import dresden.components.links.PP2PPayload
import se.sics.kompics.sl._
import se.sics.kompics.{KompicsEvent, Start}
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.croupier.event.CroupierSample
import se.sics.ktoolbox.util.network.KAddress


class GossippingBasicBroadcast(init: Init[GossippingBasicBroadcast]) extends ComponentDefinition with StrictLogging {


    type BasicSample = CroupierPort

    val gbeb = provides[BestEffortBroadcast]

    val pp2p = requires[PerfectLink]
    val bs = requires[BasicSample]

    val self = init match {
        case Init(s: KAddress) => s
    }

    private var past = Set.empty[Any]

    ctrl uponEvent {
        case _: Start => handle {
            logger.debug("Starting!")
        }
    }

    gbeb uponEvent {
        case x: BEB_Broadcast => handle {
            logger.debug(s"$self Broadcasting $x")
            past += ((self, x.payload))
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

            val unseen: Set[Any] = history.diff(past)

            logger.debug(s"Unseen: ${unseen} vs past: ${past.size}")
            for ((pp: KAddress, m: KompicsEvent) <- unseen) {
                logger.info(s"Sending $m")
                trigger(BEB_Deliver(pp, m) -> gbeb)
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

case class HistoryResponse(past: Set[Any]) extends KompicsEvent

