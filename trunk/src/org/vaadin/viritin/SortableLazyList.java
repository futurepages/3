package org.vaadin.viritin;

import java.io.Serializable;
import java.util.*;
import org.vaadin.viritin.LazyList.CountProvider;

/**
 * A general purpose helper class to us MTable/ListContainer for service layers
 * (EJBs, Spring Data etc) that provide large amount of data. Makes paged
 * requests to PagingProvider, caches recently used pages in memory and this way
 * hides away Vaadin Container complexity from you. The class generic helper and
 * is probably useful also other but Vaadin applications as well.
 *
 * @author Matti Tahvonen
 * @param <T> The type of the objects in the list
 */
public class SortableLazyList<T> extends LazyList implements Serializable {

    public void sort(boolean ascending, String property) {
        sortAscending = ascending;
        sortProperty = property;
        // TODO resetting size at this point is actually obsolete?
        reset();
    }

    private boolean sortAscending = true;
    private String sortProperty;

    // Split into subinterfaces for better Java 8 lambda support
    /**
     * Interface via the LazyList communicates with the "backend"
     *
     * @param <T> The type of the objects in the list
     */
    public interface SortablePagingProvider<T> extends Serializable {

        /**
         * Fetches one "page" of entities form the backend. The amount
         * "maxResults" should match with the value configured for the LazyList
         *
         * @param firstRow the index of first row that should be fetched
         * @param sortAscending the direction to be used for sorting, true if ascending
         * @param property the property based on the sorting should be done, null for natural order
         * @return a sub list from given first index
         */
        public List<T> findEntities(int firstRow, boolean sortAscending,
                String property);
    }

    /**
     * Interface via the LazyList communicates with the "backend"
     *
     * @param <T> The type of the objects in the list
     */
    public interface SortableEntityProvider<T> extends SortablePagingProvider,
            CountProvider {
    }

    private final SortablePagingProvider sortablePageProvider;

    /**
     * Constructs a new LazyList with given provider and default page size of
     * DEFAULT_PAGE_SIZE (30).
     *
     * @param dataProvider the data provider that is used to fetch pages of
     * entities and to detect the total count of entities
     */
    public SortableLazyList(SortableEntityProvider dataProvider) {
        this(dataProvider, DEFAULT_PAGE_SIZE);
    }

    /**
     * Constructs a new LazyList with given provider and default page size of
     * DEFAULT_PAGE_SIZE (30).
     *
     * @param dataProvider the data provider that is used to fetch pages of
     * entities and to detect the total count of entities
     * @param pageSize the page size to be used
     */
    public SortableLazyList(SortableEntityProvider dataProvider, int pageSize) {
        super(dataProvider, pageSize);
        this.sortablePageProvider = dataProvider;
    }

    /**
     * Constructs a new LazyList with given providers and default page size of
     * DEFAULT_PAGE_SIZE (30).
     *
     * @param pageProvider the interface via "pages" of entities are requested
     * @param countProvider the interface via the total count of entities is
     * detected.
     */
    public SortableLazyList(SortablePagingProvider pageProvider,
            CountProvider countProvider) {
        this(pageProvider, countProvider, DEFAULT_PAGE_SIZE);
    }

    /**
     * Constructs a new LazyList with given providers and page size.
     *
     * @param pageProvider the interface via "pages" of entities are requested
     * @param countProvider the interface via the total count of entities is
     * detected.
     * @param pageSize the page size that should be used
     */
    public SortableLazyList(SortablePagingProvider pageProvider,
            CountProvider countProvider, int pageSize) {
        super(countProvider, pageSize);
        this.sortablePageProvider = pageProvider;
    }

    @Override
    protected List findEntities(int i) {
        return sortablePageProvider.findEntities(i, isSortAscending(), getSortProperty());
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }

    public String getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(String sortProperty) {
        this.sortProperty = sortProperty;
    }

}
