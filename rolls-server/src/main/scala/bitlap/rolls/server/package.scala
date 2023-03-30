package bitlap.rolls.server

import bitlap.rolls.compiler.plugin.ClassSchema
import bitlap.rolls.annotations.RollsRuntime
import com.sun.net.httpserver.HttpExchange

/** @author
 *    梦境迷离
 *  @version 1.0,2023/3/22
 */
def getParamsFromQuery(exchange: HttpExchange): Map[String, String] = {
  val items = exchange.getRequestURI.getQuery.split("&")
  items.map { kv =>
    kv.split("=")
  }.map(a => if (a.length == 2) Option(a(0) -> a(1)) else None)
    .collect { case Some(value) =>
      value
    }
    .toMap
}
def schemaAsJson(schema: ClassSchema): String =
  RollsRuntime.mapper.writeValueAsString(schema)
