package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.type.Type
import java.lang.System.exit
import java.util.TreeSet

class Scope(val parent: Scope? = null,
						var depth: Int,
						var entity: DeclContext? = null,
						var decls: MutableSet<Decl> = TreeSet()) {
	init {
		depth = if (parent == null) 0 else parent.depth + 1
	}

	fun addDecl(decl: Decl) = decls.add(decl)
	fun removeDecl(decl: Decl) = decls.remove(decl)
	fun lookupLocally(name: String) = decls.filter { decl -> decl.nameStr?.equals(name) ?: false }
	fun lookup(name: String) = lookup(this, name)
}

tailrec fun lookup(scope: Scope, name: String): List<Decl> {
	val localResult = scope.lookupLocally(name)
	if (localResult.isEmpty() && scope.parent != null) {
		return lookup(scope.parent, name)
	}
	return localResult
}

class Sema(var currentDeclContext: DeclContext,
					 var currentScope: Scope,
					 var topLevelDeclContext: DeclContext) {
	init {
		topLevelDeclContext = TransUnitDecl()
		currentDeclContext = topLevelDeclContext
	}

	fun pushDeclContext(scope: Scope, declContext: DeclContext) {
		assert(declContext.withinContext == currentDeclContext)
		currentDeclContext = declContext
		scope.entity = currentDeclContext
	}

	fun popDeclContext(): DeclContext {
		val popedContext = currentDeclContext
		currentDeclContext = currentDeclContext.withinContext!!
		return popedContext
	}

	fun pushScope() {
		currentScope = Scope(currentScope, currentScope.depth+1)
	}

	fun popScope() {
		currentScope = currentScope.parent!!
	}

	fun pushOnScopeChains(decl: Decl, scope: Scope, addToContext: Boolean) {
		if (addToContext) {
			currentDeclContext.addDecl(decl)
		}
		scope.addDecl(decl)
	}

	fun removeFromScopeChains(decl: Decl, scope: Scope, removeFromContext: Boolean) {
		if (removeFromContext) {
			currentDeclContext.removeDecl(decl)
		}
		scope.removeDecl(decl)
	}

	fun checkDuplicate(scope: Scope, nameStr: String) {
		if (scope.lookupLocally(nameStr).isNotEmpty()) {
			error("redefinition of ${nameStr}")
		}
	}

	fun actOnVarDecl(scope: Scope, atLine: Int, name: String, type: Type, addToContext: Boolean): VarDecl {
		checkDuplicate(scope, name)
		val varDecl = VarDecl(name, type, currentDeclContext)
		pushOnScopeChains(varDecl, scope, addToContext)
		return varDecl
	}

	fun actOnClass(scope: Scope, atLine: Int, name: String, addToContext: Boolean): ClassDecl {
		checkDuplicate(scope, name)
		val classDecl = ClassDecl(name, currentDeclContext)
		pushOnScopeChains(classDecl, scope, addToContext)
		return classDecl
	}

	fun actOnEnum(scope: Scope, atLine: Int, name: String, addToContext: Boolean): EnumDecl {
		checkDuplicate(scope, name)
		val enumDecl = EnumDecl(name, currentDeclContext)
		pushOnScopeChains(enumDecl, scope, addToContext)
		return enumDecl
	}

	fun actOnTagStartDefinition(scope: Scope, tagDecl: Decl) {
		pushDeclContext(scope, tagDecl as DeclContext)
	}

	fun actOnTagFinishDefinition() {
		popDeclContext()
	}

	fun actOnEnumarator(scope: Scope, enumDecl: EnumDecl, name: String, init: Int?): EnumeratorDecl {
		checkDuplicate(scope, name)
		val declContext = enumDecl as DeclContext
		val enumerator = EnumeratorDecl(name, init?: 0, declContext)
		pushOnScopeChains(enumerator, scope, true)
		return enumerator
	}
}
