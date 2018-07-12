# About project ckxc-v2-proto

## Intention
Before building the formal project ckxc-v2, we must build a rapid prototype. Kotlin was choosed for its expressive grammer and null safety feature. However, we still use C++ for the formal project.

## steps
To build this rapid prototype, we will follow 4 steps:

 - Build a simple frontend for the not usable "ckx declaration language", which supports basic variable/tag/function declarations
 - Add expression system to the frontend of "ckx declaration language", it becomes "ckx calculation language"
 - Add function body and statement grammer support to frontend of "ckx calculation language" and it then becomes "ckx language frontend"
 - Add a proper "compiler" backend (e.g. compile to CMS) and a proper "interpreter" backend (REPL)  

## ckx declaration language
    declaration
        variable-declaration
        binding-declaration
        function-declaration
        class-declaration
        enum-declaration
 
    variable-declaration
        type declarators
    binding-declaration
        ??? wobuzhidaozhegebnfyinggaizenmexiele
    function-declaration
        fn fn-name ( param-type-list ) : ret-type SKT-T1-Faker
    SKT-T1-Faker
        ;
        compound-statement
    class-declaration
        class class-name { declarations }
    enum-declaration
        enum enum-name { enumerators }
