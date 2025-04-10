package es.upm.emse.absd.team1.agents.platform.RegistrationManager;

import es.upm.emse.absd.ontology.woa.WoaOntologyVocabulary;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.ontology.woa.concepts.NewPhase;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Data;
import lombok.extern.java.Log;

import static es.upm.emse.absd.team1.agents.Utils.*;

@Log
@Data
public class RegistrationSecondBehaviour extends ParallelBehaviour {

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();
    private AgRegistrationManager agente;

    public RegistrationSecondBehaviour(AgRegistrationManager a) {
        super();
        this.agente=a;

        //Registra tribus
        this.addSubBehaviour(new SimpleBehaviour() {

            @Override
            public void action() {
                ACLMessage msg = this.getAgent().receive(
                        MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                        MessageTemplate.MatchOntology(ontology.getName())),
                                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.REGISTER))
                );
                if(msg!=null) {
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje");
                    registrationManagerLogic(msg);
                }
            }

            @Override
            public boolean done() {
                return false;
            }
        });

        //Escucha y recibe cambios de fase del PhaseManager
        this.addSubBehaviour(new SimpleBehaviour() {

            @Override
            public void action() { receiveNotifyChangePhase(); }

            @Override
            public boolean done() {
                return agente.getPhase()==2;
            }
        });
    }


    private void registrationManagerLogic(ACLMessage msg) {
        if (msg.getProtocol().equals(WoaOntologyVocabulary.REGISTER)) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                    " Msg recibido de petición de registro");
            ACLMessage response = msg.createReply();
            if (this.agente.getPhase()==0) { //enviar mensaje con agree con ontología
                //1. enviar agree
                response.setPerformative(ACLMessage.AGREE);
                this.getAgent().send(response);
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Respuesta AGREE enviada");
                //2. mirar si está ya registrado o no.
                if (this.agente.getRegisteredTribes().containsKey(msg.getSender())) {
                    //2.1 Ya registrado --> enviar failure.
                    response.setPerformative(ACLMessage.FAILURE);
                    this.getAgent().send(response);
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Respuesta FAILURE enviada, agente " +
                            msg.getSender() + " ya registrado");
                } else {
                    //2.2 No registrado --> enviar inform y registrar
                    response.setPerformative(ACLMessage.INFORM);
                    this.getAgent().send(response);
                    String tribeName = msg.getSender().getLocalName();
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Respuesta INFORM enviada, tribu " + tribeName + " registrandose...");
                    this.agente.getRegisteredTribes().put(msg.getSender(), tribeName);
                    //mandar mensaje a tribe accountant para que les cree recursos
                    Object[] newTribeInfo = {msg.getSender(), tribeName};
                    ACLMessage newTribeAccMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                            newTribeInfo, this.agente.getTribeAccountant(), null);
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje enviado a TribeAccountant con la info del nuevo agente: " +
                            msg.getSender() + " con nombre de tribu: " + tribeName);
                    this.getAgent().send(newTribeAccMsg);

                    //mandar mensaje a unit manager para que les cree unidades
                    ACLMessage newUnitManMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                            newTribeInfo, this.agente.getUnitManager(),"nuevaTribu");
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje enviado a UnitManager con la info del nuevo agente: " +
                            msg.getSender() + " con nombre de tribu: " + tribeName);
                    this.getAgent().send(newUnitManMsg);

                    //mandar mensaje a map manager con el nombre de la tribu
                    ACLMessage newMapManMsg = newMsgWithObject(this.getAgent(), ACLMessage.INFORM,
                            newTribeInfo, this.agente.getMapManager(),"nuevaTribu");
                    log.info(ANSI_YELLOW + this.getAgent().getLocalName() +
                            " Mensaje enviado a MapManager con el nombre de la tribu: " +tribeName +
                            msg.getSender());
                    this.getAgent().send(newMapManMsg);

                }
                //not allowRegistration
            } else {
                //enviar mensaje con refuse
                response.setPerformative(ACLMessage.REFUSE);
                this.getAgent().send(response);
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Respuesta REFUSE enviada");
            }
        } else {
            ACLMessage response = msg.createReply();
            response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            this.getAgent().send(response);
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Respuesta NOT_UNDERSTOOD enviada");
        }
    }


    //Escucha al PhaseManager para cambiar de fases.
    private void receiveNotifyChangePhase() {
        ACLMessage msg = this.getAgent().receive(MessageTemplate.and(MessageTemplate.and(
                        MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.MatchOntology(ontology.getName())),
                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.CHANGE_PHASE)));
        if (msg != null) {
            log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Recibe mensaje de cambio de fase");
            Action actionMsg = Utils.extractMsgOntoContent(this.getAgent(), msg);
            NewPhase nuevaFase;
            if (actionMsg != null) {
                nuevaFase = (NewPhase) actionMsg.getAction();
                this.agente.setPhase(nuevaFase.getPhase());
                log.info(ANSI_YELLOW + this.getAgent().getLocalName() + " Cambio a fase: " + this.agente.getPhase());
            }
        }
    }

}
