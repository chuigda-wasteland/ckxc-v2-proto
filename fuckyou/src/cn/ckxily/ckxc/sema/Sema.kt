package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.expr.*
import cn.ckxily.ckxc.ast.stmt.DeclStmt
import cn.ckxily.ckxc.ast.stmt.ExprStmt
import cn.ckxily.ckxc.ast.type.*
import cn.ckxily.ckxc.ast.type.getNoSpecifier
import cn.ckxily.ckxc.parser.QualifiedName
import cn.ckxily.ckxc.util.*
import java.util.*

class Scope(val parent: Scope? = null,
						var depth: Int,
						private var decls: MutableList<Decl> = ArrayList()) {
	init {
		depth = if (parent == null) 0 else parent.depth + 1
	}

	fun addDecl(decl: Decl) {
		decls.add(decl)
	}

	fun removeDecl(decl: Decl) {
		decls.remove(decl)
	}

	fun lookupLocally(name: String) = decls.filter { decl -> decl.nameStr?.equals(name) ?: false }

	private fun lookup(name: String): List<Decl> = dlLookup(this, name)

	private fun lookup(qualifiedName: QualifiedName): List<Decl> {
		var basicDecl = dlLookup(this, qualifiedName.nameChains.first())
		var i = 1
		while (i < qualifiedName.nameChains.size) {
			if (basicDecl.size != 1) {
				unrecoverableError("懒癌发作不想写错误信息了!")
			}

			if (basicDecl.first() !is DeclContext) {
				unrecoverableError("$basicDecl is not a DeclContext")
			}

			basicDecl = (basicDecl.first() as DeclContext).lookupLocalDecl(qualifiedName.nameChains[i])
			i += 1
		}

		return LinkedList(basicDecl)
	}

	fun lookup(maybeQualifiedName: Either<String, QualifiedName>): List<Decl> = when(maybeQualifiedName) {
		is Left -> lookup(maybeQualifiedName.asLeft())
		is Right -> lookup(maybeQualifiedName.asRight())
	}
}

tailrec fun dlLookup(scope: Scope, name: String): List<Decl> {
	val localResult = scope.lookupLocally(name)
	if (localResult.isEmpty() && scope.parent != null) {
		return dlLookup(scope.parent, name)
	}
	return localResult
}

