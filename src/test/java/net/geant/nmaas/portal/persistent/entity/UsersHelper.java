package net.geant.nmaas.portal.persistent.entity;

import java.util.Arrays;
import java.util.List;

public class UsersHelper {

	public final static Domain GLOBAL = new Domain(1L, "GLOBAL", "GLOBAL");
	public final static Domain DOMAIN1 = new Domain(2L, "domain1", "D1");
	public final static Domain DOMAIN2 = new Domain(3L, "domain2", "D2");

	public final static User ADMIN = new User(1L, "admin", true, GLOBAL, Role.ROLE_SYSTEM_ADMIN);
	public final static User TOOL_MANAGER = new User(2L , "toolman", true, GLOBAL, Role.ROLE_TOOL_MANAGER);
	
	public final static User DOMAIN1_ADMIN = new User(3L, "domain1_admin", true, DOMAIN1, Role.ROLE_DOMAIN_ADMIN);
	public final static User DOMAIN1_USER1 = new User(4L, "domain1_user1", true, DOMAIN1, Role.ROLE_USER);
	public final static User DOMAIN1_USER2 = new User(5L, "domain1_user2", true, DOMAIN1, Role.ROLE_USER);
	public final static User DOMAIN1_GUEST = new User(6L, "domain1_guest", true, DOMAIN1, Role.ROLE_GUEST);
	
	public final static User DOMAIN2_ADMIN = new User(7L, "domain2_admin", true, DOMAIN2, Role.ROLE_DOMAIN_ADMIN);
	public final static User DOMAIN2_USER1 = new User(8L, "domain2_user1", true, DOMAIN2, Role.ROLE_USER);
	public final static User DOMAIN2_USER2 = new User(9L, "domain2_user2", true, DOMAIN2, Role.ROLE_USER);
	public final static User DOMAIN2_GUEST = new User(10L, "domain2_guest", true, DOMAIN2, Role.ROLE_GUEST);
	
	public final static User GLOBAL_GUEST = new User(11L, "unassigned_guest", true, GLOBAL, Role.ROLE_GUEST);
	public final static User OPERATOR = new User(12L, "operator", true, GLOBAL, Role.ROLE_OPERATOR);

	public final static List<User> USERS = Arrays.asList(ADMIN, TOOL_MANAGER, DOMAIN1_ADMIN, DOMAIN1_USER1, DOMAIN1_USER2, DOMAIN1_GUEST, DOMAIN2_ADMIN, DOMAIN2_USER1, DOMAIN2_USER2, DOMAIN2_GUEST, GLOBAL_GUEST);
	public final static List<Domain> DOMAINS = Arrays.asList(GLOBAL, DOMAIN1, DOMAIN2);
	
	public final static Application APP1 = new Application(1L, "App1","testversion");
	public final static Application APP2 = new Application(2L, "App2","testversion");
	public final static Application APP3 = new Application(3L, "App3","testversion");

	public final static ApplicationBase APP1_BASE = new ApplicationBase(1L, "App1");
	public final static ApplicationBase APP2_BASE = new ApplicationBase(2L, "App2");
	public final static ApplicationBase APP3_BASE = new ApplicationBase(3L, "App3");

	public final static AppInstance DOMAIN1_APP1 = new AppInstance(1L, APP1, "domain1_app1", DOMAIN1, true, DOMAIN1_USER1);
	public final static AppInstance DOMAIN1_APP2 = new AppInstance(2L, APP3, "domain1_app2", DOMAIN1, false, DOMAIN1_USER2);
	public final static AppInstance DOMAIN2_APP1 = new AppInstance(3L, APP2, "domain2_app1", DOMAIN2, true, DOMAIN2_USER1);
	public final static AppInstance DOMAIN2_APP2 = new AppInstance(4L, APP3, "domain2_app2", DOMAIN2, false, DOMAIN2_USER1);

    List<Application> applications = Arrays.asList(APP1, APP2, APP3);
	List<AppInstance> appInstances = Arrays.asList(DOMAIN1_APP1, DOMAIN1_APP2, DOMAIN2_APP1, DOMAIN2_APP2);

	public final static Comment COMMENT1 = new Comment(1L, APP1_BASE, "app1_comment1", DOMAIN1_USER1);
	public final static Comment COMMENT2 = new Comment(2L, APP2_BASE, "app2_comment1", DOMAIN1_USER1);
	public final static Comment COMMENT3 = new Comment(3L, APP2_BASE, "app2_comment2", DOMAIN2_USER2);
	
	List<Comment> comments = Arrays.asList(COMMENT1, COMMENT2, COMMENT3);
}
