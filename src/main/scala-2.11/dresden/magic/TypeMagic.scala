package dresden.magic

import scala.reflect.runtime.universe._

/* Freely taken from http://www.cakesolutions.net/teamblogs/ways-to-pattern-match-generic-types-in-scala */

trait TypeTaggedTrait[Self] { self: Self =>
    val selfTypeTag: TypeTag[Self]

    def hasType[Other: TypeTag]: Boolean =
        typeOf[Other] =:= selfTypeTag.tpe

    def cast[Other: TypeTag]: Option[Other] =
        if (hasType[Other])
            Some(this.asInstanceOf[Other])
        else
            None
}

abstract class TypeTagged[Self: TypeTag] extends TypeTaggedTrait[Self] { self: Self =>
    val selfTypeTag: TypeTag[Self] = typeTag[Self]
}

class TypeTaggedExtractor[T: TypeTag] {
    def unapply(a: Any): Option[T] = a match {
        case t: TypeTaggedTrait[_] => t.cast[T]
        case _ => None
    }
}
