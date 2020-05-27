package courseproject
import java.util
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jdt.core.dom._
import org.eclipse.jdt.core.dom.rewrite.{ASTRewrite, ListRewrite}

/**
 * ASTVisitor with the important concrete visit methods overrided in order to log for:
 * * var declarations / expressions
 * * scopes (blocks)
 * * assignments
 *
 */
case class MyASTVisitor(cu: CompilationUnit, rewriter: ASTRewrite) extends ASTVisitor with LazyLogging {

  import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
  def getLineNumber(position: Int): Int = {
    cu.getLineNumber(position)
  }

  /**
   * Constructs a new "fully qualified string" of the given node, with specified scope and parents
   * Example, a variable x, defined in main method of class called OtherClass would construct the string OtherClass.main.x
   */
  val constructQualifiedString = new Function1[ASTNode, String] {
    override def apply(node: ASTNode): String = {
      node.getNodeType match {
        case ASTNode.TYPE_DECLARATION =>
          node.asInstanceOf[TypeDeclaration].resolveBinding().getQualifiedName
        case ASTNode.METHOD_DECLARATION =>
          apply(node.getParent) + '.' +  node.asInstanceOf[MethodDeclaration].resolveBinding().getName
        case _ =>
           if (node.isInstanceOf[VariableDeclarationFragment]) {
            logger.info("VarDecFrag")
            apply(node.getParent) + '.' + node.asInstanceOf[VariableDeclarationFragment].resolveBinding().getName
          } else if (node.isInstanceOf[SimpleName]) {
            logger.info("simpename")
            apply(node.getParent) + '.' + node.asInstanceOf[SimpleName].getIdentifier
          } else {
            apply(node.getParent)
          }
      }
    }
  }

  /**
   * Traverses through the parent ASTNode until it reaches a ASTNode of instance Block
   * Returns the ASTNode of instance Block once it reaches it
   */
  val getParentBlockNode = new Function1[ASTNode, ASTNode] {
    override def apply(node: ASTNode): ASTNode = {
      if (node.getParent().isInstanceOf[Block])
        node.getParent
      else
        apply(node.getParent)
    }
  }

  /**
   * Traverses through the parent ASTNode until it reaches a ASTNode of instance ExpressionStatement
   * Returns the ASTNode of instance ExpressionStatement once it reaches it
   */
  val getParentExpressNode = new Function1[ASTNode, ASTNode] {
    override def apply(node: ASTNode): ASTNode = {
      if (node.getParent().isInstanceOf[ExpressionStatement])
        node.getParent
      else
        apply(node.getParent)
    }
  }

  /**
   * Creates the TemplateClass.pair method call
   * @param ast the ASTRewrite instance to use
   * @param literal_0 A literal to be the first part of the pair
   * @param literal_1 A literal to be the second part of the pair
   * @tparam T
   * @tparam B
   * @return A new MethodInvocation in the format of TemplateClass.pair(T, B)
   */
  def createPair[T,B](ast: AST, literal_0: T, literal_1: B): MethodInvocation = {
    val astname = ast.newSimpleName("TemplateClass")

    val arg2: MethodInvocation = ast.newMethodInvocation()
    arg2.setExpression(astname);
    arg2.setName(ast.newSimpleName("pair"))
    val arg2_0: T = literal_0
    val arg2_1: B = literal_1

    arg2.arguments().asInstanceOf[util.List[Expression]].add(arg2_0.asInstanceOf[Expression])
    arg2.arguments().asInstanceOf[util.List[Expression]].add(arg2_1.asInstanceOf[Expression])
    arg2
  }

  /**
   * Creates the template method call for TemplateClass.instrum()
   * @param ast the ASTRewrite instance to use
   * @param node the current ASTNode
   * @param typeOfOp "Assign", "Declaration", etc. the aliases for the node types / production rules that we chose to implement
   * @return A new MethodInvocation in the format of TemplateClass.instrum(int, String, ...)
   */
  def createTemplate(ast: AST, node: ASTNode, typeOfOp: String): MethodInvocation = {
    val astname = ast.newSimpleName("TemplateClass")

    val methodInvocation = ast.newMethodInvocation()
    methodInvocation.setExpression(astname)
    methodInvocation.setName(ast.newSimpleName("instrum"))

    val arg0: NumberLiteral = ast.newNumberLiteral(getLineNumber(node.getStartPosition).toString)
    methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg0)

    val arg1: StringLiteral = ast.newStringLiteral
    arg1.setLiteralValue(typeOfOp)
    methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg1)
    methodInvocation
  }


  /**
   * Assignment
   * Any time that the visitor encounters a node that syntatically matches the following format
   * Expression AssignmentOperator Expression
   * enter this function.
   * Insert an instrumentation in the following format:
   * (linenum, "Assign", TemplateClass.pair(var, var_qualified_name), expression);
   **/
  override def visit(node: Assignment): Boolean = {
    val lhs_name: String = node.getLeftHandSide.toString
    val rhs_name: String = node.getRightHandSide.toString
    val operator: String = node.getOperator.toString
    logger.info("Encountered assignment " + node.toString)

    val ast = rewriter.getAST
    val methodInvocation = createTemplate(ast, node, "Assign")
    val arg2_0 = ast.newSimpleName(lhs_name)

    /**
     * There are three types of assignments
     * 1. The right hand side is A SimpleName
     * 2. The right hand side is a InFixExpression
     * 3. The right hand side is a classInstanceCreation
     * All other cases handled the same as the first case
     *
     * This is for assignments that are like x = name
     */
    if (node.getRightHandSide.getNodeType() == ASTNode.SIMPLE_NAME) {
      val arg2_1: StringLiteral = ast.newStringLiteral()
      arg2_1.setLiteralValue(constructQualifiedString(node.getLeftHandSide))
      val arg2_2: SimpleName = ast.newSimpleName(rhs_name)
      val arg2 = createPair(ast, arg2_0 ,arg2_1)
      methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg2)
      methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg2_2)
     }

    /**
     * This is for assignments that are like x = x-1
     */
    else if (node.getRightHandSide.getNodeType() == ASTNode.INFIX_EXPRESSION) {
      val nodeinfix: InfixExpression = node.getRightHandSide.asInstanceOf[InfixExpression]
      nodeinfix.getLeftOperand
      val arg2_1: InfixExpression = ast.newInfixExpression()
      arg2_1.setLeftOperand(ast.newSimpleName(nodeinfix.getLeftOperand.toString))
      val exprstff: AST = nodeinfix.getRightOperand.getAST()
      val exprASTNode: ASTNode = ASTNode.copySubtree(exprstff, nodeinfix.getRightOperand)
      arg2_1.setOperator(nodeinfix.getOperator)
      arg2_1.setRightOperand(exprASTNode.asInstanceOf[Expression])
      val arg2_2 = ast.newStringLiteral()
      arg2_2.setLiteralValue(constructQualifiedString(node.getLeftHandSide))
      val arg2 = createPair(ast, arg2_0, arg2_2)
      methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg2)
      methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg2_1)
    }

    /**
     * this is for assignments that are like
     * x = new MyClass()
     */
    else if (node.getRightHandSide.getNodeType == ASTNode.CLASS_INSTANCE_CREATION) {
      val nodeClassCreation: ClassInstanceCreation = node.getRightHandSide.asInstanceOf[ClassInstanceCreation]
      val arg2_1: ClassInstanceCreation = ast.newClassInstanceCreation()

      val nodeclassAST: AST = nodeClassCreation.getType.getAST
      val exprASTNode: ASTNode = ASTNode.copySubtree(nodeclassAST, nodeClassCreation.getType)
      arg2_1.setType(exprASTNode.asInstanceOf[Type])
      val arg2 = createPair(ast, arg2_0 ,arg2_1)
      methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg2)
    }

    /**
     * Finally this is for assignments like x = 5
     */
    else {
      val arg2_1: NumberLiteral = ast.newNumberLiteral(rhs_name)
      val arg2_name: StringLiteral = ast.newStringLiteral()
      arg2_name.setLiteralValue(constructQualifiedString(node.getLeftHandSide))
      val arg2 = createPair(ast, arg2_0, arg2_name)
      methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg2)
      methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg2_1)
    }

    val expressionStatement = ast.newExpressionStatement(methodInvocation)
    ast.newBlock().statements().asInstanceOf[util.List[Statement]].add(expressionStatement)

    val pNode: ASTNode = getParentBlockNode(node)
    val peNode: ASTNode = getParentExpressNode(node)

    val lrw: ListRewrite = rewriter.getListRewrite(pNode, Block.STATEMENTS_PROPERTY)
    lrw.insertAfter(expressionStatement, peNode, null)

    true
  }

  /**
   * https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FBlock.html
   * Block:
   * { { Statement } }
   *
   * Blocks can contain a lot of statements, and they are indicated by the start of an opening curly brace.
   * Therefore, it is useful to use these nodes as a way to keep track of scope
   **/
  override def visit(node: Block): Boolean = {
    val name: String = node.getLocationInParent.getId
    val startingPosition: String = node.getStartPosition().toString

    logger.info(name + " starting position: " + startingPosition)

    val ast = rewriter.getAST

    val methodInvocation = createTemplate(ast, node, "Block")

    val expressionStatement: ExpressionStatement = ast.newExpressionStatement(methodInvocation)
    ast.newBlock().statements().asInstanceOf[util.List[Statement]].add(expressionStatement)

    val lrw: ListRewrite = rewriter.getListRewrite(node, Block.STATEMENTS_PROPERTY)
    lrw.insertFirst(expressionStatement,  null)

    true
  }

  /**
   * (NOTE) VariableDeclarationFragment: variables are declared inside of a VariableDeclarationStatement
   * When we declare a variable for the first time, the visitor shall call this overriden method
   * The instrumentation shall be in the format such as TemplateClass.instrum(3, "Declaration", "OtherClass.main.x") for int x in the main method of the OtherClass class.
   *
   **/
  override def visit(node: VariableDeclarationFragment): Boolean = {
    val varName = node.getName.getIdentifier
    /**
     * Traverse ASTNodes until the VariableDeclarationStatement is reached, and return that node
     */
    val getParStatementNode = new Function1[ASTNode, ASTNode] {
      override def apply(node: ASTNode): ASTNode = {
        if (node.getParent().isInstanceOf[VariableDeclarationStatement])
          node.getParent
        else
          apply(node.getParent)
      }
    }

    /**
     * get the VariableDeclarationStatement, because it has more information
     */
    val parStmt:VariableDeclarationStatement = getParStatementNode(node).asInstanceOf[VariableDeclarationStatement]
    val varType = parStmt.getType()

    logger.info("Encountered declaration statement: " + varName + " " + node.getName.resolveTypeBinding().getQualifiedName)

    val ast = rewriter.getAST
    val methodInvocation = createTemplate(ast, node, "Declaration")
    val arg2_0: StringLiteral = ast.newStringLiteral()
    arg2_0.setLiteralValue(constructQualifiedString(node))

    if (node.getInitializer() == null) {
      methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg2_0)
    } else {
      val arg2_1: SimpleName = ast.newSimpleName(varName)
      val arg2 = createPair(ast, arg2_1, arg2_0)
      methodInvocation.arguments().asInstanceOf[util.List[Expression]].add(arg2)
    }

    val expressionStatement: ExpressionStatement = ast.newExpressionStatement(methodInvocation)
    ast.newBlock().statements().asInstanceOf[util.List[Statement]].add(expressionStatement)

    val pNode: ASTNode = getParentBlockNode(node)
  
    val lrw: ListRewrite = rewriter.getListRewrite(pNode, Block.STATEMENTS_PROPERTY)
    lrw.insertAfter(expressionStatement, parStmt, null)
    true
  }
}
