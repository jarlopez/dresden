package dresden.client

import com.google.common.util.concurrent.SettableFuture
import com.typesafe.scalalogging.StrictLogging
import se.sics.kompics.Start
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.{SchedulePeriodicTimeout, Timeout, Timer}
import se.sics.ktoolbox.util.network.KAddress


class ClientService extends ComponentDefinition with StrictLogging {
    var network = requires[Network]
    var timer = requires[Timer]

    val self: KAddress = config.getValue("dresden.address", classOf[KAddress])

//    private val pending = new java.util.TreeMap[String, SettableFuture[OperationResponse]]()
//    private var connected: Option[Ack] = None


    ctrl uponEvent {
        case _: Start => handle {
            logger.info(s"Starting client at $self")
//            val timeout: Long = cfg.getValue[Long]("stormy.keepAlivePeriod") * 2
//            val spt = new SchedulePeriodicTimeout(timeout, timeout)
//            spt.setTimeoutEvent(ConnectTimeout(spt))
//            logger.debug(s"Setting up with timeout ID ${spt.getTimeoutEvent.getTimeoutId}")
//            trigger(NetMessage(self, coordinator, Connect(spt.getTimeoutEvent.getTimeoutId.toString)) -> network)
//            trigger(spt -> timer)
        }
    }

    loopbck uponEvent {
//        case ctx@OpWithFuture(op: Operation) => handle {
//            val msg: RouteMessage = RouteMessage(op.key, op) // don't know which partition is responsible, so ask the bootstrap server to forward it
//            trigger(NetMessage(self, coordinator, msg) -> network)
//            pending.put(op.id, ctx.sf)
//        }
        case _ => handle {
            // TODO
        }
    }

    network uponEvent {
        case _ => handle {
            // TODO
        }
//        case NetMessage(source, self, ack: Ack) => handle {
//            logger.info(s"Client connected to $source, cluster size is ${ack.clusterSize}")
//            connected = Some(ack)
//            val console: Console = new Console(ClientService.this)
//            val th: Thread = new Thread(console)
//            th.start()
//        }
//        case NetMessage(source, self, response: OperationResponse) => handle {
//            logger.debug(s"Received response $response")
//            val sf: Option[SettableFuture[OperationResponse]] = Some(pending.remove(response.id))
//            sf match {
//                // TODO Confirm majority in coordinator's Router to avoid these checks
//                case None =>        // Ignore
//                case Some(null) =>  // Ignore
//                case Some(value) => value.set(response)
//            }
//        }
    }

    timer uponEvent {
        case _ => handle {
            // TODO
        }
//        case ev: ConnectTimeout => handle {
//            logger.debug(s"Received event: $ev\n${connected.get}\n${connected.get.id}\n${ev.getTimeoutId}")
//            connected match {
//                case Some(ack: Ack) =>
//                    if (!ack.id.equals(ev.getTimeoutId.toString)) {
//                        logger.error("Received wrong response ID earlier! System may be inconsistent. Shutting down")
//                        System.exit(1)
//                    }
//                case None =>
//                    logger.error(s"Connection to server $coordinator did not succeed. Shutting down.")
//                    Kompics.asyncShutdown()
//            }
//        }
    }

    private def op(key: String) = {
//        val op = GetOperation(key, UUID.randomUUID().toString, self)
//        val owf = OpWithFuture(op)
//        trigger(owf, onSelf)
//        owf.sf
    }
//
//    private[kvstore] def op(cmdline: Array[String]): SettableFuture[OperationResponse] = {
//        val key = cmdline(1)
//        var op: Option[Operation] = None
//        cmdline(0).toLowerCase match {
//            case "status" =>
//                op = Some(StatusRequest(key, UUID.randomUUID().toString, self))
//            case "put" =>
//                val value = cmdline(2)
//                op = Some(PutOperation(key, value, UUID.randomUUID().toString, self))
//            case "get" =>
//                op = Some(GetOperation(key, UUID.randomUUID().toString, self))
//            case "cas" =>
//                val refValue = cmdline(2)
//                val newValue = cmdline(3)
//                op = Some(CASOperation(key, refValue, newValue, UUID.randomUUID().toString, self))
//
//        }
//        val owf = OpWithFuture(op.get)
//        trigger(owf, onSelf)
//        owf.sf
//    }


}

case class ConnectTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

//case class OpWithFuture(op: Operation) extends KompicsEvent {
//    val sf: SettableFuture[OperationResponse] = SettableFuture.create[OperationResponse]
//}
