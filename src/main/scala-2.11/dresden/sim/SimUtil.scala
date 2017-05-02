package dresden.sim

import se.sics.kompics.KompicsEvent
import se.sics.kompics.timer.{SchedulePeriodicTimeout, Timeout}
import se.sics.ktoolbox.util.network.KAddress

object SimUtil {
    val RECV_STR: String = "-recv"
    val SEND_STR: String = "-sent"

    case class DresdenTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

    case class BroadcastPayload(from: KAddress, id: String) extends KompicsEvent {
        override def equals(o: Any) = o match {
            case that: BroadcastPayload => that.id.equals(this.id)
            case _ => false
        }
        override def hashCode = id.hashCode
    }
}
