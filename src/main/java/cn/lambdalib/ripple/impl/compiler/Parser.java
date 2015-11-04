package cn.lambdalib.ripple.impl.compiler;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import cn.lambdalib.ripple.IFunction;
import cn.lambdalib.ripple.Path;
import cn.lambdalib.ripple.ScriptProgram;
import cn.lambdalib.ripple.RippleException.RippleCompilerException;
import cn.lambdalib.ripple.impl.compiler.Token.MultiCharSymbol;

public class Parser {
    
    public static class ScriptObject {
        public Object value;
        public String path;
        public IFunction func;
        public int funcArgNum;
    }
    
    public ScriptProgram program;
    
    private Reader inputReader;
    private LineNumberReader lineNumberReader;
    private PushbackReader reader;
    private Token currentToken;
    
    // Used for debug
    private final String scriptName;
    private int lineNumber = 0;
    
    private ArrayList<ScriptObject> parsedObject = new ArrayList();
    private Path currentPath = new Path(null);
    
    private Parser(ScriptProgram program, Reader input, String scriptName) {
        this.program = program;
        this.scriptName = scriptName;
        
        this.inputReader = input;
        this.lineNumberReader = new LineNumberReader(input);
        this.reader = new PushbackReader(lineNumberReader);
        
        this.currentToken = new Token();
    }
    
    //parse
    
    private void parseProgram() throws IOException {
        this.readToken();
        this.parseNamespace();
        if (!this.currentToken.isEOS()) {
            throw new RippleCompilerException("Invalid token. Should be end of stream but got" + currentToken, this);
        }
        this.reader.close();
        this.lineNumberReader.close();
        this.inputReader.close();
    }
    
