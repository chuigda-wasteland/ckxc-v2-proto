package cn.ckxily.ckxc.parser

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.stmt.*
import cn.ckxily.ckxc.ast.type.*
import cn.ckxily.ckxc.err.unrecoverableError
import cn.ckxily.ckxc.lex.Token
import cn.ckxily.ckxc.lex.TokenType
import cn.ckxily.ckxc.sema.Sema

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
				TokenType.Func -> {
					val decl = parseFuncDecl()
					if (decl.funcBody == null) {
						expectAndConsume(TokenType.Semicolon)
					}
					decl
				}
				TokenType.LeftParen -> unrecoverableError("Structual binding not allowed at top level of program")
				else -> unrecoverableError("Token ${currentToken()} not allowed at top level of program")
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
		unrecoverableError("parseCustomType not implemented")
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
				else -> unrecoverableError("Unexpected token ${currentToken()}, expected vi8, vi16, vi32, vi64, vr32, class, enum or id")
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

	private fun parseFuncDecl(): FuncDecl {
		/// @todo here is a dirty implementation.
		/// I will give as much comment as possible to illustrate its behaviour.
		/// Remember to refactor this.
		assert(currentToken().tokenType == TokenType.Func)
		nextToken()
		expect(TokenType.Id)
		val name = currentToken().value as String
		nextToken()
		val funcDecl =  sema.actOnFuncDecl(sema.currentScope, name)
		expectAndConsume(TokenType.LeftParen)
		/// @todo
		/// Now we enter a "param list scope" which is used for parameters.
		sema.actOnStartParamList(sema.currentScope, funcDecl)
		while (currentToken().tokenType != TokenType.RightParen) {
			val type = parseType()
			expect(TokenType.Id)
			val paramName = currentToken().value as String
			nextToken()
			val paramDecl = sema.actOnParam(sema.currentScope, funcDecl, paramName, type)
			/// add the parameter declaration into "param list scope"
			/// @todo it is suggested that there should be an individual ASTNode type for parameter since it "accepts"
			/// @todo an `ASTConsumer` in an different way
			sema.actOnDeclInScope(paramDecl)
			if (currentToken().tokenType == TokenType.Comma) {
				nextToken()
			}
		}
		expectAndConsume(TokenType.RightParen)
		/// @todo
		/// If the following item is '{', then this declaration will be a definition. We do not quit the "param list scope"
		/// at once so that we can access items in the param scope. This is quite disgusting.
		if (currentToken().tokenType == TokenType.LeftBrace) {
			parseFuncDef(funcDecl)
		}
		sema.actOnFinishParamList(sema.currentScope, funcDecl)
		return funcDecl
	}

	private fun parseFuncDef(funcDecl: FuncDecl) {
		assert(currentToken().tokenType == TokenType.LeftBrace)
		nextToken()
		val compoundStmt = CompoundStmt()
		sema.actOnStartFuncDef()
		while (currentToken().tokenType != TokenType.RightBrace) {
			compoundStmt.addStmt(parseStmt())
		}
		expectAndConsume(TokenType.RightBrace)
		sema.actOnFinishFuncDef()
		funcDecl.funcBody = compoundStmt
	}

	private fun parseStmt(): Stmt =
		when (currentToken().tokenType) {
			TokenType.Vi8, TokenType.Vi16, TokenType.Vi32, TokenType.Vi64, TokenType.Vr32, TokenType.Id -> parseDeclStmt()
			TokenType.LeftBrace -> parseCompoundStmt()
			else -> error("Fuck you!")
		}

	private fun parseCompoundStmt(): Stmt {
		assert(currentToken().tokenType == TokenType.LeftBrace)
		TODO("not implemented")
	}

	private fun parseDeclStmt(): Stmt {
		val varDecl = parseVarDecl()
		expectAndConsume(TokenType.Semicolon)
		sema.actOnDeclInScope(varDecl, sema.currentScope)
		return sema.actOnDeclStmt(varDecl)
	}

	private fun expect(tokenType: TokenType) {
		if (currentToken().tokenType != tokenType) {
			unrecoverableError("Expected ${tokenType.str} got ${currentToken()}")
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
