package application;


import application.model.*;
import application.repositories.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
public class AppLauncherTesterDB {

    // Injeção de Dependências
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;


    @EventListener(ApplicationReadyEvent.class)
    @Transactional(timeout = 10) // Tempo limite de 10 segundos
    public void testeDB() {

        System.out.println("inicio**************************************************************************");

        User user1 = new User(null, "Daniel", "daniel@gmail", "daniel");
        User user2 = new User(null, "Rui", "rui@gmail", "rui");
        User user3 = new User(null, "a", "a@a", "a");
        User user4 = new User(null, "b", "a@b", "b");
        User user5 = new User(null, "c", "a@c", "c");

        //COLABORADORES
        Colaborador colaborador1 = new Colaborador(
                null,
                "Ana",
                "Ana@gmail",
                "922 222 222",
                TipoColaborador.ADMINISTRATIVO,
                null
        );

        Colaborador colaborador2 = new Colaborador(
                null,
                "Marco",
                "Marco@gmail",
                "922 222 222",
                TipoColaborador.GESTOR,
                null
        );

        Colaborador colaborador3 = new Colaborador(
                null,
                "a",
                "a@aa",
                "922 222 222",
                TipoColaborador.MOTORISTA,
                null
        );

        Colaborador colaborador4 = new Colaborador(
                null,
                "b",
                "a@bb",
                "922 222 222",
                TipoColaborador.OUTRO,
                null
        );

        Colaborador colaborador5 = new Colaborador(
                null,
                "c",
                "a@cc",
                "922 222 222",
                TipoColaborador.MOTORISTA,
                null
        );

        //CARROS
        Carro carro1 = new Carro(
                null,
                "Tesla",
                "Electrico",
                "123-ABC-321",
                false,
                10.0,
                true,
                null,   // autonomia
                null,   // cartoes
                null,   // motoristas
                null    // despesas
        );

        Carro carro2 = new Carro(
                null,
                "Renault",
                "Clio",
                "321-CBA-321",
                false,
                100.0,
                true,
                null,   // autonomia
                null,   // cartoes
                null,   // motoristas
                null
        );

        Carro carro3 = new Carro(
                null,
                "a",
                "a",
                "a",
                false,
                100.0,
                true,
                null,   // autonomia
                null,   // cartoes
                null,   // motoristas
                null
        );

        Carro carro4 = new Carro(
                null,
                "b",
                "b",
                "b",
                false,
                100.0,
                true,
                null,   // autonomia
                null,   // cartoes
                null,   // motoristas
                null
        );

        Carro carro5 = new Carro(
                null,
                "c",
                "c",
                "c",
                false,
                100.0,
                true,
                null,   // autonomia
                null,      // cartoes
                null,   // motoristas
                null
        );


        Cartao cartao1 = new Cartao(null, TipoCartao.ABASTECIMENTO, "54321", "54321", "Prio", carro1);
        Cartao cartao2 = new Cartao(null, TipoCartao.PORTAGEM, "12345", "12345", "Via Verde", carro1);
        Cartao cartao3 = new Cartao(null, TipoCartao.OUTRA, "1", "1", "V1", carro3);
        Cartao cartao4 = new Cartao(null, TipoCartao.ABASTECIMENTO, "2", "2", "V2", carro4);
        Cartao cartao5 = new Cartao(null, TipoCartao.PORTAGEM, "3", "3", "V3", carro5);

        Despesa despesa1 = new Despesa(null, cartao1, "Gasóleo", "gasóleo", cartao1.getCarro(), colaborador2, Instant.now(), 10.0, 50, 10.0);
        Despesa despesa2 = new Despesa(null, cartao2, "outra", "outra", cartao2.getCarro(), colaborador1, Instant.now(), 20.0);
        Despesa despesa3 = new Despesa(null, cartao3, "2", "2", cartao3.getCarro(), colaborador3, Instant.now(), 30.0, 30, 30.0);
        Despesa despesa4 = new Despesa(null, cartao4, "3", "3", cartao4.getCarro(), colaborador4, Instant.now(), 40.0, 40, 40.0);
        Despesa despesa5 = new Despesa(null, cartao5, "4", "4", cartao5.getCarro(), colaborador5, Instant.now(), 50.0);

        // PAGAMENTOS
        Pagamento pagamento1 = new Pagamento(
                colaborador1,
                Plataforma.UBER,
                LocalDate.now(),
                1000.0,
                TipoPagamento.MENSAL
        );

        Pagamento pagamento2 = new Pagamento(
                colaborador2,
                Plataforma.BOLT,
                LocalDate.now(),
                500.0,
                TipoPagamento.SEMANAL
        );

        Pagamento pagamento3 = new Pagamento(
                colaborador2,
                Plataforma.OUTRA,
                LocalDate.now(),
                500.0,
                TipoPagamento.FIXO
        );


        // Salvar entidades na ordem correta
        userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
        colaboradorRepository.saveAll(List.of(colaborador1, colaborador2, colaborador3, colaborador4, colaborador5));
        carroRepository.saveAll(List.of(carro1, carro2, carro3, carro4, carro5));
        cartaoRepository.saveAll(List.of(cartao1, cartao2, cartao3, cartao4, cartao5));
        despesaRepository.saveAll(List.of(despesa1, despesa2, despesa3, despesa4, despesa5));
        pagamentoRepository.saveAll(List.of(pagamento1, pagamento2, pagamento3));

        System.out.println("Base de dados populada com sucesso!");
    }
}