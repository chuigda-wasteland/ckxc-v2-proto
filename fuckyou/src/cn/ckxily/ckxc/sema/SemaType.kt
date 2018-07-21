package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.type.BuiltinType
import cn.ckxily.ckxc.ast.type.BuiltinTypeId
import cn.ckxily.ckxc.ast.type.Type
import cn.ckxily.ckxc.ast.type.TypeId

fun Type.isInteger(): Boolean {
	if (this.typeId == TypeId.Builtin) {
		val builtinType = this as BuiltinType
		if (builtinType.builtinTypeId == BuiltinTypeId.Int8
				|| builtinType.builtinTypeId == BuiltinTypeId.Int16
				|| builtinType.builtinTypeId == BuiltinTypeId.Int32
				|| builtinType.builtinTypeId == BuiltinTypeId.Int64) {
			return true
		}
	}
	return false
}

fun Type.isFloating(): Boolean {
	if (this.typeId == TypeId.Builtin) {
		val builtinType = this as BuiltinType
		if (builtinType.builtinTypeId == BuiltinTypeId.Float) {
			return true
		}
	}
	return false
}

class TypeUtility {
	companion object {
		fun commonType(ty1: Type, ty2: Type): Type? {
			if (ty1.isInteger() && ty2.isInteger()
					|| (ty1.isFloating() && ty2.isFloating())) {
				val resultType = commonBuiltinType(ty1 as BuiltinType, ty2 as BuiltinType)
				if (ty1.specifiers.isConst || ty2.specifiers.isConst) {
					resultType.specifiers.isConst = true
				}
				if (ty1.specifiers.isVolatile || ty2.specifiers.isVolatile) {
					resultType.specifiers.isVolatile = true
				}
				return resultType
			}
			return null
		}

		fun commonBuiltinType(ty1: BuiltinType, ty2: BuiltinType): BuiltinType =
			if (ty1.builtinTypeId.rank > ty2.builtinTypeId.rank) ty1 else ty2
	}
}
