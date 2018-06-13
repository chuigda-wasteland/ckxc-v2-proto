package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.type.Type
import java.lang.System.exit
import java.util.TreeSet

class Scope(val parent: Scope? = null,
						var localDecls: MutableSet<Decl> = TreeSet(),
						var entity: DeclContext? = null,
						var depth: Int) {
	init {
		depth = if (parent == null) 0 else parent.depth + 1
	}

	fun addDecl(decl: Decl) = localDecls.add(decl)
	fun removeDecl(decl: Decl) = localDecls.remove(decl)
}

class LookupRequest()

class LookupResult()

class Sema(var currentDeclContext: DeclContext,
					 var topLevelDeclContext: DeclContext) {
	init {
		topLevelDeclContext = TransUnitDecl()
		currentDeclContext = topLevelDeclContext
	}

	fun pushDeclContext(declContext: DeclContext) {
		assert(declContext.withinContext == currentDeclContext)
		currentDeclContext = declContext
	}

	fun popDeclContext(): DeclContext {
		val popedContext = currentDeclContext
		currentDeclContext = currentDeclContext.withinContext!!
		return popedContext
	}

	fun actOnTransUnitDeclFinished(): TransUnitDecl {
		return topLevelDeclContext as TransUnitDecl
	}

	fun checkDuplicate(nameStr: String) {
		if (currentDeclContext.lookupLocalDecl(nameStr).isNotEmpty()) {
			println("Error: redefinition of name ${nameStr}")
			exit(-1);
		}
	}

	fun actOnVarDecl(nameStr: String, type: Type, withinContext: DeclContext = currentDeclContext): VarDecl? {
		checkDuplicate(nameStr)
		val varDecl = VarDecl(nameStr, type, withinContext)
		currentDeclContext.pushDecl(varDecl)
		return varDecl
	}

	fun actOnStartClassDecl(nameStr: String) {
		val classDecl = ClassDecl(nameStr, currentDeclContext)
		currentDeclContext.pushDecl(classDecl)
		pushDeclContext(classDecl)
	}

	fun actOnFinishClassDecl(): ClassDecl {
		return popDeclContext() as ClassDecl
	}

	fun actOnStartEnumDecl(nameStr: String) {
		val enumDecl = EnumDecl(nameStr, currentDeclContext)
		currentDeclContext.pushDecl(enumDecl)
		pushDeclContext(enumDecl)
	}

	fun actOnEnumerator(nameStr: String, init: Int?, lastEnumerator: EnumeratorDecl?): EnumeratorDecl {
		checkDuplicate(nameStr)
		val enumeratorDecl = EnumeratorDecl(nameStr, init ?: (lastEnumerator?.init ?: 0), currentDeclContext)
		currentDeclContext.pushDecl(enumeratorDecl)
		return enumeratorDecl
	}

	fun actOnFinishEnumDecl(): EnumDecl = popDeclContext() as EnumDecl
}
