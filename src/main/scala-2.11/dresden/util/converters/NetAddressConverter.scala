//package dresden.util.converters
//
//import java.net.{InetAddress, InetSocketAddress}
//import java.util
//
//import com.typesafe.scalalogging.LazyLogging
//import dresden.networking.KAddress
//import se.sics.kompics.config.{Conversions, Converter}
//
///**
//  * Custom Java-esque converter for parsing application configurations with network addresses
//  * into TAddress types.
//  *
//  * To register:
//  *     Conversions.register(TAddressConverter);
//  */
//class NetAddressConverter extends Converter[KAddress] with LazyLogging {
//
//    override def convert(o: Object): KAddress = {
//        o match {
//            case m: util.Map[String, Any]@unchecked => {
//                try {
//                    val hostname = Conversions.convert(m.get("host"), classOf[String])
//                    val port = Conversions.convert(m.get("port"), classOf[Integer])
//                    val ip = InetAddress.getByName(hostname)
//                    return KAddress(new InetSocketAddress(ip, port))
//                } catch {
//                    case ex: Throwable =>
//                        logger.error(s"Could not convert $m to TAddress", ex)
//                        return null
//                }
//            }
//            case s: String => {
//                try {
//                    val ipPort = s.split(":")
//                    val ip = InetAddress.getByName(ipPort(0))
//                    val port = Integer.parseInt(ipPort(1))
//                    return KAddress(new InetSocketAddress(ip, port))
//                } catch {
//                    case ex: Throwable =>
//                        logger.error(s"Could not convert '$s' to TAddress", ex)
//                        return null
//                }
//            }
//        }
//        logger.warn(s"Could not convert $o to TAddress")
//        null
//    }
//
//    override def `type`(): Class[KAddress] = {
//        classOf[KAddress]
//    }
//
//}