package dresden

import com.typesafe.scalalogging.StrictLogging
import se.sics.kompics.Start
import se.sics.kompics.network.{Network, Transport}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.croupier.event.CroupierSample
import se.sics.ktoolbox.util.network.basic.{BasicContentMsg, BasicHeader}
import se.sics.ktoolbox.util.network.{KAddress, KContentMsg, KHeader}
import template.kth.app.test.{Ping, Pong}

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
            if (!sample.privateSample.isEmpty) {
                logger.info("Handling croupier sample")
                import scala.collection.JavaConversions._
                val samples = sample.privateSample.values().map {it => it.getSource }
                samples.foreach { peer: KAddress =>
//                    val header: KHeader[_] = new BasicHeader(self, peer, Transport.UDP)
//                    val msg: KContentMsg[_, KHeader[_], Ping] = new BasicContentMsg(header, new Ping())
                    val header = new BasicHeader[KAddress](self, peer, Transport.UDP)
                    val msg = new BasicContentMsg[KAddress, KHeader[KAddress], Ping](header, new Ping)
                    trigger(msg -> network)
                }
            }
        }
    }

    network uponEvent {
        // TODO Fix this
        case msg: KContentMsg[_, _, Ping] => handle {
            logger.info(s"Received ping from ${msg.getHeader.getSource}")
            trigger(msg.answer(new Pong()) -> network)
        }
        case msg: KContentMsg[_, KHeader[_], Pong] => handle {
            logger.info(s"Received pong from ${msg.getHeader.getSource}")
        }
    }
}
