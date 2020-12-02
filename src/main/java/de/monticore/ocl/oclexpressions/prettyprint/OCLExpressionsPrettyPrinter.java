// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.oclexpressions.prettyprint;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symbols.basicsymbols._ast.ASTVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OCLExpressionsPrettyPrinter extends ExpressionsBasisPrettyPrinter
  implements OCLExpressionsVisitor {

  protected OCLExpressionsVisitor realThis;

  public OCLExpressionsPrettyPrinter(IndentPrinter printer) {
    super(printer);
    realThis = this;
  }

  @Override
  public void handle(ASTInDeclaration node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.isPresentMCType()) {
      node.getMCType().accept(getRealThis());
    }
    List<ASTInDeclarationVariable> variables = node.getInDeclarationVariableList();
    List<String> variableNames = new ArrayList<>();
    for (ASTVariable var : variables){
      variableNames.add(var.getName());
    }
    Iterator<String> iter = variableNames.iterator();
    getPrinter().print(iter.next());
    while (iter.hasNext()) {
      getPrinter().print(", ");
      getPrinter().print(iter.next());
    }

    if (node.isPresentExpression()) {
      getPrinter().print(" in ");
      node.getExpression().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTImpliesExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getRealThis());
    getPrinter().print(" implies ");
    node.getRight().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTForallExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("forall ");
    node.getInDeclarationList().forEach(e -> e.accept(getRealThis()));

    getPrinter().print(":");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTExistsExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("exists ");
    node.getInDeclarationList().forEach(e -> e.accept(getRealThis()));

    getPrinter().print(":");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTAnyExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("any ");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTLetinExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("let ");
    for (ASTOCLVariableDeclaration ast : node.getOCLVariableDeclarationList()) {
      ast.accept(getRealThis());
      getPrinter().print("; ");
    }
    getPrinter().print("in ");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLVariableDeclaration node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if(node.isPresentMCType()){
      node.getMCType().accept(getRealThis());
      getPrinter().print(" ");
    }
    for (int i = 0; i < node.getDimList().size(); i++){
      getPrinter().print("[]");
      getPrinter().print(" ");
    }
    getPrinter().print(node.getName());
    if (node.isPresentExpression()){
      getPrinter().print(" = ");
      node.getExpression().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTIterateExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("iterate { ");
    node.getIteration().accept(getRealThis());
    getPrinter().print("; ");
    node.getInit().accept(getRealThis());
    getPrinter().print(" : ");
    getPrinter().print(node.getName() + " = ");
    node.getValue().accept(getRealThis());
    getPrinter().print(" }");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("if ");
    node.getCondition().accept(getRealThis());
    getPrinter().print(" then ");
    node.getThenExpression().accept(getRealThis());
    getPrinter().print(" else ");
    node.getElseExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTTypeCastExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("(");
    node.getMCType().accept(getRealThis());
    getPrinter().print(")");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLArrayQualification node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    for (int i = 0; i < node.getArgumentsList().size(); i++){
      getPrinter().print("[");
      node.getArguments(i).accept(getRealThis());
      getPrinter().print("]");
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());

  }

  @Override
  public void handle(ASTOCLAtPreQualification node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("@pre");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTOCLTransitiveQualification node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("**");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTEquivalentExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeft().accept(getRealThis());
    getPrinter().print("<=>");
    node.getRight().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTTypeIfExpression node){
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("typeif ");
    getPrinter().print(node.getName());
    getPrinter().print(" instanceof ");
    node.getMCType().accept(getRealThis());
    getPrinter().print(" then ");
    node.getThenExpression().accept(getRealThis());
    getPrinter().print(" else ");
    node.getElseExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTInstanceOfExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(" instanceof ");
    node.getMCType().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }


  public IndentPrinter getPrinter() {
    return this.printer;
  }

  public String prettyprint(ASTExpression node) {
    getPrinter().clearBuffer();
    node.accept(getRealThis());
    return getPrinter().getContent();
  }

  @Override
  public void setRealThis(OCLExpressionsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public OCLExpressionsVisitor getRealThis() {
    return realThis;
  }
}