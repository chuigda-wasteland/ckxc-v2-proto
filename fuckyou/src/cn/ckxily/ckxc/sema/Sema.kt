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

	fun lookupVarDeclLocally(name: String) =
			lookupLocally(name).filter { decl -> postSelect(decl, LookupKind.LookupVarDecl) }

	fun lookupFuncLocally(name: String) =
			lookupLocally(name).filter { decl -> postSelect(decl, LookupKind.LookupFunc) }

	fun lookupClassLocally(name: String) =
			lookupLocally(name).filter { decl -> postSelect(decl, LookupKind.LookupClass) }

	fun lookupEnumLocally(name: String) =
			lookupLocally(name).filter { decl -> postSelect(decl, LookupKind.LookupEnum) }

	fun lookupASTContextLocally(name: String) =
			lookupLocally(name).filter { decl -> postSelect(decl, LookupKind.LookupASTContext) }

	fun lookupLocally(name: String) = decls.filter { decl -> decl.nameStr?.equals(name) ?: false }

	enum class LookupKind { LookupEverything, LookupVarDecl, LookupClass, LookupEnum, LookupASTContext, LookupFunc }

	private fun lookup(name: String, lookupKind: LookupKind): List<Decl> = dlLookup(this, name, lookupKind)

	private fun lookup(qualifiedName: QualifiedName, lookupKind: LookupKind): List<Decl> {
		var basicDecl = dlLookup(this, qualifiedName.nameChains.first(), LookupKind.LookupASTContext)
		var i = 1
		while (i < qualifiedName.nameChains.size - 1) {
			if (basicDecl.size != 1) {
				unrecoverableError("懒癌发作不想写错误信息了!")
			}

			if (basicDecl.first() !is DeclContext) {
				unrecoverableError("$basicDecl is not a DeclContext")
			}

			basicDecl = (basicDecl.first() as DeclContext).lookupLocalDecl(qualifiedName.nameChains[i])
			i += 1
		}

		return LinkedList(basicDecl).filter { decl -> postSelect(decl, lookupKind) }
	}

	private fun postSelect(decl: Decl, lookupKind: LookupKind): Boolean = when (lookupKind) {
		LookupKind.LookupEverything -> true
		LookupKind.LookupVarDecl -> decl.declKind == DeclKind.VarDecl
		LookupKind.LookupClass -> decl.declKind == DeclKind.ClassDecl
		LookupKind.LookupEnum -> decl.declKind == DeclKind.EnumDecl
		LookupKind.LookupASTContext -> decl.declKind == DeclKind.ClassDecl || decl.declKind == DeclKind.EnumDecl
		LookupKind.LookupFunc -> decl.declKind == DeclKind.FuncDecl
	}

	fun lookup(maybeQualifiedName: Either<String, QualifiedName>, lookupKind: LookupKind): List<Decl> =
			when (maybeQualifiedName) {
				is Left -> lookup(maybeQualifiedName.asLeft(), lookupKind)
				is Right -> lookup(maybeQualifiedName.asRight(), lookupKind)
			}
}

