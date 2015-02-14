package apps.info.workset.dedicada;

import apps.info.workset.dedicada.model.entities.Transaction;
import apps.info.workset.dedicada.view.components.ProfilePreferencesWindow;
import apps.info.workset.dedicada.view.pages.dashboard.HomeView;
import apps.info.workset.dedicada.view.pages.reports.ReportsView;
import apps.info.workset.dedicada.view.pages.sales.SalesView;
import apps.info.workset.dedicada.view.pages.schedule.ScheduleView;
import apps.info.workset.dedicada.view.pages.transactions.TransactionsView;
import com.google.common.eventbus.Subscribe;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import modules.admin.model.entities.User;
import org.futurepages.core.admin.DefaultUser;
import org.futurepages.core.control.vaadin.DefaultEventBus;
import org.futurepages.core.control.vaadin.DefaultMenu;
import org.futurepages.core.control.vaadin.DefaultUI;
import org.futurepages.core.control.vaadin.DefaultViewItem;
import org.futurepages.core.locale.Txt;

import java.util.Collection;

/**
 * A responsive menu component providing user information and the controls for
 * primary navigation between the views.
 */
@SuppressWarnings({"serial", "unchecked"})
public final class AppMenu extends DefaultMenu {

	public  static final String ID                     = "dashboard-menu";
	public  static final String REPORTS_BADGE_ID       = "dashboard-menu-reports-badge";
	public  static final String NOTIFICATIONS_BADGE_ID = "dashboard-menu-notifications-badge";
	private static final String STYLE_VISIBLE          = "valo-menu-visible";

	private Label notificationsBadge;
	private Label reportsBadge;
	private MenuItem settingsItem;

	public AppMenu() {
		addStyleName("valo-menu");
		setId(ID);
		setSizeUndefined();

		// There's only one DashboardMenu per UI so this doesn't need to be
		// unregistered from the UI-scoped DashboardEventBus.
		DefaultEventBus.register(this);

		setCompositionRoot(buildContent());
	}

	private Component buildContent() {
		final CssLayout menuContent = new CssLayout();
		menuContent.addStyleName("sidebar");
		menuContent.addStyleName(ValoTheme.MENU_PART);
		menuContent.addStyleName("no-vertical-drag-hints");
		menuContent.addStyleName("no-horizontal-drag-hints");
		menuContent.setWidth(null);
		menuContent.setHeight("100%");

		menuContent.addComponent(buildTitle());
		menuContent.addComponent(buildUserMenu());
		menuContent.addComponent(buildToggleButton());
		menuContent.addComponent(buildMenuItems());

		return menuContent;
	}

