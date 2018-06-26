package cn.ckxily.ckxc.lex

import cn.ckxily.ckxc.err.assertionFailed

internal class LexerStateMachine(private val srcCode: String) {
	private val lowerCaseLetter: String = "abcdefghijklmnopqrstuvwxyz"
	private val upperCaseLetter: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	private val number: String = "1234567890"
	private val symbols: String = ".,:;+-*/=<>{}[]()!&"

	private var index: Int = 0
	private var cachedTokens: MutableList<Token> = ArrayList()

	private fun lexImpl() {
		while (index < srcCode.length) {
			when (srcCode[index]) {
				in lowerCaseLetter -> cachedTokens.add(lexIdentifier())
				in upperCaseLetter -> cachedTokens.add(lexIdentifier())
				in number -> cachedTokens.add(lexNumber())
				in symbols -> cachedTokens.add(lexSymbol())
				in " \n\t" -> index++
				else -> error("character ${srcCode[index]} is not allowed")
			}
		}
		cachedTokens.add(Token(TokenType.EOI))
	}

	private fun lexIdentifier(): Token {
		assert(srcCode[index].isLetter())
		val idStr = lexFullString(lowerCaseLetter + upperCaseLetter + number)
		return idKwdMap[idStr]?.let { Token(it) } ?: Token(TokenType.Id, idStr)
	}

	private fun lexNumber(): Token {
		return Token(TokenType.Number, lexFullString(number).toInt())
	}

	private fun lexSymbol(): Token {
		val symbolStr: String = when (srcCode[index]) {
			in ".,:;+*/<>{}[]()&" -> run {
				++index
				srcCode[index-1].toString()
			}
			'-' -> run {
				if (index + 1 < srcCode.length && srcCode[index + 1] == '>') {
					index += 2
					"->"
				}
				else {
					index++
					"-"
				}
			}
			'=' -> run {
				if (index + 1 < srcCode.length && srcCode[index + 1] == '=') {
					index += 2
					"=="
				}
				else {
					index++
					"="
				}
			}
			'!' -> run {
				if (index + 1 < srcCode.length && srcCode[index + 1] == '=') {
					index += 2
					"!="
				}
				else {
					index++
					"!"
				}
			}
			else -> assertionFailed("No other characters allowed when lexing symbol") as String
		}
		return Token(idKwdMap[symbolStr]!!)
	}

	private fun lexFullString(allowedChars: CharSequence) = buildString {
		while (srcCode[index] in allowedChars) {
			append(srcCode[index])
			++index
		}
	}

	init {
		lexImpl()
	}

	val tokens get() = cachedTokens

	companion object {
		val idKwdMap: Map<String, TokenType> = TokenType.values().map { it.str to it }.toMap()
	}
}

class Lexer {
	fun lex(srcCode: String): List<Token> {
		return LexerStateMachine(srcCode).tokens
	}
}
