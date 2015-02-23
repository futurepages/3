package org.vaadin.viritin.fields;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.*;
import org.vaadin.viritin.ListContainer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A select implementation with better typed API than in core Vaadin.
 *
 * By default the options toString is used to generate the caption for option.
 * To override this behavior, use setCaptionGenerator or override getCaption(T)
 * to provide your own strategy.
 * <p>
 * Behind the scenes uses Vaadin cores NativeSelect (default) or other cores
 * AbstractSelect implementation, type provided in constructor. Tree and Table
 * are not supported, see MTable.
 * <p>
 * Note, that this select is always in single select mode. See MultiSelectTable
 * for a proper "multiselect".
 *
 * @author mstahv
 * @param <T> the type of selects value
 */
public class TypedSelect<T> extends CustomComponent implements Field<T> {

    private CaptionGenerator<T> captionGenerator;

    private AbstractSelect select;

    private ListContainer<T> bic;

    private Class<T> fieldType;

    /**
     * The type of element options in the select
     * 
     * @param type the type of options in the list
     */
    public TypedSelect(Class<T> type) {
        this.fieldType = type;
        bic = new ListContainer<T>(type);
    }

    /**
     * Note, that with this constructor, you cannot override the select type.
     *
     * @param options options to select from
     */
    public TypedSelect(T... options) {
        setOptions(options);
    }

