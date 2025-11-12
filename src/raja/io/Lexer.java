/* $Id: Lexer.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
 * Copyright (C) 1999-2000 E. Fleury & G. Sutre
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package raja.io;

import java.io.*;


class Lexer
{
  static final Token CLASS = new Token ();
  static final Token LABEL = new Token ();
  static final Token FIELD = new Token ();
  static final Token NUMBER = new Token ();
  static final Token COMMA = new Token ();
  static final Token SEMICOLON = new Token ();
  static final Token EQUAL = new Token ();
  static final Token PRIOR = new Token ();
  static final Token LPAR = new Token ();
  static final Token RPAR = new Token ();
  static final Token LBRA = new Token ();
  static final Token RBRA = new Token ();
  static final Token LSQBRA = new Token ();
  static final Token RSQBRA = new Token ();
  static final Token DBLELSQBRA = new Token ();
  static final Token DBLERSQBRA = new Token ();
  
  //ADDED by Murat Inan - New token types for String, Boolean and Character
  static final Token STRING = new Token ();
  static final Token BOOLEAN = new Token ();
  static final Token CHARACTER = new Token ();
  
  /**  Special token mark the End Of Stream (EOS) */
  static Token EOS = new Token();
  
  private Reader stream;
  private StringBuffer svalue;
  private int inputChar, nbLines, nbCol, nbCurrentCol;
  
  Lexer(Reader r) throws IOException
  {
    stream = r;
    svalue = new StringBuffer();
    inputChar = stream.read();
    nbLines = 1;
    nbCol = 0;
  }
  
  /** Gives the string value of the current token */
  String getLexedString()
  {
    return svalue.toString();
  }
  
  /** Gives the current line number */
  int getLine()
  {
    return nbLines;
  }
  
  /** Gives the current column number */
  int getCol()
  {
    return nbCol;
  }
  
  private void readNextChar() throws IOException
  {
    if (inputChar == '\n')
    {
      nbLines++;
      nbCurrentCol = 0;
    }
    else
    {
      nbCurrentCol++;
    }
    
    svalue.append((char) inputChar);
    inputChar = stream.read();
  }
  
  //ADDED by Murat Inan - Helper method to peek at next characters without consuming them
  private boolean peekNextChars(String chars) throws IOException {
    stream.mark(chars.length());
    boolean matches = true;
    for (int i = 0; i < chars.length(); i++) {
      int nextChar = stream.read();
      if (nextChar != chars.charAt(i)) {
        matches = false;
        break;
      }
    }
    stream.reset();
    return matches;
  }
  
  /** Read the next token */
  Token nextToken()
  throws IOException
  {
    svalue = new StringBuffer();
    nbCol = nbCurrentCol;
    
    Token token = null;
    
    if (inputChar == -1)
    {
      return(EOS);
    }
    
    if (inputChar == ',')
    {
      token = COMMA;
      readNextChar();
    }
    else if (inputChar == ';')
    {
      token = SEMICOLON;
      readNextChar();
    }
    else if (inputChar == '=')
    {
      token = EQUAL;
      readNextChar();
    }
    else if (inputChar == '>')
    {
      readNextChar();
      if (inputChar == '>')
      {
        token = PRIOR;
        readNextChar();
      }
      else
      {
        throw new NoMatchingTokenException(svalue.toString() + ((char) inputChar));
      }
    }
    else if (inputChar == '(')
    {
      token = LPAR;
      readNextChar();
    }
    else if (inputChar == ')')
    {
      token = RPAR;
      readNextChar();
    }
    else if (inputChar == '{')
    {
      token = LBRA;
      readNextChar();
    }
    else if (inputChar == '}')
    {
      token = RBRA;
      readNextChar();
    }
    else if (inputChar == '[')
    {
      readNextChar();
      if (inputChar == '[')
      {
        token = DBLELSQBRA;
        readNextChar();
      }
      else
      {
        token = LSQBRA;
      }
    }
    else if (inputChar == ']')
    {
      readNextChar();
      if (inputChar == ']')
      {
        token = DBLERSQBRA;
        readNextChar();
      }
      else
      {
        token = RSQBRA;
      }
    }
    //ADDED by Murat Inan - String literal support
    else if (inputChar == '\"')
    {
      readNextChar(); // consume opening quote
      while (inputChar != '\"' && inputChar != -1 && inputChar != '\n') {
        readNextChar();
      }
      if (inputChar == '\"') {
        readNextChar(); // consume closing quote
        token = STRING;
        } else {
        throw new NoMatchingTokenException("Unterminated string literal");
      }
    }
    //ADDED by Murat Inan - Character literal support
    else if (inputChar == '\'')
    {
      readNextChar(); // consume opening quote
      if (inputChar != -1 && inputChar != '\n') {
        readNextChar(); // read the character
        if (inputChar == '\'') {
          readNextChar(); // consume closing quote
          token = CHARACTER;
          } else {
          throw new NoMatchingTokenException("Unterminated character literal");
        }
        } else {
        throw new NoMatchingTokenException("Empty character literal");
      }
    }
    //ADDED by Murat Inan - Boolean literal support
    else if (inputChar == 't' && peekNextChars("rue"))
    {
      // true
      readNextChar(); // t
      readNextChar(); // r
      readNextChar(); // u
      readNextChar(); // e
      token = BOOLEAN;
    }
    else if (inputChar == 'f' && peekNextChars("alse"))
    {
      // false
      readNextChar(); // f
      readNextChar(); // a
      readNextChar(); // l
      readNextChar(); // s
      readNextChar(); // e
      token = BOOLEAN;
    }
    else if (Character.isLetter((char) inputChar) && Character.isUpperCase((char) inputChar))
    {
      readNextChar();
      while ((inputChar == '_') || Character.isLetter((char) inputChar) || Character.isDigit((char) inputChar))
      {
        readNextChar();
      }
      token = CLASS;
    }
    else if (Character.isLetter((char) inputChar) && Character.isLowerCase((char) inputChar))
    {
      readNextChar();
      
      while ((Character.isLetter((char) inputChar) && Character.isLowerCase((char) inputChar)) || Character.isDigit((char) inputChar))
      {
        readNextChar();
      }
      
      if (inputChar == '.')
      {
        // Lexing a CLASS
        readNextChar();
        
        while ((inputChar == '.') || (Character.isLetter((char) inputChar) && Character.isLowerCase((char) inputChar)))
        {
          readNextChar();
        }
        
        if (Character.isLetter((char) inputChar) && Character.isUpperCase((char) inputChar))
        {
          while ((inputChar == '_') || Character.isLetter((char) inputChar) || Character.isDigit((char) inputChar))
          {
            readNextChar();
          }
          token = CLASS;
        }
        else
        {
          throw new NoMatchingTokenException(svalue.toString() + ((char) inputChar));
        }
      }
      else
      {
        // Lexing a FIELD
        while ((inputChar == '_') || Character.isLetter((char) inputChar) || Character.isDigit((char) inputChar))
        {
          readNextChar();
        }
        token = FIELD;
      }
    }
    else if (inputChar == '@')
    {
      readNextChar();
      while ((inputChar == '_') || (inputChar == '.') || Character.isLetter((char) inputChar) || Character.isDigit((char) inputChar))
      {
        readNextChar();
      }
      token = LABEL;
    }
    else if (Character.isDigit((char) inputChar) || (inputChar == '-'))
    {
      readNextChar();
      while ((inputChar != '.') && Character.isDigit((char) inputChar))
      {
        readNextChar();
      }
      if (inputChar == '.')
      {
        readNextChar();
        while (Character.isDigit((char) inputChar))
        {
          readNextChar();
        }
      }
      token = NUMBER;
    }
    else if (inputChar == '.')
    {
      readNextChar();
      while (Character.isDigit((char) inputChar))
      {
        readNextChar();
      }
      token = NUMBER;
    }
    else if (inputChar == '/')
    {
      readNextChar();
      if (inputChar == '/')
      {
        readNextChar();
        while (inputChar != '\n')
        {
          readNextChar();
        }
        return nextToken();
      }
      else
      {
        throw new NoMatchingTokenException(svalue.toString() + ((char) inputChar));
      }
    }
    else if (Character.isWhitespace((char) inputChar))
    {
      readNextChar();
      while (Character.isWhitespace((char) inputChar))
      {
        readNextChar();
      }
      return nextToken();
    }
    else
    {
      throw new NoMatchingTokenException(svalue.toString() + ((char) inputChar));
    }
    return token;
  }
  
}
