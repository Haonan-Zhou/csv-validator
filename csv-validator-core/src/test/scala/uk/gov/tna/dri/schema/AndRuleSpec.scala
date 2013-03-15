package uk.gov.tna.dri.schema

import org.specs2.mutable.Specification
import uk.gov.tna.dri.metadata.{Cell, Row}
import scalaz.{Failure, Success}

class AndRuleSpec extends Specification {

  "AndRule" should {
    "fail when left rule only validates" in {
      val globalDirectives = List(TotalColumns(1))
      val schema = Schema(globalDirectives, List(ColumnDefinition("Country")))

      val leftInRule = InRule(Literal(Some("Germany")))
      val rightInRule = InRule(Literal(Some("France")))

      val andRule = AndRule(leftInRule, rightInRule)

      andRule.evaluate(0, Row(List(Cell("Germany")), 1), schema) must beLike {
        case Failure(messages) => messages.list mustEqual List("""in("Germany") and in("France") fails for line: 1, column: Country, value: "Germany"""")
      }
    }

    "fail when left rule only validates" in {
      val globalDirectives = List(TotalColumns(1))
      val schema = Schema(globalDirectives, List(ColumnDefinition("Country")))

      val leftInRule = InRule(Literal(Some("Germany")))
      val rightInRule = InRule(Literal(Some("France")))

      val andRule = AndRule(leftInRule, rightInRule)

      andRule.evaluate(0, Row(List(Cell("France")), 1), schema) must beLike {
        case Failure(messages) => messages.list mustEqual List("""in("Germany") and in("France") fails for line: 1, column: Country, value: "France"""")
      }
    }

    "fail when left/right rules are both invalid" in {
      val globalDirectives = List(TotalColumns(1))
      val schema = Schema(globalDirectives, List(ColumnDefinition("ThisOrThat")))

      val leftInRule = InRule(Literal(Some("This")))
      val rightInRule = InRule(Literal(Some("That")))

      val andRule = AndRule(leftInRule, rightInRule)

      andRule.evaluate(0, Row(List(Cell("SomethingElse")), 1), schema) must beLike {
        case Failure(messages) => messages.list mustEqual List("""in("This") and in("That") fails for line: 1, column: ThisOrThat, value: "SomethingElse"""")
      }
    }

    "succeed when both left and right match" in {
      val globalDirectives = List(TotalColumns(1))
      val schema = Schema(globalDirectives, List(ColumnDefinition("Country")))

      val leftInRule = InRule(Literal(Some("UK")))
      val rightInRule = InRule(Literal(Some("UK")))

      val andRule = AndRule(leftInRule, rightInRule)

      andRule.evaluate(0, Row(List(Cell("UK")), 1), schema) mustEqual Success(true)
    }

    "succeed when both left and parentheses right match" in {
      val globalDirectives = List(TotalColumns(1))
      val schema = Schema(globalDirectives, List(ColumnDefinition("Country")))

      val pLeftInRule = IsRule(Literal(Some("UK")))
      val pRightInRule = IsRule(Literal(Some("UK")))

      val leftInRule = IsRule(Literal(Some("UK")))
      val rightInRule = ParenthesesRule(pLeftInRule :: pRightInRule :: Nil )

      val andRule = AndRule(leftInRule, rightInRule)

      andRule.evaluate(0, Row(List(Cell("UK")), 1), schema) mustEqual Success(List(true,true))
    }

    "succeed when both left and parentheses right match" in {
      val globalDirectives = List(TotalColumns(1))
      val schema = Schema(globalDirectives, List(ColumnDefinition("Country")))

      val pLeftInRule = IsRule(Literal(Some("UK")))
      val pRightInRule = IsRule(Literal(Some("UK1")))

      val leftInRule = IsRule(Literal(Some("UK")))
      val rightInRule = ParenthesesRule(pLeftInRule :: pRightInRule :: Nil )

      val andRule = AndRule(leftInRule, rightInRule)

      andRule.evaluate(0, Row(List(Cell("SomethingElse")), 1), schema) must beLike {
        case Failure(messages) => messages.list mustEqual List("""is("UK") and (is("UK") is("UK1")) fails for line: 1, column: Country, value: "SomethingElse"""")
      }
    }
  }
}