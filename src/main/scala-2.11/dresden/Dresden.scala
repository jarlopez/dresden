package dresden

import com.typesafe.scalalogging.StrictLogging
import dresden.sim.{Ping, Pong}
import se.sics.kompics.Start
import se.sics.kompics.network.{Network, Transport}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.croupier.event.CroupierSample
import se.sics.ktoolbox.util.network.basic.{BasicContentMsg, BasicHeader}
import se.sics.ktoolbox.util.network.{KAddress, KContentMsg, KHeader}

class Dresden(init: Init[Dresden]) extends ComponentDefinition with StrictLogging {
    val timer = requires[Timer]
    val network = requires[Network]
    val croupier = requires[CroupierPort]

    private val self = init match {
        case Init(s: KAddress) => s
    }

    ctrl uponEvent {
        case _: Start => handle {
            logger.info("Starting Dresden application")
        }
    }

    croupier uponEvent {
        case sample: CroupierSample[_] => handle {
            if (!sample.publicSample.isEmpty) {
                logger.info("Handling croupier sample")
                import scala.collection.JavaConversions._
                val samples = sample.publicSample.values().map {it => it.getSource }
                samples.foreach { peer: KAddress =>
                    val header = new BasicHeader[KAddress](self, peer, Transport.UDP)
                    val msg = new BasicContentMsg[KAddress, KHeader[KAddress], Ping](header, new Ping)
                    trigger(msg -> network)
                }
            } else {
                logger.debug("Empty croupier sample")
            }
        }
    }

    network uponEvent {
        case msg: BasicContentMsg[_, _, _] => handle {
            val that = msg
            val header = msg.getHeader
            val content = msg.getContent
            content match {
                case x: Ping =>
                    logger.info(s"A ping! $msg")
                    trigger(msg.answer(new Pong) -> network)
                case x: Pong =>
                    logger.info(s"A pong! $msg")
                case _ =>
                    logger.info("Unknown!")
            }
        }
    }
}
