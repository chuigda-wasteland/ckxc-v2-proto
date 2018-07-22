package cn.ckxily.ckxc.parser

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.expr.*
import cn.ckxily.ckxc.ast.stmt.CompoundStmt
import cn.ckxily.ckxc.ast.stmt.DeclStmt
import cn.ckxily.ckxc.ast.stmt.ExprStmt
import cn.ckxily.ckxc.ast.stmt.Stmt
import cn.ckxily.ckxc.ast.type.*
import cn.ckxily.ckxc.util.unrecoverableError
import cn.ckxily.ckxc.lex.Token
import cn.ckxily.ckxc.lex.TokenType
import cn.ckxily.ckxc.sema.Sema
import cn.ckxily.ckxc.util.*

class QualifiedName(val nameChains: List<String>)

class ParserStateMachine(private val tokens: List<Token>, private val sema: Sema = Sema(),
												 private var currentTokenIndex: Int = 0) {
	fun parseTransUnit(): TransUnitDecl {
		while (currentToken().tokenType != TokenType.EOI) {
			val thisDecl = when (currentToken().tokenType) {
				TokenType.Class -> parseClassDecl()
				TokenType.Enum -> parseEnumDecl()
				TokenType.Let -> {
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
				TokenType.LeftParen -> unrecoverableError("Structural binding not allowed at top level of program")
				else -> unrecoverableError("Token ${currentToken()} not allowed at top level of program")
			}
			sema.actOnDeclInContext(thisDecl, sema.currentDeclContext)
		}
		return sema.topLevelDeclContext as TransUnitDecl
	}

	private fun parseMaybeQualifiedId(): Either<String, QualifiedName> {
		assert(currentToken().tokenType == TokenType.Id)
		val firstName = currentToken().value as String
		nextToken()
		return if (currentToken().tokenType != TokenType.ColonColon) {
			Left(firstName)
		}
		else {
			val nameChain = mutableListOf(firstName)
			while (currentToken().tokenType == TokenType.ColonColon) {
				nextToken()
				expect(TokenType.Id)
				nameChain.add(currentToken().value as String)
				nextToken()
			}
			Right(QualifiedName(nameChain))
		}
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
		assert(currentToken().tokenType == TokenType.Id)
		val maybeQualifiedId = parseMaybeQualifiedId()
		val lookupResult = sema.currentScope.lookup(maybeQualifiedId)
		if (lookupResult.size != 1) {
			unrecoverableError("More than one or no type declarations found")
		}

		if (lookupResult[0] is ClassDecl) return (lookupResult[0] as ClassDecl).type
		if (lookupResult[0] is EnumDecl) return (lookupResult[0] as EnumDecl).type
		unrecoverableError("No such type")
	}

	private fun parseBuiltinType(): Type? {
		val type = when (currentToken().tokenType) {
			TokenType.Vi8 -> BuiltinType(BuiltinTypeId.Int8, getNoSpecifier())
			TokenType.Vi16 -> BuiltinType(BuiltinTypeId.Int16, getNoSpecifier())
			TokenType.Vi32 -> BuiltinType(BuiltinTypeId.Int32, getNoSpecifier())
			TokenType.Vi64 -> BuiltinType(BuiltinTypeId.Int64, getNoSpecifier())
			TokenType.Vr32 -> BuiltinType(BuiltinTypeId.Float, getNoSpecifier())
			TokenType.Boolean -> BuiltinType(BuiltinTypeId.Boolean, getNoSpecifier())
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
		sema.actOnDeclInScope(enumDecl, sema.currentScope)
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
			val value = currentToken().value!! as Long
			nextToken()
			val enumerator = sema.actOnEnumerator(sema.currentScope, enumDecl, name, value.toInt())
			sema.actOnDeclInContext(enumerator, enumDecl)
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
		sema.actOnDeclInScope(classDecl, sema.currentScope)
		parseClassFields(classDecl)
		return classDecl
	}

	private fun parseClassFields(classDecl: ClassDecl) {
		expectAndConsume(TokenType.LeftBrace)
		sema.actOnTagStartDefinition()
		while (currentToken().tokenType != TokenType.RightBrace) {
			val decl = parseFieldDecl()
			sema.actOnDeclInScope(decl)
			sema.actOnDeclInContext(decl, classDecl)
		}
		sema.actOnTagFinishDefinition()
		expectAndConsume(TokenType.RightBrace)
	}

	private fun parseFieldDecl(): Decl {
		return when (currentToken().tokenType) {
			TokenType.Vi8, TokenType.Vi16, TokenType.Vi32, TokenType.Vi64, TokenType.Vr32, TokenType.Id -> {
				val varDecl = parseVarDecl(false)
				expectAndConsume(TokenType.Semicolon)
				varDecl
			}
			else -> parseDecl()
		}
	}

	private fun parseDecl(): Decl =
			when (currentToken().tokenType) {
				TokenType.Let -> {
					val varDecl = parseVarDecl()
					expectAndConsume(TokenType.Semicolon)
					varDecl
				}
				TokenType.Class -> parseClassDecl()
				TokenType.Enum -> parseEnumDecl()
				else -> unrecoverableError("Unexpected token ${currentToken()}, expected let, class or enum")
			}

	private fun parseVarDecl(requireLet: Boolean = true): VarDecl {
		if (requireLet) {
			assert(currentToken().tokenType == TokenType.Let)
			nextToken()
		}
		val type = parseType()
		expect(TokenType.Id)
		val name = currentToken().value as String
		nextToken()
		val varDecl = sema.actOnVarDecl(sema.currentScope, name, type)
		sema.actOnDeclInScope(varDecl)
		return varDecl
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
		val funcDecl = sema.actOnFuncDecl(sema.currentScope, name)
		sema.actOnDeclInScope(funcDecl)
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
		sema.actOnStartFuncDef()
		val compoundStmt = parseCompoundStmt()
		sema.actOnFinishFuncDef()
		funcDecl.funcBody = compoundStmt
	}

	private fun parseStmt(): Stmt =
		when (currentToken().tokenType) {
			TokenType.Let -> parseDeclStmt()
			TokenType.Id, TokenType.Number -> parseExprStmt()
			TokenType.LeftBrace -> parseCompoundStmt()
			else -> unrecoverableError("Fuck you!")
		}

	private fun parseCompoundStmt(): CompoundStmt {
		assert(currentToken().tokenType == TokenType.LeftBrace)
		nextToken()
		val compoundStmt = CompoundStmt()
		sema.actOnCompoundStmtBegin()
		while (currentToken().tokenType != TokenType.RightBrace) {
			compoundStmt.addStmt(parseStmt())
		}
		expectAndConsume(TokenType.RightBrace)
		sema.actOnCompoundStmtEnd()
		return compoundStmt
	}

	private fun parseDeclStmt(): DeclStmt {
		val varDecl = parseVarDecl()
		expectAndConsume(TokenType.Semicolon)
		return sema.actOnDeclStmt(varDecl)
	}

	private fun parseExprStmt(): ExprStmt {
		val expr = parseExpr()
		expectAndConsume(TokenType.Semicolon)
		return sema.actOnExprStmt(expr)
	}

	private fun parseExpr(): Expr = parseBinaryExpr(-1)

	private fun parseBinaryExpr(allowedPrec: Int): Expr {
		var lhs = parseUnaryExpr()
		var currentOperator = token2Binary(currentToken().tokenType)
		while (currentOperator != BinaryOpCode.NotBinaryOperator && currentOperator.prec >= allowedPrec) {
			nextToken()

			val rhs = parseBinaryExpr(currentOperator.prec + 1)
			lhs = sema.actOnBinaryExpr(lhs, rhs, currentOperator)
			currentOperator = token2Binary(currentToken().tokenType)
		}
		return lhs
	}

	private fun parseUnaryExpr(): Expr = when (currentToken().tokenType) {
		TokenType.Id -> parseDeclRefExpr()
		TokenType.Number -> parseLiteral()
		else -> assertionFailed("Unreachable code!")
	}

	private fun parseLiteral(): Expr = when(currentToken().tokenType) {
		TokenType.Number -> {
			val expr = IntegralLiteralExpr(currentToken().value as Long)
			nextToken()
			expr
		}
		else -> assertionFailed("Unreachable code!")
	}

	private fun parseMemberAccessExpr(): Expr {
		TODO("not implemented")
	}

	private fun parseDeclRefExpr(): DeclRefExpr {
		val id = parseMaybeQualifiedId()
		val decl = sema.currentScope.lookup(id)

		// TODO This is troublesome when we get to function overloading.
		if (decl.size != 1) {
			unrecoverableError("Ambiguous!")
		}

		return sema.actOnDeclRefExpr(decl.first() as? VarDecl ?: unrecoverableError("Expected VarDecl!"))
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