	private Component buildTitle() {
		Label logo = new Label(Txt.get("menu.app_title"), ContentMode.HTML);
		logo.setSizeUndefined();
		HorizontalLayout logoWrapper = new HorizontalLayout(logo);
		logoWrapper.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);
		logoWrapper.addStyleName("valo-menu-title");
		return logoWrapper;
	}

	private Component buildUserMenu() {
		final MenuBar settings = new MenuBar();
		settings.addStyleName("user-menu");
		final User user = (User) DefaultUI.getCurrentUser();
		settingsItem = settings.addItem("", new ThemeResource("img/profile-pic-300px.jpg"), null);
		updateUserName(null);
		settingsItem.addItem(Txt.get("menu.edit_profile"), selectedItem -> ProfilePreferencesWindow.open(user, false));
		settingsItem.addItem(Txt.get("menu.preferences"), selectedItem -> ProfilePreferencesWindow.open(user, true));
		settingsItem.addSeparator();
		settingsItem.addItem(Txt.get("menu.sign_out"), selectedItem -> DefaultEventBus.post(new AppEvents.UserLoggedOutEvent()));
		return settings;
	}

	private Component buildToggleButton() {
		Button valoMenuToggleButton = new Button("Menu", event -> {
			if (getCompositionRoot().getStyleName().contains(STYLE_VISIBLE)) {
				getCompositionRoot().removeStyleName(STYLE_VISIBLE);
			} else {
				getCompositionRoot().addStyleName(STYLE_VISIBLE);
			}
		});
		valoMenuToggleButton.setIcon(FontAwesome.LIST);
		valoMenuToggleButton.addStyleName("valo-menu-toggle");
		valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_SMALL);
		return valoMenuToggleButton;
	}

	private Component buildMenuItems() {
		CssLayout menuItemsLayout = new CssLayout();
		menuItemsLayout.addStyleName("valo-menuitems");
		menuItemsLayout.setHeight(100.0f, Unit.PERCENTAGE);

		for (final String viewItemName  : getItemViews().keySet()) {
			Component menuItemComponent = new ValoMenuItemButton(getItemViews().get(viewItemName));

			if (viewItemName.equals("reports")) {
				// Add drop target to reports button
				DragAndDropWrapper reports = new DragAndDropWrapper(
						menuItemComponent);
				reports.setDragStartMode(DragStartMode.NONE);
				reports.setDropHandler(new DropHandler() {

					@Override
					public void drop(final DragAndDropEvent event) {
						UI.getCurrent().getNavigator().navigateTo("reports");
						Table table = (Table) event.getTransferable().getSourceComponent();
						DefaultEventBus.post(new AppEvents.TransactionReportEvent((Collection<Transaction>) table.getValue()));
					}

					@Override
					public AcceptCriterion getAcceptCriterion() {
						return AcceptItem.ALL;
					}

				});
				menuItemComponent = reports;
			}

			if (viewItemName.equals("home")) {
					notificationsBadge = new Label();
				notificationsBadge.setId(NOTIFICATIONS_BADGE_ID);
				menuItemComponent = buildBadgeWrapper(menuItemComponent, notificationsBadge);
			}
			if (viewItemName.equals("reports")) {
				reportsBadge = new Label();
				reportsBadge.setId(REPORTS_BADGE_ID);
				menuItemComponent = buildBadgeWrapper(menuItemComponent,reportsBadge);
			}
			menuItemsLayout.addComponent(menuItemComponent);
		}
		return menuItemsLayout;

	}

	private Component buildBadgeWrapper(final Component menuItemButton, final Component badgeLabel) {
		CssLayout dashboardWrapper = new CssLayout(menuItemButton);
		dashboardWrapper.addStyleName("badgewrapper");
		dashboardWrapper.addStyleName(ValoTheme.MENU_ITEM);
		dashboardWrapper.setWidth(100.0f, Unit.PERCENTAGE);
		badgeLabel.addStyleName(ValoTheme.MENU_BADGE);
		badgeLabel.setWidthUndefined();
		badgeLabel.setVisible(false);
		dashboardWrapper.addComponent(badgeLabel);
		return dashboardWrapper;
	}

	@Override
	public void attach() {
		super.attach();
		updateNotificationsCount(null);
	}

   @Override
    protected DefaultViewItem homeViewItem() {
        return       new DefaultViewItem("home",    HomeView.class,         FontAwesome.HOME,        true);
    }

    @Override
    protected void registerOtherItemViews() {
        registerView(new DefaultViewItem("sales",        SalesView.class,        FontAwesome.BAR_CHART_O, false));
        registerView(new DefaultViewItem("transactions", TransactionsView.class, FontAwesome.TABLE,       false));
        registerView(new DefaultViewItem("reports",      ReportsView.class,      FontAwesome.FILE_TEXT_O, true));
        registerView(new DefaultViewItem("schedule",     ScheduleView.class,     FontAwesome.CALENDAR_O,  false));
    }

	@Subscribe
	public void postViewChange(final AppEvents.PostViewChangeEvent event) {
		// After a successful view change the menu can be hidden in mobile view.
		getCompositionRoot().removeStyleName(STYLE_VISIBLE);
	}

	@Subscribe
	public void updateNotificationsCount(
			final AppEvents.NotificationsCountUpdatedEvent event) {
		int unreadNotificationsCount = AppUI.getDataProvider().getUnreadNotificationsCount();
		notificationsBadge.setValue(String.valueOf(unreadNotificationsCount));
		notificationsBadge.setVisible(unreadNotificationsCount > 0);
	}

	@Subscribe
	public void updateReportsCount(final AppEvents.ReportsCountUpdatedEvent event) {
		reportsBadge.setValue(String.valueOf(event.getCount()));
		reportsBadge.setVisible(event.getCount() > 0);
	}

	@Subscribe
	public void updateUserName(final AppEvents.ProfileUpdatedEvent event) {
		DefaultUser user = DefaultUI.getCurrentUser();
		settingsItem.setText(user.getFullName());
	}

	public final class ValoMenuItemButton extends Button {

		private static final String STYLE_SELECTED = "selected";

		private final DefaultViewItem view;

		public ValoMenuItemButton(final DefaultViewItem view) {
			this.view = view;
			setPrimaryStyleName("valo-menu-item");
			setIcon(view.getIcon());
			setCaption(view.getViewName().substring(0, 1).toUpperCase() + view.getViewName().substring(1));
			DefaultEventBus.register(this);
			addClickListener(event -> UI.getCurrent().getNavigator().navigateTo(view.getViewName()));

		}

		@Subscribe
		public void postViewChange(final AppEvents.PostViewChangeEvent event) {
			removeStyleName(STYLE_SELECTED);
			if (event.getViewItem() == view) {
				addStyleName(STYLE_SELECTED);
			}
		}
	}
}
