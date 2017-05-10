package dresden.client

import java.util.UUID

import org.apache.commons.cli.{CommandLineParser, DefaultParser, Options}
import se.sics.kompics.Kompics
import se.sics.kompics.config.{Config, ValueMerger}
import se.sics.ktoolbox.util.network.KAddress

object Client {

    def main(args: Array[String]): Unit = {
        val opts: Options = prepareOptions
        val clientParser: CommandLineParser = new DefaultParser
        val cmd = clientParser.parse(opts, args)

        val cfg = Kompics.getConfig.asInstanceOf[Config.Impl]
        var self: KAddress = cfg.getValue("dresden.address", classOf[KAddress])
        val cb: Config.Builder = cfg.modify(UUID.randomUUID())

        cb.setValue("stormy.address", self)

        val cu = cb.finalise
        cfg.apply(cu, ValueMerger.NONE)
        Kompics.createAndStart(classOf[ClientManager])
        Kompics.waitForTermination()
    }

    private def prepareOptions: Options = {
        val opts = new Options
//        opts.addOption("b", true, "Set Bootstrap server to <arg> (ip:port)")
//        opts.addOption("p", true, "Change local port to <arg> (default from config file)")
//        opts.addOption("i", true, "Change local ip to <arg> (default from config file)")
        opts
    }
}