package dresden.sim


import scala.collection.{Set, concurrent}
import scala.collection.convert.decorateAsScala._
import java.util.concurrent.ConcurrentHashMap

object SimResults {

    val data: concurrent.Map[String, Any] = new ConcurrentHashMap[String, Any]().asScala

    def put(key: String, value: Any): Unit = {
        data.put(key, value)
    }

    def get(key: String): Option[Any] = {
        data.get(key)
    }

    def keys(): Set[String] = {
        data.keySet
    }

}
