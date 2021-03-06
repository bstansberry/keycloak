package org.keycloak.model.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.models.ApplicationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.services.managers.ApplicationManager;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ApplicationModelTest extends AbstractModelTest {
    private ApplicationModel application;
    private RealmModel realm;
    private ApplicationManager appManager;

    @Before
    public void before() throws Exception {
        super.before();
        appManager = new ApplicationManager(realmManager);

        realm = realmManager.createRealm("original");
        application = realm.addApplication("application");
        application.setBaseUrl("http://base");
        application.setManagementUrl("http://management");
        application.setName("app-name");
        application.addRole("role-1");
        application.addRole("role-2");
        application.addDefaultRole("role-1");
        application.addDefaultRole("role-2");

        application.getApplicationUser().addRedirectUri("redirect-1");
        application.getApplicationUser().addRedirectUri("redirect-2");

        application.getApplicationUser().addWebOrigin("origin-1");
        application.getApplicationUser().addWebOrigin("origin-2");

        application.updateApplication();
    }

    @Test
    public void persist() {
        RealmModel persisted = realmManager.getRealm(realm.getId());

        assertEquals(application, persisted.getApplicationNameMap().get("app-name"));
    }

    @Test
    public void json() {
        ApplicationRepresentation representation = appManager.toRepresentation(application);

        RealmModel realm = realmManager.createRealm("copy");
        ApplicationModel copy = appManager.createApplication(realm, representation);

        assertEquals(application, copy);
    }

    public static void assertEquals(ApplicationModel expected, ApplicationModel actual) {
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getBaseUrl(), actual.getBaseUrl());
        Assert.assertEquals(expected.getManagementUrl(), actual.getManagementUrl());
        Assert.assertEquals(expected.getDefaultRoles(), actual.getDefaultRoles());

        UserModel auser = actual.getApplicationUser();
        UserModel euser = expected.getApplicationUser();

        Assert.assertTrue(euser.getRedirectUris().containsAll(auser.getRedirectUris()));
        Assert.assertTrue(euser.getWebOrigins().containsAll(auser.getWebOrigins()));
    }

    public static void assertEquals(List<RoleModel> expected, List<RoleModel> actual) {
        Assert.assertEquals(expected.size(), actual.size());
        Iterator<RoleModel> exp = expected.iterator();
        Iterator<RoleModel> act = actual.iterator();
        while (exp.hasNext()) {
            Assert.assertEquals(exp.next().getName(), act.next().getName());
        }
    }

}

