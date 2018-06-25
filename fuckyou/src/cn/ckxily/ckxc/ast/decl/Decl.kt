package cn.ckxily.ckxc.ast.decl

import cn.ckxily.ckxc.ast.type.Type
import cn.ckxily.ckxc.codegen.ASTConsumer

enum class DeclKind(val str: String) {
	TransUnitDecl("Translation Unit"),
	VarDecl("Variable Declaration"),
	EnumDecl("Enumeration Declaration"),
	EnumeratorDecl("Enumerator"),
	ClassDecl("Class Declaration"),
	FieldDecl("Field")
}

val DeclKind.description get() = str

enum class DeclContextKind(val str: String) {
	TransUnitContext("Top Level Of Translation Unit"),
	EnumContext("Inside Enumeration"),
	ClassContext("Inside Class")
}

val DeclContextKind.description get() = str

abstract class Decl(val declKind: DeclKind, val withinContext: DeclContext?) {
	abstract val nameStr: String?
	abstract fun accept(astConsumer: ASTConsumer): Any?
}

abstract class DeclContext(val declContextKind: DeclContextKind, declKind: DeclKind, withinContext: DeclContext?)
	: Decl(declKind, withinContext) {
	var decls: MutableList<Decl> = ArrayList()

	fun addDecl(decl: Decl) = decls.add(decl)
	fun removeDecl(decl: Decl) = decls.remove(decl)
	fun lookupLocalDecl(name: String) = decls.filter { decl -> decl.nameStr == name }
	fun lookupDecl(name: String) = lookupDecl(this, name)
}

tailrec fun lookupDecl(declContext: DeclContext, name: String): List<Decl> {
	val localResult = declContext.lookupLocalDecl(name)
	if (declContext.withinContext != null && localResult.isEmpty()) {
		return lookupDecl(declContext.withinContext, name)
	}
	return ArrayList()
}

class TransUnitDecl : DeclContext(DeclContextKind.TransUnitContext, DeclKind.TransUnitDecl, null) {
	override val nameStr: String? get() = null
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitTransUnitDecl(this)
}

open class VarDecl(override var nameStr: String, var type: Type, withinContext: DeclContext)
	: Decl(DeclKind.VarDecl, withinContext) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitVarDecl(this)
}

class EnumeratorDecl(override var nameStr: String, var init: Int, withinContext: DeclContext)
	: Decl(DeclKind.EnumDecl, withinContext) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitEnumeratorDecl(this)
}

class ClassDecl(override val nameStr: String, withinContext: DeclContext)
	: DeclContext(DeclContextKind.ClassContext, DeclKind.ClassDecl, withinContext) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitClassDecl(this)
}

class EnumDecl(override val nameStr: String, withinContext: DeclContext)
	: DeclContext(DeclContextKind.EnumContext, DeclKind.EnumDecl, withinContext) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitEnumDecl(this)
}
