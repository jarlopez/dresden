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

    private var past = Set.empty[Any]

    val self = init match {
        case Init(s: KAddress) => s
    }

    gbeb uponEvent {
        case x: GBEB_Broadcast => handle {
            past += (self, x.payload)
        }
    }

    bs uponEvent {
        case croupierSample: CroupierSample[_] => handle {
            if (!croupierSample.publicSample.isEmpty) {
                val nodes = new util.LinkedList[KAddress]()
//                for (addr: KAddress <- croupierSample.publicSample.values()) {
//                    nodes.add(addr)
//                }

//                for (peer <- nodes) {
                    // TODO XXX
//                    val header = new BasicHeader[_ <: KAddress](self, peer, Transport.UDP)
//                    val msg = new BasicContentMsg[_ <: KAddress, _ <: KHeader[_ <: KAddress], _](header,)
//                    trigger(msg -> network)
//                }


                //                val sample = CroupierHelper.getSample(croupierSample)
                /*
    import scala.collection.JavaConversions._
for (e <- sample.publicSample.values)  { s.add(e.getSource)
}
    return s
                 */
//                import scala.collection.JavaConversions._
//                for (peer <- sample) {
//                    val header = new BasicHeader[_ <: KAddress](selfAdr, peer, Transport.UDP)
//                    val msg = new BasicContentMsg[_ <: KAddress, _ <: KHeader[_ <: KAddress], _](header, new Ping)
//                    trigger(msg, networkPort)
//                }
            }
        }
    }
}
