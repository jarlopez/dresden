package dresden.components.broadcast

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports._
import se.sics.kompics.sl.{Init, _}
import se.sics.kompics.{KompicsEvent, ComponentDefinition => _, Port => _}
import se.sics.ktoolbox.util.network.KAddress

case class OriginatedData(src: KAddress, payload: KompicsEvent) extends KompicsEvent

class EagerReliableBroadcast(init: Init[EagerReliableBroadcast]) extends ComponentDefinition with StrictLogging {

    val rb = provides[ReliableBroadcast]

    val beb = requires[BestEffortBroadcast]

    val self: KAddress = init match {
        case Init(s: KAddress) => s
    }

    private var delivered = Set.empty[KompicsEvent]

    rb uponEvent {
        case RB_Broadcast(payload) => handle {
            logger.debug(s"$self RB_Broadcasting $payload")
            trigger(BEB_Broadcast(OriginatedData(self, payload)) -> beb)
        }
    }

    beb uponEvent {
        // BEB_Deliver.src should match origin
        case msg@BEB_Deliver(_, data@OriginatedData(origin, payload)) => handle {
            if (msg.src != origin) {
                logger.warn(s"Source ($msg.src) and origin($origin) do not match on $msg")
            } else {
                if (!delivered.contains(payload)) {
                    delivered = delivered + payload
                    logger.debug(s"$self RB_Delivering $payload")
                    trigger(RB_Deliver(origin, payload) -> rb)
                    trigger(BEB_Broadcast(data) -> beb)
                }
            }
        }
    }
}