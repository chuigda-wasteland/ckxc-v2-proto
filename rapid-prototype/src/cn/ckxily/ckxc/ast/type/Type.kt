package cn.ckxily.ckxc.ast.type

import cn.ckxily.ckxc.ast.decl.*

enum class TypeId(val str: String) {
	Builtin("Builtin"),
	Class("Class"),
	Enum("Enum")
}

enum class BuiltinTypeId(val str: String) {
	Int8("8bit Integer"),
  Int16("16bit Integer"),
	Int32("32bit Integer"),
	Float("Float")
}

abstract class Type(var typeId: TypeId)

class BuiltinType(val builinTypeId: BuiltinTypeId) : Type(TypeId.Builtin)

class ClassType(var decl: ClassDecl) : Type(TypeId.Class)

class EnumType(var decl: EnumDecl) : Type(TypeId.Enum)
