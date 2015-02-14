package apps.info.workset.dedicada;

import apps.info.workset.dedicada.model.data.DataProvider;
import apps.info.workset.dedicada.model.data.dummy.DummyDataProvider;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import modules.admin.model.entities.User;
import modules.admin.model.exceptions.ExpiredPasswordException;
import modules.admin.model.exceptions.InvalidUserOrPasswordException;
import modules.admin.model.services.UserServices;
import org.futurepages.core.admin.DefaultUser;
import org.futurepages.core.control.vaadin.BrowserCookie;
import org.futurepages.core.control.vaadin.DefaultMenu;
import org.futurepages.core.control.vaadin.DefaultUI;
import org.futurepages.exceptions.UserException;
import org.futurepages.util.Is;

@Title("Workset Dedicada")
@Theme("dashboard")
public class AppUI extends DefaultUI {

    private static final String LOCAL_USER_KEY = "_luserk";

    //TODO tempo while learning, delete it soon...
    private final DataProvider dataProvider = new DummyDataProvider();
    public static DataProvider getDataProvider() {
        return ((AppUI) getCurrent()).dataProvider;
    }

    @Override
    protected DefaultMenu initAppMenu() {
        return new AppMenu();
    }

    @Override
    protected DefaultUser authenticate(String login, String password) {
        try {
            return UserServices.authenticatedAndDetachedUser(login, password);
        } catch (InvalidUserOrPasswordException | ExpiredPasswordException e) {
            throw new UserException(e);
        }
    }

    @Override
    protected void storeUserLocally(DefaultUser user) {
        BrowserCookie.setCookie(LOCAL_USER_KEY, ((User)user).identifiedHashToStore());
    }

    @Override
    protected DefaultUser loadUserLocally() {
       String loggedValue = BrowserCookie.getByName(LOCAL_USER_KEY);
        if (!Is.empty(loggedValue)) {
            User dbUser = UserServices.getByIdentiedHash(loggedValue);
            if(dbUser!=null){
                BrowserCookie.setCookie(LOCAL_USER_KEY, loggedValue);
                UserServices.turnDetached(dbUser);
                return dbUser;
            }
        }
        return null;
    }

    @Override
    protected void removeUserLocally() {
        BrowserCookie.removeCookie(LOCAL_USER_KEY);
    }
}