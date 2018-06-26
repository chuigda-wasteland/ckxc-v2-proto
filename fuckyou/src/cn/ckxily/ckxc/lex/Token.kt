package cn.ckxily.ckxc.lex

enum class TokenType(val str: String) {
	Vi8("vi8"),
	Vi16("vi16"),
	Vi32("vi32"),
	Vi64("vi64"),
	Vu8("vu8"),
	Vu16("vu16"),
	Vu32("vu32"),
	Vu64("vu64"),
	Vr32("vr32"),
	Vr64("vr64"),
	Class("class"),
	Enum("enum"),
	Const("const"),
	Volatile("volatile"),
	Func("fn"),
	Number("Number\$"),
	Id("Identifier\$"),
	Colon(":"),
	Semicolon(";"),
	Comma(","),
	Period("."),
	Lt("<"),
	Gt(">"),
	Eq("="),
	DupEq("=="),
	Arrow("->"),
	Add("+"),
	Sub("-"),
	Mul("*"),
	Div("/"),
	Not("!"),
	Neq("!="),
	Amp("&"),
	LeftBrace("{"),
	RightBrace("}"),
	LeftBracket("["),
	RightBracket("]"),
	LeftParen("("),
	RightParen(")"),
	EOI("EOI\$")
}

class Token(val tokenType: TokenType, val value: Any? = null) {
	override fun toString(): String {
		return "${tokenType} (${value ?: tokenType.str})"
	}
}
