package dresden

import com.typesafe.scalalogging.StrictLogging
import dresden.networking.NetAddress
import dresden.util.converters.NetAddressConverter
import se.sics.kompics.config.Conversions
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}
import se.sics.kompics.sl._
import se.sics.kompics.timer.java.JavaTimer
import se.sics.kompics.{Component, Kompics, Start}

object Dresden {

    Conversions.register(new NetAddressConverter())

    def main(args: Array[String]): Unit = {
        try {
            Kompics.createAndStart(classOf[Dresden])
            Kompics.waitForTermination()
        } catch {
            case ex: Throwable =>
                ex.printStackTrace()
        }
    }
}

class Dresden extends ComponentDefinition with StrictLogging {

    val self: NetAddress = cfg.getValue[NetAddress]("dresden.address")

    val timer: Component = create(classOf[JavaTimer], Init.NONE)
    val network: Component = create(classOf[NettyNetwork], new NettyInit(self))

    ctrl uponEvent {
        case _: Start => handle {
            logger.debug("Started Dresden")
        }
    }
}

