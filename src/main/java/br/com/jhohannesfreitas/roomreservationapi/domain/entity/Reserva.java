package br.com.jhohannesfreitas.roomreservationapi.domain.entity;

import br.com.jhohannesfreitas.roomreservationapi.domain.enums.StatusReserva;
import br.com.jhohannesfreitas.roomreservationapi.dto.ReservaRequest;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservas")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "hora_inicial", nullable = false)
    private LocalTime horaInicial;

    @Column(name = "hora_final", nullable = false)
    private LocalTime horaFinal;

    @Column(name = "quantidade_pessoas", nullable = false)
    private Integer quantidadePessoas;

    @Enumerated(EnumType.STRING)
    private StatusReserva status = StatusReserva.ATIVA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "sala_id", nullable = false)
    private Sala sala;

    public Reserva() {}

    public Reserva(LocalDate data, LocalTime hora_inicial, LocalTime hora_final, Integer quantidadePessoas) {
        this.data = data;
        this.horaInicial = hora_inicial;
        this.horaFinal = hora_final;
        this.quantidadePessoas = quantidadePessoas;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHoraInicial() {
        return horaInicial;
    }

    public LocalTime getHoraFinal() {
        return horaFinal;
    }

    public Integer getQuantidadePessoas() {
        return quantidadePessoas;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    public void alterarHorario(LocalTime hora_inicial, LocalTime hora_final) {
        this.horaInicial = hora_inicial;
        this.horaFinal = hora_final;
    }

    public void atualizar(ReservaRequest reservaRequest, Usuario usuario, Sala sala) {
        this.data = reservaRequest.data();
        this.horaInicial = reservaRequest.horaInicial();
        this.horaFinal = reservaRequest.horaFinal();
        this.quantidadePessoas = reservaRequest.quantidadePessoas();
        this.usuario = usuario;
        this.sala = sala;
    }
}
