// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.oclexpressions._symboltable;

import de.monticore.ocl.oclexpressions._ast.ASTInDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclarationVariable;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.basicsymbols._visitor.BasicSymbolsVisitor2;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

public class OCLExpressionsSymbolTableCompleter
  implements BasicSymbolsVisitor2, OCLExpressionsVisitor2, OCLExpressionsHandler {
  protected static final String USED_BUT_UNDEFINED = "0xB0028: Type '%s' is used but not defined.";

  protected static final String DEFINED_MUTLIPLE_TIMES = "0xB0031: Type '%s' is defined more than once.";

  protected final List<ASTMCImportStatement> imports;

  protected final String packageDeclaration;

  protected OCLExpressionsTraverser traverser;

  protected DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typesCalculator) {
    if (typesCalculator != null) {
      this.typeVisitor = typesCalculator;
    }
    else {
      Log.error("0xA3201 The typesVisitor has to be set");
    }
  }

  public OCLExpressionsSymbolTableCompleter(List<ASTMCImportStatement> imports, String packageDeclaration) {
    this.imports = imports;
    this.packageDeclaration = packageDeclaration;
    typeVisitor = new DeriveSymTypeOfOCLCombineExpressions();
  }

  @Override
  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void traverse(IOCLExpressionsScope node) {
    OCLExpressionsHandler.super.traverse(node);
    for (IOCLExpressionsScope subscope : node.getSubScopes()) {
      subscope.accept(this.getTraverser());
    }
  }

  @Override
  public void visit(ASTInDeclaration ast){
    for(ASTInDeclarationVariable node: ast.getInDeclarationVariableList()) {
      VariableSymbol symbol = node.getSymbol();
      symbol.setIsReadOnly(false);
      Optional<SymTypeExpression> typeResult = Optional.empty();
      if (ast.isPresentMCType()) {
        ast.getMCType().accept(typeVisitor.getTraverser());
        typeResult = typeVisitor.getResult();
        if (!typeResult.isPresent()) {
          Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), symbol.getName()));
        } else {
          symbol.setType(typeResult.get());
        }
      }
      if (ast.isPresentExpression()) {
        ast.getExpression().accept(typeVisitor.getTraverser());
        if (typeVisitor.getTypeCheckResult().isPresentCurrentResult()) {
          //if MCType present: check that type of expression and MCType are compatible
          if (typeResult.isPresent() && !OCLTypeCheck.compatible(typeResult.get(),
              OCLTypeCheck.unwrapSet(typeVisitor.getTypeCheckResult().getCurrentResult()))) {
            Log.error(String.format("The MCType (%s) and the expression type (%s) in Symbol (%s) are not compatible",
                ast.getMCType(), OCLTypeCheck.unwrapSet(typeVisitor.getTypeCheckResult().getCurrentResult()), symbol.getName()));
          }
          //if no MCType present: symbol has type of expression
          if (!typeResult.isPresent()) {
            symbol.setType(OCLTypeCheck.unwrapSet(typeVisitor.getTypeCheckResult().getCurrentResult()));
          }
          typeVisitor.getTypeCheckResult().reset();
        } else {
          Log.error(String.format("The type of the object (%s) could not be calculated", symbol.getName()));
        }
      }
      //node has neither MCType nor expression
      if (!typeResult.isPresent() && !ast.isPresentExpression()) {
        symbol.setType(SymTypeExpressionFactory.createTypeObject("Object", ast.getEnclosingScope()));
      }
    }
  }

  @Override
  public void visit(ASTOCLVariableDeclaration ast){
    VariableSymbol symbol = ast.getSymbol();
    symbol.setIsReadOnly(false);
    if(ast.isPresentMCType()) {
      ast.getMCType().setEnclosingScope(symbol.getEnclosingScope());
      ast.getMCType().accept(getTraverser());
      ast.getMCType().accept(typeVisitor.getTraverser());
      final Optional<SymTypeExpression> typeResult = typeVisitor.getResult();
      if (!typeResult.isPresent()) {
        Log.error(String.format("The type (%s) of the object (%s) could not be calculated", ast.getMCType(), ast.getName()));
      } else {
        symbol.setType(typeResult.get());
      }
    } else {
      if(ast.isPresentExpression()){
        ast.getExpression().accept(getTraverser());
        ast.getExpression().accept(typeVisitor.getTraverser());
        if(typeVisitor.getTypeCheckResult().isPresentCurrentResult()){
          symbol.setType(typeVisitor.getTypeCheckResult().getCurrentResult());
        } else {
          Log.error(String.format("The type of the object (%s) could not be calculated", ast.getName()));
        }
      }
      else {
        symbol.setType(SymTypeExpressionFactory.createTypeObject("Object", ast.getEnclosingScope()));
      }
    }
  }

}
