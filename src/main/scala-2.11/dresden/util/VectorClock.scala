package dresden.util

import dresden.networking.KAddress

case class VectorClock(var vc: Map[KAddress, Int]) {

    def inc(addr: KAddress) = {
        vc = vc + ((addr, vc.get(addr).get + 1))
    }

    def set(addr: KAddress, value: Int) = {
        vc = vc + ((addr, value))
    }

    def <=(that: VectorClock): Boolean = vc.foldLeft[Boolean](true)((leq, entry) => leq & (entry._2 <= that.vc.getOrElse(entry._1, entry._2)))

}

object VectorClock {

    def empty(topology: scala.Seq[KAddress]): VectorClock = {
        VectorClock(topology.foldLeft[Map[KAddress, Int]](Map[KAddress, Int]())((mp, addr) => mp + ((addr, 0))))
    }

    def apply(that: VectorClock): VectorClock = {
        VectorClock(that.vc)
    }

}