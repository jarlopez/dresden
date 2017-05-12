package dresden.sim

import se.sics.kompics.KompicsEvent
import se.sics.kompics.timer.{SchedulePeriodicTimeout, Timeout}
import se.sics.ktoolbox.util.network.KAddress

object SimUtil {
    val RECV_STR: String = "-recv"
    val SEND_STR: String = "-sent"
    val CAUSAL_STR: String = "-caused"
    val GSET_STR: String = "-gset"
    val TWOPSET_STR: String = "-twopset"
    val ORSET_STR: String = "-orset"
    val TWOPTWOPGRAPH_STR: String = "-orset"

    val DELIM_STR: String = "::"

    val CRDT_SET_KEY: String = "test"

    case class DresdenTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

    case class BroadcastPayload(from: KAddress, id: String) extends KompicsEvent {
        override def equals(o: Any) = o match {
            case that: BroadcastPayload => that.id.equals(this.id)
            case _ => false
        }

        override def hashCode = id.hashCode
    }

    def genPeerToIdStr(peer: KAddress, id: String): String = genPeerToIdStr(peer.toString, id)
    def genPeerToIdStr(peer: String, id: String): String = concat(peer, id)
    def getPeerAndId(peerIdStr: String): Array[String] = {
        val parts = split(peerIdStr)
        assert(parts.length == 2)
        parts
    }

    def concat(args: String*): String = args.mkString(DELIM_STR)
    def split(it: String): Array[String] = it.split(DELIM_STR)
}
