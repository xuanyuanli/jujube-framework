package cn.xuanyuanli.jdbc;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class Test {

    public static void main(String[] args2) throws JSQLParserException {
        String sql="SELECT\n"
                + "  COUNT(*)\n"
                + "FROM\n"
                + "  `spider_python_product` p\n"
                + "  LEFT JOIN `spider_python_auction` pa\n"
                + "    ON pa.id = p.auction_id\n"
                + "WHERE p.id IN\n"
                + "  (SELECT\n"
                + "    ip.id\n"
                + "  FROM\n"
                + "    `spider_python_product` ip\n"
                + "  ORDER BY ip.auction_id DESC)";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        if (select instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) select;
            Expression where = plainSelect.getWhere();
            if (where instanceof InExpression){
                Expression rightExpression = ((InExpression) where).getRightExpression();
                System.out.println(((ParenthesedSelect) rightExpression).getSelect());
            }
        }
    }

}
