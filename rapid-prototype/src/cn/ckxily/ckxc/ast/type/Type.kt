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
	Float("Float")
}

data class TypeSpecifiers(var isConst: Boolean, var isVolatile: Boolean)

sealed class Type(var typeId: TypeId, var specifiers: TypeSpecifiers)

class BuiltinType(val builinTypeId: BuiltinTypeId, specifiers: TypeSpecifiers) : Type(TypeId.Builtin, specifiers)

class PointerType(var pointee: Type, specifiers: TypeSpecifiers) : Type(TypeId.Pointer, specifiers)

class ReferenceType(var referenced: Type, specifiers: TypeSpecifiers) : Type(TypeId.Reference, specifiers)

class ClassType(var decl: ClassDecl, specifiers: TypeSpecifiers) : Type(TypeId.Class, specifiers)

class EnumType(var decl: EnumDecl, specifiers: TypeSpecifiers) : Type(TypeId.Enum, specifiers)
