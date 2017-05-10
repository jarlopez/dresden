package dresden.client

import com.typesafe.scalalogging.StrictLogging
import se.sics.kompics.network.Network
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}
import se.sics.kompics.sl.{ComponentDefinition, Init}
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer
import se.sics.ktoolbox.util.network.KAddress

class ClientManager extends ComponentDefinition with StrictLogging {
    val self: KAddress = config.getValue("stormy.address", classOf[KAddress])
    val timer = create(classOf[JavaTimer], Init.NONE)
    val network = create(classOf[NettyNetwork], new NettyInit(self))
    val client = create(classOf[ClientService], Init.NONE)

    connect[Timer](timer -> client)
    connect[Network](network -> client)
}
