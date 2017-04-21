//package dresden.networking
//
//import se.sics.kompics.KompicsEvent
//import se.sics.kompics.network.{Msg, Transport}
//import se.sics.ktoolbox.util.network.{KAddress, KHeader}
//
//final case class NetMessage[C <: KompicsEvent](src: KAddress, dst:KAddress, payload: C) extends Msg[KAddress, KHeader] with Serializable {
//    val serialVersionUID:Long = -5669973156467202337L
//
//    def header: NetHeader = NetHeader(src, dst, Transport.TCP)
//
//    override def getDestination: KAddress = header.dst
//
//    override def getHeader: NetHeader = header
//
//    override def getProtocol: Transport = header.proto
//
//    override def getSource: KAddress = header.src
//
//    override def toString: String = super.toString
//
//}
