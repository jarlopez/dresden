package dresden.networking


import se.sics.ktoolbox.util.network.{KContentMsg, KHeader}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

abstract class MessageCheck[T: TypeTag] {
    type msgType = KContentMsg[_, KHeader[_], _]

    @unchecked
    def unapply(msg: msgType)(implicit tag: ClassTag[T]): Boolean = {
        msg.getContent match {
            case _: T => true
            case _ => false
        }
    }
}
