package dresden.components.links

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.{Network, Transport}
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.basic.{BasicContentMsg, BasicHeader}
import se.sics.ktoolbox.util.network.{KAddress, KHeader}

case class PP2PPayload(data: KompicsEvent) extends KompicsEvent

class PerfectP2PLink(init: Init[PerfectP2PLink]) extends ComponentDefinition with StrictLogging {

    val pLink = provides[PerfectLink]

    val network = requires[Network]

    val self: KAddress = init match {
        case Init(self: KAddress) =>
            logger.info(s"Initializing as $self")
            self
    }

    pLink uponEvent {
        case PL_Send(dest, payload: PP2PPayload) => handle {
            val header = new BasicHeader[KAddress](self, dest, Transport.TCP)
            val msg = new BasicContentMsg[KAddress, KHeader[KAddress], PP2PPayload](header, payload)
            trigger(msg -> network)
        }
        case anything => handle {
            logger.debug(s"$self Received unexpected pLink message $anything")
        }
    }

    network uponEvent {
        case msg: BasicContentMsg[_, _, _] => handle {
            val src = msg.getSource
            val content = msg.getContent
            content match {
                case payload: PP2PPayload =>
                    logger.debug(s"$self Received Kompics Event on pLink $payload")
                    trigger(PL_Deliver(src, payload.data) -> pLink)
                case anything => // TODO Figure out how to extract payload types
                // logger.warn(s"$self Unexpected payload $content")
            }
        }
    }

}