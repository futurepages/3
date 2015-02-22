package apps.info.workset.dedicada.view.components;

import apps.info.workset.dedicada.AppUI;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import jdk.nashorn.internal.ir.ContinueNode;
import modules.admin.model.core.AdminConstants;
import modules.admin.model.entities.Log;
import modules.admin.model.entities.Role;
import modules.admin.model.entities.User;
import modules.admin.model.services.UserServices;
import org.futurepages.apps.simple.SimpleWindow;
import org.futurepages.core.event.Eventizer;
import org.futurepages.core.event.Events;
import org.futurepages.core.locale.Txt;
import org.futurepages.core.upload.UploadField;
import org.futurepages.exceptions.UserException;
import org.futurepages.formatters.brazil.DateTimeFormatter;
import org.futurepages.util.ImageUtil;
import org.futurepages.util.Is;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UserWindow extends SimpleWindow {

    private final BeanFieldGroup<User> fieldGroup;

    @PropertyId("fullName")
    private TextField fullNameField;

    @PropertyId("email")
    private TextField emailField;

    @PropertyId("oldPassword")
    private PasswordField oldPassword;

    @PropertyId("newPassword")
    private PasswordField newPassword;

    @PropertyId("newPasswordAgain")
    private PasswordField newPasswordAgain;

    @PropertyId("avatarValue")
    private TextField avatarValue;


    private UserWindow(User user) {
        user = UserServices.getInstance().read(user.getLogin());
//        addStyleName("profile-window"); //deixo aqui para estudo posterior. Este style aparentemente só dava o width e height e padding.
//        setId("profilepreferenceswindow");

        setWidth (50.0f, Unit.PERCENTAGE);
        setHeight(82.0f, Unit.PERCENTAGE);

        addTab(buildUserTab(user));

        if(user.hasProfile()){
            addTab(buildProfileTab(user));
        }
        addTab(buildLogAccessesTab(user));

        fieldGroup = new BeanFieldGroup<>(User.class);
        fieldGroup.bindMemberFields(this);
        fieldGroup.setItemDataSource(user);
        addFooter(buildFooter());
    }

    private Component buildUserTab(User user) {
        final HorizontalLayout root = new HorizontalLayout();
        final VerticalLayout picLayout = new VerticalLayout();
        final Image profilePic = new Image(null, user.getAvatarRes());
        final FormLayout details = new FormLayout();

        root.setCaption(Txt.get("user.basic_info"));
        root.setIcon(FontAwesome.USER);
        root.setWidth(100.0f, Unit.PERCENTAGE);
        root.setSpacing(true);
        root.setMargin(true);
        // root.addStyleName("profile-form");

        picLayout.setSizeUndefined();
        picLayout.setSpacing(true);
        profilePic.setWidth(118.0f, Unit.PIXELS);
        picLayout.addComponent(profilePic);
        picLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);

        final UploadField uploadField = new UploadField("Trocar Foto", AdminConstants.AVATAR_MAX_SIZE, UploadField.AllowedTypes.IMAGES,
        event -> {
            File file = event.getReceiver().getNewFileResource().getSourceFile();
                try {
                    ImageUtil.resizeCropping(file, 300, 300, file.getAbsolutePath(), false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                profilePic.setSource(event.getReceiver().getNewFileResource());
                avatarValue.setValue(event.getReceiver().getNewFileName());
        });
        picLayout.addComponent(uploadField);

        final Button removerAvatar = new Button("");
        removerAvatar.setDescription("Remover Avatar");
        removerAvatar.setIcon(FontAwesome.TIMES);
        removerAvatar.setStyleName(ValoTheme.BUTTON_TINY);

         if(Is.empty(user.getAvatarValue())){
           removerAvatar.setVisible(false);
        }

        removerAvatar.addClickListener(event -> {
            profilePic.setSource(AdminConstants.AVATAR_DEFAULT_RES);
            avatarValue.setValue("");
            removerAvatar.setVisible(false);
        });
        uploadField.setStartListener(()  ->  removerAvatar.setVisible(false));
        uploadField.setFinishListener(() ->{  if(!Is.empty(avatarValue.getValue())) removerAvatar.setVisible(true); });
        picLayout.addComponent(removerAvatar);
        removerAvatar.setStyleName("user-no-avatar-button " + ValoTheme.BUTTON_TINY + " " + ValoTheme.BUTTON_ICON_ONLY);


        root.addComponent(picLayout);
        VerticalLayout verticalLayout = new VerticalLayout();
        root.addComponent(verticalLayout);
        root.setExpandRatio(verticalLayout, 1);


        details.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        details.setMargin(false);
        verticalLayout.addComponent(details);
        verticalLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);

        Label labelPrincipal = new Label("Informações Principais");
        labelPrincipal.setStyleName(ValoTheme.LABEL_COLORED);
        labelPrincipal.setIcon(FontAwesome.USER);
        details.addComponent(labelPrincipal);


        Label lbLogin = new Label(user.getLogin());
        lbLogin.setCaption("Login");
        details.addComponent(lbLogin);

        if(user.hasProfile()){
            Label lbProfile = new Label(user.getProfile().getLabel());
            lbProfile.setCaption("Perfil");
            details.addComponent(lbProfile);
        }

        fullNameField = new TextField("Nome Completo");
        details.addComponent(fullNameField);
        emailField = new TextField("Email");
        details.addComponent(emailField);

        Label lbX = new Label("");

        lbX.setStyleName(ValoTheme.LABEL_LARGE);
        details.addComponent(lbX);

        Label labelAlterarSenha = new Label("Definição de Nova Senha");
        labelAlterarSenha.setStyleName(ValoTheme.LABEL_COLORED);
        labelAlterarSenha.setIcon(FontAwesome.LOCK);
        details.addComponent(labelAlterarSenha);

        oldPassword = new PasswordField("Senha Atual");
        details.addComponent(oldPassword);

        newPassword = new PasswordField("Nova Senha");
        details.addComponent(newPassword);

        newPasswordAgain = new PasswordField("Nova Senha (Repetir)");
        details.addComponent(newPasswordAgain);

        avatarValue = new TextField("Avatar Hidden Value");
        avatarValue.setVisible(false);
        details.addComponent(avatarValue);

        return root;
    }

    private Component buildProfileTab(User user) {
        VerticalLayout root = new VerticalLayout();
        root.setCaption(Txt.get("user.profile"));
        root.setIcon(FontAwesome.UNLOCK);
        root.setSpacing(true);
        root.setMargin(true);
        root.setSizeFull();
        root.setHeightUndefined();

        FormLayout details = new FormLayout();
        details.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        root.addComponent(details);
        root.setExpandRatio(details, 1);


        Label perfilLabel = new Label(user.getProfile().getLabel());
        perfilLabel.setCaption("Perfil");
        perfilLabel.setSizeUndefined();
        perfilLabel.addStyleName(ValoTheme.LABEL_LIGHT);
        details.addComponent(perfilLabel);

        TextArea profileDescription = new TextArea("Descrição", user.getProfile().getDescription());
        profileDescription.setEnabled(false);
        profileDescription.setReadOnly(true);
        details.addComponent(profileDescription);
        details.setComponentAlignment(perfilLabel, Alignment.MIDDLE_CENTER);
        Label lbPapeis = new Label();
        lbPapeis.setCaption("Permissões ("+user.getRoles().size()+")");
        lbPapeis.addStyleName(ValoTheme.LABEL_BOLD);
        lbPapeis.setEnabled(true);
        details.addComponent(lbPapeis);
        for(Role role : user.getRoles()){
            Label labelRole = new Label(role.getTitle(), ContentMode.TEXT);
            labelRole.setIcon(FontAwesome.CHECK_CIRCLE);
            details.addComponent(labelRole);
        }
        return root;
    }

    private Component buildLogAccessesTab(User user) {
        VerticalLayout root = new VerticalLayout();
        root.setCaption(Txt.get("user.log_accesses"));
        root.setIcon(FontAwesome.TH_LIST);
        root.setSpacing(true);
        root.setMargin(true);
        root.setSizeFull();
        root.setHeightUndefined();

        FormLayout details = new FormLayout();
        Label label = new Label("Últimos Acessos Realizados:");
        label.setStyleName(ValoTheme.LABEL_COLORED);
        label.setIcon(FontAwesome.TH);
        details.addComponent(label);
        details.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        root.addComponent(details);
        List<Log> accesses = user.getLastAccesses(20);
        for(Log access : accesses){
            Label labelAccess = new Label(new DateTimeFormatter().format(access.getDateTime(),getLocale())+ (access.getIpHost()!=null? " (a partir de "+access.getIpHost()+")":""));
            labelAccess.setIcon(FontAwesome.CLOCK_O);
            details.addComponent(labelAccess);
        }
        return root;
    }

    private Component buildFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);

        Button ok = buildOkButton();
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        return footer;
    }

    private Button buildOkButton() {
        Button ok = new Button(Txt.get("save"));
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
        ok.addClickListener(event -> {
            try {
                User user = fieldGroup.getItemDataSource().getBean();
                fieldGroup.commit();
                UserServices.getInstance().update(user);
                AppUI.getCurrent().notifySuccess(Txt.get("user.profile_successfully_updated"));
                Eventizer.post(new Events.LoggedUserChanged(user));
                close();
            } catch (UserException e) {
                AppUI.getCurrent().notifyErrors(e);
            } catch (CommitException e) {
                throw new RuntimeException(e);
            }
        });
        ok.focus();
        return ok;
    }

    public static void open(final User user, final int tabIdx) {
        (new UserWindow(user)).openTab(tabIdx);
    }
}