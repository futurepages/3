package org.vaadin.viritin.layouts;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import java.util.Collection;

public class MHorizontalLayout extends HorizontalLayout {

    public MHorizontalLayout() {
        super.setSpacing(true);
    }

    public MHorizontalLayout(Component... components) {
        this();
        addComponents(components);
    }

    public MHorizontalLayout with(Component... components) {
        addComponents(components);
        return this;
    }

    public MHorizontalLayout withSpacing(boolean spacing) {
        setSpacing(spacing);
        return this;
    }

    public MHorizontalLayout withMargin(boolean marging) {
        setMargin(marging);
        return this;
    }

    public MHorizontalLayout withMargin(MarginInfo marginInfo) {
        setMargin(marginInfo);
        return this;
    }

    public MHorizontalLayout withWidth(String width) {
        setWidth(width);
        return this;
    }

    public MHorizontalLayout withFullWidth() {
        setWidth("100%");
        return this;
    }

    public MHorizontalLayout withHeight(String height) {
        setHeight(height);
        return this;
    }

    public MHorizontalLayout withFullHeight() {
        setHeight("100%");
        return this;
    }

    public MHorizontalLayout alignAll(Alignment alignment) {
        for (Component component : this) {
            setComponentAlignment(component, alignment);
        }
        return this;
    }

    /**
     * Expands selected components. Also sets the only sane width for expanded
     * components (100%).
     *
     * @param componentsToExpand the components that should be expanded
     * @return the object itself for further configuration
     */
    public MHorizontalLayout expand(Component... componentsToExpand) {
        if (getWidth() < 0) {
            // Make full height if no other size is set
            withFullWidth();
        }

        for (Component component : componentsToExpand) {
            if (component.getParent() != this) {
                addComponent(component);
            }
            setExpandRatio(component, 1);
            component.setWidth(100, Unit.PERCENTAGE);
        }
        return this;
    }

    public MHorizontalLayout add(Component... component) {
        return with(component);
    }

    public MHorizontalLayout add(Collection<Component> component) {
        return with(component.toArray(new Component[component.size()]));
    }

    public MHorizontalLayout add(Component component, Alignment alignment) {
        return add(component).withAlign(component, alignment);
    }

    public MHorizontalLayout withAlign(Component component, Alignment alignment) {
        setComponentAlignment(component, alignment);
        return this;
    }

    public MHorizontalLayout withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    public MHorizontalLayout withStyleName(String styleName) {
        setStyleName(styleName);
        return this;
    }

    /**
     * Adds "spacer" to layout that expands to consume remaining space. If
     * multiple spacers are added they share equally sized slot. Also tries to 
     * configure layout for proper settings needed for this kind of usage.
     *
     * @return the layout with space added
     */
    public MHorizontalLayout space() {
        return expand(new Label());
    }

}
