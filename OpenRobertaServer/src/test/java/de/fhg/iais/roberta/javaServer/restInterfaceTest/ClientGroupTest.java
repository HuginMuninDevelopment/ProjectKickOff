package de.fhg.iais.roberta.javaServer.restInterfaceTest;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;

import de.fhg.iais.roberta.factory.IRobotFactory;
import de.fhg.iais.roberta.javaServer.restServices.all.ClientAdmin;
import de.fhg.iais.roberta.javaServer.restServices.all.ClientGroup;
import de.fhg.iais.roberta.javaServer.restServices.all.ClientUser;
import de.fhg.iais.roberta.javaServer.restServices.robot.RobotCommand;
import de.fhg.iais.roberta.javaServer.restServices.robot.RobotDownloadProgram;
import de.fhg.iais.roberta.main.ServerStarter;
import de.fhg.iais.roberta.persistence.util.DbSetup;
import de.fhg.iais.roberta.persistence.util.HttpSessionState;
import de.fhg.iais.roberta.persistence.util.SessionFactoryWrapper;
import de.fhg.iais.roberta.robotCommunication.RobotCommunicator;
import de.fhg.iais.roberta.testutil.JSONUtilForServer;
import de.fhg.iais.roberta.util.Key;
import de.fhg.iais.roberta.util.RobertaProperties;
import de.fhg.iais.roberta.util.Util1;
import de.fhg.iais.roberta.util.dbc.DbcException;

/**
 * Testing the REST interface for groups of the OpenRoberta server
 *
 * @author Evgeniya
 */
public class ClientGroupTest {

    private SessionFactoryWrapper sessionFactoryWrapper; // used by REST services to retrieve data base sessions
    private DbSetup memoryDbSetup; // use to query the test data base, change the data base at will, etc.

    private Response response; // store all REST responses here
    private HttpSessionState sPid; // reference user 1 is "pid"
    private HttpSessionState sMinscha; // reference user 2 is "minscha"

    // objects for specialized user stories
    private String connectionUrl;

    private RobotCommunicator brickCommunicator;

    private ClientUser restUser;
    private ClientGroup restGroup;

    private ClientAdmin restBlocks;
    private RobotDownloadProgram downloadJar;
    private RobotCommand brickCommand;

    @Before
    public void setup() throws Exception {
        Properties robertaProperties = Util1.loadProperties(null);
        RobertaProperties.setRobertaProperties(robertaProperties);

        this.connectionUrl = "jdbc:hsqldb:mem:performanceInMemoryDb";
        this.brickCommunicator = new RobotCommunicator();
        this.restUser = new ClientUser(this.brickCommunicator, null);
        this.restBlocks = new ClientAdmin(this.brickCommunicator);

        this.brickCommand = new RobotCommand(this.brickCommunicator);

        this.sessionFactoryWrapper = new SessionFactoryWrapper("hibernate-test-cfg.xml", this.connectionUrl);
        Session nativeSession = this.sessionFactoryWrapper.getNativeSession();
        this.memoryDbSetup = new DbSetup(nativeSession);
        this.memoryDbSetup.runDefaultRobertaSetup();
        Map<String, IRobotFactory> robotPlugins = new HashMap<>();
        loadPlugin(robotPlugins);
        this.sPid = HttpSessionState.init(this.brickCommunicator, robotPlugins, 1);
        this.sMinscha = HttpSessionState.init(this.brickCommunicator, robotPlugins, 2);
    }

    @Ignore
    public void createGroup() throws Exception {
        Assert.assertTrue(!this.sPid.isUserLoggedIn());
        long initNumberOfGroups = this.memoryDbSetup.getOneBigIntegerAsLong("select count(*) from GROUPS");
        restGroup(this.sPid, "{'cmd':'createGroup';'name':'restTestGroup';'userId':'1'}", "ok", Key.GROUP_CREATE_SUCCESS);
        long finalNumberOfGroups = this.memoryDbSetup.getOneBigIntegerAsLong("select count(*) from GROUPS");
        long diff = finalNumberOfGroups - initNumberOfGroups;
        Assert.assertEquals(1, diff);
    }

    @Ignore
    public void getGroup() throws Exception {
        restGroup(this.sPid, "{'cmd':'getGroup';'name':'TestGroup'}", "ok", Key.GROUP_GET_SUCCESS);
    }

    @Ignore
    public void addUser() throws Exception {
        long initNumberOfUsersInGroup = this.memoryDbSetup.getOneBigIntegerAsLong("select count(*) from USER_GROUP where GROUP_ID=:5");
        restGroup(this.sPid, "{'cmd':'addUser';'userId':'1';'groupId':'5'}", "ok", Key.GROUP_CREATE_SUCCESS);
        long finalNumberOfUsersInGroup = this.memoryDbSetup.getOneBigIntegerAsLong("select count(*) from USER_GROUP where GROUP_ID=:5");
        long diff = finalNumberOfUsersInGroup - initNumberOfUsersInGroup;
        Assert.assertEquals(1, diff);
    }

    @Ignore
    public void deleteUser() throws Exception {
        long initNumberOfUsersInGroup = this.memoryDbSetup.getOneBigIntegerAsLong("select count(*) from USER_GROUP where GROUP_ID=:1");
        restGroup(this.sPid, "{'cmd':'deleteUser';'userId':'1';'groupId':'1'}", "ok", Key.GROUP_CREATE_SUCCESS);
        long finalNumberOfUsersInGroup = this.memoryDbSetup.getOneBigIntegerAsLong("select count(*) from USER_GROUP where GROUP_ID=:1");
        long diff = finalNumberOfUsersInGroup - initNumberOfUsersInGroup;
        Assert.assertEquals(-1, diff);
    }

    @Ignore
    public void deleteGroup() throws Exception {
        long initNumberOfGroups = this.memoryDbSetup.getOneBigIntegerAsLong("select count(*) from GROUPS");
        restGroup(this.sPid, "{'cmd':'deleteGroup';'name':'restTestGroup'}", "ok", Key.GROUP_CREATE_SUCCESS);
        long finalNumberOfGroups = this.memoryDbSetup.getOneBigIntegerAsLong("select count(*) from GROUPS");
        long diff = finalNumberOfGroups - initNumberOfGroups;
        Assert.assertEquals(-1, diff);
    }

    private void restGroup(HttpSessionState httpSession, String jsonAsString, String result, Key msgOpt) throws Exception {
        this.response = this.restGroup.command(httpSession, this.sessionFactoryWrapper.getSession(), JSONUtilForServer.mkD(jsonAsString));
        JSONUtilForServer.assertEntityRc(this.response, result, msgOpt);
    }

    private void loadPlugin(Map<String, IRobotFactory> robotPlugins) {
        try {
            @SuppressWarnings("unchecked")
            Class<IRobotFactory> factoryClass = (Class<IRobotFactory>) ServerStarter.class.getClassLoader().loadClass("de.fhg.iais.roberta.factory.EV3Factory");
            Constructor<IRobotFactory> factoryConstructor = factoryClass.getDeclaredConstructor(RobotCommunicator.class);
            robotPlugins.put("ev3", factoryConstructor.newInstance(this.brickCommunicator));
        } catch ( Exception e ) {
            throw new DbcException("robot plugin ev3 has an invalid factory. Check the properties. Server does NOT start", e);
        }
    }
}