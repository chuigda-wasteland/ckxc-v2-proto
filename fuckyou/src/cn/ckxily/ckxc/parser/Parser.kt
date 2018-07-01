package cn.ckxily.ckxc.parser

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.type.*
import cn.ckxily.ckxc.err.error
import cn.ckxily.ckxc.lex.Token
import cn.ckxily.ckxc.lex.TokenType
import cn.ckxily.ckxc.sema.Sema

import kotlin.error as _aliased_error_

class ParserStateMachine(val tokens: List<Token>, val sema: Sema = Sema(), var currentTokenIndex: Int = 0) {
	fun parseTransUnit(): TransUnitDecl {
		while (currentToken().tokenType != TokenType.EOI) {
			val thisDecl = when (currentToken().tokenType) {
				TokenType.Class -> parseClassDecl()
				TokenType.Enum -> parseEnumDecl()
				TokenType.Vi8, TokenType.Vi16, TokenType.Vi32, TokenType.Vi64, TokenType.Vr32, TokenType.Id -> {
					val decl = parseVarDecl()
					expectAndConsume(TokenType.Semicolon)
					decl
				}
				TokenType.LeftParen -> error("Structural binding not allowed at top level of program")
				else -> error("Token ${currentToken()} not allowed at top level of program")
			}
			sema.actOnDeclInScope(thisDecl, sema.currentScope)
			sema.actOnDeclInContext(thisDecl, sema.currentDeclContext)
		}
		return sema.topLevelDeclContext as TransUnitDecl
	}

	private fun parseType(): Type {
		val ret = parseBuiltinType() ?: parseCustomType()
		return parsePostSpecifiers(ret)
	}

	private fun parsePostSpecifiers(ret: Type): Type {
		var modifiedType = ret
		while (true) {
			when (currentToken().tokenType) {
				TokenType.Const -> modifiedType.specifiers.isConst = true
				TokenType.Volatile -> modifiedType.specifiers.isVolatile = true
				TokenType.Mul -> modifiedType = PointerType(modifiedType, getNoSpecifier())
				TokenType.Amp -> modifiedType = ReferenceType(modifiedType, getNoSpecifier())
				else -> return modifiedType
			}
			nextToken()
		}
	}

	private fun parseCustomType(): Type {
		error("parseCustomType not implemented")
	}

	private fun parseBuiltinType(): Type? {
		val type = when (currentToken().tokenType) {
			TokenType.Vi8 -> BuiltinType(BuiltinTypeId.Int8, getNoSpecifier())
			TokenType.Vi16 -> BuiltinType(BuiltinTypeId.Int16, getNoSpecifier())
			TokenType.Vi32 -> BuiltinType(BuiltinTypeId.Int32, getNoSpecifier())
			TokenType.Vi64 -> BuiltinType(BuiltinTypeId.Int64, getNoSpecifier())
			TokenType.Vr32 -> BuiltinType(BuiltinTypeId.Float, getNoSpecifier())
			else -> return null
		}
		nextToken()
		return type
	}

	private fun parseEnumDecl(): EnumDecl {
		assert(currentToken().tokenType == TokenType.Enum)
		nextToken()

		expect(TokenType.Id)
		val name = currentToken().value!! as String
		nextToken()

		val enumDecl = sema.actOnEnum(sema.currentScope, name)
		parseEnumerators(enumDecl)
		return enumDecl
	}

	private fun parseEnumerators(enumDecl: EnumDecl) {
		expectAndConsume(TokenType.LeftBrace)
		sema.actOnTagStartDefinition()
		while (currentToken().tokenType != TokenType.RightBrace) {
			expect(TokenType.Id)
			val name = currentToken().value!! as String
			nextToken()
			expectAndConsume(TokenType.Eq)
			expect(TokenType.Number)
			val value = currentToken().value!! as Int
			nextToken()
			val enumerator = sema.actOnEnumerator(sema.currentScope, enumDecl, name, value)
			if (currentToken().tokenType == TokenType.Comma) {
				nextToken()
			}
		}
		expectAndConsume(TokenType.RightBrace)
		sema.actOnTagFinishDefinition()
	}

	private fun parseClassDecl(): ClassDecl {
		assert(currentToken().tokenType == TokenType.Class)
		nextToken()

		expect(TokenType.Id)
		val name = currentToken().value!! as String
		nextToken()

		val classDecl = sema.actOnClass(sema.currentScope, name)
		parseClassFields(classDecl)
		return classDecl
	}

	private fun parseClassFields(classDecl: ClassDecl) {
		expectAndConsume(TokenType.LeftBrace)
		sema.actOnTagStartDefinition()
		while (currentToken().tokenType != TokenType.RightBrace) {
			val decl = parseDecl()
			sema.actOnDeclInScope(decl)
			sema.actOnDeclInContext(decl, classDecl)
		}
		sema.actOnTagFinishDefinition()
		expectAndConsume(TokenType.RightBrace)
	}

	private fun parseDecl(): Decl =
			when (currentToken().tokenType) {
				TokenType.Vi8, TokenType.Vi16, TokenType.Vi32, TokenType.Vi64, TokenType.Vr32, TokenType.Id -> {
					val varDecl = parseVarDecl()
					expectAndConsume(TokenType.Semicolon)
					varDecl
				}
				TokenType.LeftParen -> {
					val structuralBinding = parseStructuralBinding()
					expectAndConsume(TokenType.Semicolon)
					structuralBinding
				}
				TokenType.Class -> parseClassDecl()
				TokenType.Enum -> parseEnumDecl()
				else -> error("Unexpected token ${currentToken()}, expected vi8, vi16, vi32, vi64, vr32, class, enum or id")
			}

	private fun parseStructuralBinding(): Decl {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	private fun parseVarDecl(): VarDecl {
		val type = parseType()
		expect(TokenType.Id)
		val name = currentToken().value as String
		nextToken()
		return sema.actOnVarDecl(sema.currentScope, name, type)
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
	private fun peekOneToken() = tokens[currentTokenIndex + 1]
	private fun nextToken() = currentTokenIndex++
}

class Parser {
	fun parse(tokens: List<Token>): TransUnitDecl {
		return ParserStateMachine(tokens).parseTransUnit()
	}
}
