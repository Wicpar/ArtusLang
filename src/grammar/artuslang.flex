package com.artuslang.lang;

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

DECIMAL=[0-9]+
OCTAL=0o[0-7]+
HEXADECIMAL=0x[0-9a-fA-F]+
BINARY=0b[0-1]+
FLOAT=-?([1-9][0-9]* | '0')(\.[0-9]*)?(('e' | 'E')(-|\+)?[1-9][0-9]*)?
CHAR='([^'\\]|(\\(['\"`\\?abtnvfre] | (u[0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]) | 0x[0-9a-fA-F][0-9a-fA-F] | ([0-7] [0-7] [0-7]))))'
STRING=(\"([^\"\\]|(\\(['\"`\\?abtnvfre] | (u[0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]) | 0x[0-9a-fA-F][0-9a-fA-F] | ([0-7] [0-7] [0-7]))))*\")
EXPR=(\`([^\"\\]|(\\(['\"`\\?abtnvfre] | (u[0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]) | 0x[0-9a-fA-F][0-9a-fA-F] | ([0-7] [0-7] [0-7]))))*\`)
NAME_=[\p{Pc}\p{L}\p{M}\p{No}][\p{Pc}\p{L}\p{M}\p{N}]*
OP=([\p{Ps}\p{Pe}\p{Pi}\p{Pf}.,\n&&[^\"'\`]])|([\p{Pd}\p{S}\p{Po}&&[^,]])+
WHITE_SPACE=\p{Z}+

%%
<YYINITIAL> {
  {WHITE_SPACE}      { return WHITE_SPACE; }

  "SDECIMAL"         { return SDECIMAL; }

  {DECIMAL}          { return DECIMAL; }
  {OCTAL}            { return OCTAL; }
  {HEXADECIMAL}      { return HEXADECIMAL; }
  {BINARY}           { return BINARY; }
  {FLOAT}            { return FLOAT; }
  {CHAR}             { return CHAR; }
  {STRING}           { return STRING; }
  {EXPR}             { return EXPR; }
  {NAME_}            { return NAME_; }
  {OP}               { return OP; }
  {WHITE_SPACE}      { return WHITE_SPACE; }

}

[^] { return BAD_CHARACTER; }