    public TypedSelect(String caption) {
        setCaption(caption);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the width of the wrapped select component.
     *
     * @param width the new width for this select
     * @param unit the unit of the new width
     */
    @Override
    public void setWidth(float width, Unit unit) {
        getSelect().setWidth(width, unit);
        super.setWidth(width, unit);
    }

    @Override
    public void addStyleName(String style) {
        getSelect().addStyleName(style);
        super.addStyleName(style);
    }

    @Override
    public void setStyleName(String style) {
        getSelect().setStyleName(style);
        super.setStyleName(style);
    }

    /**
     * Note, that with this constructor, you cannot override the select type.
     *
     * @param caption the caption for the select
     * @param options available options for the select
     */
    public TypedSelect(String caption, Collection<T> options) {
        this(caption);
        setOptions(options);
    }

    public TypedSelect<T> withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    public TypedSelect<T> withSelectType(
            Class<? extends AbstractSelect> selectType) {
        if (selectType == ListSelect.class) {
            setSelectInstance(new ListSelect() {
                @SuppressWarnings("unchecked")
                @Override
                public String getItemCaption(Object itemId) {
                    return TypedSelect.this.getCaption((T) itemId);
                }
            });
        } else if (selectType == OptionGroup.class) {
            setSelectInstance(new OptionGroup() {
                @SuppressWarnings("unchecked")
                @Override
                public String getItemCaption(Object itemId) {
                    return TypedSelect.this.getCaption((T) itemId);
                }
            });
        } else if (selectType == ComboBox.class) {
            setSelectInstance(new ComboBox() {
                @SuppressWarnings("unchecked")
                @Override
                public String getItemCaption(Object itemId) {
                    return TypedSelect.this.getCaption((T) itemId);
                }
            });
        } else if (selectType == TwinColSelect.class) {
            setSelectInstance(new TwinColSelect() {
                @SuppressWarnings("unchecked")
                @Override
                public String getItemCaption(Object itemId) {
                    return TypedSelect.this.getCaption((T) itemId);
                }
            });
        } else /*if (selectType == null || selectType == NativeSelect.class)*/ {
            setSelectInstance(new NativeSelect() {
                @SuppressWarnings("unchecked")
                @Override
                public String getItemCaption(Object itemId) {
                    return TypedSelect.this.getCaption((T) itemId);
                }
            });
        }
        return this;
    }
    
    protected void setSelectInstance(AbstractSelect select) {
        this.select = select;
    }

    /**
     * 
     * @return  the backing select instance, overriding this method may be 
     * hazardous
     */
    protected AbstractSelect getSelect() {
        if (select == null) {
            withSelectType(null);
            if (bic != null) {
                select.setContainerDataSource(bic);
            }
        }
        return select;
    }

    protected String getCaption(T option) {
        if (captionGenerator != null) {
            return captionGenerator.getCaption(option);
        }
        return option.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getValue() {
        return (T) getSelect().getValue();
    }

    @Override
    public void focus() {
        getSelect().focus();
    }

    public final TypedSelect<T> setOptions(T... values) {
        return setOptions(Arrays.asList(values));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getType() {

        if (fieldType == null) {
            try {
                fieldType = (Class<T>) ((Container.Sortable) select
                        .getContainerDataSource()).firstItemId().getClass();
            } catch (Exception e) {
            }
        }
        return fieldType;
    }

    /**
     * Explicitly sets the element type of the select.
     *
     * @param type the type of options in the select
     * @return this typed select instance
     */
    public TypedSelect setType(Class<T> type) {
        this.fieldType = type;
        return this;
    }

    /**
     * Explicitly sets the element type of the select.
     *
     * @param type the type of options in the select
     * @return this typed select instance
     */
    public TypedSelect setFieldType(Class<T> type) {
        this.fieldType = type;
        return this;
    }

    @Override
    public boolean isInvalidCommitted() {
        return getSelect().isInvalidCommitted();
    }

    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        getSelect().setInvalidCommitted(isCommitted);
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        getSelect().commit();
    }

    @Override
    public void discard() throws SourceException {
        getSelect().discard();
    }

    @Override
    public void setBuffered(boolean buffered) {
        getSelect().setBuffered(buffered);
    }

    @Override
    public boolean isBuffered() {
        return getSelect().isBuffered();
    }

    @Override
    public boolean isModified() {
        return getSelect().isModified();
    }

    @Override
    public void addValidator(Validator validator) {
        getSelect().addValidator(validator);
    }

    @Override
    public void removeValidator(Validator validator) {
        getSelect().removeValidator(validator);
    }

    @Override
    public void removeAllValidators() {
        getSelect().removeAllValidators();
    }

    @Override
    public Collection<Validator> getValidators() {
        return getSelect().getValidators();
    }

    @Override
    public boolean isValid() {
        return getSelect().isValid();
    }

    @Override
    public void validate() throws InvalidValueException {
        getSelect().validate();
    }

    @Override
    public boolean isInvalidAllowed() {
        return getSelect().isInvalidAllowed();
    }

    @Override
    public void setInvalidAllowed(boolean invalidValueAllowed)
            throws UnsupportedOperationException {
        getSelect().setInvalidAllowed(invalidValueAllowed);
    }

    @Override
    public void setValue(T newValue) throws ReadOnlyException {
        getSelect().setValue(newValue);
    }

    public TypedSelect<T> addMValueChangeListener(
            MValueChangeListener<T> listener) {
        addListener(MValueChangeEvent.class, listener,
                MValueChangeEventImpl.VALUE_CHANGE_METHOD);
        ensurePiggybackListener();
        return this;
    }

    public void removeMValueChangeListener(MValueChangeListener<T> listener) {
        removeListener(MValueChangeEvent.class, listener,
                MValueChangeEventImpl.VALUE_CHANGE_METHOD);
    }

    @Override
    public void addValueChangeListener(ValueChangeListener listener) {
        getSelect().addValueChangeListener(listener);
    }

    @Override
    public void addListener(ValueChangeListener listener) {
        getSelect().addValueChangeListener(listener);
    }

    @Override
    public void removeValueChangeListener(ValueChangeListener listener) {
        getSelect().removeValueChangeListener(listener);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
        getSelect().removeValueChangeListener(listener);
    }

    @Override
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        getSelect().valueChange(event);
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        getSelect().setPropertyDataSource(newDataSource);
    }

    @Override
    public Property getPropertyDataSource() {
        return getSelect().getPropertyDataSource();
    }

    @Override
    public int getTabIndex() {
        return getSelect().getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        getSelect().setTabIndex(tabIndex);
    }

    @Override
    public boolean isRequired() {
        return getSelect().isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        getSelect().setRequired(required);
    }

    @Override
    public void setRequiredError(String requiredMessage) {
        getSelect().setRequiredError(requiredMessage);
    }

    @Override
    public String getRequiredError() {
        return getSelect().getRequiredError();
    }

    public CaptionGenerator<T> getCaptionGenerator() {
        return captionGenerator;
    }

    public TypedSelect<T> setCaptionGenerator(
            CaptionGenerator<T> captionGenerator) {
        this.captionGenerator = captionGenerator;
        return this;
    }

    public final TypedSelect<T> setOptions(Collection<T> options) {
        if (bic != null) {
            bic.setCollection(options);
        } else {
            bic = new ListContainer<T>(options);
        }
        getSelect().setContainerDataSource(bic);
        return this;
    }
    
    public final List<T> getOptions() {
        if(bic == null) {
            return Collections.EMPTY_LIST;
        } else {
            return (List<T>) bic.getItemIds();
        }
    }
    
    public TypedSelect<T> setNullSelectionAllowed(boolean nullAllowed) {
        getSelect().setNullSelectionAllowed(nullAllowed);
        return this;
    }

    public TypedSelect<T> setBeans(Collection<T> options) {
        return setOptions(options);
    }

    @Override
    public void attach() {
        if (getCompositionRoot() == null) {
            setCompositionRoot(getSelect());
            if (bic != null && getSelect().getContainerDataSource() != bic) {
                getSelect().setContainerDataSource(bic);
            }
        }
        super.attach();
    }

    private ValueChangeListener piggyBackListener;

    private void ensurePiggybackListener() {
        if (piggyBackListener == null) {
            piggyBackListener = new ValueChangeListener() {

                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    fireEvent(new MValueChangeEventImpl<T>(TypedSelect.this));
                }
            };
            getSelect().addValueChangeListener(piggyBackListener);
        }
    }

    public TypedSelect<T> withFullWidth() {
        setWidth("100%");
        return this;
    }

    public TypedSelect<T> withReadOnly(boolean readOnly) {
        setReadOnly(readOnly);
        return this;
    }

    public TypedSelect<T> withValidator(Validator validator) {
        setImmediate(true);
        addValidator(validator);
        return this;
    }

    public TypedSelect<T> withWidth(float width, Unit unit) {
        setWidth(width, unit);
        return this;
    }

    public TypedSelect<T> withWidth(String width) {
        setWidth(width);
        return this;
    }

    public void selectFirst() {
        if(bic != null && bic.size() > 0) {
            getSelect().setValue(bic.getIdByIndex(0));
        }
    }

    /**
     * 
     * @return gets the ListContainer used by this component
     */
    protected ListContainer<T> getBic() {
        return bic;
    }

    /**
     * 
     * @param listContainer  sets the ListContainer used by this select. For 
     * extensions only, should be set early or will fail.
     */
    protected void setBic(ListContainer<T> listContainer) {
        bic = listContainer;
    }
    

}
