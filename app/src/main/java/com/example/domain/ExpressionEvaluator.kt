package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

/**
 * Pure offline recursive-descent mathematical expression evaluator for Standard and Scientific calculators.
 */
object ExpressionEvaluator {

    fun evaluate(rawExpression: String, useDegrees: Boolean = true): Result<Double> {
        val cleaned = rawExpression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", Math.PI.toString())
            .replace(" ", "")
            .trim()

        if (cleaned.isEmpty()) return Result.failure(IllegalArgumentException("Empty expression"))

        return try {
            val parser = Parser(cleaned, useDegrees)
            val value = parser.parse()
            if (value.isNaN() || value.isInfinite()) {
                Result.failure(ArithmeticException("Undefined result"))
            } else {
                Result.success(value)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val bd = BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
        val plain = bd.toPlainString()
        return if (plain.length > 14 && (abs(value) >= 1e10 || (abs(value) > 0 && abs(value) < 1e-6))) {
            String.format("%.6e", value)
        } else {
            if (plain == "-0") "0" else plain
        }
    }

    private class Parser(private val expr: String, private val useDegrees: Boolean) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expr.length) throw IllegalArgumentException("Unexpected character: ${expr[pos]}")
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parsePower()
            while (true) {
                when {
                    eat('*'.code) -> x *= parsePower()
                    eat('/'.code) -> {
                        val denom = parsePower()
                        if (denom == 0.0) throw ArithmeticException("Division by zero")
                        x /= denom
                    }
                    else -> return x
                }
            }
        }

        private fun parsePower(): Double {
            var x = parseUnary()
            if (eat('^'.code)) {
                x = x.pow(parsePower())
            }
            return x
        }

        private fun parseUnary(): Double {
            if (eat('+'.code)) return parseUnary()
            if (eat('-'.code)) return -parseUnary()
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var x = parseFactor()
            while (true) {
                x = when {
                    eat('%'.code) -> x / 100.0
                    eat('!'.code) -> factorial(x)
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            val startPos = pos
            var x: Double
            if (eat('('.code)) {
                x = parseExpression()
                if (!eat(')'.code)) throw IllegalArgumentException("Missing closing parenthesis")
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = expr.substring(startPos, pos).toDouble()
            } else if (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code || ch == '√'.code) {
                if (ch == '√'.code) {
                    nextChar()
                    return sqrt(parseUnary())
                }
                while (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code) nextChar()
                val name = expr.substring(startPos, pos).lowercase()
                if (name == "e") return Math.E
                if (name == "pi") return Math.PI
                val arg = if (eat('('.code)) {
                    val v = parseExpression()
                    if (!eat(')'.code)) throw IllegalArgumentException("Missing ')'")
                    v
                } else {
                    parseUnary()
                }
                x = applyFunc(name, arg)
            } else {
                throw IllegalArgumentException("Unexpected token")
            }
            return x
        }

        private fun toRad(v: Double): Double = if (useDegrees) Math.toRadians(v) else v
        private fun fromRad(v: Double): Double = if (useDegrees) Math.toDegrees(v) else v

        private fun applyFunc(name: String, v: Double): Double = when (name) {
            "sqrt" -> {
                if (v < 0) throw ArithmeticException("Negative sqrt")
                sqrt(v)
            }
            "sin" -> sin(toRad(v))
            "cos" -> cos(toRad(v))
            "tan" -> tan(toRad(v))
            "asin" -> fromRad(asin(v))
            "acos" -> fromRad(acos(v))
            "atan" -> fromRad(atan(v))
            "log" -> log10(v)
            "ln" -> ln(v)
            "abs" -> abs(v)
            "exp" -> exp(v)
            else -> throw IllegalArgumentException("Unknown function: $name")
        }

        private fun factorial(v: Double): Double {
            val n = v.toInt()
            if (v < 0 || v != n.toDouble() || n > 170) throw ArithmeticException("Invalid factorial")
            var res = 1.0
            for (i in 2..n) res *= i
            return res
        }
    }
}
