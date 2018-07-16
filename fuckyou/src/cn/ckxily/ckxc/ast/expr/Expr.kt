package cn.ckxily.ckxc.ast.expr

import cn.ckxily.ckxc.codegen.ASTConsumer
import cn.ckxily.ckxc.ast.decl.Decl

enum class ExprId(val str: String) {
	DeclRefExpr("Declaration reference expression"),
	MemberAccessExpr("Member access expression"),
	IntegralLiteralExpr("Integral literal"),
	FloatingLiteralExpr("Floating literal"),
	UnaryExpr("Unary expression"),
	BinaryExpr("Binary expression"),
	ShortcutBinaryExpr("Shortcut binary expression")
}

enum class UnaryOpCode(val str: String) {
	PreInc("Pre increment"),
	PreDec("Pre decrement"),
	Positive("Unary positive"),
	Negative("Unary negative"),
	Not("Logical not"),
	BitwiseRevert("Bitwise revert"),
	AddressOf("Taking address"),
	DePointer("Dereferencing pointer")
}

enum class BinaryOpCode(val str: String) {
	Add("Add"),
	Sub("Substract"),
	Mul("Multiply"),
	Div("Divide"),
	Mod("Mod"),
	BitwiseAnd("Bitwise and"),
	BitwiseOr("Bitwise or"),
	BitwiseXor("Bitwise Xor"),
	LogicAnd("Logical and"),
	LogicOr("Logical or"),
}

val ExprId.description get() = str

val UnaryOpCode.description get() = str

abstract class Expr(val exprId: ExprId) {
	abstract fun accept(astConsumer: ASTConsumer): Any?
}

class DeclRefExpr(val decl: Decl) : Expr(ExprId.DeclRefExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitDeclRefExpr(this)
}

class MemberAccessExpr(val decl: Decl, val base: Expr, val byPointer: Boolean)
	: Expr(ExprId.MemberAccessExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}

class IntegralLiteralExpr(val value: Int) : Expr(ExprId.IntegralLiteralExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}

class FloatingLiteralExpr(val value: Double) : Expr(ExprId.FloatingLiteralExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}

class UnaryExpr(val opCode: UnaryOpCode) : Expr(ExprId.UnaryExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}

class BinaryExpr(open val opCode: BinaryOpCode) : Expr(ExprId.BinaryExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}
