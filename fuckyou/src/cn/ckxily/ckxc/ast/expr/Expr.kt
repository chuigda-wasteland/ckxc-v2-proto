package cn.ckxily.ckxc.ast.expr

import cn.ckxily.ckxc.codegen.ASTConsumer
import cn.ckxily.ckxc.ast.decl.Decl
import cn.ckxily.ckxc.ast.type.Type

enum class ExprId(val desc: String) {
	DeclRefExpr("Declaration reference expression"),
	MemberAccessExpr("Member access expression"),
	IntegralLiteralExpr("Integral literal"),
	FloatingLiteralExpr("Floating literal"),
	UnaryExpr("Unary expression"),
	BinaryExpr("Binary expression"),
	ImplicitCastExpr("Implicit cast expression")
}

enum class ValueCategory(val desc: String) {
	LValue("Left value"),
	RValue("Right value")
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

	fun getValueCategory(): ValueCategory {
		if (cachedValueCategory == null) {
			cachedValueCategory = getValueCategoryImpl()
		}
		return cachedValueCategory!!
	}

	private var cachedValueCategory: ValueCategory? = null

	protected abstract fun getValueCategoryImpl(): ValueCategory
}

class DeclRefExpr(val decl: Decl) : Expr(ExprId.DeclRefExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitDeclRefExpr(this)

	override fun getValueCategoryImpl(): ValueCategory = ValueCategory.LValue
}

class MemberAccessExpr(val decl: Decl, val base: Expr, val byPointer: Boolean)
	: Expr(ExprId.MemberAccessExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getValueCategoryImpl(): ValueCategory = ValueCategory.LValue
}

class IntegralLiteralExpr(val value: Int) : Expr(ExprId.IntegralLiteralExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getValueCategoryImpl(): ValueCategory = ValueCategory.RValue
}

class FloatingLiteralExpr(val value: Double) : Expr(ExprId.FloatingLiteralExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getValueCategoryImpl(): ValueCategory = ValueCategory.RValue
}

class UnaryExpr(val opCode: UnaryOpCode) : Expr(ExprId.UnaryExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getValueCategoryImpl(): ValueCategory = when(opCode) {
		UnaryOpCode.DePointer -> ValueCategory.LValue
		else -> ValueCategory.RValue
	}
}

class BinaryExpr(val opCode: BinaryOpCode) : Expr(ExprId.BinaryExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getValueCategoryImpl(): ValueCategory = when(opCode) {
		BinaryOpCode.Assign -> ValueCategory.LValue
		else -> ValueCategory.RValue
	}
}

enum class CastOperation(val desc: String) {
	UpgradeCast(""),
	DowngradeCast(""),
	LValueToRValueDecay(""),
	AddConst(""),
	RemoveConst("")
}

class ImplicitCastExpr(val castOp: CastOperation, val expr: Expr, val destType: Type) {

}
