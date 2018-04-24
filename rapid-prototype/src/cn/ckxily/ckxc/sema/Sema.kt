package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.decl.Decl
import cn.ckxily.ckxc.ast.decl.DeclContext

// question: do you need overloading?
// If so, `lookup` should collect all definitions with the same name
class Scope(private val entity: DeclContext, val parentScope: Scope?) {
	fun lookupLocal(name: String): List<Decl> = entity.lookupDecl(name)
	fun lookup(name: String) = lookup(name, this)
	fun pushDecl(decl: Decl) = entity.pushDecl(decl)
}

tailrec fun lookup(name: String, scope: Scope): List<Decl> {
	val localResult = scope.lookupLocal(name)
	return if (localResult.isEmpty() && scope.parentScope != null) {
		lookup(name, scope.parentScope)
	} else localResult
}