tailrec fun dlLookup(scope: Scope, name: String, lookupKind: Scope.LookupKind): List<Decl> {
	val localResult = when(lookupKind) {
		Scope.LookupKind.LookupVarDecl -> scope.lookupVarDeclLocally(name)
		Scope.LookupKind.LookupFunc -> scope.lookupFuncLocally(name)
		Scope.LookupKind.LookupClass -> scope.lookupClassLocally(name)
		Scope.LookupKind.LookupEnum -> scope.lookupEnumLocally(name)
		Scope.LookupKind.LookupASTContext -> scope.lookupASTContextLocally(name)
		Scope.LookupKind.LookupEverything -> scope.lookupLocally(name)
	}

	if (localResult.isEmpty() && scope.parent != null) {
		return dlLookup(scope.parent, name, lookupKind)
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
		/// TODO add support for operator overloading
		return when (opCode) {
			BinaryOpCode.Assign -> actOnAssignmentExpr(lhs, rhs, opCode)
			BinaryOpCode.LogicAnd, BinaryOpCode.LogicOr -> actOnLogicalExpr(lhs, rhs, opCode)
			else -> actOnAlgebraExpr(lhs, rhs, opCode)
		}
	}

	fun actOnAssignmentExpr(lhs: Expr, rhs: Expr, opCode: BinaryOpCode): Expr {
		assert(opCode == BinaryOpCode.Assign)
		if (lhs.valueCategory != ValueCategory.LValue) {
			unrecoverableError("Expected lvalue at the left hand side of assignment expression")
		}
		if (lhs.type.qualifiers.isConst) {
			unrecoverableError("Expected non-const value at left hand side of assignment expression")
		}

		val castedRhs = actOnImplicitCast(rhs, lhs.type) ?: unrecoverableError("failed to cast assignee")
		return BinaryExpr(opCode, lhs, actOnLValueToRValueDecay(castedRhs), lhs.type)
	}

	fun actOnLogicalExpr(lhs: Expr, rhs: Expr, opCode: BinaryOpCode): Expr {
		val castedLhsType =
			lhs.type as? BuiltinType ?: unrecoverableError("Non-bool types used in logical expression")
		val castedRhsType =
			rhs.type as? BuiltinType ?: unrecoverableError("Non-bool types used in logical expression")
		if (castedLhsType.builtinTypeId != BuiltinTypeId.Boolean
				|| castedRhsType.builtinTypeId != BuiltinTypeId.Boolean) {
			unrecoverableError("Non-bool types used in logical expression")
		}

		return BinaryExpr(
				opCode,
				actOnLValueToRValueDecay(lhs),
				actOnLValueToRValueDecay(rhs),
				BuiltinType(BuiltinTypeId.Boolean, getNoSpecifier()))
	}

	fun actOnAlgebraExpr(lhs: Expr, rhs: Expr, opCode: BinaryOpCode): Expr {
		val commonType =
				TypeUtility.commonBuiltinType(
						lhs.type as BuiltinType,
						rhs.type as BuiltinType) ?: unrecoverableError("No common type!")

		val castedLhs = actOnImplicitCast(lhs, commonType) ?: unrecoverableError("failed to cast lhs")
		val castedRhs = actOnImplicitCast(rhs, commonType) ?: unrecoverableError("failed to cast rhs")
		return BinaryExpr(opCode, actOnLValueToRValueDecay(castedLhs), actOnLValueToRValueDecay(castedRhs),
											if (opCode == BinaryOpCode.Less || opCode == BinaryOpCode.Greater
													|| opCode == BinaryOpCode.Equal || opCode == BinaryOpCode.NEQ
													|| opCode == BinaryOpCode.GEQ || opCode == BinaryOpCode.LEQ)
												BuiltinType(BuiltinTypeId.Boolean, getNoSpecifier())
											else commonType)
	}

	fun dehugify(type: Type) = if (type.typeId == TypeId.Reference) (type as ReferenceType).referenced else type
	fun botherIf(condi: Boolean, desc: String) = if (condi) unrecoverableError(desc) else 0

	fun canImplicitCast(fromType: Type, destType: Type, bother: Boolean = false): Boolean {
		val castedFromType = dehugify(fromType)
		val castedDestType = dehugify(destType)

		@Suppress("NON_EXHAUSTIVE_WHEN")
		when (castedFromType.qualifiers.compareWith(castedDestType.qualifiers)) {
			TypeQualifiers.CompareResult.Nonsense, TypeQualifiers.CompareResult.LessQualified -> {
				botherIf(bother, "反正是qualifier出问题了你看着办吧")
				return false
			}
		}

		if (castedFromType.typeId == TypeId.Builtin && castedDestType.typeId == TypeId.Builtin) {
			return canImplicitCastBuiltin(castedFromType as BuiltinType, castedDestType as BuiltinType, bother)
		}

		/// TODO handle user-defined conversion stuffs

		return false
	}

	fun checkValueCategoryForCast(fromValueCategory: ValueCategory,
																				destValueCategory: ValueCategory,
																				bother: Boolean, destType: Type): Boolean {
		if (fromValueCategory == ValueCategory.RValue && destValueCategory == ValueCategory.LValue) {
			botherIf(bother, "Shouldn't use rvalue when a lvalue is required")
			return false
		} else if (fromValueCategory == ValueCategory.RValue
				&& destType.typeId == TypeId.Reference
				&& !(destType as ReferenceType).referenced.qualifiers.isConst) {
			botherIf(bother, "Binding rvalue to non-const lvalue reference")
			return false
		}
		return true
	}

	fun canImplicitCastBuiltin(fromType: BuiltinType, destType: BuiltinType, bother: Boolean): Boolean {
		return false
	}

	// TODO this function looks nasty, split it into several parts in further commits.
	fun actOnImplicitCast(expr: Expr, desired: Type): Expr? {
		assert(expr.type.typeId == TypeId.Builtin)
		assert(desired.typeId == TypeId.Builtin)

		var currentExpr = expr
		var builtinSrcType = expr.type as BuiltinType
		val builtinDesiredType = desired as BuiltinType

		if (builtinSrcType.builtinTypeId == builtinDesiredType.builtinTypeId) {
			return currentExpr
		}

		if (!builtinDesiredType.qualifiers.isConst && builtinSrcType.qualifiers.isConst
				|| !builtinDesiredType.qualifiers.isVolatile && builtinSrcType.qualifiers.isVolatile) {
			return null
		}

		if (builtinDesiredType.qualifiers.isConst && !builtinSrcType.qualifiers.isConst) {
			builtinSrcType = qualType(builtinSrcType,
						if (builtinSrcType.qualifiers.isVolatile) getCVSpecifiers() else getCSpecifier()) as BuiltinType
			currentExpr = ImplicitCastExpr(CastOperation.AddConst, currentExpr, builtinSrcType)
		}

		if (builtinDesiredType.qualifiers.isVolatile && !builtinSrcType.qualifiers.isVolatile) {
			builtinSrcType = qualType(builtinSrcType,
							if (builtinSrcType.qualifiers.isConst) getCVSpecifiers() else getVSpecifier()) as BuiltinType
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
