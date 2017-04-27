package dresden

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{GBEB_Broadcast, GossippingBestEffortBroadcast}
import se.sics.kompics.Start
import se.sics.kompics.network.{Network, Transport}
import se.sics.kompics.sl._
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timeout, Timer}
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.croupier.event.CroupierSample
import se.sics.ktoolbox.util.network.basic.{BasicContentMsg, BasicHeader}
import se.sics.ktoolbox.util.network.{KAddress, KHeader}

class Dresden(init: Init[Dresden]) extends ComponentDefinition with StrictLogging {
    val timer = requires[Timer]
    val network = requires[Network]
    val croupier = requires[CroupierPort]
    val gossip = requires[GossippingBestEffortBroadcast]

    private var timerId: Option[UUID] = None
    private val period: Long = 5000 // TODO

    private val self = init match {
        case Init(s: KAddress) => s
    }

    ctrl uponEvent {
        case _: Start => handle {
            logger.info("Starting Dresden application")
            val spt = new SchedulePeriodicTimeout(0, period)
            val timeout = DresdenTimeout(spt)
            spt.setTimeoutEvent(timeout)
            trigger(spt -> timer)
            timerId = Some(timeout.getTimeoutId)
        }
    }

    timer uponEvent {
        case DresdenTimeout(_) => handle {
        }
    }

    croupier uponEvent {
        case sample: CroupierSample[_] => handle {
            if (!sample.publicSample.isEmpty) {
                logger.info("Handling croupier sample")
                import scala.collection.JavaConversions._
                val samples = sample.publicSample.values().map { it => it.getSource }
//                samples.foreach { peer: KAddress =>
//                    val header = new BasicHeader[KAddress](self, peer, Transport.UDP)
//                    val msg = new BasicContentMsg[KAddress, KHeader[KAddress], Ping](header, new Ping)
//                    trigger(msg -> network)
//                }
            } else {
                logger.debug("Empty croupier sample")
            }
        }
    }

//    network uponEvent {
//        // TODO Figure out how to 'extract' payload types without type erasure
//        case msg: BasicContentMsg[_, _, _] => handle {
//            val content = msg.getContent
//            content match {
//                case _: Ping =>
//                    logger.info(s"A ping! $msg")
//                    trigger(msg.answer(new Pong) -> network)
//                case _: Pong =>
//                    logger.info(s"A pong! $msg")
//                case _ => // Ignore
//            }
//        }
//    }

    override def tearDown(): Unit = {
        timerId match {
            case Some(id) =>
                trigger(new CancelPeriodicTimeout(id) -> timer)
            case None => // Nothing to do
        }
    }
}

case class DresdenTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)
