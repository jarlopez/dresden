package dresden.components.links

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.{Network, Transport}
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.basic.{BasicContentMsg, BasicHeader}
import se.sics.ktoolbox.util.network.{KAddress, KContentMsg, KHeader}

class PerfectP2PLink(init: Init[PerfectP2PLink]) extends ComponentDefinition with StrictLogging {

    val pLink = provides[PerfectLink]
    val network = requires[Network]

    val self: KAddress = init match {
        case Init(self: KAddress) => self
    }

    pLink uponEvent {
        case PL_Send(dest, payload) => handle {
            val header = new BasicHeader[KAddress](self, dest, Transport.TCP)
            val msg = new BasicContentMsg[KAddress, KHeader[KAddress], KompicsEvent](header, payload)
            trigger(msg -> network)
        }
    }

    network uponEvent {
        // TODO Figure out how to extract payload types
        case msg: BasicContentMsg[_, _, _] => handle {
            val src = msg.getSource
            val content = msg.getContent
            content match {
                case payload: KompicsEvent =>
                    trigger(PL_Deliver(src, payload) -> pLink)
            }
        }
    }

}