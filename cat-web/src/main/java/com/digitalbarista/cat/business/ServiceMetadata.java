package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Date: 1/6/13
 */
@XmlRootElement(name="metadata")
public class ServiceMetadata
{
    public static final int DEFAULT_LIMIT = 999;

    private int limit = DEFAULT_LIMIT;
    private int offset = 0;
    private int total = 0;
    private String sortField;
    private int sortDirection = 1;

    public ServiceMetadata()
    {
    }
    public ServiceMetadata(Integer limit, Integer offset, String sortField, Integer sortDirection){
        setLimit(limit);
        setOffset(offset);
        setSortDirection(sortDirection);
        this.sortField = sortField;
    }
    @XmlAttribute
    public int getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit != null ? limit : DEFAULT_LIMIT;
    }

    @XmlAttribute
    public int getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset != null ? offset : 0;
    }

    @XmlAttribute
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @XmlAttribute
    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    @XmlAttribute
    public int getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Integer sortDirection) {
        this.sortDirection = sortDirection == -1 || sortDirection == 1 ? sortDirection : 1;
    }
}
