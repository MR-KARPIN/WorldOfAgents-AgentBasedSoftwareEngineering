package es.upm.emse.absd.team1.agents.tribe.behaviours.tribeBehaviour;

import static es.upm.emse.absd.team1.agents.Utils.ANSI_CYAN;
import es.upm.emse.absd.ontology.woa.*;
import es.upm.emse.absd.ontology.woa.actions.Register;
import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.team1.agents.tribe.AgTribe;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.extern.java.Log;


/*Behaviour inicial de la tribu:
    Funciones:
    0.1 Intentar registrarse en el juego realizando el protocolo de registro en su totalidad.
        0.2 Suscribirse al phase manager
*/


@Log
public class TribeRegistrationFirstBehaviour extends OneShotBehaviour {

    //Services:
    private final static String REGISTRATIONMANAGER = "RegistrationManager";
    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();

    public TribeRegistrationFirstBehaviour(AgTribe a) {
        super(a);
    }

    @Override
    public void action() {
        AID registrationManager= Utils.untilFindDFS(this.getAgent(), REGISTRATIONMANAGER);
        log.info(ANSI_CYAN + this.getAgent().getLocalName() + " I've found you RegistrationManager: "+registrationManager.getLocalName());
        if(registerProtocol(registrationManager)){
            log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Register completed");
            Utils.suscribePhaseManager(this.getAgent(), codec, ontology);
        } else log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Register refused, then I should die");
    }


    // Registration protocol
    private boolean registerProtocol(AID registrationManager) {
        boolean registrado=false;
        this.getAgent().send(Utils.newMsgWithOnto(this.getAgent(), registrationManager, ACLMessage.REQUEST,
                                                    codec, ontology, new Register(), WoaOntologyVocabulary.REGISTER));
        log.info(ANSI_CYAN + this.getAgent().getLocalName() + " Sending register request");

        ACLMessage msg = this.getAgent().blockingReceive(
                MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                MessageTemplate.MatchOntology(ontology.getName())),
                        MessageTemplate.MatchProtocol(WoaOntologyVocabulary.REGISTER)) );
        switch (msg.getPerformative()) {
            case ACLMessage.AGREE -> {
                log.info(ANSI_CYAN + this.getAgent().getLocalName() + "Response received, it is an AGREE");
                ACLMessage msgRegistration = this.getAgent().blockingReceive(
                        MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                                        MessageTemplate.MatchOntology(ontology.getName())),
                                MessageTemplate.MatchProtocol(WoaOntologyVocabulary.REGISTER)) );
                if (msgRegistration.getPerformative() == ACLMessage.INFORM) {
                    log.info(ANSI_CYAN + this.getAgent().getLocalName() + "Message received: INFORM");
                    registrado= true;
                } else if (msgRegistration.getPerformative() == ACLMessage.FAILURE)
                    log.info(ANSI_CYAN + this.getAgent().getLocalName() + "Message received: FAILURE");
                else
                    log.info(ANSI_CYAN + this.getAgent().getLocalName() + "Message received but I don't know what are they talking about");
            }
            case ACLMessage.REFUSE -> log.info(ANSI_CYAN + this.getAgent().getLocalName() + "Response received, it is a REFUSE");
            case ACLMessage.NOT_UNDERSTOOD -> log.info(ANSI_CYAN + this.getAgent().getLocalName() + "Response received, it is a NOT_UNDERSTOOD");
            default ->{}
        }
        return registrado;
    }

}

