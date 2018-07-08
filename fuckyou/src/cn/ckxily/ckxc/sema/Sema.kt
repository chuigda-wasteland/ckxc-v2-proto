package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.type.BuiltinType
import cn.ckxily.ckxc.ast.type.BuiltinTypeId
import cn.ckxily.ckxc.ast.type.Type
import cn.ckxily.ckxc.ast.type.getNoSpecifier

class Scope(val parent: Scope? = null,
						var depth: Int,
						var decls: MutableList<Decl> = ArrayList()) {
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
	fun lookup(name: String) = dlLookup(this, name)
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
			error("redefinition of ${nameStr}")
		}
	}

	fun actOnDeclInContext(decl: Decl, declContext: DeclContext = currentDeclContext) {
		declContext.addDecl(decl)
	}

	fun actOnDeclInScope(decl: Decl, scope: Scope = currentScope) {
		scope.addDecl(decl)
	}

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

	fun actOnTagStartDefinition() {
		pushScope()
	}

	fun actOnTagFinishDefinition() {
		popScope()
	}

	fun actOnEnumarator(scope: Scope, enumDecl: EnumDecl, name: String, init: Int?): EnumeratorDecl {
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

	fun actOnParam(scope: Scope, funcDecl: FuncDecl, name: String, type: Type): VarDecl {
		checkDuplicate(scope, name)
		val varDecl = VarDecl(name, type)
		funcDecl.paramList.add(varDecl)
		return varDecl
	}

	fun actOnStratFuncDef(scope: Scope, funcDecl: FuncDecl, compoundStmt: Any?) {
		/// TODO not implemented
	}
}
