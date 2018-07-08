package cn.ckxily.ckxc.ast.stmt

import cn.ckxily.ckxc.codegen.ASTConsumer

enum class StmtKind(val str: String) {
	CompoundStmt("Compound Statement")
}

abstract class Stmt(val stmtKind: StmtKind) {
	abstract fun accept(astConsumer: ASTConsumer): Any?
}

class CompoundStmt(val stmtList: MutableList<Stmt> = ArrayList()) : Stmt(StmtKind.CompoundStmt) {
	override fun accept(astConsumer: ASTConsumer) = astConsumer.visitCompoundStmt(this)
	fun addStmt(stmt: Stmt) = stmtList.add(stmt)
}
