package cn.ckxily.ckxc.parser

import cn.ckxily.ckxc.ast.decl.ClassDecl
import cn.ckxily.ckxc.ast.decl.Decl
import cn.ckxily.ckxc.ast.decl.EnumDecl
import cn.ckxily.ckxc.ast.decl.VarDecl
import cn.ckxily.ckxc.lex.Token
import cn.ckxily.ckxc.lex.TokenType
import cn.ckxily.ckxc.sema.Sema

class ParserStateMachine(val tokens: List<Token>, val sema: Sema = Sema(), var currentTokenIndex: Int = 0) {
	fun ParseTransUnit() {
		while (currentToken().tokenType != TokenType.EOI) {
			when (currentToken().tokenType) {
				TokenType.Class -> parseClassDecl()
				TokenType.Enum -> parseEnumDecl()
				TokenType.Vi8, TokenType.Vi16, TokenType.Vi32, TokenType.Vi64 -> ParseTopLevelVarDecl()
				else -> error("Token ${currentToken()} not allowed at top level of program")
			}
		}
	}

	private fun ParseTopLevelVarDecl(): VarDecl {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	private fun parseEnumDecl(): EnumDecl {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	private fun parseClassDecl(): ClassDecl {
		assert(currentToken().tokenType == TokenType.Class)
		nextToken()

		expect(TokenType.Id)
		val name = currentToken().value!! as String

		val classDecl = sema.actOnClass(sema.currentScope, 0, name, true)
		parseClassFields(classDecl)
		return classDecl
	}

	private fun parseClassFields(classDecl: ClassDecl) {
		expectAndConsume(TokenType.LeftBrace)
		sema.actOnTagStartDefinition(sema.currentScope, classDecl)
		while (currentToken().tokenType != TokenType.RightBrace) {
			ParseDecl()
		}
		sema.actOnTagFinishDefinition()
	}

	private fun ParseDecl(): Decl {
		return null!!
	}

	private fun expect(tokenType: TokenType) {
		if (currentToken().tokenType != tokenType) {
			error("Expected ${tokenType.str} got ${currentToken()}")
		}
	}

	private fun expectAndConsume(tokenType: TokenType) {
		expect(tokenType)
		nextToken()
	}

	private fun currentToken() = tokens[currentTokenIndex]
	private fun peekOneToken() = tokens[currentTokenIndex+1]
	private fun nextToken() = currentTokenIndex++
}

class Parser {

}