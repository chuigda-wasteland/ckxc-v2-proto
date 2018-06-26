package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.type.Type
import java.util.TreeSet

class Scope(val parent: Scope? = null,
						var depth: Int,
						var entity: DeclContext? = null,
						var decls: MutableSet<Decl> = TreeSet()) {
	init {
		depth = if (parent == null) 0 else parent.depth + 1
	}

	fun addDecl(decl: Decl) {
		decls.add(decl)
		entity?.addDecl(decl)
	}
	fun removeDecl(decl: Decl) {
		decls.remove(decl)
		entity?.removeDecl(decl)
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
					 var currentScope: Scope = Scope(null, 0, topLevelDeclContext)) {
	fun pushScope(declContext: DeclContext? = null) {
		currentScope = Scope(currentScope, currentScope.depth+1, declContext)
	}

	fun popScope() {
		currentScope = currentScope.parent!!
	}

	fun checkDuplicate(scope: Scope, nameStr: String) {
		if (scope.lookupLocally(nameStr).isNotEmpty()) {
			error("redefinition of ${nameStr}")
		}
	}

	fun actOnVarDecl(scope: Scope, atLine: Int, name: String, type: Type): VarDecl {
		checkDuplicate(scope, name)
		val varDecl = VarDecl(name, type)
		scope.addDecl(varDecl)
		return varDecl
	}

	fun actOnClass(scope: Scope, atLine: Int, name: String): ClassDecl {
		checkDuplicate(scope, name)
		val classDecl = ClassDecl(name)
		scope.addDecl(classDecl)
		return classDecl
	}

	fun actOnEnum(scope: Scope, atLine: Int, name: String): EnumDecl {
		checkDuplicate(scope, name)
		val enumDecl = EnumDecl(name)
		scope.addDecl(enumDecl)
		return enumDecl
	}

	fun actOnTagStartDefinition(tagDecl: Decl) {
		pushScope(tagDecl as DeclContext)
	}

	fun actOnTagFinishDefinition() {
		popScope()
	}

	fun actOnEnumarator(scope: Scope, enumDecl: EnumDecl, name: String, init: Int?): EnumeratorDecl {
		checkDuplicate(scope, name)
		val declContext = enumDecl as DeclContext
		val enumerator = EnumeratorDecl(name, init?: 0)
		scope.addDecl(enumerator)
		return enumerator
	}
}
