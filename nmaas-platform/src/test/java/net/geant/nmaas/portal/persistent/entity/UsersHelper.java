package net.geant.nmaas.portal.persistent.entity;

import java.util.Arrays;
import java.util.List;

public class UsersHelper {

	public final static Domain GLOBAL = new Domain(1l, "GLOBAL", "GLOBAL");
	public final static Domain DOMAIN1 = new Domain(2l, "domain1", "D1");
	public final static Domain DOMAIN2 = new Domain(3l, "domain2", "D2");

	
	public final static User ADMIN = new User(1l, "admin", true, GLOBAL, Role.ROLE_SYSTEM_ADMIN);
	public final static User TOOL_MANAGER = new User(2l , "toolman", true, GLOBAL, Role.ROLE_TOOL_MANAGER);
	
	public final static User DOMAIN1_ADMIN = new User(3l, "domain1_admin", true, DOMAIN1, Role.ROLE_DOMAIN_ADMIN);
	public final static User DOMAIN1_USER1 = new User(4l, "domain1_user1", true, DOMAIN1, Role.ROLE_USER);
	public final static User DOMAIN1_USER2 = new User(5l, "domain1_user2", true, DOMAIN1, Role.ROLE_USER);
	public final static User DOMAIN1_GUEST = new User(6l, "domain1_guest", true, DOMAIN1, Role.ROLE_GUEST);
	
	
	public final static User DOMAIN2_ADMIN = new User(7l, "domain2_admin", true, DOMAIN2, Role.ROLE_DOMAIN_ADMIN);
	public final static User DOMAIN2_USER1 = new User(8l, "domain2_user1", true, DOMAIN2, Role.ROLE_USER);
	public final static User DOMAIN2_USER2 = new User(9l, "domain2_user2", true, DOMAIN2, Role.ROLE_USER);
	public final static User DOMAIN2_GUEST = new User(10l, "domain2_guest", true, DOMAIN2, Role.ROLE_GUEST);
	
	public final static User GLOBAL_GUEST = new User(11l, "unassigned_guest", true, GLOBAL, Role.ROLE_GUEST);

	public final static List<User> USERS = Arrays.asList(ADMIN, TOOL_MANAGER, DOMAIN1_ADMIN, DOMAIN1_USER1, DOMAIN1_USER2, DOMAIN1_GUEST, DOMAIN2_ADMIN, DOMAIN2_USER1, DOMAIN2_USER2, DOMAIN2_GUEST, GLOBAL_GUEST);
	public final static List<Domain> DOMAINS = Arrays.asList(GLOBAL, DOMAIN1, DOMAIN2);
		
	
	public final static Application APP1 = new Application(1l, "App1","testversion", "owner");
	public final static Application APP2 = new Application(2l, "App2","testversion", "owner");
	public final static Application APP3 = new Application(3l, "App3","testversion", "owner");
	
	
	public final static AppInstance DOMAIN1_APP1 = new AppInstance(1l, APP1, "domain1_app1", DOMAIN1, DOMAIN1_USER1);
	public final static AppInstance DOMAIN1_APP2 = new AppInstance(2l, APP3, "domain1_app2", DOMAIN1, DOMAIN1_USER2);
	public final static AppInstance DOMAIN2_APP1 = new AppInstance(3l, APP2, "domain2_app1", DOMAIN2, DOMAIN2_USER1);
	public final static AppInstance DOMAIN2_APP2 = new AppInstance(4l, APP3, "domain2_app2", DOMAIN2, DOMAIN2_USER1);
	
	List<Application> applications = Arrays.asList(APP1, APP2, APP3);
	List<AppInstance> appInstances = Arrays.asList(DOMAIN1_APP1, DOMAIN1_APP2, DOMAIN2_APP1, DOMAIN2_APP2);
	
	
	public final static Comment COMMENT1 = new Comment(1l, APP1, "app1_comment1", DOMAIN1_USER1);
	public final static Comment COMMENT2 = new Comment(2l, APP2, "app2_comment1", DOMAIN1_USER1);
	public final static Comment COMMENT3 = new Comment(3l, APP2, "app2_comment2", DOMAIN2_USER2);
	
	List<Comment> comments = Arrays.asList(COMMENT1, COMMENT2, COMMENT3);
}
