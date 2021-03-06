package org.futurepages.core.persistence;

import java.util.List;

public class PaginationSlice<T> extends HQLProvider{

    private Long totalSize;
    private Integer pageSize;
    private int totalPages;
    private int pageNumber;
    private int pagesOffset;
    private int firstResult;
    private HQLQuery hqlQuery;
    private GenericDao dao;
    private List<T> list;

    public PaginationSlice(int pageNumber, int pageSize, int pagesOffset, GenericDao dao, HQLQuery<T> hqlQuery) {
        this.dao = dao;
        this.hqlQuery = hqlQuery;

        this.pagesOffset = pagesOffset; // have a public set
        this.pageSize = pageSize; // have a public set
        loadPage(pageNumber);
    }


    public PaginationSlice(GenericDao dao, HQLQuery<T> hqlQuery) {
        this.dao = dao;
        this.hqlQuery = hqlQuery;
        this.totalSize = calcTotalSize();
        this.firstResult = 0;
    }

    public PaginationSlice<T> loadPage(int rawPageNumber) {
        this.totalSize = calcTotalSize();
        this.totalPages  = calcTotalPages(totalSize,pageSize);
        this.pageNumber = calcCorrectPageNumber(rawPageNumber,totalPages);
        this.firstResult = calcFirstResult(pageNumber, pageSize);
        this.list = loadList();
        return this;
    }

    public PaginationSlice<T> loadPageByFirstResult(int firstResult) {
        this.totalSize = dao.numRows(hql(count("*"), hqlQuery.getEntity(), hqlQuery.getWhere()));
        this.totalPages  = calcTotalPages(totalSize,pageSize);
        this.pageNumber = calcCorrectPageNumberByFirstResult(firstResult);
        this.firstResult = firstResult;
        this.list = loadList();
        return this;
    }

    public PaginationSlice<T> loadRows(int firstResult,int maxResult) {
        this.pageSize = maxResult;
        loadPageByFirstResult(firstResult);
        return this;
    }

    private int calcCorrectPageNumberByFirstResult(int firstResult) {
       int page = (int) Math.ceil((firstResult+1)/pageSize);
       pagesOffset =  (firstResult+1)-(pageSize*page-1);
       return page;
    }


    private List<T> loadList() {
        //System.out.println(hqlQuery.toString() + "first:" + firstResult + "; pageSize:" + pageSize);
        //System.out.println();
        return dao.selectQuery(hqlQuery)
                  .setFirstResult(firstResult)
                  .setMaxResults(pageSize)
                  .list();
    }

    public PaginationSlice<T> loadPrevious(){
        return loadPage(pageNumber-1);
    }

    public PaginationSlice<T> loadNext(){
        return loadPage(pageNumber+1);
    }

    public void setPagesOffset(int pagesOffset) {
        this.pagesOffset = pagesOffset;
    }

    public void setPageSize(int pageSize){
        this.pageSize = pageSize;
    }


    public Long getTotalSize() {
        return totalSize;
    }

    public long calcTotalSize(){
        return dao.numRows(hql(count("*"),hqlQuery.getEntity(), hqlQuery.getWhere()));
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPagesOffset() {
        return pagesOffset;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public HQLQuery getHqlQuery() {
        return hqlQuery;
    }

    public GenericDao getDao() {
        return dao;
    }

    public List<T> getList() {
        return list;
    }


    private int calcTotalPages(long totalSize, int pageSize) {
        return (int) Math.ceil(totalSize / (double) pageSize);
    }


    private int calcFirstResult(int pageNumber, int pageSize){
        return ((pageNumber * pageSize) - pageSize)+pagesOffset;
    }

    private int calcCorrectPageNumber(int pageNumber, int totalPages) {
		if(pagesOffset == 0){
			if (pageNumber > totalPages) {
				return totalPages;
			}
		}
		return pageNumber;
    }

    public void reloadPage() {
        loadPage(pageNumber);
    }
}