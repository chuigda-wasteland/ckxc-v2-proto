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

	fun pushDeclContext(declContext: DeclContext) {
		assert(declContext.withinContext == currentDeclContext)
		currentDeclContext = declContext
		currentScope.entity = currentDeclContext
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

	fun checkDuplicate(nameStr: String) {
		if (currentDeclContext.lookupLocalDecl(nameStr).isNotEmpty()) {
			error("redefinition of ${nameStr}")
		}
	}
}
