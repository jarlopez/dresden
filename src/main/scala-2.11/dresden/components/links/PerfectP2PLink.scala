//package dresden.components.links
//
//import dresden.components.Ports.{PL_Deliver, PL_Send, PerfectLink}
//import dresden.networking.NetMessage
//import se.sics.kompics.network.Network
//import se.sics.kompics.sl._
//import se.sics.ktoolbox.util.network.KAddress
//
//object PerfectP2PLink {
//
//    val PerfectLinkMessage = NetMessage
//}
//
//class PerfectP2PLink(init: Init[PerfectP2PLink]) extends ComponentDefinition {
//    import PerfectP2PLink._
//
//    val pLink = provides[PerfectLink]
//    val network = requires[Network]
//
//    val self: KAddress = init match {
//        case Init(self: KAddress) => self
//    }
//
//    pLink uponEvent {
//        case PL_Send(dest, payload) => handle {
//            trigger(PerfectLinkMessage(self, dest, payload) -> network)
//        }
//    }
//
//    network uponEvent {
//        case PerfectLinkMessage(src, dest, payload) => handle {
//            trigger(PL_Deliver(src, payload) -> pLink)
//        }
//    }
//
//}