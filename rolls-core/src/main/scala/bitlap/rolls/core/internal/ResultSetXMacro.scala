package bitlap.rolls.core.internal

import java.sql.ResultSet

import scala.compiletime.*
import scala.deriving.Mirror
import scala.quoted.*

import bitlap.rolls.core.jdbc.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/4/8
 */
object ResultSetXMacro {

  def resultSetXImpl[T <: TypeRow: Type](fetchInput: Expr[FetchInput])(using quotes: Quotes): Expr[ResultSetX] =
    import quotes.reflect.*
    def error = report.errorAndAbort(
      s"Cannot derive ResultSetX for ${TypeRepr.of[T].show}. Only case classes are supported."
    )
    Expr.summon[Mirror.ProductOf[T]].getOrElse(error) match
      case '{ $m: Mirror.ProductOf[T] { type MirroredElemTypes = types } } =>
        '{
          val stat      = $fetchInput._1
          val resultSet = $fetchInput._2
          new ResultSetX:
            override type RowType = T
            override def fetch(typeMappingFunc: TypeMappingArgs => TypeRow): LazyList[TypeRow] =
              val columnSize = resultSet.getMetaData.getColumnCount
              val result     = _root_.scala.collection.immutable.LazyList.newBuilder[TypeRow]
              while (resultSet.next()) {
                val row = typeMappingFunc(TypeMappingArgs(resultSet, columnSize))
                result.addOne(row)
              }
              if (!resultSet.isClosed()) resultSet.close()
              if (!stat.isClosed()) stat.close()
              result.result()
        }
}
