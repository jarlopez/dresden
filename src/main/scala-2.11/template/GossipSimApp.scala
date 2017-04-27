package template

import com.typesafe.scalalogging.StrictLogging
import dresden.networking.{PingMessage, PongMessage}
import se.sics.kompics.network.{Network, Transport}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.{KompicsEvent, Start}
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.croupier.event.CroupierSample
import se.sics.ktoolbox.util.identifiable.Identifier
import se.sics.ktoolbox.util.network.{KAddress, KContentMsg, KHeader}
import se.sics.ktoolbox.util.network.basic.{BasicContentMsg, BasicHeader}

import scala.reflect.ClassTag


object GossipSimApp {

    case class Init(selfAdr: KAddress, gradientOId: Identifier) extends se.sics.kompics.Init[GossipSimApp]

    case class Ping() extends KompicsEvent

    case class Pong() extends KompicsEvent

}

class GossipSimApp(val init: GossipSimApp.Init) extends ComponentDefinition with StrictLogging {
    import GossipSimApp.{Ping, Pong}

    val selfAdr = init match {
        case GossipSimApp.Init(self, gradientOid) => self
    }

    val timerPort = requires[Timer]
    val networkPort = requires[Network]
    val croupierPort = requires[CroupierPort]

    ctrl uponEvent {
        case _: Start => handle {
            logger.info(s"$selfAdr starting...")
        }
    }

    croupierPort uponEvent {
        case sample: CroupierSample[_] => handle {
            if (!sample.publicSample.isEmpty) {
                logger.info("Handling croupier sample")
                import scala.collection.JavaConversions._
                val samples = sample.publicSample.values().map { it => it.getSource }
                samples.foreach { peer: KAddress =>
                    val header = new BasicHeader[KAddress](selfAdr, peer, Transport.UDP)
                    val msg = new BasicContentMsg[KAddress, KHeader[KAddress], Ping](header, new Ping)
                    trigger(msg -> networkPort)
                }
            } else {
                logger.debug("Empty croupier sample")
            }
        }
    }

    networkPort uponEvent {
        case msg@PingMessage() => handle {
            logger.info(s"$selfAdr received special ping ${msg.getContent} from ${msg.getHeader.getSource}")
            trigger(msg.answer(new Pong), networkPort)
        }
        case msg@PongMessage() => handle {
            logger.info(s"$selfAdr received special pong from from ${msg.getHeader.getSource}")
        }

    }
}