class Sema(var topLevelDeclContext: DeclContext = TransUnitDecl(),
					 var currentDeclContext: DeclContext = topLevelDeclContext,
					 var currentScope: Scope = Scope(null, 0)) {
	private fun pushScope() {
		currentScope = Scope(currentScope, currentScope.depth+1)
	}

	private fun popScope() {
		currentScope = currentScope.parent!!
	}

	private fun checkDuplicate(scope: Scope, nameStr: String) {
		if (scope.lookupLocally(nameStr).isNotEmpty()) {
			unrecoverableError("redefinition of $nameStr")
		}
	}

	fun actOnDeclInContext(decl: Decl, declContext: DeclContext = currentDeclContext) = declContext.addDecl(decl)

	fun actOnDeclInScope(decl: Decl, scope: Scope = currentScope) = scope.addDecl(decl)

	fun actOnVarDecl(scope: Scope, name: String, type: Type): VarDecl {
		checkDuplicate(scope, name)
		return VarDecl(name, type)
	}

	fun actOnClass(scope: Scope, name: String): ClassDecl {
		checkDuplicate(scope, name)
		return ClassDecl(name)
	}

	fun actOnEnum(scope: Scope, name: String): EnumDecl {
		checkDuplicate(scope, name)
		return EnumDecl(name)
	}

	fun actOnTagStartDefinition() = pushScope()

	fun actOnTagFinishDefinition() = popScope()

	fun actOnEnumerator(scope: Scope, enumDecl: EnumDecl, name: String, init: Int?): EnumeratorDecl {
		checkDuplicate(scope, name)
		val enumerator = EnumeratorDecl(name, init?: 0)
		actOnDeclInScope(enumerator)
		actOnDeclInContext(enumerator, enumDecl)
		return enumerator
	}

	fun actOnFuncDecl(scope: Scope, name: String): FuncDecl {
		checkDuplicate(scope, name)
		return FuncDecl(name, ArrayList(), BuiltinType(BuiltinTypeId.Int8, getNoSpecifier()), null)
	}

	@Suppress("UNUSED_PARAMETER")
	fun actOnStartParamList(scope: Scope, funcDecl: FuncDecl) = pushScope()

	@Suppress("UNUSED_PARAMETER")
	fun actOnFinishParamList(scope: Scope, funcDecl: FuncDecl) = popScope()

	fun actOnParam(scope: Scope, funcDecl: FuncDecl, name: String, type: Type): VarDecl {
		checkDuplicate(scope, name)
		val varDecl = VarDecl(name, type)
		funcDecl.paramList.add(varDecl)
		return varDecl
	}

	fun actOnStartFuncDef() {}

	fun actOnFinishFuncDef() {}

	fun actOnCompoundStmtBegin() = pushScope()

	fun actOnCompoundStmtEnd() = popScope()

	fun actOnDeclStmt(decl: Decl): DeclStmt = DeclStmt(decl)

	fun actOnDeclRefExpr(decl: VarDecl): DeclRefExpr = DeclRefExpr(decl)

	fun actOnExprStmt(expr: Expr): ExprStmt = ExprStmt(expr)

	fun actOnBinaryExpr(lhs: Expr, rhs: Expr, opCode: BinaryOpCode): Expr {
		val commonType =
			TypeUtility.commonType(lhs.type, rhs.type) ?: unrecoverableError("No common type!")

		if (opCode == BinaryOpCode.LogicAnd	|| opCode == BinaryOpCode.LogicOr) {
			if (commonType.typeId != TypeId.Builtin
					&& (commonType as BuiltinType).builtinTypeId == BuiltinTypeId.Boolean) {
				unrecoverableError("Non-bool type for logical operations!")
			}
		}

		val castedLhs = actOnImplicitCast(lhs, commonType) ?: unrecoverableError("failed to cast lhs")
		val castedRhs = actOnImplicitCast(rhs, commonType) ?: unrecoverableError("failed to cast rhs")
		return BinaryExpr(opCode, actOnLValueToRValueDecay(lhs), actOnLValueToRValueDecay(rhs))
	}

	// TODO this function looks nasty, split it into several parts in further commits.
	fun actOnImplicitCast(expr: Expr, desired: Type): Expr? {
		assert(expr.type.typeId == TypeId.Builtin)
		assert(desired.typeId == TypeId.Builtin)

		var currentExpr = expr
		var builtinSrcType = expr.type as BuiltinType
		val builtinDesiredType = desired as BuiltinType

		if (!builtinDesiredType.specifiers.isConst && builtinSrcType.specifiers.isConst
				|| !builtinDesiredType.specifiers.isVolatile && builtinSrcType.specifiers.isVolatile) {
			return null
		}

		if (builtinDesiredType.specifiers.isConst && !builtinSrcType.specifiers.isConst) {
			builtinSrcType = qualType(builtinSrcType,
						if (builtinSrcType.specifiers.isVolatile) getCVSpecifiers() else getCSpecifier()) as BuiltinType
			currentExpr = ImplicitCastExpr(CastOperation.AddConst, currentExpr, builtinSrcType)
		}

		if (builtinDesiredType.specifiers.isVolatile && !builtinSrcType.specifiers.isVolatile) {
			builtinSrcType = qualType(builtinSrcType,
							if (builtinSrcType.specifiers.isConst) getCVSpecifiers() else getVSpecifier()) as BuiltinType
			currentExpr = ImplicitCastExpr(CastOperation.AddVolatile, currentExpr, builtinSrcType)
		}

		if (builtinDesiredType.isInteger() && builtinSrcType.isInteger()
				|| builtinDesiredType.isFloating() && builtinSrcType.isFloating()) {
			if (builtinDesiredType.builtinTypeId.rank > builtinSrcType.builtinTypeId.rank) {
				return ImplicitCastExpr(CastOperation.IntegerWidenCast, currentExpr, desired)
			}
			else if (builtinDesiredType.builtinTypeId.rank == builtinSrcType.builtinTypeId.rank) {
				return currentExpr
			}
		}

		return null
	}

	fun actOnLValueToRValueDecay(expr: Expr): Expr {
		return if (expr.valueCategory == ValueCategory.RValue) expr else ImplicitDecayExpr(expr)
	}
}
