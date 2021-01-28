package de.monticore.ocl.types.check;

import de.monticore.cdassociation._ast.ASTCDRole;
import de.monticore.cdassociation._symboltable.CDRoleSymbol;
import de.monticore.expressions.commonexpressions._ast.ASTCallExpression;
import de.monticore.expressions.commonexpressions._ast.ASTConditionalExpression;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.commonexpressions._ast.ASTInfixExpression;
import de.monticore.expressions.prettyprint.CommonExpressionsFullPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbolSurrogate;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.monticore.ocl.types.check.OCLTypeCheck.compatible;
import static de.monticore.ocl.types.check.OCLTypeCheck.isBoolean;

public class DeriveSymTypeOfCommonExpressions
  extends de.monticore.types.check.DeriveSymTypeOfCommonExpressions {

  /**
   * All methods in this class are identical to the methods in
   * de.monticore.types.check.DeriveSymTypeOfCommonExpressions.
   * This class is used to ensure that OCLTypeCheck methods are
   * used instead of the normal TypeCheck methods.
   */

  @Override
  public void traverse(ASTFieldAccessExpression expr) {
    CommonExpressionsFullPrettyPrinter printer = new CommonExpressionsFullPrettyPrinter(
      new IndentPrinter());
    SymTypeExpression innerResult;
    expr.getExpression().accept(getTraverser());
    if (typeCheckResult.isPresentCurrentResult()) {
      //store the type of the inner expression in a variable
      innerResult = typeCheckResult.getCurrentResult();
      //look for this type in our scope
      TypeSymbol innerResultType = innerResult.getTypeInfo();
      //search for a method, field or type in the scope of the type of the inner expression
      List<VariableSymbol> fieldSymbols = innerResult
        .getFieldList(expr.getName(), typeCheckResult.isType());
      Optional<TypeSymbol> typeSymbolOpt = innerResultType.getSpannedScope()
        .resolveType(expr.getName());
      if (!fieldSymbols.isEmpty()) {
        //cannot be a method, test variable first
        //durch AST-Umbau kann ASTFieldAccessExpression keine Methode sein
        //if the last result is a type then filter for static field symbols
        if (typeCheckResult.isType()) {
          fieldSymbols = filterStaticFieldSymbols(fieldSymbols);
        }
        if (fieldSymbols.size() != 1) {
          if (!checkIfCorrect(fieldSymbols)) {
            typeCheckResult.reset();
            logError("0xA0237", expr.get_SourcePositionStart());
          }
        }
        if (!fieldSymbols.isEmpty()) {
          VariableSymbol var = fieldSymbols.get(0);
          SymTypeExpression type = var.getType();

          //TODO: Fixt dass CD4A keine Arraytypen für Assiziationen setzt
          if (var.getAstNode() instanceof ASTCDRole) {
            ASTCDRole astRole = (ASTCDRole) var.getAstNode();
            CDRoleSymbol symbol = astRole.getSymbol();
            if (symbol.isPresentCardinality()) {
              if (symbol.getCardinality().isMult() ||
                symbol.getCardinality().isAtLeastOne()) {
                type = SymTypeExpressionFactory
                  .createTypeArray(type.getTypeInfo(), symbol.getCardinality().getUpperBound(),
                    type);
              }
            }
          }

          typeCheckResult.setField();
          typeCheckResult.setCurrentResult(type);
        }
      }
      else if (typeSymbolOpt.isPresent()) {
        //no variable found, test type
        TypeSymbol typeSymbol = typeSymbolOpt.get();
        boolean match = true;
        //if the last result is a type and the type is not static then it is not accessible
        if (typeCheckResult.isType()) {
          if (!(typeSymbol instanceof OOTypeSymbol) || !(((OOTypeSymbol) typeSymbol)
            .isIsStatic())) {
            match = false;
          }
        }
        if (match) {
          SymTypeExpression wholeResult = SymTypeExpressionFactory
            .createTypeExpression(typeSymbol.getName(), typeSymbol.getEnclosingScope());
          typeCheckResult.setType();
          typeCheckResult.setCurrentResult(wholeResult);
        }
        else {
          typeCheckResult.reset();
          logError("0xA0303", expr.get_SourcePositionStart());
        }
      }
      else {
        typeCheckResult.reset();
        logError("0xA0306", expr.get_SourcePositionStart());
      }
    }
    else {
      //inner type has no result --> try to resolve a type
      String toResolve = printer.prettyprint(expr);
      Optional<TypeSymbol> typeSymbolOpt = getScope(expr.getEnclosingScope())
        .resolveType(toResolve);
      if (typeSymbolOpt.isPresent()) {
        TypeSymbol typeSymbol = typeSymbolOpt.get();
        SymTypeExpression type = SymTypeExpressionFactory
          .createTypeExpression(typeSymbol.getName(), typeSymbol.getEnclosingScope());
        typeCheckResult.setType();
        typeCheckResult.setCurrentResult(type);
      }
      else {
        //the inner type has no result and there is no type found
        typeCheckResult.reset();
        Log.info("package suspected", "DeriveSymTypeOfCommonExpressions");
      }
    }
  }

  protected List<VariableSymbol> filterStaticFieldSymbols(List<VariableSymbol> fieldSymbols) {
    return fieldSymbols.stream().filter(f -> f instanceof FieldSymbol)
      .filter(f -> ((FieldSymbol) f).isIsStatic()).collect(Collectors.toList());
  }

  protected boolean checkIfCorrect(List<VariableSymbol> fieldSymbols) {
    if (fieldSymbols.size() == 0) {
      return false;
    }
    TypeSymbol type = fieldSymbols.get(0).getType().getTypeInfo();
    if (type instanceof TypeSymbolSurrogate) {
      type = ((TypeSymbolSurrogate) type).lazyLoadDelegate();
    }
    for (VariableSymbol field : fieldSymbols) {
      TypeSymbol typeOfField = field.getType().getTypeInfo();
      if (typeOfField instanceof TypeSymbolSurrogate) {
        typeOfField = ((TypeSymbolSurrogate) typeOfField).lazyLoadDelegate();
      }
      if (!type.equals(typeOfField)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected Optional<SymTypeExpression> calculateConditionalExpressionType
    (ASTConditionalExpression expr,
      SymTypeExpression conditionResult,
      SymTypeExpression trueResult,
      SymTypeExpression falseResult) {
    Optional<SymTypeExpression> wholeResult = Optional.empty();
    //condition has to be boolean
    if (isBoolean(conditionResult)) {
      //check if "then" and "else" are either from the same type or are in sub-supertype relation
      if (compatible(trueResult, falseResult)) {
        wholeResult = Optional.of(trueResult);
      }
      else if (compatible(falseResult, trueResult)) {
        wholeResult = Optional.of(falseResult);
      }
      else {
        // first argument can be null since it should not be relevant to the type calculation
        wholeResult = getBinaryNumericPromotion(trueResult, falseResult);
      }
    }
    return wholeResult;
  }

  @Override
  protected Optional<SymTypeExpression> calculateTypeLogical(ASTInfixExpression
    expr, SymTypeExpression rightResult, SymTypeExpression leftResult) {
    //Option one: they are both numeric types
    if (isNumericType(leftResult) && isNumericType(rightResult)
      || isBoolean(leftResult) && isBoolean(rightResult)) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    //Option two: none of them is a primitive type and they are either the same type or in a super/sub type relation
    if (!leftResult.isTypeConstant() && !rightResult.isTypeConstant() &&
      (compatible(leftResult, rightResult) || compatible(rightResult, leftResult))
    ) {
      return Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
    }
    //should never happen, no valid result, error will be handled in traverse
    return Optional.empty();
  }

  private List<FunctionSymbol> getFittingMethods
    (List<FunctionSymbol> methodlist, ASTCallExpression expr) {
    List<FunctionSymbol> fittingMethods = new ArrayList<>();
    for (FunctionSymbol method : methodlist) {
      //for every method found check if the arguments are correct
      if (expr.getArguments().getExpressionList().size() == method.getParameterList().size()) {
        boolean success = true;
        for (int i = 0; i < method.getParameterList().size(); i++) {
          expr.getArguments().getExpression(i).accept(getTraverser());
          //test if every single argument is correct
          if (!method.getParameterList().get(i).getType()
            .deepEquals(typeCheckResult.getCurrentResult()) &&
            !compatible(method.getParameterList().get(i).getType(),
              typeCheckResult.getCurrentResult())) {
            success = false;
          }
        }
        if (success) {
          //method has the correct arguments and return type
          fittingMethods.add(method);
        }
      }
    }
    return fittingMethods;
  }
}
