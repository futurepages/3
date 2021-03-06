/*
 * Copyright 2014 Matti Tahvonen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.futurepages.containers;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractContainer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.beanutils.WrapDynaClass;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang3.ClassUtils;

/**
 * A replacement for BeanItemContainer from the core
 * <p>
 * The ListContainer is rather similar to the cores BeanItemContainer, but has
 * better typed API, much smaller memory overhead (practically no overhead if
 * data is given as List) and also otherwise better performance.
 *
 * @param <T> the type of beans in the backed list
 */
public abstract class CopyOfListContainer<T> extends AbstractContainer implements
        Container.Indexed, Container.Sortable, ItemSetChangeNotifier {

    private List<T> backingList;

    public CopyOfListContainer() {
	    setCollection(getList());
    }

	protected abstract Collection<T> getList();

	public CopyOfListContainer(Collection<T> backingList) {
        setCollection(backingList);
    }

    public CopyOfListContainer(Class<T> type, Collection<T> backingList) {
        dynaClass = WrapDynaClass.createDynaClass(type);
        setCollection(backingList);
    }

    public final void setCollection(Collection<T> backingList1) {
        if (backingList1 instanceof List) {
            this.backingList = (List<T>) backingList1;
        } else {
            this.backingList = new ArrayList<T>(backingList1);
        }
        fireItemSetChange();
    }

    public CopyOfListContainer(Class<T> type) {
        backingList = new ArrayList<T>();
        dynaClass = WrapDynaClass.createDynaClass(type);
    }

    protected List<T> getBackingList() {
        return backingList;
    }

    private transient WrapDynaClass dynaClass;

    private WrapDynaClass getDynaClass() {
        if (dynaClass == null && !backingList.isEmpty()) {
            dynaClass = WrapDynaClass.createDynaClass(backingList.get(0).
                    getClass());
        }
        return dynaClass;
    }

    @Override
    public int indexOfId(Object itemId) {
        return backingList.indexOf(itemId);
    }

    public int indexOf(T bean) {
        return indexOfId(bean);
    }

    @Override
    public T getIdByIndex(int index) {
        return backingList.get(index);
    }

    @Override
    public List<T> getItemIds(int startIndex, int numberOfItems) {
        return backingList.subList(startIndex, startIndex + numberOfItems);
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T nextItemId(Object itemId) {
        int i = backingList.indexOf(itemId) + 1;
        if(backingList.size() == i) {
            return null;
        }
        return backingList.get(i);
    }

    @Override
    public T prevItemId(Object itemId) {
        int i = backingList.indexOf(itemId) -1;
        if(i < 0) {
            return null;
        }
        return backingList.get(i);
    }

    @Override
    public T firstItemId() {
        return backingList.get(0);
    }

    @Override
    public T lastItemId() {
        return backingList.get(backingList.size() - 1);
    }

    @Override
    public boolean isFirstId(Object itemId) {
        return itemId.equals(firstItemId());
    }

    @Override
    public boolean isLastId(Object itemId) {
        return itemId.equals(lastItemId());
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item getItem(Object itemId) {
        if (itemId == null) {
            return null;
        }
        return new DynaBeanItem<T>((T) itemId);
    }

    @Override
    public Collection<String> getContainerPropertyIds() {
        ArrayList<String> properties = new ArrayList<String>();
        if (getDynaClass() != null) {
            for (DynaProperty db : getDynaClass().getDynaProperties()) {
                properties.add(db.getName());
            }
            properties.remove("class");
        }
        return properties;
    }

    @Override
    public Collection<?> getItemIds() {
        return backingList;
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        return getItem(itemId).getItemProperty(propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        final Class<?> type = getDynaClass().getDynaProperty(propertyId.toString()).getType();
        if(type.isPrimitive()) {
            // Vaadin can't handle primitive types in _all_ places, so use
            // wrappers instead. FieldGroup works, but e.g. Table in _editable_
            // mode fails for some reason
            return ClassUtils.primitiveToWrapper(type);
        }
        return type;
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public boolean containsId(Object itemId) {
        return backingList.contains((T) itemId);
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        backingList.add((T) itemId);
        fireItemSetChange();
        return getItem(itemId);
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        final boolean remove = backingList.remove((T) itemId);
        if (remove) {
            fireItemSetChange();
        }
        return remove;
    }

    @Override
    public boolean addContainerProperty(Object propertyId,
            Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        backingList.clear();
        fireItemSetChange();
        return true;
    }

    public CopyOfListContainer addAll(Collection<T> beans) {
        backingList.addAll(beans);
        fireItemSetChange();
        return this;
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        Comparator c = new NullComparator();
        if (!ascending[0]) {
            c = new ReverseComparator(c);
        }
        BeanComparator bc = new BeanComparator(propertyId[0].toString(), c);
        Collections.sort(backingList, bc);
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        ArrayList<String> properties = new ArrayList<String>();
        for (DynaProperty db : getDynaClass().getDynaProperties()) {
            if (db.getType().isPrimitive() || Comparable.class.isAssignableFrom(
                    db.getType())) {
                properties.add(db.getName());
            }
        }
        properties.remove("class");
        return properties;
    }

    public void addItemSetChangeListener(
            Container.ItemSetChangeListener listener) {
        super.addItemSetChangeListener(listener);
    }

    public void removeItemSetChangeListener(
            Container.ItemSetChangeListener listener) {
        super.removeItemSetChangeListener(listener);
    }

    public void addListener(Container.ItemSetChangeListener listener) {
        super.addListener(listener);
    }

    public void removeListener(Container.ItemSetChangeListener listener) {
        super.removeListener(listener);
    }

    public class DynaBeanItem<T> implements Item {

        private class DynaProperty implements Property {

            private final String propertyName;

            public DynaProperty(String property) {
                propertyName = property;
            }

            @Override
            public Object getValue() {
                return getDynaBean().get(propertyName);
            }

            @Override
            public void setValue(Object newValue) throws Property.ReadOnlyException {
                getDynaBean().set(propertyName, newValue);
            }

            @Override
            public Class getType() {
                return CopyOfListContainer.this.getType(propertyName);
            }

            @Override
            public boolean isReadOnly() {
                return getDynaClass().getPropertyDescriptor(propertyName).
                        getWriteMethod() == null;
            }

            @Override
            public void setReadOnly(boolean newStatus) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }

        private T bean;

        private transient DynaBean db;

        public DynaBeanItem(T bean) {
            this.bean = bean;
        }

        private DynaBean getDynaBean() {
            if (db == null) {
                db = new WrapDynaBean(bean);
            }
            return db;
        }

        @Override
        public Property getItemProperty(Object id) {
            return new DynaProperty(id.toString());
        }

        @Override
        public Collection<String> getItemPropertyIds() {
            return CopyOfListContainer.this.getContainerPropertyIds();
        }

        @Override
        public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
