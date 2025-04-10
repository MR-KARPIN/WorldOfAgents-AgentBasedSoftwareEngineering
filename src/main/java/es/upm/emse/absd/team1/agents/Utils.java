package es.upm.emse.absd.team1.agents;

import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.ontology.woa.actions.ChangePhase;
import es.upm.emse.absd.team1.agents.platform.AgPhaseManager;
import jade.content.Concept;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.Serializable;

@Log
public final class Utils {

    public static final String TRIBE_0 = "Tribe-0";
    public static final String TRIBE_1 = "Tribe-1";
    public static final String TRIBE_2 = "Tribe-2";
    public static final String TRIBE_3 = "Tribe-3";
    public static final String TRIBE_4 = "Tribe-4";
    public static final String TRIBE_5 = "Tribe-5";
    public static final String TRIBE_6 = "Tribe-6";

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static int cidCnt = 0;
    private static String cidBase;
    public static String modeRun;
    public static final double secondsPerHour = 0.3;
    public static final float STORE_CAPACITY = 1000;

    public static String getTribeController(String tribeName){
        String unitControllerClass="";
        if(modeRun.equals("build")) {
            switch (tribeName) {
                case "Team1" -> unitControllerClass = es.upm.emse.absd.team1.agents.tribe.AgUnitController.class.getName();
                case "Team2" -> unitControllerClass = es.upm.emse.absd.team2.agents.tribe.AgUnitController.class.getName();
                case "Team3" -> unitControllerClass = es.upm.emse.absd.team3.agents.tribe.unitcontroller.AgUnit.class.getName();
                case "Team4"->  unitControllerClass = es.upm.emse.absd.team4.agents.tribe.UnitController.class.getName();
            }
        } else unitControllerClass = es.upm.emse.absd.team1.agents.tribe.AgUnitController.class.getName();
        return unitControllerClass;
    }


    /**
     * Generate a Conversation ID for identifying messages.
     * @param agent the initialising agent of the conversation.
     * @return the CID.
     */
    public static String genCID(Agent agent)
    {
        if (cidBase==null) {
            cidBase = agent.getLocalName() + agent.hashCode() +
                System.currentTimeMillis()%10000 + "_";
        }
        return  cidBase + (cidCnt++);
    }

    /**
     * Returns an ACLMessage with content ready to be sent to a destination.
     * @param origin the initialising agent of the conversation.
     * @param perf the message performative.
     * @param content the message.
     * @param dest identification of the destination agent.
     * @return the message ready to be sent.
     */
    public static ACLMessage newMsg(Agent origin, int perf, String content, AID dest)
    {
        ACLMessage msg = newMsg(origin, perf);
        if (dest != null) msg.addReceiver(dest);
        msg.setContent( content );
        return msg;
    }

    /**
     * Returns an ACLMessage with content ready to be sent to a destination.
     * @param origin the initialising agent of the conversation.
     * @param perf the message performative.
     * @param content the message.
     * @param dest identification of the destination agent.
     * @return the message ready to be sent.
     */
    public static ACLMessage newMsgWithObject(Agent origin, int perf, Serializable content, AID dest, String protocol) {
        ACLMessage msg = newMsg(origin, perf);
        if (dest != null) msg.addReceiver(dest);
        if(protocol != null)
            msg.setProtocol(protocol);
        try {
            msg.setContentObject(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return msg;
    }

    /**
     * Returns an ACLMessage for a concrete Agent with a concrete performative and an unique CID
     * @param origin the initialising agent of the conversation.
     * @param perf the message performative.
     * @return the configured message
     */
    public static ACLMessage newMsg(Agent origin, int perf)
    {
        ACLMessage msg = new ACLMessage(perf);
        msg.setConversationId(genCID(origin));
        return msg;
    }

    /**
     * Returns true if the agent is registered in the DFService.
     * @param agent the agent.
     * @param type the service.
     * @return true if the agent is registered, false otherwise.
     */
    public static boolean register(Agent agent, String type)
    {
        ServiceDescription sd  = new ServiceDescription();
        sd.setType(type);
        sd.setName(agent.getLocalName());
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        dfd.addServices(sd);

        try {
            DFService.register(agent, dfd );
            return true;
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
            return false;
        }
    }

    /**
     * Deregister the agent from all attached services.
     * @param agent the agent.
     */
    public static void deregister(Agent agent) {
        try { DFService.deregister(agent); }
        catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * The agent obtains a list of agents that offer a concrete service.
     * @param agent the agent who searches in the yellow pages
     * @param service the service.
     * @return the ID array of agents attached to the service.
     */
    public static AID[] searchDF(Agent agent, String service)
    {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( service );
        dfd.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(agent, dfd);
            AID[] agents = new AID[result.length];
            for (int i=0; i<result.length; i++)
                agents[i] = result[i].getName();
            return agents;
        }
        catch (FIPAException e) { return null; }
    }


    //bucle infinito hasta encontrar el servicio en el DFS
    public static AID untilFindDFS(Agent agent, String service) {
        AID[] agentes = new AID[0];
        while(agentes.length==0) {
            agentes=Utils.searchDF(agent, service);
        }
        return agentes[0];
    }


    public static ACLMessage newMsgWithOnto(Agent agent, AID destination, int typeMsg,
                                      Codec codec, Ontology ontology,
                                      Object content, String protocol) {

        ACLMessage newMsg = Utils.newMsg(agent, typeMsg,null, destination);
        newMsg.setLanguage(codec.getName());
        newMsg.setOntology(ontology.getName());
        if(protocol != null)
            newMsg.setProtocol(protocol);
        // As it is an action and the encoding language the SL, it must be wrapped into an Action.
        if(content!=null) {
            Action agAction = new Action(destination, (Concept) content);
            try {
                agent.getContentManager().fillContent(newMsg, agAction);

            } catch (Codec.CodecException | OntologyException e) {
                e.printStackTrace();
            }
        }
        return newMsg;
    }

    //Extrae contenido de un mensaje con ontologia
    public static Action extractMsgOntoContent(Agent ag, ACLMessage msg) {
        try {
            // The ContentManager transforms the content (string) in java objects
            return (Action) ag.getContentManager().extractContent(msg);
        } catch (Codec.CodecException | OntologyException e) {
            return null;
        }
    }

    public static void suscribePhaseManager(Agent agent, Codec codec, Ontology ontology){
        AID phaseManager = Utils.untilFindDFS(agent, AgPhaseManager.PHASEMANAGER);
        ChangePhase cambioFase = new ChangePhase();
        agent.send(Utils.newMsgWithOnto(agent, phaseManager, ACLMessage.SUBSCRIBE,
                codec, ontology, cambioFase, WoaOntologyVocabulary.CHANGE_PHASE));
        ACLMessage msg = agent.blockingReceive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CHANGE_PHASE)));
        switch (msg.getPerformative()){
            case ACLMessage.AGREE -> log.info(ANSI_YELLOW + agent.getLocalName() + "Respuesta recibida de subscripcion: AGREE");
            case ACLMessage.REFUSE -> log.info(ANSI_YELLOW + agent.getLocalName() + "Respuesta recibida de subscripcion: REFUSE");
            case ACLMessage.NOT_UNDERSTOOD -> log.info(ANSI_YELLOW + agent.getLocalName() + "Respuesta recibida de subscripcion: NOT_UNDERSTOOD");
            default ->{}
        }
    }


}