    private Path parsePath(String first) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        //first has been skipped
        while (currentToken.isSingleChar('.')) {
            sb.append('.');
            this.readToken();
            if (!currentToken.isIdentifier()) {
                throw new RippleCompilerException("Invalid token. Should be identifier", this);
            }
            sb.append(currentToken.str);
            this.readToken();
        }
        return new Path(sb.toString());
    }
    
    private void parseNamespace() throws IOException {
        while (currentToken.isIdentifier()) {
            String name = currentToken.str;
            this.readToken();
            Path path = this.parsePath(name);
            if (currentToken.isSingleChar('{')) {
                this.readToken();
                Path parentPath = currentPath;
                currentPath = new Path(parentPath, path);
                
                this.parseNamespace();
                
                this.currentPath = parentPath;
                if(currentToken.isInteger() || currentToken.isDouble()) {
                	this.parseValue(new Path(currentPath, path));
                }
                if (!currentToken.isSingleChar('}')) {
                    throw new RippleCompilerException("Invalid token. Should be '}' but got " + currentToken, this);
                }
                this.readToken();
            } else if (currentToken.isSingleChar('(')) {
                this.parseFunction(new Path(currentPath, path));
            } else {
                throw new RippleCompilerException("Invalid token. Should be function or namespace", this);
            }
        }
    }
    
    private void parseFunction(Path functionPath) throws IOException {
        CodeGenerator gen = new CodeGenerator(this, functionPath);
        
        //param list
        this.readToken();
        int nargs = 0;
        if (!currentToken.isSingleChar(')')) {
            //has params
            while (true) {
                if (!currentToken.isIdentifier()) {
                    throw new RippleCompilerException("Invalid token. Should be parameter name", this);
                }
                gen.addParameter(currentToken.str);
                ++nargs;
                this.readToken();
                if (currentToken.isSingleChar(')')) {
                    break;
                }
                if (!currentToken.isSingleChar(',')) {
                    throw new RippleCompilerException("Invalid token. Should be ','", this);
                }
                this.readToken();
            }
        }
        this.readToken(); //skip ')'
        if (!currentToken.isSingleChar('{')) {
            throw new RippleCompilerException("Invalid token. Should be '{'", this);
        }
        this.readToken(); //skip '{'
        gen.functionBodyBegin();
        this.parseExpression(gen);
        IFunction f = gen.functionBodyEnd();
        if (!currentToken.isSingleChar('}')) {
            throw new RippleCompilerException("Invalid token. Should be '}'", this);
        }
        this.readToken();
        
        ScriptObject obj = new ScriptObject();
        obj.path = functionPath.path;
        obj.func = f;
        obj.funcArgNum = nargs;
        
        // System.out.println("Parsed function " + functionPath.path);
        this.parsedObject.add(obj);
    }
    
    private void parseValue(Path valuePath) throws IOException {
    	ScriptObject obj = new ScriptObject();
    	obj.path = valuePath.path;
    	if(currentToken.isDouble())
    		obj.value = currentToken.doubleValue;
    	else if(currentToken.isInteger())
    		obj.value = currentToken.integerValue;
    	else
    		throw new RippleCompilerException("Invalid value when parsing value: " + currentToken, this);
    	this.readToken();
    	
    	// System.out.println("Parsed value " + valuePath.path);
    	this.parsedObject.add(obj);
    }
    
    private void parseExpression(CodeGenerator gen) throws IOException {
        this.parseSubExpr(gen, 0);
    }
    
    private void parseSubExpr(CodeGenerator gen, int priority) throws IOException {
        UnaryOperator unop = currentToken.toUnOp();
        if (unop != UnaryOperator.UNKNOWN) {
            this.readToken();
            this.parseSubExpr(gen, BinaryOperator.MAX_PRIORITY);
            gen.calcUnary(unop);
        } else {
            this.parseSimpleExpr(gen);
        }
        BinaryOperator binop = currentToken.toBinOp();
        while (binop.priority > priority) {
            this.readToken();
            this.parseSubExpr(gen, binop.priority);
            gen.calcBinary(binop);
            binop = currentToken.toBinOp();
        }
    }
    
    private void parseSimpleExpr(CodeGenerator gen) throws IOException {
        if (currentToken.isKeyword("true")) {
            gen.pushBooleanConst(true);
            this.readToken();
        } else if (currentToken.isKeyword("false")) {
            gen.pushBooleanConst(false);
            this.readToken();
        } else if (currentToken.isInteger()) {
            gen.pushIntegerConst(currentToken.integerValue);
            this.readToken();
        } else if (currentToken.isDouble()) {
            gen.pushDoubleConst(currentToken.doubleValue);
            this.readToken();
        } else if (currentToken.isSingleChar('(')) {
            this.readToken();
            this.parseExpression(gen);
            if (!currentToken.isSingleChar(')')) {
                throw new RippleCompilerException("Invalid token. Should be ')'", this);
            }
            this.readToken();
        } else if (currentToken.isKeyword("switch")) {
            this.readToken();
            if (!currentToken.isSingleChar('(')) {
                throw new RippleCompilerException("Invalid token. Should be '('", this);
            }
            this.readToken();
            
            this.parseExpression(gen);
            
            if (!currentToken.isSingleChar(')')) {
                throw new RippleCompilerException("Invalid token. Should be ')'", this);
            }
            this.readToken();
            
            gen.pushSwitchBlock();
            if (!currentToken.isSingleChar('{')) {
                throw new RippleCompilerException("Invalid token. Should be '{'", this);
            }
            
            do {
                this.readToken();
                if (currentToken.isKeyword("default")) {
                    this.readToken();
                    gen.switchCaseDefault();
                } else if (currentToken.isKeyword("when")) {
                    this.readToken();
                    this.parseExpression(gen);
                    gen.switchCase(true);
                } else {
                    this.parseExpression(gen);
                    gen.switchCase(false);
                }
                
                if (!currentToken.isSingleChar(':')) {
                    throw new RippleCompilerException("Invalid token. Should be ':'", this);
                }
                this.readToken();
                
                this.parseExpression(gen);
                gen.switchCaseEnd();
            } while (currentToken.isSingleChar(';'));

            if (!currentToken.isSingleChar('}')) {
                throw new RippleCompilerException("Invalid token. Should be '}'", this);
            }
            
            this.readToken();
            gen.popSwitchBlock();
        } else if (currentToken.isIdentifier()) {
            String name = currentToken.str;
            this.readToken();
            if (currentToken.isSingleChar('.') || currentToken.isSingleChar('(')) {
                Path path = this.parsePath(name);
                if (currentToken.isSingleChar('(')) {
                    gen.beforeCallFunction(Path.concatenate(this.currentPath, path));
                    this.readToken();
                    int nargs = 0;
                    if (!currentToken.isSingleChar(')')) {
                        ++nargs;
                        this.parseExpression(gen);
                        while (currentToken.isSingleChar(',')) {
                            this.readToken();
                            ++nargs;
                            this.parseExpression(gen);
                        }
                        if (!currentToken.isSingleChar(')')) {
                            throw new RippleCompilerException("Invalid token. Should be ')'", this);
                        }
                    }
                    this.readToken(); //skip ')'
                    gen.afterCallFunction(nargs);
                } else {
                    //namespace value
                    throw new RippleCompilerException("Value reference is not supported", this);
                }
            } else {
                //param
                gen.pushParameter(name);
            }
        } else {
            throw new RippleCompilerException("Invalid token. Should be expression", this);
        }
    }
    
    //lex

    private char peekChar() throws IOException {
        char c = (char) reader.read();
        reader.unread(c);
        return c;
    }
    
    private void readNumber(char first) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        char c;
        while (Character.isDigit(c = peekChar())) {
            reader.read();
            sb.append(c);
        }
        if (peekChar() == '.') {
            reader.read();
            sb.append('.');
            while (Character.isDigit(c = peekChar())) {
                reader.read();
                sb.append(c);
            }
            String resultStr = sb.toString();
            if (resultStr.endsWith(".")) {
                throw new RippleCompilerException("Invalid number format", this);
            }
            //double
            currentToken.setDouble(Double.parseDouble(resultStr));
            return;
        }
        //integer
        currentToken.setInteger(Integer.parseInt(sb.toString()));
    }
    
    private void readIdentifier(char first) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        char c = peekChar();
        while (Character.isLetterOrDigit(c) || c == '_') {
            reader.read();
            sb.append(c);
            c = peekChar();
        }
        currentToken.setString(sb.toString());
    }
    
    private void skipLine() throws IOException {
        char c = peekChar();
        while (c != '\r' && c != '\n') {
            reader.read();
            c = peekChar();
        }
    }
    
    private void readToken() throws IOException {
        int charEnd = reader.read();
        char c;
        if (charEnd == -1) {
            currentToken.setEOS();
            return;
        }
        c = (char) charEnd;
        switch (c) {
        //single char
        case '+':
        case '-':
        case '*':
        case '/':
        case '(':
        case ')':
        case '{':
        case '}':
        case '.':
        case ',':
        case ';':
        case ':':
        case '=': //currently no ==
        case '&':
        case '|':
            currentToken.setSingleChar(c);
            break;
        //followed by '='
        case '>':
            if (peekChar() == '=') {
                currentToken.setMultiChar(MultiCharSymbol.S_GE);
                reader.read();
            } else {
                currentToken.setSingleChar(c);
            }
            break;
        case '<':
            if (peekChar() == '=') {
                currentToken.setMultiChar(MultiCharSymbol.S_LE);
                reader.read();
            } else {
                currentToken.setSingleChar(c);
            }
            break;
        case '!':
            if (peekChar() == '=') {
                currentToken.setMultiChar(MultiCharSymbol.S_NE);
                reader.read();
            } else {
                currentToken.setSingleChar(c);
            }
            break;
        //number
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            readNumber(c);
            break;
        case '#':
            skipLine();
            readToken(); //read again
            return;
        default:
            if (Character.isWhitespace(c)) {
                readToken(); //read again
                if(c == '\n' || c == '\r')
                	lineNumber++;
                return;
            } else if (Character.isLetter(c) || c == '_') {
                readIdentifier(c);
                return;
            } else {
                throw new RippleCompilerException("Unknown token", this);
            }
        }
    }
    
    //api
    public static List<ScriptObject> parse(ScriptProgram program, Reader input, String scriptName) {
        Parser p = new Parser(program, input, scriptName);
        try {
            p.parseProgram();
        } catch (IOException e) {
            throw new RippleCompilerException(e, p);
        }
        return p.parsedObject;
    }
    
    //debug
    public int getLineNumber() {
    	return lineNumber;
    }
    
    public String getScriptName() {
    	return scriptName;
    }
}
