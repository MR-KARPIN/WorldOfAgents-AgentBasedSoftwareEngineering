package es.upm.emse.absd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.upm.emse.absd.team1.agents.Utils;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.junit.jupiter.api.*;

class UtilsTest {

    static private ACLMessage expectedMsg;

    @BeforeAll static void setUp() {
        expectedMsg = new ACLMessage(ACLMessage.INFORM);
        expectedMsg.setSender(null);
        expectedMsg.setContent("test");
    }

    @Test void testNewMsg() {
        ACLMessage msg = Utils.newMsg(new Agent(), ACLMessage.INFORM, "test", null);
        assertEquals(expectedMsg.getPerformative(), msg.getPerformative());
        assertEquals(expectedMsg.getContent(), msg.getContent());
        assertNull(msg.getSender());
    }
}