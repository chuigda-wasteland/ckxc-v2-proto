package cn.ckxily.ckxc.ast.type

import cn.ckxily.ckxc.ast.decl.ClassDecl
import cn.ckxily.ckxc.ast.decl.EnumDecl

enum class TypeId {
	Builtin,
	Pointer,
	Reference,
	Class,
	Enum;

	val str get() = name
}

enum class BuiltinTypeId(val str: String) {
	Int8("8bit Integer"),
	Int16("16bit Integer"),
	Int32("32bit Integer"),
	Int64("64bit Integer"),
	Float("Float")
}

data class TypeSpecifiers(var isConst: Boolean, var isVolatile: Boolean) {
	override fun toString() = buildString {
		if (isConst) append("Const")
		if (isVolatile) {
			if (isConst) {
				append(' ')
			}
			append("Volatile")
		}
	}
}

fun getCVSpecifiers() = TypeSpecifiers(true, true)
fun getCSpecifier() = TypeSpecifiers(true, false)
fun getVSpecifier() = TypeSpecifiers(false, true)
fun getNoSpecifier() = TypeSpecifiers(false, false)

sealed class Type(var typeId: TypeId, var specifiers: TypeSpecifiers) {
	abstract override fun toString(): String
}

class BuiltinType(val builinTypeId: BuiltinTypeId, specifiers: TypeSpecifiers) : Type(TypeId.Builtin, specifiers) {
	override fun toString() = "${builinTypeId.str} $specifiers"
}

class PointerType(var pointee: Type, specifiers: TypeSpecifiers) : Type(TypeId.Pointer, specifiers) {
	override fun toString() = "$specifiers pointer to $pointee"
}

class ReferenceType(var referenced: Type, specifiers: TypeSpecifiers) : Type(TypeId.Reference, specifiers) {
	override fun toString() = "$specifiers reference to $referenced"
}

class ClassType(var decl: ClassDecl, specifiers: TypeSpecifiers) : Type(TypeId.Class, specifiers) {
	override fun toString() = "${decl.nameStr} $specifiers"
}

class EnumType(var decl: EnumDecl, specifiers: TypeSpecifiers) : Type(TypeId.Enum, specifiers) {
	override fun toString() = "${decl.nameStr} $specifiers"
}
