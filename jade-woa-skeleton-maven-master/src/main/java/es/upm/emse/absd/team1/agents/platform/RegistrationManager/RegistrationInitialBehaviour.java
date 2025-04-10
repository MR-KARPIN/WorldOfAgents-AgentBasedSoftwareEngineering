package es.upm.emse.absd.team1.agents.platform.RegistrationManager;

import es.upm.emse.absd.team1.agents.Utils;
import es.upm.emse.absd.ontology.woa.WoaOntology;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.behaviours.OneShotBehaviour;
import lombok.extern.java.Log;
import static es.upm.emse.absd.team1.agents.Utils.ANSI_YELLOW;

@Log
public class RegistrationInitialBehaviour extends OneShotBehaviour {

    public final  String REGISTRATIONMANAGER = "RegistrationManager";
    public final  String TRIBEACCOUNTANT = "TribeAccountant";

    public final  String UNITMANAGER = "UnitManager";
    public final  String MAPMANAGER = "MapManager";

    private final Codec codec = new SLCodec();
    private final Ontology ontology = WoaOntology.getInstance();

    private final AgRegistrationManager agente;

    public RegistrationInitialBehaviour(AgRegistrationManager a) {
        super(a);
        this.agente=a;
    }

    @Override
    public void action() {
        Utils.register(this.getAgent(), REGISTRATIONMANAGER);
        log.info(ANSI_YELLOW + this.getAgent().getLocalName()+ " Registra servicio en DFS");
        agente.setTribeAccountant(Utils.untilFindDFS(this.getAgent(), TRIBEACCOUNTANT));
        agente.setUnitManager(Utils.untilFindDFS(this.getAgent(), UNITMANAGER));
        agente.setMapManager(Utils.untilFindDFS(this.getAgent(), MAPMANAGER));
        log.info(ANSI_YELLOW + this.getAgent().getLocalName()+
                " Busca tribeAccountant en DFS y encuentra: "+ agente.getTribeAccountant());
        Utils.suscribePhaseManager(this.agente, codec, ontology);
    }

}
