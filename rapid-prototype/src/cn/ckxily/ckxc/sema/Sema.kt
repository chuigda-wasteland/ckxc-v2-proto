package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.decl.Decl
import cn.ckxily.ckxc.ast.decl.DeclContext

// question: do you need overloading?
// If so, `lookup` should collect all definitions with the same name
class Scope(private val entity: DeclContext, val parentScope: Scope?) {
	fun lookupLocal(name: String): List<Decl> = entity.lookupDecl(name)

	tailrec fun lookup(name: String, scope: Scope = this): List<Decl> {
		val localResult = lookupLocal(name)
		return if (localResult.isEmpty() && parentScope != null) {
			lookup(name, parentScope)
		} else localResult
	}

	fun pushDecl(decl: Decl) = entity.pushDecl(decl)
}
