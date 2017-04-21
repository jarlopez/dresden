package dresden.networking

import se.sics.kompics.network.{Header, Transport}

final case class NetHeader(src: KAddress, dst: KAddress, proto: Transport) extends Header[KAddress] {
    override def getDestination: KAddress = dst

    override def getProtocol: Transport = proto

    override def getSource: KAddress = src
}
