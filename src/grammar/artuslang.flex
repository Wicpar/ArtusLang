package com.artuslang.lang.parser;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static generated.GeneratedTypes.*;

%%

%{
  public _ArtusLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _ArtusLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

EOL=\R
WHITE_SPACE=[ \t\n\x0B\f\r]+
ESC=(\\(['\"`\\?abtnvfre] | {UNICODE} | {HEXADECIMAL} | {ESCOCT}))
UNICODE=(u[0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F])
ESCOCT=([0-7] [0-7] [0-7])
DECIMAL=[1-9][0-9]*
SDECIMAL=-{DECIMAL}
OCTAL=0o[0-7]+
HEXADECIMAL=0x[0-9a-fA-F]+
BINARY=0b[0-1]+
FLOAT=-?([1-9][0-9]* | '0')(\.[0-9]*)?(('e' | 'E')(-|\+)?[1-9][0-9]*)?
CHAR='([^'\\]|{ESC})'
STRING=(\"([^\"\\]|{ESC})*\")
EXPR=(\`([^\"\\]|{ESC})*\`)
NAME_=[a-zA-Z_][0-9a-zA-Z_]*
OP=(('<' | '>')[;\[\](){}&#|\^@%!:?/*$§+=<>,.\-]+)|([;\[\](){}&#|\^@%!:?/*$§+=,.\-]+)

%%
<YYINITIAL> {
  {WHITE_SPACE}      { return WHITE_SPACE; }

  ";"                { return SEMI; }

  {EOL}              { return EOL; }
  {WHITE_SPACE}      { return WHITE_SPACE; }
  {ESC}              { return ESC; }
  {UNICODE}          { return UNICODE; }
  {ESCOCT}           { return ESCOCT; }
  {DECIMAL}          { return DECIMAL; }
  {SDECIMAL}         { return SDECIMAL; }
  {OCTAL}            { return OCTAL; }
  {HEXADECIMAL}      { return HEXADECIMAL; }
  {BINARY}           { return BINARY; }
  {FLOAT}            { return FLOAT; }
  {CHAR}             { return CHAR; }
  {STRING}           { return STRING; }
  {EXPR}             { return EXPR; }
  {NAME_}            { return NAME_; }
  {OP}               { return OP; }

}

[^] { return BAD_CHARACTER; }
