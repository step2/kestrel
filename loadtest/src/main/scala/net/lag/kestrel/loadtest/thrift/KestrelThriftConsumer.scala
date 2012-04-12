package net.lag.kestrel.loadtest.thrift

import com.twitter.ostrich.stats.Stats
//import com.twitter.parrot.server.{ParrotRequest, ParrotService} // 0.4.6
import com.twitter.parrot.server.{ParrotRequest, ParrotThriftService} // 0.4.5
import java.nio.ByteBuffer
import net.lag.kestrel.loadtest.KestrelConsumerLoadTestConfig
import net.lag.kestrel.thrift.{Item, Kestrel}

// class KestrelThriftConsumer(parrotService: ParrotService[ParrotRequest, Array[Byte]]) // 0.4.6
class KestrelThriftConsumer(parrotService: ParrotThriftService)
extends AbstractKestrelThriftLoadTest[Seq[Item]](parrotService) with KestrelConsumerLoadTestConfig {
  var maxItemsPerRequest = 1

  Stats.incr("items_consumed", 0)

  lazy val commands = {
    log.info("generating consumer thrift commands")
    log.info(distribution.toString)

    distribution.map { segment =>
      if (timeout > 0) {
        (client: Kestrel.FinagledClient) => {
          client.get(segment.queueName, maxItemsPerRequest, timeout) onSuccess { items =>
            Stats.incr("items_consumed", items.size)
          }
        }
      } else {
        (client: Kestrel.FinagledClient) => {
          client.get(segment.queueName, maxItemsPerRequest) onSuccess { items =>
            Stats.incr("items_consumed", items.size)
          }
        }
      }
    }
  }
}
