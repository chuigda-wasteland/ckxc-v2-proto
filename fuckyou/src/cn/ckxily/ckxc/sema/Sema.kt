package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.type.Type

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
	fun pushScope() {
		currentScope = Scope(currentScope, currentScope.depth+1)
	}

	fun popScope() {
		currentScope = currentScope.parent!!
	}

	fun checkDuplicate(scope: Scope, nameStr: String) {
		if (scope.lookupLocally(nameStr).isNotEmpty()) {
			error("redefinition of ${nameStr}")
		}
	}

	fun actOnGlobalDecl(decl: Decl) {
		topLevelDeclContext.addDecl(decl)
	}

	fun actOnFieldDecl(declContext: DeclContext, decl: Decl) {
		declContext.addDecl(decl)
	}

	fun actOnVarDecl(scope: Scope, name: String, type: Type): VarDecl {
		val varDecl = VarDecl(name, type)
		scope.addDecl(varDecl)
		return varDecl;
	}

	fun actOnClass(scope: Scope, name: String): ClassDecl {
		checkDuplicate(scope, name)
		val classDecl = ClassDecl(name)
		scope.addDecl(classDecl)
		return classDecl
	}

	fun actOnEnum(scope: Scope, name: String): EnumDecl {
		checkDuplicate(scope, name)
		val enumDecl = EnumDecl(name)
		scope.addDecl(enumDecl)
		return enumDecl
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
		scope.addDecl(enumerator)
		actOnFieldDecl(enumDecl, enumerator)
		return enumerator
	}
}
