package lox;

public class Interpreter implements Expr.Visitor<Object> {
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOrStringOperands(null, left, right);

                if(left instanceof Double) {
                    return (double) left > (double) right;
                } else {
                    return compareString(left, right) > 0;
                }
            case GREATER_EQUAL:
                checkNumberOrStringOperands(expr.operator, left, right);
                if(left instanceof Double) {
                    return (double) left > (double) right;
                } else {
                    return compareString(left, right) >= 0;
                }
            case LESS:
                checkNumberOrStringOperands(expr.operator, left, right);
                if(left instanceof Double) {
                    return (double) left > (double) right;
                } else {
                    return compareString(left, right) < 0;
                }
            case LESS_EQUAL:
                checkNumberOrStringOperands(expr.operator, left, right);
                if(left instanceof Double) {
                    return (double) left > (double) right;
                } else {
                    return compareString(left, right) <= 0;
                }
            case MINUS:
                checkNumberOrStringOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                if (left instanceof String) {
                    return (String) left + doubleToString(right);
                }

                if (right instanceof String) {
                    return doubleToString(left) + (String) right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or at least one must be a string");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
        }

        // Unreachable
        return null;
    }

    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    private int compareString(Object a, Object b) {
        return ((String)a).compareTo((String)b);
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private String doubleToString(Object object) {
        String doubleString = String.valueOf(object);
        if (doubleString.endsWith(".0")) {
            doubleString = doubleString.substring(0, doubleString.length() - 2);
        }
        return doubleString;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
    
    private void checkNumberOrStringOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        if (left instanceof String && right instanceof String)
            return;
        throw new RuntimeError(operator, "Operands must have matching types.");
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
}
