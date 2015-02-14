package org.futurepages.core.control.vaadin;

import com.vaadin.navigator.View;
import com.vaadin.server.Resource;

public class DefaultViewItem {

    private final String viewName;
    private final Class<? extends View> viewClass;
    private final Resource icon;
    private final boolean stateful;

	 public DefaultViewItem(final String viewName,final Class<? extends View> viewClass, final Resource icon,final boolean stateful) {
        this.viewName = viewName;
        this.viewClass = viewClass;
        this.icon = icon;
        this.stateful = stateful;
    }

	public String getViewName() {
		return viewName;
	}

	public Class<? extends View> getViewClass() {
		return viewClass;
	}

	public Resource getIcon() {
		return icon;
	}

	public boolean isStateful() {
		return stateful;
	}
}
