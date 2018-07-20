package cn.ckxily.ckxc.lex

import cn.ckxily.ckxc.util.assertionFailed
import cn.ckxily.ckxc.util.unrecoverableError

internal class LexerStateMachine(private val srcCode: String) {
	private var index: Int = 0
	private var cachedTokens: MutableList<Token> = ArrayList()

	private fun lexImpl() {
		while (index < srcCode.length) {
			when (srcCode[index]) {
				'/' -> {
					if (index + 1 < srcCode.length) {
						if (srcCode[index + 1] == '/') {
							index += 2
							skipLineComment()
						}
						else if (srcCode[index + 1] == '*') {
							index += 2
							skipBlockComment()
						}
						else {
							cachedTokens.add(lexSymbol())
						}
					}
				}
				in lowerCaseLetter -> cachedTokens.add(lexIdentifier())
				in upperCaseLetter -> cachedTokens.add(lexIdentifier())
				in number -> cachedTokens.add(lexNumber())
				in symbols -> cachedTokens.add(lexSymbol())
				in " \n\t" -> index++
				else -> unrecoverableError("character ${srcCode[index]} is not allowed")
			}
		}
		cachedTokens.add(Token(TokenType.EOI))
	}

	private fun skipBlockComment() {
		while (index < srcCode.length) {
			if (srcCode[index] == '*') {
				if (index + 1 < srcCode.length && srcCode[index + 1] == '/') {
					index += 2
					return
				}
			}
			++index
		}
		unrecoverableError("Unterminated comment!")
	}

	private fun skipLineComment() {
		while (index < srcCode.length && srcCode[index] != '\n') ++index
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
			in ".,;+*/<>{}[]()&" -> {
				++index
				srcCode[index-1].toString()
			}
			'-' -> if (index + 1 < srcCode.length && srcCode[index + 1] == '>') {
				index += 2
				"->"
			}
			else {
				index++
				"-"
			}
			'=' -> if (index + 1 < srcCode.length && srcCode[index + 1] == '=') {
				index += 2
				"=="
			}
			else {
				index++
				"="
			}
			'!' -> if (index + 1 < srcCode.length && srcCode[index + 1] == '=') {
				index += 2
				"!="
			}
			else {
				index++
				"!"
			}
			':' -> if (index + 1 < srcCode.length && srcCode[index + 1] == ':') {
				index +=2
				"::"
			}
			else {
				index++
				":"
			}
			else -> assertionFailed("No other characters allowed when lexing symbol")
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
		private const val lowerCaseLetter: String = "abcdefghijklmnopqrstuvwxyz"
		private const val upperCaseLetter: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
		private const val number: String = "1234567890"
		private const val symbols: String = ".,:;+-*/=<>{}[]()!&"

		val idKwdMap: Map<String, TokenType> = TokenType.values().map { it.str to it }.toMap()
	}
}

class Lexer {
	fun lex(srcCode: String): List<Token> {
		return LexerStateMachine(srcCode).tokens
	}
}
