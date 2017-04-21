package dresden.components.broadcast

import java.util

import dresden.components.Ports.{GBEB_Broadcast, GossippingBestEffortBroadcast, PerfectLink}
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.ktoolbox.croupier.CroupierPort
import se.sics.ktoolbox.croupier.event.CroupierSample
import se.sics.ktoolbox.util.network.KAddress


class GossippingBasicBroadcast(init: Init[GossippingBasicBroadcast]) extends ComponentDefinition {
    type BasicSample = CroupierPort

    val gbeb = provides[GossippingBestEffortBroadcast]

    val network = requires[Network]
    val pp2p = requires[PerfectLink]
    val bs = requires[BasicSample]

    val self = init match {
        case Init(s: KAddress) => s
    }
    private var past = Set.empty[Any]

    gbeb uponEvent {
        case x: GBEB_Broadcast => handle {
            past += (self, x.payload)
        }
    }

    bs uponEvent {
        case croupierSample: CroupierSample[_] => handle {
            if (!croupierSample.publicSample.isEmpty) {
                val nodes = new util.LinkedList[KAddress]()

            }
        }
    }
}
