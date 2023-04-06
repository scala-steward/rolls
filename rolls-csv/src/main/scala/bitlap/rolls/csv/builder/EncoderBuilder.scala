package bitlap.rolls.csv.builder

import bitlap.rolls.csv.*
import bitlap.rolls.csv.internal.*

import scala.compiletime.*
import scala.deriving.*

/** @author
 *    梦境迷离
 *  @version 1.0,2023/4/5
 */
trait EncoderBuilder[
  SpecificBuilder[_, _ <: Tuple, _ <: Tuple],
  From,
  FromSubs <: Tuple,
  DerivedFromSubs <: Tuple
]:
  private[csv] val computes: Map[FieldName, Any => String]

  final transparent inline def withFieldComputed[Field](
    inline selector: From => Field,
    f: Field => String
  ) = {
    val selectedField   = BuilderMacros.selectedField(selector)
    val computedField   = FieldName(selectedField) -> f.asInstanceOf[Any => String]
    val modifiedBuilder = this.construct[DerivedFromSubs](computes + computedField)
    BuilderMacros.dropCompletionField(modifiedBuilder, selector)
  }

  final inline def build(using csvFormat: CsvFormat): Encoder[From] =
    summonFrom {
      case fromMirror: Mirror.ProductOf[From] =>
        (from: From) => {
          val encoders = Derivation.encodersForAllFields[DerivedFromSubs]
          Construct.constructCSV(from.asInstanceOf[Product]) { (labelsToValuesOfFrom, label) =>
            val fieldValue                                  = labelsToValuesOfFrom(label)
            lazy val maybeValueFromDerived: Option[String]  = encoders.get(label).map(_.encode(fieldValue))
            lazy val maybeValueFromComputed: Option[String] = computes.get(label).map(f => f(fieldValue))
            maybeValueFromDerived.orElse(maybeValueFromComputed).getOrElse(fieldValue.toString)
          }
        }
      case _ => throw new Exception("Encoder Only support case classes!")
    }

  def construct[DerivedFromSubs <: Tuple](
    computes: Map[FieldName, Any => String] = this.computes
  ): SpecificBuilder[From, FromSubs, DerivedFromSubs]

end EncoderBuilder