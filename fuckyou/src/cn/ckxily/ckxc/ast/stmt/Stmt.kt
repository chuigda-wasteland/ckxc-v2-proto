package cn.ckxily.ckxc.ast.stmt

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.codegen.ASTConsumer

enum class StmtKind(val str: String) {
	CompoundStmt("Compound statement"),
	IfStmt("If statement"),
	ForStmt("For statement"),
	WhileStmt("While statement"),
	DeclStmt("Declaration statement"),
	ExprStmt("Expression statement")
}

abstract class Stmt(val stmtKind: StmtKind) {
	abstract fun accept(astConsumer: ASTConsumer): Any?
}

class CompoundStmt(val stmtList: MutableList<Stmt> = ArrayList()) : Stmt(StmtKind.CompoundStmt) {
	override fun accept(astConsumer: ASTConsumer) = astConsumer.visitCompoundStmt(this)
	fun addStmt(stmt: Stmt) = stmtList.add(stmt)
}

class IfStmt(val expr: Any, val thenStmt: Stmt, val elseStmt: Stmt? = null) : Stmt(StmtKind.IfStmt) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented")
	}
}

class ForStmt(val init: Any? = null, val cond: Any? = null, val incr: Any? = null, val body: Stmt)
	: Stmt(StmtKind.ForStmt) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented")
	}
}

class WhileStmt(val expr: Any, val body: Stmt) : Stmt(StmtKind.WhileStmt) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented")
	}
}

/// TODO one statment may correspond to multiple declarations.
class DeclStmt(val decl: Decl) : Stmt(StmtKind.DeclStmt) {
	override fun accept(astConsumer: ASTConsumer) = astConsumer.visitDeclStmt(this)
}

class ExprStmt(val expr: Any?) : Stmt(StmtKind.ExprStmt) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented")
	}
}
