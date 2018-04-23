package cn.ckxily.ckxc.Sema

import cn.ckxily.ckxc.ast.decl.Decl
import cn.ckxily.ckxc.ast.decl.DeclContext

class Scope(private val entity: DeclContext, val parentScope: Scope?) {
	fun lookupLocal(name: String): List<Decl> = entity.lookupDecl(name)
	fun lookup(name: String): List<Decl> {
		val localResult = lookupLocal(name)
		if (localResult.isEmpty() && parentScope != null) {
			return parentScope.lookup(name)
		}
		return localResult
	}
	fun pushDecl(decl: Decl) = entity.pushDecl(decl)
}
