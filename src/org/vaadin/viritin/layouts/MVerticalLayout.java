package org.vaadin.viritin.layouts;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;

public class MVerticalLayout extends VerticalLayout {

    public MVerticalLayout() {
        super.setSpacing(true);
        super.setMargin(true);
    }

    public MVerticalLayout(Component... components) {
        this();
        addComponents(components);
    }

    public MVerticalLayout with(Component... components) {
        addComponents(components);
        return this;
    }

    public MVerticalLayout withSpacing(boolean spacing) {
        setSpacing(spacing);
        return this;
    }

    public MVerticalLayout withMargin(boolean marging) {
        setMargin(marging);
        return this;
    }

    public MVerticalLayout withMargin(MarginInfo marginInfo) {
        setMargin(marginInfo);
        return this;
    }

    public MVerticalLayout withWidth(String width) {
        setWidth(width);
        return this;
    }

    public MVerticalLayout withFullWidth() {
        setWidth("100%");
        return this;
    }

    public MVerticalLayout withHeight(String height) {
        setHeight(height);
        return this;
    }

    public MVerticalLayout withFullHeight() {
        setHeight("100%");
        return this;
    }

    public MVerticalLayout alignAll(Alignment alignment) {
        for (Component component : this) {
            setComponentAlignment(component, alignment);
        }
        return this;
    }

    /**
     * Expands selected components. Also adds to layout and sets the only sane
     * height for expanded components (100%) if needed.
     *
     * @param componentsToExpand components that should be expanded
     * @return the object itself for further configuration
     */
    public MVerticalLayout expand(Component... componentsToExpand) {
        if (getHeight() < 0) {
            // Make full height if no other size is set
            withFullHeight();
        }
        for (Component component : componentsToExpand) {
            if (component.getParent() != this) {
                addComponent(component);
            }
            setExpandRatio(component, 1);
            component.setHeight(100, Unit.PERCENTAGE);
        }
        return this;
    }

    public MVerticalLayout add(Component... component) {
        return with(component);
    }

    public MVerticalLayout add(Collection<Component> component) {
        return with(component.toArray(new Component[component.size()]));
    }

    public MVerticalLayout add(Component component, Alignment alignment) {
        return add(component).withAlign(component, alignment);
    }

    public MVerticalLayout withAlign(Component component, Alignment alignment) {
        setComponentAlignment(component, alignment);
        return this;
    }

    public MVerticalLayout withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    public MVerticalLayout withStyleName(String styleName) {
        setStyleName(styleName);
        return this;
    }

}
