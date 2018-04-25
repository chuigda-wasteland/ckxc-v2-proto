package cn.ckxily.ckxc.lex

internal class LexerStateMachine(private val srcCode: String) {
	private var index: Int = 0
	private var cachedTokens: MutableList<Token> = ArrayList()

	private fun lexImpl() {
		while (index < srcCode.length) {
			when (srcCode[index]) {
				in 'a'..'z' -> cachedTokens.add(lexIdentifier())
				in 'A'..'Z' -> cachedTokens.add(lexIdentifier())
				in '0'..'9' -> cachedTokens.add(lexNumber())
				in charArrayOf('.', ',', ':', '+') -> cachedTokens.add(lexSymbol())
				else -> null!!
			}
		}
		cachedTokens.add(Token(TokenType.EOI))
	}

	private fun lexIdentifier(): Token {
		assert((srcCode[index] in 'a'..'z') or (srcCode[index] in 'A'..'Z'))
		val idStr = lexFullString(('a'..'z').toString()
															+ ('A'..'Z').toString()
															+ ('0'..'9').toString()
															+ "_")
		val kwdType: TokenType? = idKwdMap.get(idStr)
		return if (kwdType != null) Token(kwdType) else Token(TokenType.Id, idStr)
	}

	private fun lexNumber(): Token {
		return Token(TokenType.Number, lexFullString(('0'..'9').toString()).toInt())
	}

	private fun lexSymbol(): Token {
		val symbolStr: String = when (srcCode[index]) {
			in "+*/:;,.<>{}[]()" -> srcCode[index].toString()
			'-' -> run {
				if (index+1 < srcCode.length && srcCode[index+1]=='>') {
					index += 2
					"->"
				}
				else {
					"-"
				}
			}
			'=' -> run {
				if (index+1 < srcCode.length && srcCode[index+1]=='=') {
					index += 2
					"=="
				}
				else {
					"="
				}
			}
			else -> null!!
		}

		return Token(idKwdMap.get(symbolStr)!!)
	}

	private fun lexFullString(allowedChars: CharSequence): String {
		val sb = StringBuilder()
		while (srcCode[index] in allowedChars) {
			sb.append(srcCode[index])
			++index
		}
		return sb.toString()
	}

	init {
		lexImpl()
	}

	val tokens get() = cachedTokens

	companion object {
		val idKwdMap: Map<String, TokenType> = TokenType.values().map { it.name to it }.toMap()
	}
}

class Lexer {
	fun lex(srcCode: String): List<Token> {
		return LexerStateMachine(srcCode).tokens
	}
}
