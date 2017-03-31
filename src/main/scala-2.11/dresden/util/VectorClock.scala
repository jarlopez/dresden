package dresden.util

import dresden.networking.NetAddress

case class VectorClock(var vc: Map[NetAddress, Int]) {

    def inc(addr: NetAddress) = {
        vc = vc + ((addr, vc.get(addr).get + 1))
    }

    def set(addr: NetAddress, value: Int) = {
        vc = vc + ((addr, value))
    }

    def <=(that: VectorClock): Boolean = vc.foldLeft[Boolean](true)((leq, entry) => leq & (entry._2 <= that.vc.getOrElse(entry._1, entry._2)))

}

object VectorClock {

    def empty(topology: scala.Seq[NetAddress]): VectorClock = {
        VectorClock(topology.foldLeft[Map[NetAddress, Int]](Map[NetAddress, Int]())((mp, addr) => mp + ((addr, 0))))
    }

    def apply(that: VectorClock): VectorClock = {
        VectorClock(that.vc)
    }

}