package com.example.jeffrey.basiccalculator

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.widget.GridLayout
import kotlinx.android.synthetic.main.content_main.*
//import java.math.BigDecimal

const val NONE = 0
const val SINGLE = 1
const val ALL = 2
const val MULTIPLICATION = 3
const val DIVISION = 4
const val ADDITION = 5
const val SUBTRACTION = 6

const val MAX_NUM_DIGITS = 8
const val ERROR_MESSAGE = "Error"
const val INFINITY_MESSAGE = "Infinity"
const val OUTPUT_OFFSET = "  "

class MainActivity : AppCompatActivity() {

    private lateinit var mainGrid: GridLayout
    private var num1 = ""
    private var num2 = ""
    private var operation = NONE
    private var pressedEquals = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainGrid = findViewById<GridLayout>(R.id.mainGrid)
        setSingleEvent(mainGrid)
    }

    private fun setSingleEvent(mainGrid: GridLayout) {
        for(i in 0 until mainGrid.childCount) {
            val cardView: CardView = mainGrid.getChildAt(i) as CardView
            cardView.setOnClickListener {
                val tag: String = cardView.tag.toString()
                when(tag) {
                    tag.toIntOrNull().toString(), "." -> addDigit(tag)
                    "C" -> deleteDigit(SINGLE)
                    "AC" -> deleteDigit(ALL)
                    "*" -> setOperation(MULTIPLICATION)
                    "/" -> setOperation(DIVISION)
                    "+" -> setOperation(ADDITION)
                    "-" -> setOperation(SUBTRACTION)
                    "+/-" -> changeSign()
                    "=" -> {
                        solve()
                        pressedEquals = true
                    }
                }
                updateDisplay()
            }
        }
    }

    private fun addDigit(digit: String) {
        if(num1 == ERROR_MESSAGE || num1 == INFINITY_MESSAGE || pressedEquals) {
            num1 = ""
        }
        pressedEquals = false
        if(operation == NONE && plainNum(num1).length < MAX_NUM_DIGITS && (digit != "." || !num1.contains("."))) {
            if(((num1.isNotEmpty() && num1 == "0") || (num1.length >= 2 && num1 == "-0")) && digit != ".") {
                num1 = num1.substring(0 until num1.length - 1) + digit
            } else if(num1.isEmpty() && digit == ".") {
                num1 = "0$digit"
            } else {
                num1 += digit
            }
        } else if(operation != NONE && plainNum(num2).length < MAX_NUM_DIGITS && (digit != "." || !num2.contains("."))) {
            if(((num2.isNotEmpty() && num2 == "0") || (num2.length >= 2 && num2 == "-0")) && digit != ".") {
                num2 = num2.substring(0 until num2.length - 1) + digit
            } else if(num2.isEmpty() && digit == ".") {
                num2 = "0$digit"
            } else {
                num2 += digit
            }
        }
    }

    private fun deleteDigit(numDigits: Int) {
        if(numDigits == SINGLE && num1 != ERROR_MESSAGE && !pressedEquals) {
            if(operation == NONE && plainNum(num1).isNotEmpty()) {
                num1 = num1.substring(0 until num1.length - 1)
                if(plainNum(num1).isEmpty()) {
                    num1 = ""
                }
            } else if(operation != NONE && plainNum(num2).isNotEmpty()) {
                num2 = num2.substring(0 until num2.length - 1)
                if(plainNum(num2).isEmpty()) {
                    num2 = "0"
                }
            }
        } else if(numDigits == ALL) {
            num1 = ""
            num2 = ""
            setOperation(NONE)
        }
    }

    private fun setOperation(operation: Int) {
        pressedEquals = false
        if(num2.isNotEmpty()) {
            solve()
        }
        if((num1 != ERROR_MESSAGE && num1 != INFINITY_MESSAGE) || operation == NONE) {
            this.operation = operation
            operationText.text = when(operation) {
                MULTIPLICATION -> "*$OUTPUT_OFFSET"
                DIVISION -> "/$OUTPUT_OFFSET"
                ADDITION -> "+$OUTPUT_OFFSET"
                SUBTRACTION -> "-$OUTPUT_OFFSET"
                else -> ""
            }
        }
    }

    private fun changeSign() {
        if(operation == NONE && num1.isNotEmpty() && num1 != ERROR_MESSAGE) {
            num1 = if(num1[0] == '-') {
                num1.substring(1 until num1.length)
            } else {
                "-$num1"
            }
        } else if(operation != NONE && num2.isNotEmpty()) {
            num2 = if(num2[0] == '-') {
                num2.substring(1 until num2.length)
            } else {
                "-$num2"
            }
        }
    }

    private fun solve() {
        fixNums()
        when(operation) {
            MULTIPLICATION -> multiplyNums()
            DIVISION -> divideNums()
            ADDITION -> addNums()
            SUBTRACTION -> subtractNums()
        }
        if(num1 == "-$INFINITY_MESSAGE") {
            num1 = INFINITY_MESSAGE
        }
        num2 = ""
        if(pressedEquals) {
            setOperation(NONE)
            pressedEquals = true
        } else {
            setOperation(NONE)
        }
    }

    private fun updateDisplay() {
        if(num2.isEmpty()) {
            outputText.text = if(num1.isEmpty()) {
                "0$OUTPUT_OFFSET"
            } else {
                "$num1$OUTPUT_OFFSET"
            }
        } else {
            outputText.text = "$num2$OUTPUT_OFFSET"
        }
    }

    private fun plainNum(numString: String): String {
        return numString.replace(".", "").replace("-", "")
    }

    private fun multiplyNums() {
        num1 = reformatOutput((num1.toFloat() * num2.toFloat()).toString()) // toDouble()
    }

    private fun divideNums() {
        num1 = if(num2 == "0" || plainNum(num2).replace("0", "") == "") {
            ERROR_MESSAGE
        } else {
            reformatOutput((num1.toFloat() / num2.toFloat()).toString()) // toDouble()
        }
    }

    private fun addNums() {
        num1 = reformatOutput((num1.toFloat() + num2.toFloat()).toString()) // toDouble()
    }

    private fun subtractNums() {
        num1 = reformatOutput((num1.toFloat() - num2.toFloat()).toString()) // toDouble()
    }

    private fun reformatOutput(output: String): String {
        return roundNum(doubleToInt(output))
    }

    private fun doubleToInt(num: String): String {
        if(num.substring(num.length - 2 until num.length) == ".0") {
            return num.substring(0 until num.length - 2)
        }
        return num
    }

    // should round output to be at most some MAX_NUM_DIGITS (includes E but not - or .) in length
    private fun roundNum(num: String): String {
        /*var newNum = num
        if(newNum.replace(".", "").replace("-", "").length > MAX_NUM_DIGITS) {
            var removalIndex = if(newNum.contains("E")) {
                newNum.indexOf("E")
            } else {
                newNum.length
            }
            var excess = if(newNum.substring(removalIndex - (newNum.replace(".", "").length - MAX_NUM_DIGITS) until removalIndex).contains(".") || newNum[removalIndex - (newNum.replace(".", "").length - MAX_NUM_DIGITS) - 1] == '.') {
                newNum.replace(".", "").replace("-", "").length - MAX_NUM_DIGITS + 1
            } else {
                newNum.replace(".", "").replace("-", "").length - MAX_NUM_DIGITS
            }
            var removedDigits = newNum.substring(removalIndex - excess until removalIndex).replace(".", "")
            newNum = newNum.substring(0 until removalIndex - excess) + if(removalIndex < newNum.length) {
                newNum.substring(removalIndex until newNum.length)
            } else {
                ""
            }
            if(removedDigits[0].toInt() >= 5) {
                val carry = if(newNum[0] == '-') {
                    -1
                } else {
                    1
                }
                newNum = if(newNum.contains("E")) {
                    (((newNum.substring(0 until removalIndex - excess).toBigDecimal() * Math.pow(10.0, removalIndex - excess - newNum.indexOf(".") - 1.0).toString().toBigDecimal()).toInt() + carry).toBigDecimal() / Math.pow(10.0, removalIndex - excess - newNum.indexOf(".") - 1.0).toString().toBigDecimal()).toString() + newNum.substring(removalIndex - excess until newNum.length)
                } else {
                    (newNum.substring(0 until removalIndex - excess).toInt() + carry).toString()
                }
                if(newNum.replace("-", "")[2] == '.') {
                    newNum = newNum[0] + "." + newNum.substring(1 until newNum.indexOf("E") + 1).replace(".", "") + (newNum.substring(newNum.indexOf("E") + 1 until newNum.length).toInt() + 1).toString()
                }
                if(newNum.length > MAX_NUM_DIGITS) {
                    newNum = roundNum(newNum)
                }
            }
            if(newNum.contains("E") && newNum.substring(newNum.indexOf("E") + 1 until newNum.length).toInt() >= 100) {
                newNum = INFINITY_MESSAGE
            }
        }
        return newNum*/
        return num
    }

    private fun fixNums() {
        if(plainNum(num1).isEmpty() || plainNum(num1) == "0") {
            num1 = "0"
        } else if((num1.isNotEmpty() && num1[0] == '.') || (num1.length >= 2 && num1.substring(0..1) == "-.")) {
            num1 = num1.substring(0 until num1.indexOf(".")) + "0" + num1.substring(num1.indexOf(".") until num1.length)
        }
        if(num2.isEmpty()) {
            num2 = num1
        } else if(plainNum(num2).isEmpty() || plainNum(num2) == "0") {
            num2 = "0"
        } else if((num2.isNotEmpty() && num2[0] == '.') || (num2.length >= 2 && num2.substring(0..1) == "-.")) {
            num2 = num2.substring(0 until num2.indexOf(".")) + "0" + num2.substring(num2.indexOf(".") until num2.length)
        }
    }
}
