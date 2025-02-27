package entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页的结果集
 * @author 黑马程序员
 * @Company http://www.ithiema.com
 */
public class PageResult<T> implements Serializable{

    private long total;
    private List<T> rows;

    public PageResult(){

    }

    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
