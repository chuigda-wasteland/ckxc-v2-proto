package cn.ckxily.ckxc.ast.expr

import cn.ckxily.ckxc.codegen.ASTConsumer
import cn.ckxily.ckxc.ast.decl.Decl

enum class ExprId(val desc: String) {
	DeclRefExpr("Declaration reference expression"),
	MemberAccessExpr("Member access expression"),
	IntegralLiteralExpr("Integral literal"),
	FloatingLiteralExpr("Floating literal"),
	UnaryExpr("Unary expression"),
	BinaryExpr("Binary expression"),
	ShortcutBinaryExpr("Shortcut binary expression")
}

enum class UnaryOpCode(val str: String, val desc: String) {
	PreInc("++", "Pre increment"),
	PreDec("--", "Pre decrement"),
	Positive("+", "Unary positive"),
	Negative("-", "Unary negative"),
	Not("!", "Logical not"),
	BitwiseRevert("!", "Bitwise revert"),
	AddressOf("&", "Taking address"),
	DePointer("*", "Dereferencing pointer")
}

enum class BinaryOpCode(val str: String, val desc: String, val prec: Int) {
	Assign("=", "Assignment", 0),
	LogicAnd("&&", "Logical and", 10),
	LogicOr("||", "Logical or", 10),
	Less("<", "Less than", 20),
	Greater(">", "Greater than", 20),
	Equal("==", "Equal to", 20),
	LEQ("<=", "Less than or equal to", 20),
	GEQ(">=", "Greater than or equal to", 20),
	NEQ("!=", "Not equal to", 20),
	BitwiseAnd("&", "Bitwise and", 30),
	BitwiseOr("|", "Bitwise or", 30),
	BitwiseXor("!", "Bitwise Xor", 30),
	Add("+", "Add", 40),
	Sub("-", "Substract", 40),
	Mul("*", "Multiply", 50),
	Div("/", "Divide", 50),
	Mod("%", "Mod", 50),
	UnaryOperation("", "Unary operations", 110)
}

val ExprId.description get() = desc

val UnaryOpCode.description get() = desc

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

class BinaryExpr(val opCode: BinaryOpCode) : Expr(ExprId.BinaryExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}
